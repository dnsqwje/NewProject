-- 1. 회원
CREATE TABLE IF NOT EXISTS app_users (
                                         id VARCHAR(120) PRIMARY KEY,
                                         password_hash VARCHAR(100) NOT NULL,
                                         display_name VARCHAR(120) NOT NULL,
                                         email VARCHAR(255),
                                         profile_json LONGTEXT
);

-- 2. 회원별 데이터·예산·설정
CREATE TABLE IF NOT EXISTS user_data (
                                         user_id VARCHAR(120) NOT NULL,
                                         data_key VARCHAR(60) NOT NULL,
                                         json_value LONGTEXT NOT NULL,
                                         PRIMARY KEY (user_id, data_key),
                                         FOREIGN KEY (user_id)
                                             REFERENCES app_users(id) ON DELETE CASCADE
);

-- 3. 기존 커뮤니티 데이터 호환
CREATE TABLE IF NOT EXISTS community_data (
                                              data_key VARCHAR(60) PRIMARY KEY,
                                              json_value LONGTEXT NOT NULL
);

-- 4. 회원 권한·이메일 인증·약관 동의
CREATE TABLE IF NOT EXISTS account_security (
                                                user_id VARCHAR(120) PRIMARY KEY,
                                                role VARCHAR(20) NOT NULL,
                                                verified_email VARCHAR(255) UNIQUE,
                                                terms_version VARCHAR(40),
                                                privacy_version VARCHAR(40),
                                                agreed_at TIMESTAMP NULL,
                                                verified_at TIMESTAMP NULL,
                                                age_over_14 BOOLEAN NOT NULL DEFAULT FALSE,
                                                FOREIGN KEY (user_id)
                                                    REFERENCES app_users(id) ON DELETE CASCADE
);

-- 5. 이메일 인증 코드
CREATE TABLE IF NOT EXISTS signup_email_codes (
                                                  verification_id VARCHAR(36) PRIMARY KEY,
                                                  email VARCHAR(255) NOT NULL,
                                                  session_hash VARCHAR(64) NOT NULL,
                                                  code_hash VARCHAR(100) NOT NULL,
                                                  created_at TIMESTAMP NOT NULL,
                                                  expires_at TIMESTAMP NOT NULL,
                                                  verified_at TIMESTAMP NULL,
                                                  attempts INT NOT NULL DEFAULT 0,
                                                  consumed BOOLEAN NOT NULL DEFAULT FALSE
);

-- 6. 이메일 발송 제한
CREATE TABLE IF NOT EXISTS signup_mail_limits (
                                                  bucket_key VARCHAR(128) PRIMARY KEY,
                                                  started_at TIMESTAMP NOT NULL,
                                                  requests INT NOT NULL
);

-- 7. 무료체험·절약 목표·저축·알림·문의·공지·요금 비교
CREATE TABLE IF NOT EXISTS feature_records (
                                               record_id VARCHAR(36) PRIMARY KEY,
                                               owner_id VARCHAR(120) NOT NULL,
                                               user_id VARCHAR(120),
                                               record_type VARCHAR(32) NOT NULL,
                                               json_value LONGTEXT NOT NULL,
                                               created_at TIMESTAMP NOT NULL,
                                               updated_at TIMESTAMP NOT NULL,
                                               UNIQUE (owner_id, record_type, record_id),
                                               FOREIGN KEY (user_id)
                                                   REFERENCES app_users(id) ON DELETE CASCADE
);

-- 8. 계정 정지·세션 관리
CREATE TABLE IF NOT EXISTS account_restrictions (
                                                    user_id VARCHAR(120) PRIMARY KEY,
                                                    suspended BOOLEAN NOT NULL DEFAULT FALSE,
                                                    session_revision INT NOT NULL DEFAULT 0,
                                                    FOREIGN KEY (user_id)
                                                        REFERENCES app_users(id) ON DELETE CASCADE
);

-- 9. 구독
CREATE TABLE IF NOT EXISTS subscriptions (
                                             id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                             user_id VARCHAR(120) NOT NULL,
                                             name VARCHAR(255) NOT NULL,
                                             category VARCHAR(255) NOT NULL,
                                             amount BIGINT NOT NULL,
                                             cycle VARCHAR(255) NOT NULL,
                                             billing_day INT NOT NULL,
                                             status VARCHAR(255) NOT NULL,
                                             billing_anchor_date DATE,
                                             INDEX idx_subscriptions_user (user_id)
);

-- 10. 결제 내역
CREATE TABLE IF NOT EXISTS payment_records (
                                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                               user_id VARCHAR(120) NOT NULL,
                                               subscription_id BIGINT,
                                               description VARCHAR(150) NOT NULL,
                                               amount BIGINT NOT NULL,
                                               paid_on DATE NOT NULL,
                                               INDEX idx_payment_owner_date (user_id, paid_on)
);

-- 11. 게시글·이미지·태그·조회수
CREATE TABLE IF NOT EXISTS community_posts (
                                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                               author_id VARCHAR(120) NOT NULL,
                                               title VARCHAR(120) NOT NULL,
                                               category VARCHAR(40) NOT NULL,
                                               content VARCHAR(5000) NOT NULL,
                                               created_at TIMESTAMP(6) NOT NULL,
                                               html LONGTEXT,
                                               images LONGTEXT,
                                               tags LONGTEXT,
                                               view_count INT NOT NULL DEFAULT 0,
                                               updated_at TIMESTAMP(6) NULL
);

-- 12. 댓글
CREATE TABLE IF NOT EXISTS community_comments (
                                                  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                                  post_id BIGINT NOT NULL,
                                                  author_id VARCHAR(120) NOT NULL,
                                                  content VARCHAR(2000) NOT NULL,
                                                  created_at TIMESTAMP(6) NOT NULL,
                                                  updated_at TIMESTAMP(6) NULL,
                                                  INDEX idx_comments_post (post_id)
);

-- 13. 좋아요
CREATE TABLE IF NOT EXISTS community_likes (
                                               id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                               post_id BIGINT NOT NULL,
                                               user_id VARCHAR(120) NOT NULL,
                                               CONSTRAINT uq_like_post_user
                                                   UNIQUE (post_id, user_id)
);

-- 14. 신고
CREATE TABLE IF NOT EXISTS community_reports (
                                                 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                                 post_id BIGINT NOT NULL,
                                                 reporter_id VARCHAR(120) NOT NULL,
                                                 reason VARCHAR(500) NOT NULL,
                                                 status VARCHAR(20) NOT NULL,
                                                 created_at TIMESTAMP(6) NOT NULL,
                                                 CONSTRAINT uq_report_post_user
                                                     UNIQUE (post_id, reporter_id)
);

-- 15. 이미지 포함 게시글 임시저장
CREATE TABLE IF NOT EXISTS community_drafts (
                                                user_id VARCHAR(120) PRIMARY KEY,
                                                json_value LONGTEXT NOT NULL,
                                                updated_at TIMESTAMP NOT NULL,
                                                FOREIGN KEY (user_id)
                                                    REFERENCES app_users(id) ON DELETE CASCADE
);

-- 서비스 빠른 선택 목록
CREATE TABLE IF NOT EXISTS service_catalog (
                                               name VARCHAR(120) PRIMARY KEY,
                                               category VARCHAR(120) NOT NULL,
                                               icon VARCHAR(32) NOT NULL,
                                               background CHAR(7) NOT NULL,
                                               active BOOLEAN NOT NULL DEFAULT TRUE,
                                               sort_order INT NOT NULL DEFAULT 0
);