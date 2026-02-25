package org.docksidestage.handson.exercise;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 * @author jflute
 */
public class HandsOn02Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

    public void test_existsTestData() throws Exception {
        // done 絞込み条件なしのシンプルな検索処理を実装してみましょう

        // ## Arrange ##
        // ## Act ##
        int count = memberBhv.selectCount(cb -> {
        });

        // ## Assert ##
        // done ayamin どうせログ出すなら、assertより前に出して、assertで落ちた時に見られるようにしよう by jflute (2026/01/16)
        log("会員の総数: " + count);
        assertTrue(count > 0);
    }

    public void test_会員名称がSで始まる会員を検索() {
        // 会員名称がSで始まる会員を検索 (これはタイトル、この中にも要件が含まれている)
        // 会員名称の昇順で並べる (これは実装要件、Arrange or Act でこの通りに実装すること)
        // (検索結果の)会員名称がSで始まっていることをアサート (これはアサート要件、Assert でこの通りに実装すること)
        // "該当テストデータなし" や "条件間違い" 素通りgreenにならないように素通り防止を (今後ずっと同じ)
        // #1on1: op や cb のコールバックの話、Java6互換モードだとnewすることも (2026/01/16)

        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });

        // ## Assert ##
        //検索結果が空ではないこと
        assertHasAnyElement(memberList);
        //会員名がSから始まること
        for (Member member : memberList) {
            String memberName = member.getMemberName();
            log("検索された会員: " + memberName);
            assertTrue(memberName.startsWith("S"));
        }
    }
    // done ayamin 続きのエクササイズもぜひどうぞ by jflute (2026/01/16)
    public void test_会員IDが1の会員を検索(){
        //あるはずだけど、もしID1の会員がいなかったらエラーにしたいので、.alwaysPresentを使った
        // TODO done ayami.hatano .alwaysPresentと.ifPresentの中身見る (2026/02/06)
        // #1on1: Optional@ifPresent() :: あったらコールバック呼ぶけど、なかったら何もしない
        //  public void ifPresent(Consumer<? super T> consumer) {
        //      if (value != null) { // 中身があったら
        //          consumer.accept(value); // コールバックを呼ぶ
        //      }
        //      // なかったら素通りで何もしない
        //  }
        //
        // OptionalEntity@alwaysPresent() :: あったらコールバック呼ぶけど、なかったら例外throw
        //  if (_obj == null) { // 中身がなかったら
        //      _thrower.throwNotFoundException(); // ここで例外がthrowされて処理が止まる
        //  objLambda.accept(_obj); // ここは中身が必ず存在するケース、コールバックを呼ぶ
        //
        //   ↓↓ (ifPresent()と比較しやすいように書き換えると...)
        //
        //  if (_obj != null) { // 中身があったら
        //      objLambda.accept(_obj); // コールバックを呼ぶ
        //  } else { // ifPresent()にはないelse
        //      _thrower.throwNotFoundException(); // ここで例外がthrowされて処理が止まる
        //  }
        //
        // DBFluteのifPresent()の戻り値は？ → orElse()を呼ぶだけのインターフェース
        // これは、DBFluteオリジナルの拡張。
        //
        // Optionalの中の実装は、全然大したことはやってない。
        // けど、それで「ないかもしれない」という概念がオブジェクトになって、安全な実装できる。
        // そういう目に見えない頭の中だけで存在する「概念」という言われるものもオブジェクトになる。
        //
        memberBhv.selectEntity(cb -> cb.acceptPK(1)).alwaysPresent(member -> {
            Integer memberId = member.getMemberId();
            Integer memberName = member.getMemberId();
            log("検索された会員: " + memberName + ", ID: " + memberId);

            assertEquals(Integer.valueOf(1), memberId);
        });
    }

    public void test_ifPresent_成功してほしくないが成功する() {
        memberBhv.selectEntity(cb -> cb.acceptPK(9999)).ifPresent(member -> {
            log("検索された会員: " + member.getMemberName());
            assertEquals(Integer.valueOf(1), member.getMemberId());
        }).orElse(() -> {
            log("Not found");
        });
    }

    public void test_思い出_会員IDが1の会員を検索() {
        //会員IDが1の会員を検索
        //一件検索として検索すること
        //会員IDが1であることをアサート

        // ## Arrange ##
        // ## Act ##
        // #1on1: PK制約、PrimaryKey制約、Constraint、PrimaryなKeyであることをチェックしている。 (2026/01/30)
        // PrimaryなKeyというのは、メインで一意にレコードを特定できるキーのこと。それを保証している。
        // (すでに1番が存在しているのに、新たに1番を登録しようとするとエラーになるようになっている)
        // #1on1: であれば、この検索は絶対に1件しか取れないので...リストで取得する必要があるだろうか？ (2026/01/30)
        // 必要はないかもしれないけど動作はする。でも、可読性的に「複数来るのかな!?」って紛らわしい。
        // 案1: get(0)でもいいかもだけどなかったときの配慮が乏しい...し、結局DBFluteの中で無駄にListを作ってしまている。
        // 案2: DBFluteで複数件ではなく一件だけ検索したいのどうすればいい？ってググったりAIに聞く。
        // プログラム全体に言える話で、複数件と一件の処理では事務的な処理が変わってくるので、常に意識しておいて欲しい。
        // (「0」と「1」と「2以降」の3種類、これらは扱いが全然違う)
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // 会員IDが1に等しい条件を設定
            cb.query().setMemberId_Equal(1);
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            // done ayamin リファクタリングトレーニング、getMemberId()を変数に抽出してみましょう by jflute (2026/01/30)
            // IntelliJだと、control+T でリファクタリングメニューが出てきて、変数抽出があるはず。
            Integer id = member.getMemberId();
            log("検索された会員: " + member.getMemberName() + "検索されたMEMBERID:"+ id);
            assertEquals(1, id);
        }
    }

    public void test_会員IDを99999で検索() {
        //会員IDが99999の会員を検索
        //一件検索として検索すること
        // done ayamin コメントが 1 になってる1 by jflute (2026/01/30)
        //会員IDが99999であることをアサート

        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // done ayamin コメントが 1 になってる2 by jflute (2026/01/30)
            // 会員IDが99999に等しい条件を設定
            cb.query().setMemberId_Equal(99999);
        });

        // ## Assert ##
        assertHasAnyElement(memberList);

        for (Member member : memberList) {
            log("検索された会員: " + member.getMemberName() + ", ID=" + member.getMemberId());
            assertEquals(99999, member.getMemberId());
        }
    }

    public void test_生年月日がない会員を検索(){
        //更新日時の降順で並べる
        //生年月日がないことをアサート

        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // #1on1 _Equal(null) と _IsNull() の違い (2026/01/30)
            // ConditionBean的には、もうデフォルトでは null をそもそも指定できない(実行時例外)。
            // SQL的には、「where BIRTHDATE = null」 と 「where BIRTHDATE is null」 の違い。
            // BIRTHDATE = null は、nullだろうがなんだろうが絶対にヒットしない。(絶対に0件)
            // null は値では無い。空っぽという状態を示すもの。なので、SQLでは is null という決め。
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            // done ayamin BIRTHDATEは主役だし、2箇所で登場しているから、変数抽出してみましょう by jflute (2026/01/30)
            // IntelliJだと、control+T でリファクタリングメニューが出てきて、変数抽出があるはず。
            // #1on1: 大事な行を観やすくするためのテクニックの一つ (2026/02/13)
            String memberName = member.getMemberName();
            LocalDate birthdate = member.getBirthdate();
            LocalDateTime updateDatetime = member.getUpdateDatetime();

            log("検索された会員: " + memberName + ", 生年月日=" + birthdate + ", 更新日時=" + updateDatetime);
            assertNull(birthdate);
        }


        // #1on1: like 'S%' escape '|' の escape の話 (2026/01/16)
        // イメージ的には escaped by '|' の方がわかりやすいかも。'|' はエスケープ文字。(エスケープする側)
        //
        // Javaだと、String sea = "a\"ya\"min"; // → a"ya"min
        // エスケープ文字: バックスラッシュ \
        // エスケープされる文字: ダブルクォーテーション " (つまり、特殊文字 (制御文字))
        // (エスケープ文字自身も特殊文字なので、文字として認識させたい場合はエスケープする \\)
        //
        // ↑と全く同じなので...
        // エスケープ文字: パイプライン |
        // エスケープされる文字: ワイルドカード % _ (つまり、特殊文字 (制御文字))
        // setMemberName_LikeSearch("S%", op -> op.likePrefix()); // "S%" で始まる人
        //  → where dfloc.MEMBER_NAME like 'S|%%' escape '|' 
    }
}

