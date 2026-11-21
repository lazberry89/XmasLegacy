# XmasLegacy 코드 분석 보고서

분석 대상: `common`, `velocity`, `paper` 모듈 전체 (Java 21, Paper 1.21.11, Velocity).
방법: 소스를 패키지 단위로 읽고, 중요한 오류만 기록했다. 코드를 실행하지는 않았다(정적 분석).
표기: **확인** = 코드를 읽어 직접 확인함, **재현필요** = 읽기만 했고 게임/서버에서 확인해야 함.

## 1. 분석 범위

| 구분 | 패키지 | 깊이 |
|---|---|---|
| 정독 | common 전체, velocity 업무 로직, paper: LazberryRegistryFramework, plugin, enchant(핵심), role, collectors, stock, region, blueprint, gacha, shop, airdrop, deathbox, mining, teleporter, chat, party(명령), rank, location, huntingZone(핵심), currency, exp, knockout, user, infoNpcs, joinListeners, ghost, wild, icing, temperature, inquiry, cosmetics, sin, food, filter | 코드를 끝까지 읽음 |
| 훑어봄 | enchant(재료/효과/초월 능력 로직 일부), scoreboard, advancement, huntingZone 몬스터 구현, rank 보드, player/UserTagManager 외 | 시그니처와 핵심 파일 위주 |
| 이전 작업에서 검토 | bags, casino, api/donate, party/tpa, 직업 선택 GUI | 이번 정독 범위 밖 |

## 2. 중요한 오류

심각도: **높음** = 돈/아이템 손실·복제, 권한 우회, 기능 불능. **중간** = 조건부 손실, 성능, 기능 결함.
경미한 항목(로그 스팸, 미사용 코드 등)은 맨 아래에 한 줄씩만 적었다.

### 2.1 높음

| # | 위치 | 문제 | 상태 |
|---|---|---|---|
| H1 | `common/user` SqlUserRepository.loadUser, UserSaveManager.loadOrCreateUser | DB 예외 시 `loadUser`가 null을 반환하고, null이면 "신규 유저"로 처리해 `REPLACE INTO`로 저장한다. 접속 순간 DB가 잠깐 끊기면 기존 유저 행이 시작값으로 덮어써질 수 있다. | 확인 |
| H2 | `paper` UserLoadCommand (`/0947345`) | 권한 검사가 없다. 누구나 자기 User를 Redis/DB 마지막 저장본으로 되돌릴 수 있어, 돈을 쓴 뒤 실행하면 환불/복제가 된다(저장 주기 5분). | 확인 |
| H3 | `enchant` EnchantCommand (`/강화`) | `booster`, `start`, `here`, `test`에 OP 검사가 없다. `/강화 booster`는 누구에게나 +5%/+10% 성공 부스터를 무한 지급한다. | 확인 |
| H4 | `collectors` 패키지 전체 | `CollectorsManager.join/leave`, `Session.startLoop/stopLoop`를 부르는 코드가 패키지 밖에 없다(명령, NPC 없음). 던전 컨텐츠가 연결되지 않아 동작하지 않는다. | 확인(grep) |
| H5 | `stock` StockManager.buyStock | 돈을 먼저 빼고 `addItem(cert)`의 반환값을 무시한다. 인벤토리가 가득하면 주권이 사라진다. 수량 상한도 없다. | 확인 |
| H6 | `region` RegionCommand, RegionSettingListener | `/구역 <ID>`가 소유자 확인 없이 설정창을 연다. 설정/삭제 클릭에도 소유자 검사가 없고, ID는 표지 위에 이름표로 공개된다. 누구나 남의 구역을 삭제/변경할 수 있다. | 확인 |
| H7 | `gacha` GachaListener.onBundleUse | 번들을 소모하지 않는다. 한 개로 무한 개봉, 손 구분 없이 처리해 클릭당 2회 굴릴 수도 있다. | 확인 |
| H8 | `gacha` GachaContentsConfig | 번들 타입(일반/고급/크로마틱)을 저장하지 않고, 불러올 때 전부 일반 풀로 들어간다. 재시작하면 고급/크로마틱 보상이 일반 번들에서 나온다. 저장은 종료 시에만 한다. | 확인 |
| H9 | `gacha` GachaCommand (`/gacha`) | 권한 검사가 없다. 누구나 모든 번들을 만들고 보상 목록을 추가/삭제한다. | 확인 |
| H10 | `shop` GoodsCommand 계열 (`/goods`) | 권한 검사가 없고 가격에 음수를 허용한다. `UserManager.withdraw`가 음수를 막지 않아, 음수 가격 상품을 만들어 사면 돈이 늘어난다. | 확인 |
| H11 | `cosmetics` CosmeticsCommand (`/cos`) | 권한 검사가 없다. 누구나 아이템을 치장으로 등록/삭제하고 공짜로 장착한다. 상점 수익 구조를 우회한다. 등록 정보는 메모리에만 있어 재시작 시 사라진다. | 확인 |
| H12 | 여러 명령 | `/ability`(초월 무기 최고 강화 상태 지급), `/head`, `/nightmare`(투명+이름 숨김)가 권한 없이 열려 있다. plugin.yml에 permission 정의가 하나도 없다. | 확인 |
| H13 | `deathbox` DeathBoxConfig | 저장하는 `contents`가 사망 당시 목록이고 꺼낸 뒤에도 갱신되지 않는다. 아이템을 꺼낸 뒤 서버를 재시작하면 상자가 원래 내용으로 복원된다(복제). | 확인 |
| H14 | `deathbox` DeathBox | 상자 인벤토리가 최대 36칸인데 사망 드롭은 최대 41칸(방어구/보조손 포함)이다. 넘친 아이템은 버려지고 12,000$는 이미 차감된다. | 확인 |
| H15 | `enchant/transcendence` FlameWeaverListener | 공격자/피해자가 뒤바뀌어 있다. +30% 피해가 무기 소유자가 "맞을 때" 적용되고 "때릴 때"는 적용되지 않는다. | 확인 |

