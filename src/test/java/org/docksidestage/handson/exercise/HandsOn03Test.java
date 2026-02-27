package org.docksidestage.handson.exercise;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.helper.HandyDate;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberStatusBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.*;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 * @author jflute
 */
public class HandsOn03Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

    public void test_会員名称がSで始まる1968年1月1日以前に生まれた会員を検索(){
        //[1] 会員名称がSで始まる1968年1月1日以前に生まれた会員を検索
            //会員ステータスも取得する
            //生年月日の昇順で並べる
            //会員が1968/01/01以前であることをアサート
            //※"以前" の解釈は、"その日ぴったりも含む" で。

        // ## Arrange ##
        // 検索用の基準日を作る
        LocalDate targetDate = LocalDate.of(1968, 1, 1);

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // done ayamin 実装順序は、データの取得、絞り込み、並び替え by jflute (2026/01/16)
            //  => http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.setupSelect_MemberStatus(); //(setupSelect:会員テーブル(member_status)を取得)
            // #1on1: 前方一致想定のものがユーザー入力で部分一致になったら攻撃の穴になる (2026/01/30)
            // なので、like検索もワイルドカードが入力されたときは、単なる文字として扱うようにエスケープする。
            // DBFluteのConditionBeanは、それを自動でエスケープされるようになっている。
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix()); //Sから始まる人を検索
            cb.query().setBirthdate_LessEqual(targetDate); //生まれが基準日以前を検索
            cb.query().addOrderBy_Birthdate_Asc(); //昇順
        });

        // ## Assert ##
        //検索結果が空でないこと
        //会員が1968/01/01以前生まれであること
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            LocalDate birthdate = member.getBirthdate();
            log("検索された会員: " + member.getMemberName() + ", 生年月日: " + birthdate);
            // #1on1: afterにして否定にするのGood (2026/01/16)
            //assertTrue(birthdate.isBefore(targetDate) || birthdate.isEqual(targetDate));
            // done ayamin assertFalse()を使っちゃいましょう by jflute (2026/01/16)
            assertFalse(birthdate.isAfter(targetDate));
        }
    }

    public void test_会員ステータスと会員セキュリティ情報も取得して会員を検索若い順で並べる() {
    //[2]会員ステータスと会員セキュリティ情報も取得して会員を検索 若い順で並べる
        // 生年月日がない人は会員IDの昇順で並ぶようにする
        // 会員ステータスと会員セキュリティ情報が存在することをアサート
        // ※カージナリティを意識しましょう

        // ## Act ##
        //会員を全検索
        //年齢が若い順で並べる
        //生年月日がない人は会員IDの昇順
        //setupSelect:会員ステータステーブルを取得
        //setupSelect:会員セキュリティ情報
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            cb.query().addOrderBy_Birthdate_Desc(); //若い順＝数字が大きい順＝降順
            cb.query().addOrderBy_MemberId_Asc(); // #1on1: 第二ソートキーの話 (2026/01/16)
            // #1on1: ユニークなソートの話。基本的には業務で検索するときはユニークにソートする。 (2026/01/16)
        });

        // ## Assert ##
        //検索結果が空でないこと
        //会員ステータスと会員セキュリティ情報が存在すること
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            log("検索された会員: " + member.getMemberName()
                    + ", ステータス有無=" + member.getMemberStatus().isPresent()
                    + ", セキュリティ有無=" + member.getMemberSecurityAsOne().isPresent());

            // #1on1: すべての会員は、会員ステータスを必ず持っているものか？ (2026/01/16)
            // 外部キー制約 → ForeignKey制約 → FK制約 (えふけー)
            // NotNullかつFK制約、だから、探しに行けば絶対に存在する
            assertTrue(member.getMemberStatus().isPresent());

            // #1on1: すべての会員は、会員セキュリティを必ず持っているものか？ (2026/01/16)
            // 探しにいく方向とFKの方向が逆なので、さっきの論理は通用しない。
            // 物理的には必ず存在する保証はない。ただ、ER図上で黒丸がないので、必ず存在する1:1であることを示してる。
            // この場合、物理制約ないけれども、業務制約(論理制約: 単なる人間の決め事)はあるという感じ。
            // そもそも、SchemaHTML上でDBコメントに絶対存在するって書いてある。
            assertTrue(member.getMemberSecurityAsOne().isPresent());
            
            // → if文を書かない理由というのを明確にすること (2026/01/16)
            //
            // 必ず存在する1:1なのか？
            // いないかもしれない1:1なのか？
            // を気にしてみてください。
        }
    }

    public void test_会員セキュリティ情報のリマインダ質問で2という文字が含まれている会員を検索() {
        // [3] 会員セキュリティ情報のリマインダ質問で2という文字が含まれている会員を検索
        // 会員セキュリティ情報のデータ自体は要らない
        // (Actでの検索は本番でも実行されることを想定し、テスト都合でパフォーマンス劣化させないこと)
        // リマインダ質問に2が含まれていることをアサート
        // アサートするために別途検索処理を入れても誰も文句は言わない
        // #1on1: 検索のデータの大きな目的３つ (2026/02/13)
        // select: データを持ってくる
        //  where: データを絞り込む
        //  order: データで並び替える
        // セキュリティ情報は、そのうちの "データを持ってくる" が不要であるとのこと。
        // ただ、会員の一覧を絞り込むために "データを絞り込む" のところで必要。

        //memo:ER図から、MEMBER_IDがPK = FKである(黒い丸がついていない)ので、setupselectが使える
        //memo:reminderAnswerはなぜかString型で入ってる
        //memo:for文のgetReminderAnswerは冗長かも、変数に入れちゃう方がいいかも

        // done ayami.hatano 2という「文字が含まれている」に注意 (2026/01/29)
        // done ayami.hatano 会員セキュリティ情報のデータ自体は要らない (2026/01/29)
        // done ayami.hatano 曖昧性に気づくには？▶︎推測のまま動かない▶︎推測をしていることを自覚する、推測を確証する工程を踏む (2026/01/30)
        // done ayamin 要件の対象カラムを間違えている (指差し確認しましょう) by jflute (2026/01/30)
        // done なのでUnitTestも落ちてる (実行してgreenを確認確認しましょう) by jflute (2026/01/30)
        // #1on1: 関連テーブル側(1:1)のカラムでの絞り込みのやり方Good (2026/01/30)
        // cb.query()... は基点テーブルのカラムの絞り込みなので、
        // cb.query().queryMemberSecurityAsOne()... query,queryで繋げることで関連テーブルの絞り込みができる。

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            memberSecurityBhv.selectByPK(member.getMemberId()).alwaysPresent(security -> {
                // dobe ayamin getReminderQuestion()を抽出してみましょう (IntelliJのcontrol+Tで抽出) by jflute (2026/02/13)
                String reminderQuestion = security.getReminderQuestion();
                log("検索された会員: " + member.getMemberName() + " " + reminderQuestion);
                assertTrue(reminderQuestion.contains("2"));
            });
        }
    }
    // #1on1: 自然言語大事。特にjavatryに比べて世界観が業務的な表現が多くなった。 (2026/01/30)
    // 正確にプログラムを書くためには、要件の自然言語の解釈を正しくすることが大事。
    // どれだけ立派なプログラムを書いても、要件と違っていたらバグだし価値がない。
    // 要件に合ったプログラムを書くから、お金がもらえる。
    // 要件に合ったプログラムを書ける人 (間違えない人) は仕事ができる人と言っても過言では無い。
    //
    // 要件をしっかり読めるようになるためには、日本語力以外に何かあるか？
    // (2という文字が含まれている、の意味がわからなかった話)
    // o 日本語表現を知っていれば(慣れていれば)、読み飛ばさない
    // o 一方で、わからないこと曖昧なこと(表現)を、読み飛ばさずに調べることができたら...
    // 1とか2という選択肢のことかな？って自分の中で推測で先に進んじゃった。
    // 推測は悪く無い、でも推測は推測のままで進めて、後で推測を確証する工程を忘れないこと。
    // でないと、推測がどこかで勝手に確定事項に自然と変わってしまう。
    // done ayamin [読み物課題] 自分の中でデマを広げさせない by jflute (2026/01/30)
    // https://jflute.hatenadiary.jp/entry/20110619/nodema
    // done ayamin [読み物課題] 論理的矛盾が発生したら、思い込み前提を探す by jflute (2026/01/30)
    // https://jflute.hatenadiary.jp/entry/20180831/contradictionstep

    // done ayami.hatano 聞く：「会員ステータスのデータ自体は要らない」場合、どのようにクエリを書くべき？ (2026/02/13)
    // cb.query().queryMemberStatus()では、CQ(検索条件のオブジェクト)が返ってきているので、これを使うと良さそう
    // #1on1: selectの目的が不要と言うことで、setupSelect_MemberStatus()を呼ばなければOK (2026/02/13)
    // orderの目的を達成するために会員ステータスは(一時的に)必要なので、なので queryMemberStatus() をする。
    // SQLで言うと、fromでjoinした会員ステータスを、select句では使わず、order by句だけで使う。
    public void test_会員ステータスの表示順カラムで会員を並べて検索(){
        //[4] 会員ステータスの表示順カラムで会員を並べて検索
        //済：会員ステータスの "表示順" カラムの昇順で並べる
        //済：会員ステータスのデータ自体は要らない
        //済：その次には、会員の会員IDの降順で並べる
        //済：会員ステータスのデータが取れていないことをアサート
        //済：会員が会員ステータスごとに固まって並んでいることをアサート (順序は問わない)

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // #1on1: assertのロジックが合ってるかどうか？のために一時的にActを壊して実行も大切 (2026/02/13)
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);

        // #1on1: 最初の時は、長めの変数名でわかりやすくでOK。慣れてくると自然に減らせるので (2026/02/13)
        // 最初から短めに寄せて増やしていくのは、何が必要なのか？何が不要なのか？が学びにくい。

        Set<String> historyStatusCodeSet = new HashSet<>();
        String previousStatusCode = null;
        // done ayamin Setで十分。重複のないリストを使ってみましょう by jflute (2026/02/13)
        // setは重複するデータを保持できないルール。
        // 「これまでに登場したステータスの種類を記憶しておき、後で再登場しないかチェックする」という目的に対して、setの方がパフォーマンス観点で良いし、コード見ただけでやりたいことわかる
        // done ayamin StatusCodeってCodeであることをわかりやすくしてるなら、ここもStatusCodeListでは？ by jflute (2026/02/13)

        for (Member member : memberList) {
            assertFalse(member.getMemberStatus().isPresent());

            String currentStatusCode = member.getMemberStatusCode();
            log("検索された会員: " + member.getMemberName() + ", 会員ID: " + member.getMemberId() + ", ステータス: " + member.getMemberStatusCode());

            // done ayamin ifがtrueだったらOUT, ってことは、そのifの条件がfalseでなきゃいけないってこと by jflute (2026/02/13)
            // ってことは、contains()の戻り値が false であるはず、というアサートでも良いのでは？
            // わかやすさの許容範囲内であれば、短い方が良い。
            // 別に今の実装が悪いわけではなく、コードの変化を体験するトレーニングとして。
            // ▶︎assertFalse使えばいいのか！

            if (previousStatusCode != null && !currentStatusCode.equals(previousStatusCode)) {
                assertFalse(historyStatusCodeSet.contains(currentStatusCode));
            }

            historyStatusCodeSet.add(currentStatusCode);
            previousStatusCode = currentStatusCode;
        }
        log(historyStatusCodeSet);
        
        // done jflute ↑次回1on1にて、違うやり方のふぉろー (2026/02/13)
        // #1on1: 違うやり方のふぉろー:
        // あやみんさんのやり方:
        // AABBCCと並んでて、切りかわったタイミングで、新しいコードが今までに登場してないはず、をつどチェック。
        // つまり、AABBACC だったら、2回目のAでOUT。
        //
        // AABBCCと並んでて、切りかわったタイミングの数と、全体で登場したステータスの種類数を比較して、
        // 「切りかわったタイミングの数 + 1」==「全体で登場したステータスの種類数」のはず。
        //
        // AABBCC => タイミングの数は2, 種類数は3, つまり 2+1 = 3
        // AABBACC => タイミングの数は3, 種類数は3, つまり 3+1 = 3 でOUT
        //
        // 前者がプログラミング的なチェック
        // 後者がデータ分析的なチェック (データの特徴を探す)
    }
    
    // TODO ayamin 3-5がない。一度ひながただけ作ってて、自分で消している by jflute (2026/02/27)
    
        // TODO ayami.hatano  setupSelectはテーブルのすべてのカラムを取得するので使えない (2026/02/17)
        // TODO ayami.hatano 1行ずつ確認するやつ、まだやっていない (2026/02/17)
    public void test_2005年10月の1日から3日までに正式会員になった会員を検索() {
        // [6] 2005年10月の1日から3日までに正式会員になった会員を検索
        // 画面からの検索条件で2005年10月1日と2005年10月3日がリクエストされたと想定して...
        // Arrange で String の "2005/10/01", "2005/10/03" を一度宣言してから日時クラスに変換する
        // 自分で日付移動などはせず、DBFluteの機能を使って、そのままの日付(日時)を使って条件を設定
        // 会員ステータスも一緒に取得
        // ただし、会員ステータス名称だけ取得できればいい (説明や表示順カラムは不要)
        // 会員名称に "vi" を含む会員を検索
        // 会員名称と正式会員日時と会員ステータス名称をログに出力
        // 会員ステータスがコードと名称だけが取得されていることをアサート
        // 会員の正式会員日時が指定された条件の範囲内であることをアサート

        String fromDateString = "2005/10/01";
        String toDateString = "2005/10/03";
        // #1on1: 文字列日付を、日付オブジェクトに変換するオーソドックスなやり方は、DateTimeFormatter:
        //  e.g. DateTimeFormatter.ofPattern("yyyy/MM/dd").parse(fromDateString);
        // ↑形式を意識して、特定して変換するのが一般的。
        // ただ、DBFluteが提供している HandyDate だと、まあ楽に変換できる。ここではOK。
        // ↑HandyDate は色々な形式を吸収するもの。(ハイフンもスラッシュも両方受け付ける)
        LocalDateTime fromDate = new HandyDate(fromDateString).getLocalDateTime();
        LocalDateTime toDate = new HandyDate(toDateString).getLocalDateTime();
        String keyword = "vi";

        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // #1on1: SpecifyColumnの使い所 (2026/02/27)
            // 共通カラムなどを除外するメソッドもある。現場でも使われているっぽい？
            //  e.g. cb.specify().exceptRecordMetaColumn();
            //
            // #1on1: CHAR, VARCHARの違い (2026/02/27)
            cb.setupSelect_MemberStatus();
            cb.specify().specifyMemberStatus().columnMemberStatusName();

            //compareAsDate()よって、Toの条件を1日ずらし、< '2005-10-04 00:00:00'（10月4日の0時0分0秒より前、つまり10月3日の23時59分59秒まで）という条件のSQLを組み立ててくれている
            //要は、DBに保存されているジフン病の情報を切り捨てる働き
            // #1on1: DateFromToの仕組みのお話まで (2026/02/27)
            cb.query().setFormalizedDatetime_FromTo(fromDate, toDate, op -> op.compareAsDate());
            cb.query().setMemberName_LikeSearch(keyword, op -> op.likeContain());
        });

        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            String memberName = member.getMemberName();
            LocalDateTime formalizedDatetime = member.getFormalizedDatetime();
            MemberStatus memberStatusCode = member.getMemberStatus().get();
            //MemberStatusを取得するメソッドはOptionalEntity型を返す。ので、.get()で取り出す
            String memberStatusName = memberStatusCode.getMemberStatusName();

            log("検索された会員: " + memberName + ", 正式会員日時: " + formalizedDatetime + ", ステータス: " + memberStatusName);

            // TODO ayamin 実行してみて例外が発生するので確認を by jflute (2026/02/27)
            assertNotNull(memberStatusCode.getMemberStatusCode());
            assertNotNull(memberStatusName);
            assertNull(memberStatusCode.getDescription());
            assertNull(memberStatusCode.getDisplayOrder());

            assertTrue(memberName.contains(keyword));

            // TODO ayamin "9/30 23:59:59.000" は対象外、"9/30 23:59:59.001" は対象になる by jflute (2026/02/27)
            // formalizedDatetimeがミリ秒があるカラムだった場合に、↑のようなことが起きる。
            // ハンズオンとしては、MySQLのDATETIMEを使っているので、ミリ秒が存在しないから、大丈夫なんだけど...
            // あまりそこに依存したプログラムを書かない方が無難。後から DATETIME(3) とかミリ秒を追加する可能性も。
            // じゃあ、-1するのは秒じゃなくてミリ秒だったらいい？厳密には、ナノ秒とか想定するとキリがない。
            // ミリ秒を意識したロジックというのは、そういうジレンマに陥りやすい。
            // なので、isAfter || isEqual で判定しちゃった方が世話ないかも。(その方がfromDateの日付操作が要らなくなる)
            //  e.g. assertTrue(formalizedDatetime.isAfter(fromDate)   // 10/1 00:00:01以降が対象
            //               || formalizedDatetime.isEqual(fromDate)); // 10/1 00:00:00ぴったりが対象
            //
            // SQLだったら <, <= で含む含まないを簡単に制御
            // DBFluteでの LessThan, LessEqual で含む含まないを制御
            // でも、LocalDateTimeさんは、isBefore(), isAfter() とか含むニュアンスのメソッドがない。
            //
            // TODO ayamin nextDayOfToDate は、ループごとに変わる値ではないので... by jflute (2026/02/27)
            // plusDays(1)をループの外に持っていきましょう。(毎ループやる必要はない)
            // UnitTestだから普段はめくじら立てないけど、トレーニングとしては意識しておきましょうということで。
            assertTrue(formalizedDatetime.isAfter(fromDate.minusSeconds(1))); // e.g. 9/30 23:59:59
            LocalDateTime nextDayOfToDate = toDate.plusDays(1); // e.g. 10/4
            assertTrue(formalizedDatetime.isBefore(nextDayOfToDate));
        }
    }
}
