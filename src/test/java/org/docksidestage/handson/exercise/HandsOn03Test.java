package org.docksidestage.handson.exercise;
import java.time.LocalDate;
import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
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
        assertHasAnyElement(memberList);
        //会員が1968/01/01以前生まれであること
        for (Member member : memberList) {
            LocalDate birthdate = member.getBirthdate();
            log("検索された会員: " + member.getMemberName() + ", 生年月日: " + birthdate);
            assertTrue(!birthdate.isAfter(targetDate));
        }
    }

}