### 2.2 중간

**데이터/경제**

| 위치 | 문제 | 상태 |
|---|---|---|
| `common/user` UserSaveManager.onJoinAsync/onQuitAsync | 서버 이동 시 이전 서버의 퇴장 처리가 새 서버가 쓴 `user:<uuid>:server`를 지우거나, 새 서버가 저장 전 Redis를 읽어 데이터가 되돌아갈 수 있다. | 확인(타이밍 의존) |
| `UserManager.withdraw` / `EconomyManager.deposit` | `withdraw`에 `amount>0` 검사가 없다. `deposit`은 유저가 로드되지 않아도 true를 반환한다. | 확인 |
| `currency` CurrencyListener | 유저가 로드되기 전에 현금 아이템을 우클릭하면 입금 없이 아이템만 사라진다. | 확인 |
| `rank` UserPlaytimeManager/RankTask | 30초/3초 주기의 동기 작업이 메인 스레드에서 Redis를 여러 번 호출한다. 유저 JSON을 읽고-수정하고-쓰는 방식이라 비동기 저장과 겹치면 값이 덮어써질 수 있다. | 확인(경합은 타이밍 의존) |
| `role/hunter`, `role/miner`, `mining` | 몬스터 처치 보상(50~30,000$)과 포트 광산(부서져도 즉시 재생성, 파괴당 약 130$ 기대값)에 제한이 없다. 몹 농장/광산으로 돈이 무한 생성된다. 헌터 치명타가 이벤트를 다시 일으켜 보상이 2번 나갈 수 있다. | 확인(기대값은 코드 수치로 계산) |
| `role/fisherman` | 물병(포션)이 쓰레기로 분류되지 않아 "보물 판매"(5,000~15,000$)로 팔 수 있다. | 확인, 아이템 드롭은 재현필요 |
| `role` BlackSmithPassiveListener | 대장간 도구 우클릭(30초 쿨다운)마다 최상위 재료 PrismFractal 5개를 공짜로 준다(테스트 코드로 보임). | 확인 |
| `stock` StockMarketTask/StockUpdater | 장이 열려 있는 동안 매 틱 시세를 갱신한다(하루 약 600회). 변동률 평균이 -20%라 하한가에 붙는다. | 확인, 의도 여부 확인 필요 |
| `blueprint` | 도면 아이템은 건축 시작 시 소모되지만 진행은 정상 종료 때만 저장된다. 크래시 시 도면과 비용이 날아간다. | 확인 |

**기능 결함**

