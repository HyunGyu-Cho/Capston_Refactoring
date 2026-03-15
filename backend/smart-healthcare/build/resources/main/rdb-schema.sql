-- Smart Healthcare RDB Schema (Member/Auth/Inbody)
-- 목적: 로컬/테스트/운영에서 동일한 스키마를 재현하기 위한 기준 SQL

CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    birth_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    auth_provider VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_member_email UNIQUE (email),
    CONSTRAINT ck_member_gender CHECK (gender IN ('MALE', 'FEMALE')),
    CONSTRAINT ck_member_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    CONSTRAINT ck_member_auth_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE', 'NAVER', 'KAKAO'))
);

CREATE TABLE IF NOT EXISTS member_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT fk_member_role_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT ck_member_role_role CHECK (role IN ('USER', 'MANAGER', 'ADMIN'))
);

CREATE INDEX IF NOT EXISTS idx_member_role_member_id ON member_role(member_id);

CREATE TABLE IF NOT EXISTS inbody (
    inbody_id BIGINT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    measured_at TIMESTAMP NOT NULL,
    height_cm DECIMAL(5,2) NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    body_fat_mass_kg DECIMAL(5,2),
    skeletal_muscle_mass_kg DECIMAL(5,2),
    body_water_l DECIMAL(5,2),
    waist_hip_ratio DECIMAL(4,2),
    visceral_fat_level INT,
    bmi DECIMAL(5,2) NOT NULL,
    body_fat_percent DECIMAL(5,2),
    age_at_measurement INT NOT NULL,
    calculation_version VARCHAR(20) NOT NULL,
    CONSTRAINT fk_inbody_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT uk_inbody_member_measured_at UNIQUE (member_id, measured_at)
);

CREATE INDEX IF NOT EXISTS idx_inbody_member_measured_at_id
    ON inbody(member_id, measured_at DESC, inbody_id DESC);
