# TODO

- [2026-11-26] TODO `catalog/` — 저장소(`SqlCatalogRepository.load/register`)와 `CatalogProgress`만 있고 연결이 없음. `CatalogManager`가 비어 있어 접속 시 load→캐시, 칸 등록 시 `progress.register`가 true일 때만 `repository.register`, 퇴장 시 캐시 제거, GUI(`catalog/gui` 비어 있음) 연결이 필요.
- [2026-11-26] TODO `catalog/CatalogType` — 도감 종류별 칸→아이템 목록이 코드에 없음. 농사 도감 순서는 `design/catalog/catalog_farm.py`의 `SLOTS`(0~21 작물/금별, 23~25 특수, 22·26 빈칸)와 맞춰야 함. 도감은 최대 27칸.
