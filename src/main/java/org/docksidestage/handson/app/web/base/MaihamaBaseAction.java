package org.docksidestage.handson.app.web.base;

import org.lastaflute.web.LaAction;

// [scaffold] 「WebアプリでのDBFluteを知ろう」section: makeashtml の素振り。
// このプロジェクトには LastaFlute 本体が未導入のため、import org.lastaflute.web.LaAction は
// まだ解決されず、現時点ではコンパイルは通りません（学習用スキャフォルド）。
// 参考: https://dbflute.seasar.org/ja/lastaflute/howto/action/makeashtml.html
/**
 * 当アプリの全 Action の基底クラス。チュートリアルの HarborBaseAction 相当。
 * 共通処理（ログイン制御・共通レンダリング・例外ハンドリングなど）を将来ここに集約する。
 * FreeGen 生成の {@link MaihamaHtmlPath} を implements することで、各 Action から
 * path_Member_MemberListHtml のようなパス定数を直接参照できる。
 * @author ayamin
 */
public abstract class MaihamaBaseAction extends LaAction implements MaihamaHtmlPath {
}
