package org.docksidestage.handson.exercise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
            // TODO done ayamin CDef使わずメソッド指定のものを使ってみましょう by jflute (2026/05/19)
            //  e.g. cb.query().queryMember().setMemberStatusCode_Equal_退会会員();
            cb.query().queryMember().setMemberStatusCode_Equal_退会会員();
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
    // 区分値を追加してみた
    // - HAN を TSV に追加
    // - ReplaceSchema -> JDBC -> Doc -> Generateを実行
    // - setMemberStatusCode_Equal_ハンズオン() を使うテストを作成
    // schemaHTMLにHANが追加されていることを確認。
    // その後、TSV から HAN レコードを削除して ReplaceSchema -> JDBC -> Doc -> Generate を再実行した。
    // 以下が確認できた
    // - SchemaHTML の区分値一覧から HAN が消えること
    // - setMemberStatusCode_Equal_ハンズオン() が見つからずコンパイルエラーになること
    //
    //    public void test_追加したハンズオンステータスを検索() {
    //
    //        // ## Act ##
    //        ListResultBean<MemberStatus> memberStatusList = memberStatusBhv.selectList(cb -> {
    //            cb.query().setMemberStatusCode_Equal_ハンズオン();
    //            cb.query().addOrderBy_DisplayOrder_Asc();
    //        });
    //    }

    public void test_サービスが利用できる会員を検索() {
        // TODO htmlでグルーピングが確認できなかった
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().setMemberStatusCode_InScope_ServiceAvailable();
            cb.query().queryMemberStatus().addOrderBy_DisplayOrder_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            String memberStatusName = member.getMemberStatus().get().getMemberStatusName();
            log("会員名称=" + member.getMemberName() + ", 会員ステータス名称=" + memberStatusName);
            assertTrue(member.isMemberStatusCode_ServiceAvailable());
        }
    }

    public void test_未払い購入のある会員を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().existsPurchase(purchaseCB -> {
                // 要件の未払いの購入か支払済みの購入かを簡単に切り替えられるようにする
                if (isTargetPurchaseCompleted()) { //falseだったら
                    purchaseCB.query().setPaymentCompleteFlg_Equal_True(); //支払済み購入を対象にする
                } else {
                    purchaseCB.query().setPaymentCompleteFlg_Equal_False();
                }
            });
            cb.query().addOrderBy_FormalizedDatetime_Desc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        //　loadPurchase(memberList) が memberList に入っている全会員のIDをまとめて使って、購入を一括取得
        memberBhv.loadPurchase(memberList, purchaseCB -> {
            if (isTargetPurchaseCompleted()) {
                purchaseCB.query().setPaymentCompleteFlg_Equal_True();
            } else {
                purchaseCB.query().setPaymentCompleteFlg_Equal_False();
            }
            purchaseCB.query().addOrderBy_PurchaseDatetime_Asc();
        });
        for (Member member : memberList) {
            for (Purchase purchase : member.getPurchaseList()) {
                assertFalse(purchase.isPaymentCompleteFlgTrue());
            }
        }
    }

    private boolean isTargetPurchaseCompleted() {
        return false;
    }

    public void test_会員ステータスの表示順カラムで会員を並べて検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Arrange ##
        // 並び替え条件を満たすために、一旦<CDef.MemberStatus>リストに入れておく
        List<CDef.MemberStatus> memberStatusList = new ArrayList<>(CDef.MemberStatus.listAll());
        // "表示順"で並び替える(javaのsortメソッド)
        memberStatusList.sort(Comparator.comparingInt(memberStatus -> Integer.parseInt(memberStatus.displayOrder())));

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().addOrderBy_MemberStatusCode_Asc().withManualOrder(op -> {
                for (CDef.MemberStatus memberStatus : memberStatusList) {
                    // ↑で並べた順にop.when_Equal(memberStatus) を呼んで、withManualOrder()に並び順を伝えている
                    op.when_Equal(memberStatus);
                }
            });
            cb.query().addOrderBy_MemberId_Desc();
        });

        // ## Assert ##
        assertHasAnyElement(memberList);
        int previousDisplayOrder = -1;
        Integer previousMemberId = null;
        for (Member member : memberList) {
            //会員ステータスのデータが取れていないこと
            assertFalse(member.getMemberStatus().isPresent());

            // 会員のステータスコードから区分値オブジェクトを取り出す
            CDef.MemberStatus memberStatus = member.getMemberStatusCodeAsMemberStatus();
            // そこから SubItem の displayOrder を取得して int に変換する
            int currentDisplayOrder = Integer.parseInt(memberStatus.displayOrder());
            log("会員ID=" + member.getMemberId() + ", 会員名称=" + member.getMemberName()
                    + ", ステータス=" + memberStatus.alias() + ", 表示順=" + currentDisplayOrder);

            if (previousMemberId != null) {
                assertTrue(previousDisplayOrder < currentDisplayOrder
                        || previousDisplayOrder == currentDisplayOrder && previousMemberId > member.getMemberId());
            }
            previousDisplayOrder = currentDisplayOrder;
            previousMemberId = member.getMemberId();
        }
    }
}
