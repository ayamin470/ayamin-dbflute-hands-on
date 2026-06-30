package org.docksidestage.handson.app.web.purchase;

import java.time.LocalDateTime;

// [scaffold] 購入一覧画面へ「出していく」表示データの入れ物 (1行 = 1購入)。
// Form が「入ってくる」DTO なのに対し、HTML Bean は「出ていく」DTO。
// 命名規約: 一覧の1行を表すため *RowBean とする。チュートリアルの SeaLandRowBean 相当。
// 参考: https://dbflute.seasar.org/ja/lastaflute/howto/action/makeashtml.html
/**
 * 購入一覧の1行分の表示データ。
 * @author ayamin
 */
public class PurchaseListRowBean {

    /** 購入ID。 */
    public Long purchaseId;

    /** 購入した会員の名称。 */
    public String memberName;

    /** 購入した商品の名称。 */
    public String productName;

    /** 購入日時。 */
    public LocalDateTime purchaseDatetime;

    /** 購入価格。 */
    public Integer purchasePrice;
}
