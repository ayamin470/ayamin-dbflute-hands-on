package org.docksidestage.handson.app.web.purchase;

import org.docksidestage.handson.dbflute.allcommon.CDef;
import org.lastaflute.web.validation.Required;

// [scaffold] /purchase/list/{productId}?pay=... のリクエストパラメータ受け取り用 Form。
// 画面から「入ってくる」データの入れ物。チュートリアルの SeaLandForm 相当。
// pay は支払方法 (CDef.PaymentMethod: HAN=手渡し / BAK=銀行振込 / CRC=クレジットカード)。
// 参考: https://dbflute.seasar.org/ja/lastaflute/howto/action/makeashtml.html
/**
 * 購入一覧画面の検索フォーム。
 * @author ayamin
 */
public class PurchaseListForm {

    /** 支払方法 (クエリパラメータ pay)。 */
    @Required
    public CDef.PaymentMethod pay;
}
