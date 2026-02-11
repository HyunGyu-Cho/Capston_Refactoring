-- MySQL 데이터베이스 설정 스크립트
-- Smart Healthcare 애플리케이션용 데이터베이스 생성

-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS finaldbsmarthealthcare 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 2. 데이터베이스 사용
USE finaldbsmarthealthcare;

-- 3. 사용자 권한 확인 (선택사항)
-- GRANT ALL PRIVILEGES ON finaldbsmarthealthcare.* TO 'root'@'localhost';
-- FLUSH PRIVILEGES;

-- 4. 데이터베이스 생성 확인
SHOW DATABASES;
SELECT DATABASE();
