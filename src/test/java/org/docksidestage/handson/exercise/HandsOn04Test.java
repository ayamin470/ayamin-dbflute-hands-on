package org.docksidestage.handson.exercise;

import java.util.ArrayList;
import java.util.Arrays;
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
            // done ayamin CDef使わずメソッド指定のものを使ってみましょう by jflute (2026/05/19)
            //  e.g. cb.query().queryMember().setMemberStatusCode_Equal_退会会員();
            cb.query().queryMember().setMemberStatusCode_Equal_退会会員();
            // done ayamin こっちも by jflute (2026/05/22)
            // cb.query().setPaymentCompleteFlg_Equal(unpaidFlg);
            cb.query().setPaymentCompleteFlg_Equal_False();
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
            cb.query().setBirthdate_IsNotNull();
            cb.query().addOrderBy_Birthdate_Desc();
            // done ayamin これで、最初の1件は取れるけど、最初の1件が一番若いとは限らない by jflute (2026/05/22)
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
        // ① まず一番若い正式会員を1人だけ特定する
        // done ayamin 一番若いというニュアンスがない by jflute (2026/05/22)
        // し、一番若い正式会員も、複数の購入をしている可能性はある。
        // 要件的には、購入は複数取りたいわけだけど、ヒットした購入の最初の1件だけしか取ってない。
        // せめて、この fetchFirst(1) は、購入に対してやるのであれば、会員に対してやりたい。

        // → ①いちばん若い会員を1人に確定させる
        Member youngestMember = memberBhv.selectEntityWithDeletedCheck(cb -> {
            cb.query().setMemberStatusCode_Equal_正式会員();
            cb.query().setBirthdate_IsNotNull();
            cb.query().addOrderBy_Birthdate_Desc();
            cb.fetchFirst(1);
        });
        Integer youngestMemberId = youngestMember.getMemberId();

        // ② その会員の支払済み購入を全部取る
        ListResultBean<Purchase> purchaseList = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.query().setMemberId_Equal(youngestMemberId);
            cb.query().setPaymentCompleteFlg_Equal_True();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });
        // e.g.
        // 一番若い正式会員(ayamin)
        //  |-購入1. ダイヤモンドを買った (支払い済み)
        //  |-購入2. スーパーカーを買った (支払い済み)
        //  |-購入3. お豆腐を買った (未払い)
        //
        // → 「購入1, 購入2」の一覧を取りたい

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
            // #1on1: DBFluteのEntity, 関連テーブルはOptionalだけど、カラムのgetはnull戻し (2026/05/22)
            // というハイブリッド方式。(カラムはOptional向かないのでそのようにしている)
            // どんどん次に渡す！
            // ↑Goodな表現、「ないかもしれないことを保留してどんどん次に渡す」
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
            // done ayamin もう一つのやり方、InScopeを使ってやり方も実装してみましょう by jflute (2026/05/22)
            // orScopeQuery()でもいいんだけども、orScopeQuery()は汎用的なor機能で、
            // いまこの場面は実は定型的なorであって「同カラムに対するequal値の列挙」と言える。
            // それにフィットするSQLの文法があるので、そっちを使いましょう。
            //  e.g. MEMBER_STATUS_CODE in ('FML', 'WDL')
            //       MEMBER_STATUS_CODE = 'FML' or MEMBER_STATUS_CODE = 'WDL'
            // コンピューターから見て、制限されているやり方になっている方が、良いパフォーマンスを選びやすい。
            // orの方だと汎用的で選択肢がいっぱいあるので判断が遅くなる。
            // inだったら、ああもうこれは「同カラムに対するequal値の列挙」ってすぐわかるので、
            // それに適した処理にできる可能性がある。
            //
            // まあ実際には、DBMSも頭良くなってるからこのレベルだとほぼ変わらないんだけど、
            // こういう思想でDBと触れ合って欲しいということ。
            // scope絞る意識 by ayamin
            // orScopeQuery()はコメントアウトとかで残して、思い出とか書いておいましょう。

            // InScope版に置き換え by ayamin (2026/06/07)
            // 思い出:
            // cb.orScopeQuery(orCB -> {
            //     orCB.query().setMemberStatusCode_Equal_正式会員();
            //     orCB.query().setMemberStatusCode_Equal_退会会員();
            // });

            // せっかくなので正式会員と退会会員をまとめて区分値グループを作ってみようかと思ったけど
            // 特に業務的にこれをまとめたい理由思いつかなかったのでそのままGO
            cb.query().setMemberStatusCode_InScope_AsMemberStatus(
                    Arrays.asList(CDef.MemberStatus.正式会員, CDef.MemberStatus.退会会員));
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
        
        // #1on1: DBFluteのEntityは、単なるJava上(メモリ上)の入れ物なので... (2026/05/22)
        // Behaviorで明示的にupdate()とかしない限りは、DBは何も変わらない。
        //
        // 他のO/Rマッパーだと、Entityにsetするだけで、DBが変わるものもある。
        // しかも、メジャーなO/Rマッパーでそういう挙動をする。
        //
        // DBFluteは、わりかし明示主義。あくまでbehaviorでDBアクセス。
        // updateって書いてあったらupdate。書いてないのにupdateしない。
    }

    public void test_銀行振込で購入を支払ったことのある会員ステータスごとに一番若い会員を検索() {
        // 要件はここで管理 ▶︎ ex04-requirements.md

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();

            // メインクエリで井銀行振込を絞り込む
            // サブクエリでは、実は絞り込みをしていなくて、maxの計算をしているだけ
            cb.query().existsPurchase(purchaseCB -> {
                purchaseCB.query().existsPurchasePayment(paymentCB -> {
                    paymentCB.query().setPaymentMethodCode_Equal_BankTransfer();
                });
            });

            // グループごとに先頭を取りたいので、fetchFirst(1)は使えない
            cb.query().scalar_Equal().max(memberCB -> { // 2026/05/22
                memberCB.specify().columnBirthdate();
                // done ayamin setBirthdate_IsNotNull()はなくてもOK by jflute (2026/05/22)
                // max()関数で、nullのものはmaxじゃないので、ただ除外されるだけ。
                memberCB.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().existsPurchasePayment(paymentCB -> {
                        paymentCB.query().setPaymentMethodCode_Equal_BankTransfer();
                    });
                });
            }).partitionBy(partitionByCB -> {
                //何で区切るかの判断のためにspecifyを使っている
                partitionByCB.specify().columnMemberStatusCode();
            });
            
            // done ayamin ちょこっと紛れが起きる by jflute (2026/05/22)
            // そのステータス内で一番若い会員で銀行振込で購入を支払ったことのある人の生年月日(2026/05/22)と、
            // そのステータス内で一番若い会員で銀行振込で購入を支払ったことのない人の生年月日(2026/05/22)と、
            // たまたま同じだったら、後者もヒットしちゃう。
            //
            // 今だと、max(BIRTHDATE)で導いた 2026/05/22 と同じ生年月日の会員を絞ってるだけ。
            // max(BIRTHDATE)を導く時に銀行振込の条件は見ているけど、本体の会員一覧を絞る時は見てない。

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
            // #1on1: こっちは、紛れの会員を最後にちゃんと除外している (2026/05/22)
            cb.query().arrangeBankTransferPurchasedMember();
            cb.query().scalar_Equal().max(memberCB -> {
                memberCB.specify().columnBirthdate();
                memberCB.query().setBirthdate_IsNotNull();
                memberCB.query().arrangeBankTransferPurchasedMember();
            }).partitionBy(partitionByCB -> {
                partitionByCB.specify().columnMemberStatusCode();
            });
        });
        // #1on1: ArrangeQuery (2026/05/22)
        // 業務的な複数のまとまり条件を再利用したい。
        // 検索まるごと再利用はしづらい。関連テーブル、ソート条件が変わりがち。
        // さあどうする？
        //  X. コピペしてちょっと変える (優待会員の条件コピーになっちゃう)
        //  Y. 最小公倍数メソッドする (関連テーブルを追加: A画面で不要なテーブルも追加しちゃう)
        //  Z. 引数リモコンパターンする
        // どれもなかなか微妙なので...
        // なので、再利用したいwhere句のまとまり条件だけでメソッドにして再利用すれば良いのでは？
        // それがArrangeQuery。
        // 現場のArrangeQueryを少し読んでみた。
        //
        // まとまった条件に業務的な意味付けができるのであれば、その業務名でプログラムを書きたい。
        // 小さな部品で再利用する方が柔軟性が高い。
        // (逆にいうと、大きな単位で無理やり再利用しようとすると、さっきのアンチパターン(XYZ)になりやすい)

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
    // done jflute 次回1on1ここから (2026/05/22)
    
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
        // done htmlでグルーピングが確認できなかった
        // #1on1: ↑MemberStatus区分値の欄にあった (2026/06/05)
        // 要件はここで管理 ▶︎ ex04-requirements.md
        // #1on1: groupingMap超重要話 (2026/06/05)
        // if (正式会員 || 仮会員) { をいかにやめられるか？
        // 自分で自分に質問みてみる。そこで出た答えで実装したい。
        // (業務概念の抽象化)

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
        // #1on1: 姉妹コード少しふぉろー (2026/06/05)

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
        // #1on1: subItemMap, 現場での独自の属性 (2026/06/05)

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
