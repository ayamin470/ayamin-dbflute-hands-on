package org.docksidestage.handson.app.web.purchase;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.app.web.base.MaihamaBaseAction;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.lastaflute.web.Execute;
import org.lastaflute.web.response.HtmlResponse;

// [scaffold] URL: /purchase/list/{productId}?pay=... (購入一覧画面) に対応する Action。
// 基点テーブルは PURCHASE。チュートリアルの SeaLandAction 相当。
// ステップ「Executeメソッドを実装」: チュートリアルの cb 検索をそのまま採用 (命名のみ Purchase に読み替え)。
// 参考: https://dbflute.seasar.org/ja/lastaflute/howto/action/makeashtml.html
/**
 * 購入一覧画面の Action。
 * @author ayamin
 */
public class PurchaseListAction extends MaihamaBaseAction {

    @Resource
    private PurchaseBhv purchaseBhv;

    /**
     * 購入一覧画面を表示する。
     * URL: GET /purchase/list/{productId} (クエリ: ?pay=HAN など)
     * @param productId 絞り込み対象の商品ID (パスパラメータ)
     * @param form 検索フォーム (NotNull)
     * @return 購入一覧画面のHTMLレスポンス (NotNull)
     */
    @Execute
    public HtmlResponse index(int productId, PurchaseListForm form) {
        validate(form, messages -> {}, () -> {
            return asHtml(path_Purchase_PurchaseListHtml);
        });
        Integer userId = getUserBean().get().getUserId();
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Product();
            cb.query().setProductId_Equal(productId);
            cb.orScopeQuery(orCB -> {
                orCB.query().setMemberId_Equal(userId);
                // [注意] existsMemberFollowingByYourMemberId は MEMBER_FOLLOWING テーブル前提だが、
                // このハンズオンのスキーマには MEMBER_FOLLOWING が無いため、この行は本スキーマでは生成されない。
                orCB.query().queryMember().existsMemberFollowingByYourMemberId(followingCB -> {
                    followingCB.query().setMyMemberId_Equal(userId);
                });
            });
            cb.query().existsPurchasePayment(paymentCB -> {
                paymentCB.query().setPaymentMethodCode_Equal_AsPaymentMethod(form.pay);
            });
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });
        return asHtml(path_Purchase_PurchaseListHtml);
    }
}