| 위치 | 문제 | 상태 |
|---|---|---|
| `common` RuleManager.addAll | 단어를 추가하고 정규식을 다시 만들지 않는다. 재시작 후 filter_words.yml의 단어가 적용되지 않는다. | 확인 |
| `common/inquiry` | INSERT 실패 시 id -1을 반환하고 쿨다운을 시작한다(실패한 문의가 전부 같은 키). | 확인 |
| `common/rank` SqlRankRepository | Connection을 닫지 않는다(시작 시 4개 누수). 실패하면 null을 반환해 랭킹판이 말없이 비어 있다. | 확인 |
| `paper/framework` | 의존성 조립 실패가 치명 오류가 아니다. 일부 빈 없이 반쯤 초기화된 채 계속 실행된다. | 확인 |
| `role/shop` RoleShopManager | 내부 상점 객체를 `plugin` 대입 이전에 만든다. 널 plugin으로 시작 시 NPE가 날 수 있다. | 재현필요 |
| `enchant` | 속성 수정자를 설정하며 기본 공격속도 등을 잃을 가능성이 있다. 부스터 제작 레시피(9 다이아)가 바닐라 다이아 블록과 겹칠 수 있다. | 재현필요 |
| `collectors` | 세션 종료(`stopLoop`) 때 `sessionMap`을 비우지 않는다. 연결하면 "이미 참가 중"이 남는다. 퇴장 중 인벤토리 복구는 HuskSync와 경합할 수 있다. | 확인/경합은 재현필요 |
| `stock/container` | 개인 주식 보관소가 어디에서도 열리지 않는다(죽은 코드). | 확인 |
| `region` | 구역이 청크 키만으로 저장되어 다른 월드의 같은 좌표 청크와 충돌한다. 보호 범위가 이동/상호작용뿐이다(블록 파괴/폭발/피스톤 등 없음, 멤버 목록 없음). 5초 주기 복구 작업이 구역 청크를 강제 로드한다. | 확인 |
| `blueprint` | `/bp create`가 이름을 인자 4번째부터 읽고(3개만 요구) 공백으로, 나머지 명령은 `_`로 합쳐 이름이 어긋난다. 미리보기는 블록마다 엔티티를 만든다(수천 개, 재시작 후 잔존). | 확인 |
| `airdrop` | `isSupplyChest(Block)`이 항상 false여서 빈 보급상자가 지워지지 않는다. | 확인 |
| `chat` | Redis의 서버 키가 없으면 채팅만 쳐도 추방한다. 위 서버 이동 경합과 겹치면 정상 유저도 추방될 수 있다. | 확인 |
| `huntingZone` | 구역/활성화 상태가 저장되지 않고, 몬스터가 있는 구역은 ICE_STAGE 하나뿐이다. 스폰 높이에 +1이 없다. | 확인(스폰 높이는 재현필요) |
| `ghost` | `DeGhostMode`가 `remove` 대신 `add`를 호출한다. 해제 후 다시 투명이 안 되고 두 번째 토글에서 방어구가 비워진다. | 확인 |
| `temperature` | 구역의 `regenerate`, `show-particle` 옵션을 무시한다. 구역이 청크 키만으로 저장된다. | 확인 |
| `player` UserTagManager | 모든 접속자 머리 위 태그를 2틱마다 다시 만든다. 장착 칭호가 첫 갱신 뒤 티어 칭호로 바뀐다. | 확인 |

**인프라**

| 위치 | 문제 | 상태 |
|---|---|---|
| `velocity` DonationCommand | 후원 지급에 거래 ID가 없어 웹상점 재시도나 이중 실행이 보상을 두 번 준다(명령 자체는 콘솔 전용으로 안전). | 확인 |
| `velocity`/`paper` Redis 채널 | TPA/후원 신호가 서명/인증 없이 Redis pub/sub로 오간다. Redis에 비밀번호와 방화벽이 필수다. | 확인 |

### 2.3 경미한 항목 (참고)

- `PartyManager.handOverLeadership`이 저장하지 않는다. `/파티 참가`는 초대 없이 누구나 가입한다. `PartyCommandHelp`가 등록되지 않았다.
- `RoleCommand`에 인자가 없으면 예외가 난다. `RoleConfig` 기본값과 `RoleManager` 기본값이 다르다(광부 탐색 반경 등).
- `ConfigInjection`이 따옴표 없는 숫자/불린 값을 String 파라미터에 그대로 넣는다. `ClassInitiator`의 종료 우선순위 정렬이 문서와 반대다.
- 신규 유저 지급금이 두 번 들어간다(생성 시 2,500/5,000 + 첫 접속 1,000/5,000).
- `EconomyManager.transferMoney`는 약 2,210만 이상에서 int 오버플로가 난다.
- `Field.spawnDropContainers`는 불변 리스트를 섞어서 호출하면 예외가 난다(현재 미사용).
- `PortVillageManager.move`는 텔레포트하지 않는다. `MainSpawnManager`의 타이틀은 만들기만 하고 보여주지 않는다.
- 플레이타임이 틱 단위(`PLAY_ONE_MINUTE`)로 저장된다.
- 서버 시작 시 월드가 아직 없으면 스폰이 null로 남는다(`/destination reload`로 복구).
- NPC 신규 보상은 플래그를 먼저 켜고 `addItem` 결과를 무시해 가방이 가득하면 영구 손실이다.
- 모든 설정/상태 파일(goods, showcase, gacha, 차단 단어 등)이 종료 시에만 저장되는 곳이 많다. 크래시에 취약하다.

