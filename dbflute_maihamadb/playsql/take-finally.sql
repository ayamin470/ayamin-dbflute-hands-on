 
-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- Member addresses should be only one at any time.
-- - - - - - - - - - -/
select adr.MEMBER_ADDRESS_ID, adr.MEMBER_ID
     , adr.VALID_BEGIN_DATE, adr.VALID_END_DATE
     , adr.ADDRESS
  from MEMBER_ADDRESS adr
 --  ()内の条件に合致する(exsistsする)会員を探す
 where exists (select subadr.MEMBER_ADDRESS_ID
                     from MEMBER_ADDRESS subadr
                    -- ↑のクエリと同じmember
                    where subadr.MEMBER_ID = adr.MEMBER_ID
                      -- 外側の住所履歴の有効期間中に、同じ会員の別の住所履歴が始まっている
                      and subadr.VALID_BEGIN_DATE > adr.VALID_BEGIN_DATE
                      and subadr.VALID_BEGIN_DATE <= adr.VALID_END_DATE
       )
;

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- Provisional members should not have formalized datetime.
-- - - - - - - - - - -/
select mb.MEMBER_ID, mb.MEMBER_NAME, mb.MEMBER_STATUS_CODE
     , mb.FORMALIZED_DATETIME
  from MEMBER mb
 where mb.MEMBER_STATUS_CODE = 'PRV'
   and mb.FORMALIZED_DATETIME is not null
;

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- Members should not be born in the future.
-- - - - - - - - - - -/
select mb.MEMBER_ID, mb.MEMBER_NAME, mb.BIRTHDATE
  from MEMBER mb
 where mb.BIRTHDATE > current_date()
;

-- #df:assertListZero#
-- /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
-- Withdrawal members should have their withdrawal information.
-- - - - - - - - - - -/
select mb.MEMBER_ID, mb.MEMBER_NAME, mb.MEMBER_STATUS_CODE
  from MEMBER mb
 where mb.MEMBER_STATUS_CODE = 'WDL'
   and not exists (select withdrawal.MEMBER_ID
                      from MEMBER_WITHDRAWAL wdr
                     where wdr.MEMBER_ID = mb.MEMBER_ID
       )
;
