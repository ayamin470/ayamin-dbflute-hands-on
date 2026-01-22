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
        //TODO done 絞込み条件なしのシンプルな検索処理を実装してみましょう

        // ## Arrange ##
        // ## Act ##
        int count = memberBhv.selectCount(cb -> {
        });

        // ## Assert ##
        assertTrue(count > 0);
        log("会員の総数: " + count);
    }

    public void test_会員名称がSで始まる会員を検索() {
        // 会員名称がSで始まる会員を検索 (これはタイトル、この中にも要件が含まれている)
        // 会員名称の昇順で並べる (これは実装要件、Arrange or Act でこの通りに実装すること)
        // (検索結果の)会員名称がSで始まっていることをアサート (これはアサート要件、Assert でこの通りに実装すること)
        // "該当テストデータなし" や "条件間違い" 素通りgreenにならないように素通り防止を (今後ずっと同じ)

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
    public void test_会員IDが1の会員を検索() {
        //会員IDが1の会員を検索
        //一件検索として検索すること
        //会員IDが1であることをアサート

        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // 会員IDが1に等しい条件を設定
            cb.query().setMemberId_Equal(1);
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            log("検索された会員: " + member.getMemberName() + ", ID=" + member.getMemberId());
            assertEquals(1, member.getMemberId());
        }
    }

    public void test_会員IDを99999で検索() {
        //会員IDが1の会員を検索
        //一件検索として検索すること
        //会員IDが1であることをアサート

        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // 会員IDが1に等しい条件を設定
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
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_UpdateDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            log("検索された会員: " + member.getMemberName()
                    + ", 生年月日=" + member.getBirthdate()
                    + ", 更新日時=" + member.getUpdateDatetime());
            assertNull(member.getBirthdate());
        }


    }


}

