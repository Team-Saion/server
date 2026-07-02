-- Member 테이블에 avatar_color 컬럼 추가
alter table member add column avatar_color varchar(20) not null default 'BLUE';
