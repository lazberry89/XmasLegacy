# TODO

- [2026-11-29] TODO `catalog/` — 도감 완성 아이템 보상 미정. `CatalogProgress.rewardGranted`는 Redis/DB에 저장까지 되지만 지급 코드가 없어 항상 false. 보상을 줄 때 `CatalogManager.grantAdvancements`의 완성 지점에서 처리.
- [2026-11-29] TODO `catalog/` — 샘플은 DB(`catalog_sample`)에 있음. 운영 전 `/도감 샘플 <종류>`로 채워야 도감이 열림. 농사 도감 순서는 `design/catalog/catalog_farm.py`의 `SLOTS`와 맞춰야 실루엣과 일치(0~21 작물/금별, 23~25 특수, 22·26 빈칸). 유저가 등록을 시작한 뒤에는 칸 순서를 바꾸면 안 됨.
- [2026-11-29] TODO 서버 설정 — wild의 UltimateAdvancementAPI가 SQLite(main은 MySQL `xmas_legacy`). 같은 MySQL로 맞춰야 도전과제가 서버 간 공유됨.
- [2026-11-29] TODO 전체 — 코드 점검에서 결정이 필요한 항목은 `audit-2026-11-29.md`의 "남겨둔 것". 주가 하락 식(`StockUpdater`)이 가장 급함.
