-- ============================================================
-- 참고용 DDL 스크립트
-- 실제로는 application.yml의 hibernate.ddl-auto=update 설정으로
-- 애플리케이션 최초 실행 시 자동 생성됩니다. 이 파일은 자동 실행되지
-- 않으며, 스키마 구조를 문서화하고 필요시 수동으로 DB를 구축하거나
-- 추후 Flyway/Liquibase 마이그레이션으로 전환할 때 기준으로 삼기 위한 참고 자료입니다.
-- ============================================================

CREATE DATABASE IF NOT EXISTS price_compare CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE price_compare;

-- ------------------------------------------------------------
-- 1. 도매 상품 원본 정보
-- ------------------------------------------------------------
CREATE TABLE wholesale_products (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_site       VARCHAR(30)   NOT NULL COMMENT '출처 사이트 (DOMEGGOOK, OWNERCLAN 등)',
    source_item_no    VARCHAR(50)   NOT NULL COMMENT '원본 사이트의 상품번호',
    item_name         VARCHAR(500)  NOT NULL,
    category          VARCHAR(100),
    wholesale_price   DECIMAL(12,2),
    shipping_fee      DECIMAL(12,2),
    moq               INT           COMMENT '최소구매수량 (1이면 위탁판매 가능)',
    stock_quantity    INT,
    item_url          VARCHAR(1000),
    collected_at      DATETIME,
    updated_at        DATETIME,

    CONSTRAINT uk_source_site_item_no UNIQUE (source_site, source_item_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_wholesale_products_category ON wholesale_products(category);

-- ------------------------------------------------------------
-- 2. 네이버 최저가 비교 결과 (스캔 시점별 이력)
-- ------------------------------------------------------------
CREATE TABLE price_comparison (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    wholesale_product_id    BIGINT        NOT NULL,
    naver_lowest_price      DECIMAL(12,2),
    naver_product_url       VARCHAR(1000),
    naver_mall_name         VARCHAR(200),
    competitiveness_score   DECIMAL(8,4)  COMMENT '(네이버최저가-(도매가+배송비+수수료+목표마진))/네이버최저가',
    margin_rate             DECIMAL(8,4),
    scanned_at              DATETIME      NOT NULL,

    CONSTRAINT fk_price_comparison_product
        FOREIGN KEY (wholesale_product_id) REFERENCES wholesale_products(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_price_comparison_product_scanned ON price_comparison(wholesale_product_id, scanned_at);
CREATE INDEX idx_price_comparison_score ON price_comparison(competitiveness_score);

-- ------------------------------------------------------------
-- 3. 감시 키워드/카테고리
-- ------------------------------------------------------------
CREATE TABLE watch_keywords (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword       VARCHAR(200)  NOT NULL,
    category      VARCHAR(100),
    priority      INT           COMMENT '숫자가 낮을수록 우선순위 높음 (1순위: 음식물처리기)',
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME,

    CONSTRAINT uk_keyword UNIQUE (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_watch_keywords_active ON watch_keywords(active);

-- ------------------------------------------------------------
-- 4. 스캔 배치 실행 이력
-- ------------------------------------------------------------
CREATE TABLE scan_logs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_type         VARCHAR(30)   NOT NULL COMMENT 'FULL, KEYWORD 등',
    started_at        DATETIME      NOT NULL,
    finished_at       DATETIME,
    status            VARCHAR(20)   NOT NULL COMMENT 'RUNNING, SUCCESS, FAILED',
    items_collected   INT,
    error_message     VARCHAR(2000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_scan_logs_started_at ON scan_logs(started_at);

-- ------------------------------------------------------------
-- 초기 데이터 예시 (1순위 카테고리 감시 키워드)
-- ------------------------------------------------------------
INSERT INTO watch_keywords (keyword, category, priority, active, created_at)
VALUES ('음식물처리기', '생활가전', 1, TRUE, NOW());
