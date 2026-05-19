package org.docksidestage.handson.exercise;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.allcommon.CDef;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.Product;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 * @author jflute
 */
public class HandsOn04Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

    public void test_退会会員の未払い購入を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Arrange ##
            // String wdl = "WDL";
            // Integer unpaidFlg = 0;

        // ## Act ##
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Product();
            // cb.query().queryMember().setMemberStatusCode_Equal(wdl);
            // TODO ayamin CDef使わずメソッド指定のものを使ってみましょう by jflute (2026/05/19)
            //  e.g. cb.query().queryMember().setMemberStatusCode_Equal_退会会員();
            cb.query().queryMember().setMemberStatusCode_Equal_AsMemberStatus(CDef.MemberStatus.退会会員);
            // cb.query().setPaymentCompleteFlg_Equal(unpaidFlg);
            cb.query().setPaymentCompleteFlg_Equal_AsFlg(CDef.Flg.False);
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        for (Purchase purchase : purchaseList) {
            Member member = purchase.getMember().get();
            Product product = purchase.getProduct().get();

            log("会員名称=" + member.getMemberName() + ", 商品名=" + product.getProductName());
            // assertEquals(unpaidFlg, purchase.getPaymentCompleteFlg());
            assertTrue(purchase.isPaymentCompleteFlgFalse());
        }
    }

    public void test_会員退会情報も取得して会員を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Arrange ##
            // String wdl = "WDL";

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
            // cb.query().setMemberStatusCode_Equal(wdl);
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);

        for (Member member : memberList) {
            String memberStatusCode = member.getMemberStatusCode();
            boolean hasWithdrawal = member.getMemberWithdrawalAsOne().isPresent();

            log("会員名称=" + member.getMemberName() + ", ステータスコード=" + memberStatusCode + ", 退会情報有無=" + hasWithdrawal);
            // if (wdl.equals(memberStatusCode)) {
            if (member.isMemberStatusCode退会会員()) { //退会会員である場合はそのデータが本当に存在するかアサート
                assertTrue(hasWithdrawal);
            } else {
                assertFalse(hasWithdrawal); //退会会員でない場合はそのデータがmember_withdrawalにデータがないことをアサート
            }
        }
    }
    // #1on1: 暗黙の区分値とテーブル区分値 (2026/05/19)
    // TODO jflute dfpropのmap/listのお話 (2026/05/19)

    public void test_一番若い仮会員の会員を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md
        // JavaDoc確認対象: setMemberStatusCode_Equal_仮会員()

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.fetchFirst(1); //limit1的な意味
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        Member youngestProvisionalMember = memberList.get(0);
        String memberStatusName = youngestProvisionalMember.getMemberStatus().get().getMemberStatusName();

        log("会員名称=" + youngestProvisionalMember.getMemberName() + ", 会員ステータス名称=" + memberStatusName);
        assertTrue(youngestProvisionalMember.isMemberStatusCode仮会員());
    }

    public void test_支払済みの購入の中で一番若い正式会員のものだけ検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md
        // 解釈: 「一番若い会員」その会員の支払済み購入を購入日時の降順で見る

        // ## Act ##
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.query().setPaymentCompleteFlg_Equal_True();
            cb.query().queryMember().setMemberStatusCode_Equal_正式会員();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
            cb.fetchFirst(1);
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        Purchase targetPurchase = purchaseList.get(0);
        Member member = targetPurchase.getMember().get();
        String memberStatusName = member.getMemberStatus().get().getMemberStatusName();

        log("会員名称=" + member.getMemberName() + ", 会員ステータス名称=" + memberStatusName
                + ", 購入日時=" + targetPurchase.getPurchaseDatetime());
        assertTrue(member.isMemberStatusCode正式会員());
    }

    public void test_生産販売可能な商品の購入を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Act ##
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Member().withMemberWithdrawalAsOne().withWithdrawalReason();
            cb.query().queryProduct().setProductStatusCode_Equal_生産販売可能();
            cb.query().addOrderBy_PurchasePrice_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(purchaseList);
        for (Purchase purchase : purchaseList) {
            Product product = purchase.getProduct().get();
            String productStatusName = product.getProductStatus().get().getProductStatusName();
            // どんどん次に渡す！
            String withdrawalReasonText = purchase.getMember()
                    .flatMap(member -> member.getMemberWithdrawalAsOne())
                    .flatMap(withdrawal -> withdrawal.getWithdrawalReason())
                    .map(reason -> reason.getWithdrawalReasonText())
                    .orElse("none");

            log("商品名=" + product.getProductName() + ", 商品ステータス名称=" + productStatusName
                    + ", 退会理由テキスト=" + withdrawalReasonText);
            assertTrue(product.isProductStatusCode生産販売可能());
        }
    }

    public void test_正式会員と退会会員の会員を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.orScopeQuery(orCB -> {
                orCB.query().setMemberStatusCode_Equal_正式会員();
                orCB.query().setMemberStatusCode_Equal_退会会員();
            });
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        boolean existsFormalizedMember = false;
        boolean existsWithdrawalMember = false;
        for (Member member : memberList) {
            assertTrue(member.isMemberStatusCode正式会員() || member.isMemberStatusCode退会会員());
            if (member.isMemberStatusCode正式会員()) {
                existsFormalizedMember = true;

                //DB上の元データを取得するためにキーが必要
                Integer memberId = member.getMemberId();
                member.setMemberStatusCode_退会会員();
                assertTrue(member.isMemberStatusCode退会会員());

                Member memberFromDb = memberBhv.selectByPK(memberId).get();
                assertTrue(memberFromDb.isMemberStatusCode正式会員());
            }
            if (member.isMemberStatusCode退会会員()) {
                existsWithdrawalMember = true;
            }
        }
        assertTrue(existsFormalizedMember);
        assertTrue(existsWithdrawalMember);
    }

    public void test_銀行振込で購入を支払ったことのある会員ステータスごとに一番若い会員を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            // グループごとに先頭を取りたいので、fetchFirst(1)は使えない
            cb.query().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setBirthdate_IsNotNull();
                memberCB.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().existsPurchasePayment(paymentCB -> {
                        paymentCB.query().setPaymentMethodCode_Equal_BankTransfer();
                    });
                });
            }).partitionBy(partitionByCB -> {
                //何で区切るかの判断のためにspecifyを使っている
                partitionByCB.specify().columnMemberStatusCode();
            });
        });

        // ## Assert ##
        assertHasAnyElement(memberList);

        boolean existsFormalizedMember = false;
        boolean existsProvisionalMember = false;
        for (Member member : memberList) {
            String memberStatusName = member.getMemberStatus().get().getMemberStatusName();
            log("会員名称=" + member.getMemberName() + ", 会員ステータス名称=" + memberStatusName
                    + ", 生年月日=" + member.getBirthdate());

            if (member.isMemberStatusCode正式会員()) {
                existsFormalizedMember = true;
            }
            if (member.isMemberStatusCode仮会員()) {
                existsProvisionalMember = true;
            }
        }
        assertTrue(existsFormalizedMember);
        assertTrue(existsProvisionalMember);
    }

    public void test_ArrangeQuery_銀行振込で購入を支払ったことのある会員ステータスごとに一番若い会員を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().arrangeBankTransferPurchasedMember();
            cb.query().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setBirthdate_IsNotNull();
                memberCB.query().arrangeBankTransferPurchasedMember();
            }).partitionBy(partitionByCB -> {
                partitionByCB.specify().columnMemberStatusCode();
            });
        });

        // ## Assert ##
        assertHasAnyElement(memberList);

        boolean existsFormalizedMember = false;
        boolean existsProvisionalMember = false;
        for (Member member : memberList) {
            String memberStatusName = member.getMemberStatus().get().getMemberStatusName();
            log("会員名称=" + member.getMemberName() + ", 会員ステータス名称=" + memberStatusName
                    + ", 生年月日=" + member.getBirthdate());

            if (member.isMemberStatusCode正式会員()) {
                existsFormalizedMember = true;
            }
            if (member.isMemberStatusCode仮会員()) {
                existsProvisionalMember = true;
            }
        }
        assertTrue(existsFormalizedMember);
        assertTrue(existsProvisionalMember);
    }
}
