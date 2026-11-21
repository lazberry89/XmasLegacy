# 수정 계획

기준: [code-analysis.md](code-analysis.md). 항목 번호(H1~H15)는 그 문서의 번호다.
제외: `collectors`(H4와 관련 중간 항목), `huntingZone` 관련 항목은 기획 취소로 수정하지 않는다. 처리 방법은 5단계 참고.
원칙: 이 코드베이스의 기존 방식(명령 안의 `isOp()` 검사)을 따른다. 새 권한 시스템은 만들지 않는다.

## 1단계. 권한 누락 + 돈 생성 (가장 급함, 작고 위험 낮음)

| 대상 | 수정 |
|---|---|
| H2 `/0947345` UserLoadCommand | 맨 앞에 `isOp()` 검사 |
| H3 `/강화` booster/start/here/test | 하위 명령별 `isOp()` 검사 (일반 유저용 명령이 따로 있으면 그것만 열어 둠) |
| H9 `/gacha` | `isOp()` 검사 |
| H10 `/goods` 계열 | `isOp()` 검사 + 가격 `> 0` 검증 |
| H11 `/cos` | `isOp()` 검사 |
| H12 `/ability`, `/head`, `/nightmare` | `isOp()` 검사 |
| 중간 `UserManager.withdraw` | `amount <= 0`이면 false. 음수 가격이 어디서 들어와도 막는 마지막 방어선 |
| 중간 `EconomyManager.deposit` | 유저가 로드 안 됐으면 false |

검증: 일반 계정으로 각 명령 실행 시 거부되는지, OP로는 기존대로 되는지 확인. 빌드(`./gradlew build`).

## 2단계. 아이템/돈 손실과 복제

| 대상 | 수정 |
|---|---|
| H5 StockManager.buyStock | `addItem` 결과 확인. 남으면 바닥 드롭 또는 구매 취소 후 환불. 수량 상한 추가 |
| H7 GachaListener | 메인 핸드만 처리, 개봉 성공 시 번들 1개 소모 |
| H8 GachaContentsConfig | 번들 타입을 같이 저장/로드. 보상 변경 시 즉시 저장 |
| H13 DeathBoxConfig | 꺼낸 뒤 내용 갱신(또는 닫을 때 현재 인벤토리를 저장) |
| H14 DeathBox | 41칸 대응(6줄 54칸 인벤토리로 확장). 차감 전에 생성 성공 확인 |
| H15 FlameWeaverListener | 공격자/피해자 방향을 바로잡아 "때릴 때" +30% 적용 |
| 경미 NPC 신규 보상 | `addItem` 성공 후에 수령 플래그를 켬 |

검증: 인벤토리 가득 찬 상태 구매, 번들 개봉 후 개수, 시체상자 꺼낸 뒤 재시작, 화염 직조기 피해 비교.

## 3단계. 구역(region) 보안 + 데이터 안전

| 대상 | 수정 |
|---|---|
| H6 RegionCommand, RegionSettingListener | 설정창 열기/삭제/변경 모두 소유자 확인(OP 예외). 이름표에 ID 대신 소유자 이름 표시 |
| 중간 region 청크 키 | 키에 월드 UID 포함. 기존 저장 파일은 기본 월드로 간주해 변환 |
| H1 loadUser/loadOrCreateUser | DB 오류와 "행 없음"을 구분. 오류면 신규 저장하지 않고 접속을 막거나 재시도 |
| 중간 UserSaveManager 서버 이동 경합 | 퇴장 처리가 `user:<uuid>:server` 값이 자기 서버일 때만 지우도록 비교 후 삭제 |
| 중간 CurrencyListener | 유저 미로드 시 우클릭 무시 |
| 중간 blueprint | 건축 시작 시점에 진행 상태 저장 |

H1과 서버 이동 경합은 수정 범위가 넓고 위험하므로 따로 설계/테스트 후 진행한다(로컬에 DB가 있는지 먼저 확인).

## 4단계. 경제/기능 결함 (중간 항목)

- 경제: 헌터 보상 이벤트 이중 지급 수정, 물병 쓰레기 분류, 대장장이 패시브 PrismFractal 공짜 지급 제거(테스트 코드 여부 확인 후), 광산/몹 보상 상한은 정책이 필요하므로 수치 결정 후 반영, `transferMoney` 오버플로는 long 연산.
- 신규 유저 지급금 이중 지급: 의도인지 확인 후 하나로 통일.
- `RuleManager.addAll` 정규식 재생성, 문의 INSERT 실패 처리, `SqlRankRepository` Connection 닫기.
- `DeGhostMode`의 `add`를 `remove`로 수정.
- 후원 거래 ID로 중복 지급 방지(velocity DonationCommand). Redis는 인증/방화벽을 서버 쪽에서 확인.
- `airdrop.isSupplyChest`, `blueprint /bp create` 인자 처리, `temperature` 구역 옵션, 채팅 서버 키 누락 처리.
- 크래시 대비: goods/showcase/gacha 등 종료 시에만 저장하는 설정을 변경 시점 저장으로 바꿈.

항목별로 작아서 묶어서 처리한다. "재현필요" 항목은 고치기 전에 게임에서 먼저 재현해 실제 문제인지 확인한다.

## 5단계. 기획 취소 컨텐츠 정리 (확인 필요)

- `collectors`, `huntingZone`은 연결되지 않은 죽은 코드다. 삭제할지 남길지 결정이 필요하다.
- 삭제하면 해당 중간 항목(세션맵, 사냥터 저장/스폰 높이)이 함께 사라진다. 다른 패키지가 참조하는지(`grep`) 먼저 확인한다.
- 보고서 "추천 컨텐츠" 1번(수집가)과 4번의 사냥터 보스 부분은 무효다.

## 진행 방식

- 1단계부터 순서대로, 단계마다 빌드 + 가능한 범위의 게임 내 확인 후 커밋한다. 커밋은 요청하실 때만 하며 Claude 표기는 넣지 않는다.
- 현재 작업 트리에 있는 `enchant` 4개 파일의 미커밋 수정과 겹치므로(H3), 2단계 이전에 그 변경이 무엇인지 확인한다.
- 단계별 예상 규모: 1단계 파일 약 8개, 2단계 약 7개, 3단계 약 6개(큰 쪽은 H1), 4단계 약 15개.
