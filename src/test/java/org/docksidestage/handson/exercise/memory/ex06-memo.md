# ハンズオン Section06 メモ

参考: https://dbflute.seasar.org/ja/tutorial/handson/section06.html

---

## selectSuffixMemberList の実装ポイント

- 後方一致は `cb.query().setMemberName_LikeSearch(suffix, op -> op.likeSuffix());`
- DBアクセス前に引数を弾く（null / 空文字 / トリムして空文字 → `IllegalArgumentException`）
- 検索結果を debug ログに出す（`logger.debug`）

### テスト観点

- 指定 suffix で検索され、全件が `endsWith(suffix)` であること
- 「全件ヒットではなく絞り込めている」ことも確認（`memberList.size() < total`）＝素通り防止
- 無効値（null / "" / "   "）で例外が出ること（`assertException`）

---

## DBコメントの「別名(alias)」設定（documentMap.dfprop）
- この設定でできることは、DBコメント側（ReplaceSchemaのDDL）に書かれた別名を、DBFluteに『別名として認識・抽出させる』こと
- なので、書いてなければ出てこない

## aliasDelimiterInDbComment のコメントを外す
- DBコメントを Alias と Comment に分割する
- デリミタ月のものが、CommentからAliasに表示されるように

## DBコメントの説明をEntity等のJavaDocに反映
- isEntityJavaDocDbCommentValid　= trueにした
- デリミタなしのMEMBER_STATUS_CODEの説明が、ColumnCommentでなく、Aliasに表示されるようになった
-  デフォルトではfalseになってる

## getであんまり検索したくない:コラム
- DBアクセスするようなメソッドにgetあまり使わないようにしよう、という話
- なぜなら、getにはgetter(コストほぼゼロで値を返す)のイメージがついてしまっているから 
  - なので、例えばgetMemberName() というメソッドを見たら、「あ、単にメモリから名前の文字列を返してくれるだけね」と思って誤解されてしまうかも？
  - その誤解の結果、ループ（for文）の中で何も気にせず何千回も呼び出してしまうかも？
  - 最終的にパフォーマンス劣化の理由になったり、、、、

## 諸々大文字に設定
- isTableDispNameUpperCase = true
  - 「テーブル名」が大文字になった
- isTableSqlNameUpperCase = true
  - HandsOn06LogicTestのログで確認
- isColumnSqlNameUpperCase = true
  - HandsOn06LogicTestのログで確認

## 空文字をDBに入れない設定
- isEntityConvertEmptyStringToNull = true

## DBFlute最新版を確認



## デコメントしよう
pieceファイル、pickupファイルってなに？
introを起動して、デコメントできる環境を作ってください



タブを二つ作り、同時に修正してコンフリクトさせる：あやみんのコンフリクトテスト

pieceファイルをコミット

Docを叩いてピックアップ＞こみっと

コンフリクトを直すデコメント＞こみっと


## 本気のデコメント
member_addressのADDRESS：住所が変更されると、最新の住所が新しいレコードとして登録される
regionのREGION_NAME：例：アメリカ、千葉。千葉は国として扱うドメインルールに基づいている
purchase_paymentのPAYMENT_AMOUNT：外貨に対応できるよう、小数点第二位まで許容


## DB設計のレビューを自動化

