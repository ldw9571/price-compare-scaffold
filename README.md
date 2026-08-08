# 위탁판매 가격비교 시스템 - 프로젝트 스캐폴딩 (1단계)

## 구조
- `backend/` : Spring Boot 3.x REST API 서버 (Java 17, Gradle, MySQL, JPA)
- `frontend/` : React + Vite 대시보드 (아직 뼈대만 있는 상태)

## 현재 상태
이번 단계에서는 **뼈대만** 만들었습니다. 실제 로직(도매꾹/네이버 API 호출, 스코어링,
배치, 화면 구현)은 다음 단계들에서 채워 나갑니다.

- [x] 패키지 구조
- [x] build.gradle / DB 연결 설정(application.yml)
- [x] JPA 엔티티 4종 (WholesaleProduct, PriceComparison, WatchKeyword, ScanLog)
- [x] React(Vite) 뼈대
- [x] 도매꾹/네이버 API 클라이언트 실제 구현
- [x] DB 스키마 확정 (인덱스/유니크 제약 + DDL 참고 스크립트)
- [x] 서비스 로직 연결 (수집 → 비교 → 스코어링)
- [x] 배치 로직 (매일 새벽 3시 자동 실행 + 수동 실행 API)
- [x] 대시보드 화면 (상품 리스트/상세/스캔이력/키워드관리)

## 전체 흐름 테스트 방법

1. MySQL에 `price_compare` DB 생성 (위 SQL 참고)
2. API 키 발급 후 환경변수 설정 (`DOMEGGOOK_API_KEY`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`)
3. 백엔드 실행: `cd backend && ./gradlew bootRun`
4. 프론트엔드 실행: `cd frontend && npm install && npm run dev`
5. 브라우저에서 `http://localhost:5173` 접속
6. **감시 키워드 관리** 탭에서 키워드 등록 (예: "음식물처리기") — 처음엔 자동으로 활성 상태로 등록됩니다
7. **스캔 이력** 탭에서 "지금 스캔 실행" 버튼 클릭 → 도매꾹 수집 + 네이버 비교가 실행됩니다
   (상품 수에 따라 몇 분 걸릴 수 있습니다. 도매꾹/네이버 호출 간격 때문에 일부러 느리게 설계했습니다)
8. **상품 리스트** 탭에서 결과 확인 — 경쟁력순으로 정렬되어 나옵니다

## 알려진 제한사항 / 다음에 개선하면 좋은 것들

- `ProductRestController`의 리스트 조회가 `findAll()` 기반이라 상품이 수만 건 이상 쌓이면 느려질 수 있습니다.
  이때는 DB 레벨 쿼리(윈도우 함수)나 페이징 처리로 개선이 필요합니다.
- 도매꾹 상품목록 API는 1페이지(50건)만 수집합니다. 키워드당 더 많은 상품이 필요하면
  `ProductCollectService.collectByKeyword`에 페이지네이션 루프를 추가해야 합니다.
- 재고 수량(`stockQuantity`)은 도매꾹 목록 API에 없어서 항상 비어있습니다. 필요하면 상품상세 API를
  추가 연동해야 합니다.
- 도매꾹 JSON 응답 필드명은 XML 문서 기준으로 매핑했습니다. 실제 API 키로 첫 호출 시
  `DomeggookClient`의 주석 처리된 로그를 활성화해서 한 번 검증해보시는 걸 권장합니다.
- 스캔 수동 실행(`POST /api/scan-logs/run`)이 동기 방식이라 브라우저가 응답을 기다립니다.
  상품 수가 많아지면 `@Async` 처리로 전환을 고려하세요.

## DB 스키마

- `hibernate.ddl-auto=update` 설정으로 애플리케이션 최초 실행 시 테이블이 자동 생성됩니다.
- 실제 생성될 구조는 `backend/src/main/resources/db/schema-reference.sql`에 문서화해두었습니다
  (자동 실행되는 파일은 아니고, 참고/수동 구축용입니다).
- 핵심 제약조건:
  - `wholesale_products`: `(source_site, source_item_no)` 유니크 — 같은 상품 중복 수집 방지, 배치에서 upsert 기준으로 사용
  - `watch_keywords`: `keyword` 유니크 — 중복 등록 방지
  - `price_comparison`: `(wholesale_product_id, scanned_at)` 복합 인덱스 — 가격 추이 조회, `competitiveness_score` 인덱스 — 대시보드 정렬용
- **운영 전환 시 참고**: 지금은 개발 편의를 위해 `ddl-auto=update`를 쓰지만, 실 서비스로 넘어갈 때는
  `ddl-auto=validate`로 바꾸고 Flyway 같은 마이그레이션 도구로 전환하는 걸 권장합니다
  (스키마 변경 이력 추적, 배포 시 자동 롤백 등 안정성 때문).

## 로컬 실행 준비 (참고용 - 지금 당장 안 하셔도 됩니다)

### 1. MySQL DB 생성
```sql
CREATE DATABASE price_compare CHARACTER SET utf8mb4;
```

### 2. 백엔드 환경변수 (API 키 발급 전에는 비워둬도 실행은 됩니다 - placeholder 사용)
```bash
export DB_PASSWORD=본인_mysql_비밀번호
export DOMEGGOOK_API_KEY=발급받은_키
export NAVER_CLIENT_ID=발급받은_ID
export NAVER_CLIENT_SECRET=발급받은_SECRET
```

### 3. 백엔드 실행
```bash
cd backend
./gradlew bootRun
```

### 4. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

## 참고
- 도매꾹 API는 **분당 180회 / 하루 15,000회** 호출 제한이 있습니다. 배치 로직 단계에서
  호출 간격을 반드시 고려해야 합니다.
- `application.yml`의 API 키는 환경변수로 오버라이드되도록 설정되어 있어,
  실제 키를 코드에 하드코딩하거나 커밋할 필요가 없습니다.
