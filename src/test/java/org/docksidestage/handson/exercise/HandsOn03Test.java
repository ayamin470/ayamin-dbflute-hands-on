package org.docksidestage.handson.exercise;
import java.time.LocalDate;
import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 * @author jflute
 */
public class HandsOn03Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

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
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix()); //Sから始まる人を検索
            cb.query().setBirthdate_LessEqual(targetDate); //生まれが基準日以前を検索
            cb.setupSelect_MemberStatus(); //(setupSelect:会員テーブル(member_status)を取得)
            cb.query().addOrderBy_Birthdate_Asc(); //昇順
        });

        // ## Assert ##
        //検索結果が空でないこと
        //会員が1968/01/01以前生まれであること
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            LocalDate birthdate = member.getBirthdate();
            log("検索された会員: " + member.getMemberName() + ", 生年月日: " + birthdate);
            assertTrue(!birthdate.isAfter(targetDate));
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
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        //検索結果が空でないこと
        //会員ステータスと会員セキュリティ情報が存在すること
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            log("検索された会員: " + member.getMemberName()
                    + ", ステータス有無=" + member.getMemberStatus().isPresent()
                    + ", セキュリティ有無=" + member.getMemberSecurityAsOne().isPresent());

            assertTrue(member.getMemberStatus().isPresent());
            assertTrue(member.getMemberSecurityAsOne().isPresent());
        }

    }

}
