package org.docksidestage.handson.exercise;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn03Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    public void test会員名がSから始まる人を探す() {

        // 新しくcbを作成し、会員名がSから始まる人を検索する
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
        });
        // 検索結果が空でないこと
        assertFalse(memberList.isEmpty());
        // すべての会員名がSで始まること
        for (Member member : memberList) {
            assertTrue(member.getMemberName().startsWith("S"));
        }
    }
}

//memo:
//@Testがあると通らなかった(@TestはJUnit4以降で使える、親クラスのUnitContainerTestCaseがJUnit3なので、それに合わせる)
//変数の宣言忘れずに
//ラムダ式を使って新しくcbを作るべし
