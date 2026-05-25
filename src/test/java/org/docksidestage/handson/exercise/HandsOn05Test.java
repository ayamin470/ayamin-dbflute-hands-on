package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberAddressBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberAddress;
import org.docksidestage.handson.dbflute.exentity.MemberLogin;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.dbflute.exentity.Region;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 * @author jflute
 */
    
public class HandsOn05Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;
    @Resource
    private MemberAddressBhv memberAddressBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

    public void test_会員住所情報を検索() {
        // 要件はここで管理 ▶︎ ex05-requirements.md

        // ## Arrange ##
        // ## Act ##
        ListResultBean<MemberAddress> memberAddressList = memberAddressBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Region();
            cb.query().addOrderBy_MemberId_Asc();
            cb.query().addOrderBy_ValidBeginDate_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberAddressList);
        for (MemberAddress memberAddress : memberAddressList) {
            Member member = memberAddress.getMember().get();
            Region region = memberAddress.getRegion().get();
            String memberName = member.getMemberName();
            LocalDate validBeginDate = memberAddress.getValidBeginDate();
            LocalDate validEndDate = memberAddress.getValidEndDate();
            String address = memberAddress.getAddress();
            String regionName = region.getRegionName();

            log("会員名称=" + memberName + ", 有効開始日=" + validBeginDate
                    + ", 有効終了日=" + validEndDate + ", 住所=" + address
                    + ", 地域名称=" + regionName);
        }
    }

    public void test_会員と共に現在の住所を取得して検索() {
        // 要件はここで管理 ▶︎ ex05-requirements.md

        // ## Arrange ##
        LocalDate targetDate = currentLocalDate();

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberAddressAsValid(targetDate);
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            assertTrue(member.getMemberAddressAsValid().isPresent());
            MemberAddress currentAddress = member.getMemberAddressAsValid().get();
            String memberName = member.getMemberName();
            String address = currentAddress.getAddress();

            log("会員名称=" + memberName + ", 住所=" + address);
        }
    }

    public void test_千葉に住んでいる会員の支払済み購入を検索() {
        // 要件はここで管理 ▶︎ ex05-requirements.md

        // ## Arrange ##
        LocalDate targetDate = currentLocalDate();

        // ## Act ##
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Member().withMemberAddressAsValid(targetDate);
            cb.query().setPaymentCompleteFlg_Equal_True();
            //CDef.javaを見る
            cb.query().queryMember().queryMemberAddressAsValid(targetDate).setRegionId_Equal_千葉();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        for (Purchase purchase : purchaseList) {
            Member member = purchase.getMember().get();
            MemberAddress currentAddress = member.getMemberAddressAsValid().get();
            String memberName = member.getMemberName();
            String memberStatusName = member.getMemberStatus().get().getMemberStatusName();
            String address = currentAddress.getAddress();

            log("会員名称=" + memberName + ", 会員ステータス名称=" + memberStatusName
                    + ", 住所=" + address);
            assertTrue(currentAddress.isRegionId千葉());
        }
    }

    public void test_最終ログイン時の会員ステータスを取得して会員を検索() {
        // 要件はここで管理 ▶︎ ex05-requirements.md
        // 最終ログイン日時でなく、「最終ログイン時の会員ステータス」が必要なので、レコード丸ごと必要

        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberLoginAsLatest().withMemberStatus();
            //そもそもログイン履歴が存在する会員だけにしたい
            cb.query().queryMemberLoginAsLatest().setMemberLoginId_IsNotNull();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            assertTrue(member.getMemberLoginAsLatest().isPresent());

            MemberLogin latestLogin = member.getMemberLoginAsLatest().get();
            String memberName = member.getMemberName();
            LocalDateTime latestLoginDatetime = latestLogin.getLoginDatetime();
            String loginMemberStatusName = latestLogin.getMemberStatus().get().getMemberStatusName();

            log("会員名称=" + memberName + ", 最終ログイン日時=" + latestLoginDatetime
                    + ", 最終ログイン時の会員ステータス名称=" + loginMemberStatusName);
        }
    }
}

// TODO sqlのエイリアス、自分は作りたくない派。エイリアス名つけると逆に混乱する気がしている(慣れていないだけかも)。
