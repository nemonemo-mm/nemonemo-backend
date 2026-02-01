-- V13__drop_notice_title_column.sql
-- notice 테이블에서 사용하지 않는 title 컬럼 제거
-- (코드에서는 content만 사용하고, title은 동적으로 생성됨)

ALTER TABLE notice
    DROP COLUMN IF EXISTS title;

