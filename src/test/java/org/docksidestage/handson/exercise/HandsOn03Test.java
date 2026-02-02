package org.docksidestage.handson.exercise;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberStatusBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 * @author jflute
 */
// TODO ayami.hatano コード補完：https://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/howto.html#completion  (2026/01/27)
public class HandsOn03Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberSecurityBhv memberSecurityBhv;

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

        //memo:ER図から、MEMBER_IDがPK = FKである(黒い丸がついていない)ので、setupselectが使える
        //memo:reminderAnswerはなぜかString型で入ってる
        //memo:for文のgetReminderAnswerは冗長かも、変数に入れちゃう方がいいかも

        // TODO done ayami.hatano 2という「文字が含まれている」に注意 (2026/01/29)
        // TODO done ayami.hatano 会員セキュリティ情報のデータ自体は要らない (2026/01/29)
        // TODO ayami.hatano handson02 も要件を満たしているか確認する (2026/01/29)
        // TODO ayami.hatano 曖昧性に気づくには？▶︎推測のまま動かない▶︎推測をしていることを自覚する、推測を確証する工程を踏む (2026/01/30)
        // TODO done ayamin 要件の対象カラムを間違えている (指差し確認しましょう) by jflute (2026/01/30)
        // TODO done なのでUnitTestも落ちてる (実行してgreenを確認確認しましょう) by jflute (2026/01/30)
        // TODO ayami.hatano linter使ってみる (2026/02/02)
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
                log("検索された会員: " + member.getMemberName() + " " + security.getReminderQuestion());
                assertTrue(security.getReminderQuestion().contains("2"));
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
    // TODO ayamin [読み物課題] 自分の中でデマを広げさせない by jflute (2026/01/30)
    // https://jflute.hatenadiary.jp/entry/20110619/nodema
    // TODO ayamin [読み物課題] 論理的矛盾が発生したら、思い込み前提を探す by jflute (2026/01/30)
    // https://jflute.hatenadiary.jp/entry/20180831/contradictionstep

    // TODO jflute 次回1on1ここから (2026/01/30)
    public void test_会員ステータスの表示順カラムで会員を並べて検索(){
        //[4] 会員ステータスの表示順カラムで会員を並べて検索
        //済：会員ステータスの "表示順" カラムの昇順で並べる
        //済：会員ステータスのデータ自体は要らない
        //済：その次には、会員の会員IDの降順で並べる
        //会員ステータスのデータが取れていないことをアサート
        //会員が会員ステータスごとに固まって並んでいることをアサート (順序は問わない)

        //memo:setupSelectせずに会員ステータスを調べるにはどうしたら良いか？(setupSelectはJOINとSELECT両方やっている気がする)▶︎cb.query()だけやる？
        //memo:固まっていることをアサートするにはどうするか？▶︎一度変数に入れて、次に見たものがnullまたは同じものだったらアウト、みたいにしたい

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        List<String> historyStatusList = new ArrayList<>();
        String previousStatusCode = null;

        for (Member member : memberList) {
            assertFalse(member.getMemberStatus().isPresent());

            String currentStatusCode = member.getMemberStatusCode();
            log("検索された会員: " + member.getMemberName() + ", 会員ID: " + member.getMemberId() + ", ステータス: " + member.getMemberStatusCode());

            if (previousStatusCode != null && !currentStatusCode.equals(previousStatusCode)) {

                if (historyStatusList.contains(currentStatusCode)) {
                    fail("会員ステータスが固まって表示されていません: " + currentStatusCode + " が再登場しました");
                }
            }

            historyStatusList.add(currentStatusCode);
            previousStatusCode = currentStatusCode;
        }
    }

    public void test_生年月日が存在する会員の購入を検索() {
        //[5]会員名称と会員ステータス名称と商品名を取得する(ログ出力)
        //購入日時の降順、購入価格の降順、商品IDの昇順、会員IDの昇順で並べる
        //OrderBy がたくさん追加されていることをログで目視確認すること
        //購入に紐づく会員の生年月日が存在することをアサート


    }


}
