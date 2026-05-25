# 要件チェック

### memo：ORで「条件のまとまり」が難しくなるとは？

ANDだけの検索は、条件を足すたびに対象が狭くなるので分かりやすい。

```sql
where MEMBER_STATUS_CODE = 'FML'
  and MEMBER_NAME like 'S%'
  and BIRTHDATE is not null
```

これは、

```text
正式会員で
かつ、名前がS始まりで
かつ、生年月日がある人
```

という意味。

ORが入ると、急に「どこからどこまでがORなのか」が重要になる。

例えば

```text
正式会員で、かつ
名前がS始まり、または、会員IDが3の人
```

これはSQLではこう書きたい。

```sql
where MEMBER_STATUS_CODE = 'FML'
  and (
       MEMBER_NAME like 'S%'
       or MEMBER_ID = 3
  )
```

でも、括弧を忘れてこう書くと意味が変わってしまう。

```sql
where MEMBER_STATUS_CODE = 'FML'
  and MEMBER_NAME like 'S%'
   or MEMBER_ID = 3
```

```text
正式会員かつ名前S始まり
または
会員IDが3の人
```

になる。

```text
A AND (B OR C)
```

なのか、

```text
(A AND B) OR C
```

なのかで、意味が変わりうる

---

### では、OrScopeQueryは何を解決しているのか？

OrScopeQueryは、この「どこからどこまでがORなのか」を、Javaのブロック構造で明示するための機能。

例えば

```java
MemberCB cb = new MemberCB();

cb.query().setMemberStatusCode_Equal_Formalized();

cb.orScopeQuery(orCB -> {
    orCB.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
    orCB.query().setMemberId_Equal(3);
});
```

生成されるSQLのイメージ

```sql
where MEMBER_STATUS_CODE = 'FML'
  and (
       MEMBER_NAME like 'S%'
       or MEMBER_ID = 3
  )
```


## 会員住所情報を検索()
- [x] 会員住所情報を検索できている
    - [x] 会員名称をログに出力している
    - [x] 住所をログに出力している
    - [x] 地域名称をログに出力している
    - [x] 会員IDの昇順で並べている
    - [x] 有効開始日の降順で並べている
    - [x] 検索結果が1件以上あることをアサートしている(素通りチェック)


### memo
- additionalForeignKeyMap.dfpropに設定を加えたことで、こういうクエリが実行されるようになった
```
select ...
  from MEMBER member
  left outer join MEMBER_ADDRESS address
    on member.MEMBER_ID = address.MEMBER_ID
   and address.VALID_BEGIN_DATE <= ?
   and address.VALID_END_DATE >= ?
```

## 会員と共に現在の住所を取得して検索()
- [x] 会員と共に現在の住所を取得して検索できている
    - [x] `setupSelect_MemberAddressAsValid()` の JavaDoc に comment があることを確認している
    - [x] 現在日付はスーパークラスの `c` 始まりのメソッドを利用している
    - [x] 会員名称をログに出力している
    - [x] 住所をログに出力している
    - [x] 会員住所情報が取得できていることをアサートしている
    - [x] 検索結果が1件以上あることをアサートしている(素通りチェック)

## 千葉に住んでいる会員の支払済み購入を検索()
- [x] 千葉に住んでいる会員の支払済み購入を検索できている
    - [x] 現在住所の地域が千葉である会員に絞り込んでいる
    - [x] 会員ステータス名称をログに出力している
    - [x] 住所をログに出力している
    - [x] 購入に紐づいている会員の住所の地域が千葉であることをアサートしている
    - [x] 検索結果が1件以上あることをアサートしている(素通りチェック)

## 最終ログイン時の会員ステータスを取得して会員を検索()
- [x] 最終ログイン時の会員ステータスを取得して会員を検索できている
    - [x] `setupSelect_MemberLoginAsLatest()` の JavaDoc に自分で設定した comment が表示されることを確認している
    - [x] 会員名称をログに出力している
    - [x] 最終ログイン日時をログに出力している
    - [x] 最終ログイン時の会員ステータス名称をログに出力している
    - [x] 最終ログイン日時が取得できていることをアサートしている
    - [x] 検索結果が1件以上あることをアサートしている(素通りチェック)

## テストデータの登録時チェック
- テストコードで確認できない「そもそも誤ったテストデータが入っている」ことを確認する 
- take-finally.sqlにルールを記述することで実現できる
- ログの中で Take Finallyがあるので、そこで「ルールに違反しているデータがあるかどうか」を確認できる
