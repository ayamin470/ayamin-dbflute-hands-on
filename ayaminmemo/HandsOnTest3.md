### set up select
- https://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/setupselect/index.html

### Docタスクの実行
- dbflute_maihamadbディレクトリで``./manage.sh``を実行 
- 選択肢から22(doc)を選択 
- 生成されるとここに入る
````
~/dbflute-hands-on/
└── dbflute_maihamadb/ 
├── manage.sh
└── output/    
    └── doc/
    ├── schema-maihamadb.html  <-- これ
````

### カージナリティ(Cardinality)
- テーブル同士の「1対1」や「1対多」といった対応関係(数)のこと
- DBfluteでは「n対1」なのか「多対1」なのかでメソッドが変わる(Asoneがつくかどうか)

### OptinalEnitity
- DBfluteでは、基点テーブルから親テーブルにデータを取りにいく時、中身が存在するか分からないので、OptionalEntity型で検索結果を返す仕組みになっている
- MemberStatusは基本的にDB上には存在する(はず)。が、setupSelectを書き忘れるとmember.getMemberStatus()がnullになって、ぬるぽになる、それを防ぐ

### .map .orelse

### 進捗memo
- 1時間で1問 
- 1on1前に3問位は解いておきたいので、月金朝はmustで取り組みたい

### 久保さんに聞く
- assertTrue()は1件ずつやるのがいいのか、まとめてやるのがいいのか？
(どっちでも良さそう)
- 自分の書いたクエリが本当に検索できているか目視で確認してみたい、どうやればいいんだろう

