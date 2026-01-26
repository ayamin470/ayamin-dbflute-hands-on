package org.docksidestage.handson.exercise;
import java.time.LocalDate;
import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberStatus;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * @author ayamin
 * @author jflute
 */
public class HandsOn03Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

    public void test_会員名称がSで始まる1968年1月1日以前に生まれた会員を検索(){
        //[1] 会員名称がSで始まる1968年1月1日以前に生まれた会員を検索
            //会員ステータスも取得する
            //生年月日の昇順で並べる
            //会員が1968/01/01以前であることをアサート
            //※"以前" の解釈は、"その日ぴったりも含む" で。

        // ## Arrange ##
        // 検索用の基準日を作る
        LocalDate targetDate = LocalDate.of(1968, 1, 1);

        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            // TODO done ayamin 実装順序は、データの取得、絞り込み、並び替え by jflute (2026/01/16)
            //  => http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
            cb.setupSelect_MemberStatus(); //(setupSelect:会員テーブル(member_status)を取得)
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix()); //Sから始まる人を検索
            cb.query().setBirthdate_LessEqual(targetDate); //生まれが基準日以前を検索
            cb.query().addOrderBy_Birthdate_Asc(); //昇順
        });

        // ## Assert ##
        //検索結果が空でないこと
        //会員が1968/01/01以前生まれであること
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            LocalDate birthdate = member.getBirthdate();
            log("検索された会員: " + member.getMemberName() + ", 生年月日: " + birthdate);
            // #1on1: afterにして否定にするのGood (2026/01/16)
            //assertTrue(birthdate.isBefore(targetDate) || birthdate.isEqual(targetDate));
            // TODO done ayamin assertFalse()を使っちゃいましょう by jflute (2026/01/16)
            assertFalse(birthdate.isAfter(targetDate));
        }
    }

    public void test_会員ステータスと会員セキュリティ情報も取得して会員を検索若い順で並べる() {
    //[2]会員ステータスと会員セキュリティ情報も取得して会員を検索 若い順で並べる
        // 生年月日がない人は会員IDの昇順で並ぶようにする
        // 会員ステータスと会員セキュリティ情報が存在することをアサート
        // ※カージナリティを意識しましょう

        // ## Act ##
        //会員を全検索
        //年齢が若い順で並べる
        //生年月日がない人は会員IDの昇順
        //setupSelect:会員ステータステーブルを取得
        //setupSelect:会員セキュリティ情報
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
            cb.query().addOrderBy_Birthdate_Desc(); //若い順＝数字が大きい順＝降順
            cb.query().addOrderBy_MemberId_Asc(); // #1on1: 第二ソートキーの話 (2026/01/16)
            // #1on1: ユニークなソートの話。基本的には業務で検索するときはユニークにソートする。 (2026/01/16)
        });

        // ## Assert ##
        //検索結果が空でないこと
        //会員ステータスと会員セキュリティ情報が存在すること
        assertHasAnyElement(memberList);
        for (Member member : memberList) {
            log("検索された会員: " + member.getMemberName()
                    + ", ステータス有無=" + member.getMemberStatus().isPresent()
                    + ", セキュリティ有無=" + member.getMemberSecurityAsOne().isPresent());

            // #1on1: すべての会員は、会員ステータスを必ず持っているものか？ (2026/01/16)
            // 外部キー制約 → ForeignKey制約 → FK制約 (えふけー)
            // NotNullかつFK制約、だから、探しに行けば絶対に存在する
            assertTrue(member.getMemberStatus().isPresent());

            // #1on1: すべての会員は、会員セキュリティを必ず持っているものか？ (2026/01/16)
            // 探しにいく方向とFKの方向が逆なので、さっきの論理は通用しない。
            // 物理的には必ず存在する保証はない。ただ、ER図上で黒丸がないので、必ず存在する1:1であることを示してる。
            // この場合、物理制約ないけれども、業務制約(論理制約: 単なる人間の決め事)はあるという感じ。
            // そもそも、SchemaHTML上でDBコメントに絶対存在するって書いてある。
            assertTrue(member.getMemberSecurityAsOne().isPresent());
            
            // → if文を書かない理由というのを明確にすること (2026/01/16)
            //
            // 必ず存在する1:1なのか？
            // いないかもしれない1:1なのか？
            // を気にしてみてください。
        }
    }
}
