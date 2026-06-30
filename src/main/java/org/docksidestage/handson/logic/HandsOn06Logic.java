package org.docksidestage.handson.logic;

import java.util.List;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// #1on1: logback.xmlの説明。現場でのログ設定。 (2026/06/19)
// #1on1: テーブル名、カラム名大文字のお話 (2026/06/30)
// #1on1: 空文字とnullのお話 (2026/06/30)
// #1on1: DBFlute Client, DBFlute Engine, DBFlute Runtimeのお話 (2026/06/30)
// #1on1: decommentのお話、ラフに書いて積み上げること優先 (2026/06/30)
// #1on1: SchemaPolicyCheckのお話 (2026/06/30)
/**
 * @author ayamin
 */
public class HandsOn06Logic {

    private static final Logger logger = LoggerFactory.getLogger(HandsOn06Logic.class);

    @Resource
    private MemberBhv memberBhv;

    /**
     * 指定された suffix で会員名称を後方一致検索する。会員名称の昇順で並べる。
     * @param suffix 会員名称の後方一致キーワード (NotNull, NotEmpty)
     * @return 検索された会員のリスト (NotNull, EmptyAllowed)
     * @throws IllegalArgumentException suffixが無効な値(null・空文字・トリムして空文字)の場合
     */
    public List<Member> selectSuffixMemberList(String suffix) {
        //間違っていたら、DBアクセス前に弾く
        if (suffix == null) {
            throw new IllegalArgumentException("引数 suffix が null です。会員名称の後方一致キーワードを指定してください。");
        }
        if (suffix.trim().isEmpty()) {
            throw new IllegalArgumentException("引数 suffix が空です: suffix=[" + suffix + "]");
        }
        ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch(suffix, op -> op.likeSuffix());
            cb.query().addOrderBy_MemberName_Asc();
        });

        for (Member member : memberList) {
            logger.debug("member: name={}, birthdate={}, formalized={}",
                    member.getMemberName(), member.getBirthdate(), member.getFormalizedDatetime());
        }
        return memberList;
    }
}
