# DBFlute

### DBfluteの特徴は？

↓厳密には、DBFluteというかDBFluteの一機能である ConditionBean の特徴になるが、
ConditionBeanが代表的な機能ではあるので大きく外れてるわけではない。

↓実際のデータベーススキーマ(テーブルの構造)の情報(メタデータ)を元にクラスを自動生成する。
(途中で、メタデータをxmlに保存しておいて、そのxmlを読み込んで自動生成する)

- SQLを文字列として書くのではない。自動生成されたクラスを用いて、クエリを組み立てることでDBへのアクセスを実現する
    - **SQL（文字列）の場合：**`SELECT ... WHERE AGE >= 20` という「文章」を書く
    - DBfluteの場合：`ConditionBean` というクラスを `new` し、その中にメソッドを埋めていく

    ```jsx
    // これは古い書き方 (10年以上前)
    MemberCB cb = new MemberCB(); 
    cb.query().setMemberAge_GreaterEqual(20); 
    cb.query().addOrderBy_MemberName_Asc();

     ↓↓↓ 

    // Java8版はこちら
    memberBhv.selectList(cb -> {
        cb.query().setMemberAge_GreaterEqual(20); 
        cb.query().addOrderBy_MemberName_Asc();
    });
    ```

- 自動生成の話
```
public void test_demo() { by jflute
    // CBのメリット:
    //
    // 1. 書いている最中に嬉しい: テーブル名とかカラム名とか間違えない → 楽だし安全だし
    //
    // 2. リリースした後に嬉しい: テーブル名やカラム名が変わった時などコンパイルエラーで検知できる
    // e.g. DB変更で、MEMBER_NAME が FIRST_NAME, LAST_NAME に分割されたとして
    // 再自動生成して自動生成クラスとビジネスロジックのギャップがコンパイルエラーになるのでそれを直す。
    // → つまり、DB変更が起きたときに(比較的)安全に対処できる
    // → これが自動生成の最大のメリット

    // 文字列パターン:
    //String sql = "select * from MEMBER where MEMBER_NAME like ...";

    // ConditionBean:
    ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
        cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
        cb.query().addOrderBy_Birthdate_Desc();
    });
    for (Member member : memberList) {
        log(member.getMemberName());
    }
}
```

- ↓これは外だしSQLの話だけど、いったん登場しないのでここの理解は後でもOK by jflute
- SQLツールをテスト用としてそのまま実行できる

    1. SQLファイル (SelectMember.sql)(外部SQlツールでのテスト用)

  SQLファイルの中に、特殊なコメント `/* ... */` を書きます。

    ```jsx
    -- ツールで実行すると、/* ... */ はコメントなので無視され、
    -- テスト値の '3' が使われて実行される（エラーにならない！）
    SELECT *
      FROM MEMBER
     WHERE MEMBER_ID = /*pmb.memberId*/3
    ```

    2. Javaプログラム(本番用)

  Javaから実行するときは、DBfluteが `/*pmb.memberId*/` の部分を見つけて、実際の変数（パラメータ）に置き換えます。

    ```jsx
    // 1. パラメータ用のクラス（自動生成される）に値をセット
    SelectMemberPmb pmb = new SelectMemberPmb();
    pmb.setMemberId(99); // テスト用の '3' ではなく本番用の '99' を検索したい
    
    // 2. 実行（DBfluteが裏側で、SQL内の '3' を '?' に書き換えて '99' を埋め込む）
    memberBhv.outsideSql().selectEntity(path, pmb, ...);
    ```

    - SQLツールで開いたときは `3` として実行されるので、構文エラーにならない。Javaで動かすときは `99` (変数) に差し替わる。

- ドキュメント生成をDoc**タスク**で実行できるので、変更があっても最新版を見ることができる

### ReplaceSchemaタスク、JDBCタスク、Docタスク、Generateタスクはそれぞれ何をしている？

大体の場合、ReplaceSchema▶︎JDBCタスク▶︎Docタスク▶︎Generateタスクの順で行われる

- **ReplaceSchema:** `./replace-schema.sh`
    - データベース環境の初期化・再構築
    - マスタデータの登録 ほぼ変更されないデータのこと。**都道府県マスタ:** 北海道、青森…沖縄とか

--- DBが存在するとして... by jflute

- **JDBC:** `./jdbc.sh`
    - コードを自動生成するために、実際のデータベースに接続し、テーブル名、カラム名、型、外部キー制約などの**メタデータ（設計情報）を取得**
    - 取得したデータは SchemaXML (project-schema-[db名].xml) に保存される

- **Doc:** `./doc.sh`
    - SchemaHTMLの作成 (要は、「DBのテーブル定義書」を作成)
    - HistoryHTMLの作成 前回のDB状態と比較したときの差分がここに入れられる
    - 外部SQLに文法ミスがないかチェックし、ドキュメント(SchemaHTML)に「こんなSQLがあるよ」と載せる
    ↑確かに、外だしSQLの情報をSchemaHTMLに載せるという機能はあるはあるので、後半はそれかな？
    ↑あと、外部SQL改めて外だしSQLに文法ミスがないかをチェックするのは、Sql2EntityやOutsideSqlTestタスク

- **Generate:** `./generate.sh`
    - JDBCタスクで取得した情報をもとに、クラスを生成する
    - (具体的には、SchemaXMLを読み込んでDBメタデータを使ってJavaファイルを生成する)

### ConditionBean、Behavior、Entityってそれぞれ何？

呼び出される順番はConditionBean▶︎Behavior▶︎Entity

- ConditionBean：クエリを組み立てるクラス
- Behavior：作られたクエリを実行するクラス
- Entity：データベースのテーブル1行分のデータが入るクラス。MEMBERテーブルならMemberクラス、PRODUCTテーブルならProductクラスが作られる

```jsx
// 1.【ConditionBean】
MemberCB cb = new MemberCB();
cb.query().setMemberId_Equal(3); 

// 2.【Behavior】
// 戻り値が 【Entity】になる
Member member = memberBhv.selectEntity(cb);

// 3. 【Entity】
String name = member.getMemberName();
```