## 3. 패키지별 기능과 컨텐츠

### common (paper와 velocity가 공유)

| 패키지 | 내용 |
|---|---|
| `user` | 유저 모델(돈, 경험치, 직업/직업 경험치, 레벨/티어/숙련도, 모바일 여부, 칭호 목록/장착, 한기 수치/면역, 접속 시간). MySQL `users` 테이블(누락 컬럼 자동 추가), Redis 캐시, 로드 순서: 비상 덤프 → Redis → DB → 신규. 신규 시작금 2,500(Java)/5,000(Bedrock). |
| `EconomyManager` | 입금/출금/송금(세금 3%), 시장 수요 기반 가격 조정 맵. |
| `RuleManager` | 욕설 필터(단어 목록, 정규식, 난독화 치환). |
| `inquiry` | 인게임 문의: MySQL + Redis, 30초 쿨다운, 서버 간 알림, 읽지 않은 답변. |
| `api` | 웹 후원 상품(시즌 패스, 스타터 팩, 서포터): MySQL 구매 기록, Redis 미지급 목록, 원자적 지급 처리. |
| `rank` | 자산/플레이타임/경험치/직업경험치 리더보드(Redis 정렬 집합). |
| `party` | Redis 기반 파티(최대 4명, 생성/참가/탈퇴/리더). |
| `roles`, `settings`, `stock` | 직업(농부/광부/어부/사냥꾼/대장장이), 티어(VISITOR~ETERNAL), 숙련도(BEGINNER~INFINITE), 칭호, 주식 모델. |
| `framework` | LRF(자체 DI): `@Components/@Inject/@ConfigValue/@Listeners/@Commands/@Task`, 초기화 우선순위, 서버 타입(GLOBAL/LOBBY/MAIN/HUNTING/WILD), 성능 모니터링. |
| `JedisService`, `utils` | Redis 헬퍼와 pub/sub, 문자열/색/한글 타자기/파티션 유틸. |

### velocity

프록시 플러그인. LRF 복사본으로 부팅한다. 콘솔 전용 `donation action:GRANT target:<uuid> product:<key>`로 웹 후원을 기록/통지한다. 서버 간 TPA 신호를 받아 대상 서버로 연결한다.

### paper

