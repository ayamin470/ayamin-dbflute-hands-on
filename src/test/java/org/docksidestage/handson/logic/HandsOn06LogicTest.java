package org.docksidestage.handson.logic;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 */
public class HandsOn06LogicTest extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

    public void test_selectSuffixMemberList_指定したsuffixで検索されること() {
        // ## Arrange ##
        HandsOn06Logic logic = new HandsOn06Logic();
        inject(logic);
        String suffix = "vic";

        // ## Act ##
        List<Member> memberList = logic.selectSuffixMemberList(suffix);

        // ## Assert ##
        // 素通り防止
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            String memberName = member.getMemberName();
            log("検索された会員: " + memberName);
            assertTrue(memberName.endsWith(suffix));
        }
        // 全件ヒットではなく「ちゃんと絞られている」ことを示す
        int total = memberBhv.selectCount(cb -> {});
        assertTrue(memberList.size() < total);
    }

    public void test_selectSuffixMemberList_suffixが無効な値なら例外が発生すること() {
        // ## Arrange ##
        HandsOn06Logic logic = new HandsOn06Logic();
        inject(logic);

        // ## Act & Assert ##
        assertException(IllegalArgumentException.class, () -> logic.selectSuffixMemberList(null));
        assertException(IllegalArgumentException.class, () -> logic.selectSuffixMemberList(""));
        assertException(IllegalArgumentException.class, () -> logic.selectSuffixMemberList("   "));
    }
}

//TODO DBFluteプロパティの設定 から
