package org.docksidestage.handson.exercise;
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
        // TODO ayamin どうせログ出すなら、assertより前に出して、assertで落ちた時に見られるようにしよう by jflute (2026/01/16)
        assertTrue(count > 0);
        log("会員の総数: " + count);
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
    
    // TODO ayamin 続きのエクササイズもぜひどうぞ by jflute (2026/01/16)
}