| 패키지 | 기능과 컨텐츠 |
|---|---|
| (루트), `plugin`, `LazberryRegistryFramework` | 부팅, 서버 타입별 초기화, `/links`(서버 공식 링크), 테스트용 `/0947345`, 스킬 이펙트 리스너. |
| `utils` | 서버 이동(BungeeCord Connect, 파티 동행, Bedrock 폼, 연출 텔레포트), 인벤토리 지급/보관/드롭, 스턴, 아이템 빌더, PDC 키, 직렬화, Floodgate, 발광/보스바/타이틀/폭죽 유틸. |
| `user` | 주기 저장(5분), 접속/퇴장 저장, 재로드 시 온라인 유저 복구. |
| `joinListeners` | 로비 환영/이동, 메인 서버 접속 연출, HuskSync 동기화 완료 이벤트(`PlayerDataReadyEvent`). |
| `exp` | 레벨 곡선(100+50×레벨), 티어 9단계 자동 칭호, 직업 경험치 → 숙련도 9단계. |
| `role` | 직업 5종과 패시브(농부: 추가 수확/성장/밟기 방지, 광부: 광물 탐색/발광, 어부: 구이/쓰레기 환전/보물 판매, 사냥꾼: 30% 치명타/처치 보상, 대장장이: 강화 보너스), `/shop`, 상점(작물/물고기/광물/전리품), 개인 구역 상인 소환(RegionShop), 직업 선택 GUI. |
| `enchant` | 강화창(폰트 글리프 GUI), 레벨별 성공/실패/파괴 확률(×1.10~×3.50), 부스터, 재료 7종(토템/보호서/PrismFractal 등), 재료 상점/제작, 이펙트, 초월 무기 3종(SonicHunter/FlameWeaver/AeroMancer). |
| `collectors` | 수집가 던전 미니게임(평화/흥분/공포 3난이도): 입장권, 항아리 부수기, 가중치 전리품 13종, 무게 슬로우, 인벤토리 백업/복구, 난이도별 추격자(벌레/위상 위더스켈레톤/워든). 현재 미연결. |
| `stock` | 주식 시장: `/주식` 구매/판매/목록/정보/상점, 주권 아이템, 수수료 3%, 개장 시간대, GUI 상점, 시세 TextDisplay 전광판. |
| `region` | 청크 단위 구역: 티켓 구매, 회전 비콘 표시, 소유자 설정창(입장/상호작용/삭제), `/구역`, 영속 저장. |
| `blueprint` | 건축 도면: OP가 영역을 도면으로 저장, 등급(하/중/상)/가격 자동 산정, 도면 상점, 구역 안에서 블록 단위 자동 건축(회전 지원, 재시작 재개), 미리보기. |
| `gacha` | 치장 번들 4종, 5등급 보상, 재고 GUI, `/gacha`. |
| `shop` | 쇼케이스 상점: 유리 진열대 + 떠 있는 아이템 + 구매 버튼, 크리스마스 히든 상점 안내. |
| `cosmetics` | 등/머리 ItemDisplay 치장, `/cos`. |
| `airdrop` | 30분 주기 보급: 낙하 연출, 잠금 상자, 무작위 보상(강화 재료 포함). |
| `deathbox` | 사망 시 12,000$로 시체상자 생성(10분 보관, 재시작 유지). |
| `mining` | 항구 광산: 내부 영역에서 광물 재생성, 외부 보호. |
| `teleporter` | 걸어 들어가는 포탈(쿨다운, 카지노 티켓 검사), OP 도구로 설정. |
| `location` | 로비/항구/메인 스폰 저장, `/destination`, `/lobby`. |
| `chat` | 서버 간 전체 채팅(칭호 표시, 욕설 처리), 서버 간 귓속말, 칭호 GUI. |
| `party` | `/파티` 6개 명령, Bedrock 폼/클릭 수락, 서버 간 `/tpa`. |
| `rank` | 랭킹 캐시/보드/`/rank`, 플레이타임 집계. |
| `inquiry` | 문의 접수/답변/조회, 접속 시 답변 알림. |
| `currency` | 현금 아이템(우클릭 입금), OP 경제 명령. |
| `icing`, `temperature` | 한기 서바이벌 루프: 3초마다 수치 감소, 0이면 동상 피해, 태양초 빵/온기 구역으로 회복. |
| `knockout` | 파티원 기절/소생(5회 터치). |
| `huntingZone` | 사냥터 구역 프레임워크와 ICE_STAGE 몬스터 3종(등급 프레임워크는 준비만 됨). |
| `infoNpcs`, `magic`, `advancement` | 안내 NPC 대사와 신규 보상, 직업 선택 책/항구 이동, 커스텀 도전과제. |
| `food`, `player`, `filter`, `scoreboard`, `ghost`, `wild`, `sin` | 태양초 빵/무료 음식, 머리 위 태그, 욕설 설정 명령, 사이드바, OP 투명, WILD 포탈 모듈, 투명화 테스트. |
| `bags`, `casino`, `api/donate` | 개인 가방(우편함 포함), 카지노(티켓/상점), 후원 상품 지급(스타터 팩 등). 이전 작업에서 검토했다. |

## 4. 추천 컨텐츠

1. **수집가 던전 마무리**: 코드는 거의 완성돼 있다. 문지기 NPC와 입장/퇴장/세션 시작·종료를 연결하고 전리품 환전만 붙이면 가장 큰 신규 컨텐츠가 된다.
2. **돈 싱크 컨텐츠**: 광산/사냥 보상 상한을 두고, 구역 확장/건축 보험/강화 수수료 같은 소비처를 늘려 인플레이션을 막는다.
3. **한기 연계 이벤트**: 눈보라 시간대, 개인 구역에 온기 구역(난로) 설치, 도면으로 난로 건축을 연결하면 한기 루프와 구역/도면이 한 흐름으로 묶인다.
4. **직업 의뢰 게시판**: 주간 납품/처치 의뢰로 직업 숙련도를 올리고 사냥터 보스(MYTHIC/BOSS 프레임워크 활용)와 연결한다.
5. **구역 멤버/공동 구역**: 파티원을 신뢰 멤버로 등록하는 기능과 블록 보호를 추가하면 구역이 실제 땅 보호 시스템이 된다.

## 5. 한계

- 코드를 실행하지 않았다. **재현필요** 항목은 게임에서 확인해야 한다.
- 훑어본 패키지는 위 표의 "훑어봄"과 "이전 작업에서 검토"이며, 그 안의 오류는 빠졌을 수 있다.
- 서버 설정(Redis 인증, DB 권한, Oraxen/HuskSync 설정)은 코드로 알 수 없어 다루지 않았다.
