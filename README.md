❄️ Project XmasLegacy ❄️

Developer: Lazberry89

TITLE 大白夜, and Last Flame
DESCRIPTION :
고대 과학자들에 의해 파괴된 세계를 배경으로 한 멀티플레이어 서바이벌 RPG 서버입니다.
플레이어는 폐허가 된 세계에서 생존하며, 다른 생존자들과 협력하고, 다양한 직업(Job/Role)을 통해 역할을 수행하며 새로운 문명을 재건하는 여정을 경험합니다.
장르: Survival RPG + Multi-Server Network

플랫폼: Minecraft Paper 1.21.1 (BungeeCord / Velocity Proxy 연동)
Main Features

User Database System
UserManager, SqlUserRepository, User, UserRepository
UUID 기반 영속성 사용자 데이터 관리
비동기 로딩 (CompletableFuture + whenComplete)
Lombok-style getter/setter, equals/hashCode 오버라이딩

Role / Job System
Role (Interface), Roles, SecondaryRole, ThirdRole, HiddenRole
다층 역할 시스템 (1차 / 2차 / 3차 / 히든직업)
RoleMastery, Tier (VISITOR ~ 상위 티어) 연동

Rule System
RuleManager, RuleCommandManager
서버 규칙 관리 및 명령어 처리

Prefix (칭호) System
PrefixManager, PrefixCommandManager, ChatPrefixListener, PrefixInterface, UserTagManager, PrefixMission
동적 칭호 획득·장착 시스템
채팅 및 Hover Tag (Kyori Adventure Component) 연동

Region System
RegionIndicator, ServerTransfer (static utility)
멀티 서버 간 이동 (BungeeCord plugin message)
파티(Party) 연동 이동 (파티장 따라가기 / 파티원 강제 탈퇴)
Floodgate (모바일 유저) 지원 및 hide 옵션 (목적지 숨김)

Party System
PartyManager, Party, PartyCommand 등
Additional Systems
Economy (dollars) & Inquire 시스템
ServerTransfer 유틸 (Early Return, Stream API, Kyori Component UX)
ColorUtils, Constants, IDGenerator 등 공통 유틸


Technical Highlights

멀티 모듈 구조: common / paper / velocity
비동기 처리와 안전한 UX (ClickCallback, Hover/Click Event)
Early Return + Stream API 적극 활용
Floodgate 지원으로 PC + 모바일 유저 모두 대응
Kyori Adventure Component를 활용한 현대적인 채팅/메시지 UI