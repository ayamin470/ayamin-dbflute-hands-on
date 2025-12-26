package org.docksidestage.handson.exercise;
import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 * @author jflute
 */
public class HandsOn02Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;
    
    public void test_テストデータがあるか確認() throws Exception {
        int count = memberBhv.selectCount(cb -> {
        });

        assertTrue(count > 0);
    }

    //会員名称がSで始まる会員を検索 (これはタイトル、この中にも要件が含まれている)
    //会員名称の昇順で並べる (これは実装要件、Arrange or Act でこの通りに実装すること)
    //(検索結果の)会員名称がSで始まっていることをアサート (これはアサート要件、Assert でこの通りに実装すること)
    //"該当テストデータなし" や "条件間違い" 素通りgreenにならないように素通り防止を (今後ずっと同じ)

    //memo:
    // ListResultBean<Member> memberList = memberBhv.selectList(cb);と書いていたら型エラー、今のverではオブジェクトを渡せず、ラムダ式で処理ないよを渡す
    public void test_会員名称がSで始まる会員を検索(){
        // TODO ayamin ハンズオンでは、Arrange, Act, Assert のコメントをお願いします by jflute (2025/12/26)
        // https://dbflute.seasar.org/ja/tutorial/handson/section02.html#actasmain

        //会員リスト作る、昇順にしておく、検索実行、会員名がSで始まる
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
            cb.query().addOrderBy_MemberName_Asc();
        });

        //アサート、リストが空でないか、会員名がSから始まっているか、昇順であるか
        //リストに入っている名前を比較する
        // TODO ayamin [いいね] 素通り防止Good, UnitTestのお約束となります by jflute (2025/12/26)
        // assertHasAnyElement(memberList) という専用のメソッド用意してるので使ってみてください。
        assertFalse("検索結果が空ではない", memberList.isEmpty());
        String previousName = "";
        for (Member member : memberList) {
            String currentName = member.getMemberName();
            // TODO ayamin ハンズオンではlog()メソッドがあるので、そっち使ってみてください by jflute (2025/12/26)
            //  e.g. log("検索された会員: " + currentName);
            System.out.println("検索された会員: " + currentName);
            assertTrue("会員名がSで始まっている", currentName.startsWith("S"));
            assertTrue("会員名が昇順である", currentName.compareTo(previousName) >= 0);
            previousName = currentName;
        }

        // TODO ayamin [いいね] orderByのアサートまでやってくださりありがとうございます。 by jflute (2025/12/26)
        // 一方で、要件的には特にorderByのアサートをせよとは書いてないので、次回以降はなくても大丈夫です。
    }
}
