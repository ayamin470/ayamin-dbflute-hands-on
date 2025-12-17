package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.unit.UnitContainerTestCase;

/**
 * クエリの書き方：cb.query().set[カラム名]_[条件]
 *　@Testがあると通らなかった(@TestはJUnit4以降で使える、親クラスのUnitContainerTestCaseがJUnit3なので、それに合わせる)
 * unit3に合わせて、ラムダ式を使って新しくcbを作るべし
 * 変数の宣言も忘れずに
 */

/**
 *  どこをみたらテストが成功しているか分かる？ XLogが要約的なやつ。
 *  (XLog@log():43) - ===========/ [00m00s339ms (4) first={1, Stojkovic, Pixy, FML, 2007-12-01T11:01:10, 1965-03-03, 2025-11-28T09:40:35, sea, 2025-11-28T09:40:35, land, 0}@388f0878]
 */

 public class HandsOn03Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    public void test会員名がSで始まりかつ1968年以前生まれの人を探す() {
        // 検索条件の日付を用意
        LocalDate targetDate = LocalDate.of(1968, 1, 1);

        List<Member> memberList = memberBhv.selectList(cb -> {
            // ① 会員名がSから始まる
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());

            // ② かつ、1968年1月1日以前に生まれている
            cb.query().setBirthdate_LessEqual(targetDate);
        });

        // --- 検証 ---
        assertFalse(memberList.isEmpty());

        for (Member member : memberList) {
            // 両方の条件を満たしているか確認
            assertTrue(member.getMemberName().startsWith("S"));
            assertTrue(member.getBirthdate().isBefore(targetDate.plusDays(1))); // 1/1”以前”、つまり1/2未満
        }
    }

    public void test会員名がSから始まる人を探す() {

        // 新しくcbを作成し、会員名がSから始まる人を検索する
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
        });
        // 検索結果が空でないこと
        assertFalse(memberList.isEmpty());
        // すべての会員名がSで始まること
        for (Member member : memberList) {
            assertTrue(member.getMemberName().startsWith("S"));
        }
    }

    public void test1968年1月1日以前に生まれた会員を探す() {
        // 新しくcbを作成し、1968年1月1日以前に生まれた会員を検索する
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_LessEqual(toLocalDate("1968-01-01"));
        });
        // 検索結果が空でないこと
        assertFalse(memberList.isEmpty());
        // すべての会員の生年月日が1968年1月1日以前であること
        for (Member member : memberList) {
            assertTrue(member.getBirthdate().isBefore(toLocalDate("1968-01-02")));
        }
    }

    /**
     * カージナリティ：検索のピンポイント性的な指標
     * 例：会員ID＞会員の生年月日＞会員ステータス
     */

    public void test会員ステータスがFMLの会員のLOGIN_PASSWORDを検索() {
        // 会員ステータスと会員セキュリティ情報も取得して会員を検索
        // 若い順で並べる。生年月日がない人は会員IDの昇順で並ぶようにする
        // 会員ステータスと会員セキュリティ情報が存在することをアサート
        List<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_Equal_Formalized();

            for (Member member : memberList) {
                MemberSecurity security = member.getMemberSecurityAsOne();
                // セキュリティ情報が存在することを確認
                assertNotNull(security);
                // ログインパスワードを取得
                String loginPassword = security.getLoginPassword();
                // ログに出して確認しとく
                log("会員名: " + member.getMemberName() + " / パスワード: " + loginPassword);
                assertNotNull(loginPassword);
            }
        });

    }

}

