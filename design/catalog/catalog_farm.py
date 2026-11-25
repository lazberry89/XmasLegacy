"""FARM 도감 배경: 27칸 상자 창의 위쪽(도감 칸 + 아래 인벤토리로 넘어가는 연결부, 176x84)을 덮는 Oraxen 글리프.
각 슬롯에 등록할 아이템의 실루엣을 그려 두고, 등록하면 플러그인이 실제 아이템을 그 칸에 올려 실루엣을 가린다.
좌표는 바닐라 3줄 상자와 같다:
  도감 칸       테두리 x=7+18c, y=17+18r (r=0..2)
  "인벤토리" 글자 x=8, y=74~80
  플레이어 칸   테두리 y=84부터 -> 여기부터는 덮지 않고 바닐라 창이 그대로 보인다
y=71~83 은 밀짚색 판 -> 나무 선반 -> 바닐라 회색으로 단계적으로 바뀌는 연결부.
마지막 열(x=175)은 투명: 아래 깔린 바닐라 창의 검은 테두리가 그대로 보인다.
items/ 의 png는 서버 plugins/Oraxen/pack/textures/item/customcrops/crop/*, sunflower_bread 에서 복사한 원본."""
from pathlib import Path
from PIL import Image

HERE = Path(__file__).parent
OUT = HERE.parent.parent / "XmasResourcePack/oraxen/pack/textures/xmaslegacy/catalog_farm.png"
W, H = 176, 84
VANILLA_H = 168  # 3줄 상자 창 전체 높이 (미리보기용)

CROPS = ["cabbage", "chinese_cabbage", "corn", "pineapple", "eggplant", "hop",
         "garlic", "tomato", "pepper", "grape", "pitaya"]
# 슬롯 번호 -> (아이템 id, 칸 종류). 플러그인 CatalogType.FARM 목록과 순서를 맞출 것.
SLOTS = {i: (c, "normal") for i, c in enumerate(CROPS)}                     # 0~10  일반 작물
SLOTS |= {11 + i: (c + "_golden_star", "gold") for i, c in enumerate(CROPS)}  # 11~21 골드스타
SLOTS |= {23: ("golden_tomato", "special"), 24: ("redpacket", "special"),
          25: ("sunflower_bread", "special")}                                 # 23~25 특수

C = {
    "black": (0, 0, 0), "white": (255, 255, 255), "shadow": (85, 85, 85), "gray": (198, 198, 198),
    "bg": (226, 208, 164), "bg_dot": (214, 194, 148),
    "plank": (150, 104, 60), "plank_hi": (178, 128, 78), "plank_lo": (112, 74, 40),
    "grass": (92, 160, 70), "grass_hi": (134, 196, 92), "grass_lo": (54, 110, 46),
}
SLOT = {  # (테두리 어두운쪽, 테두리 밝은쪽, 칸 안쪽, 실루엣)
    "normal":  ((110, 84, 48), (250, 240, 214), (196, 172, 124), (124, 98, 62)),
    "gold":    ((150, 106, 20), (255, 240, 170), (226, 190, 96), (160, 112, 26)),
    "special": ((70, 112, 52), (226, 246, 200), (160, 196, 124), (82, 122, 60)),
}

img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
px = img.load()
def put(x, y, c):
    if 0 <= x < W - 1 and 0 <= y < H: px[x, y] = (*c, 255)
def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))

# 테두리: 강화창 첫 프레임에서 그대로 가져옴 (바닐라와 같은 테두리라 아래 바닐라 창과 이어짐)
forge = Image.open(OUT.parent / "enchant_forge.png").convert("RGBA").crop((0, 0, W, 71))  # 첫 프레임만
img.paste(forge, (0, 0))
for y in range(71, H):  # 프레임 높이(71) 아래는 마지막 줄 테두리를 그대로 늘림
    img.paste(forge.crop((0, 70, W, 71)), (0, y))
px = img.load()

# 안쪽 바탕: y=3~70 밀짚색, 71~83 연결부
for y in range(3, H):
    for x in range(3, 173):
        put(x, y, C["bg"])
        if (x * 7 + y * 13) % 23 == 0: put(x, y, C["bg_dot"])

def plank(y0, grass_from):
    """y0~y0+2 판자, y0-1 잔디. 잔디 싹은 grass_from 오른쪽에만(제목 글자를 피함)."""
    for x in range(3, 173):
        put(x, y0, C["plank_hi"]); put(x, y0 + 1, C["plank"]); put(x, y0 + 2, C["plank_lo"])
        if x % 29 == 0: put(x, y0 + 1, C["plank_lo"])
        put(x, y0 - 1, C["grass"] if x % 3 else C["grass_lo"])
        if x >= grass_from and x % 5 == 1: put(x, y0 - 2, C["grass_hi"])

def sprout(x0, y0):
    for dy in range(4): put(x0, y0 - dy, C["grass_lo"])
    put(x0 - 1, y0 - 3, C["grass"]); put(x0 - 2, y0 - 4, C["grass_hi"]); put(x0 - 1, y0 - 4, C["grass_hi"])
    put(x0 + 1, y0 - 2, C["grass"]); put(x0 + 2, y0 - 3, C["grass_hi"]); put(x0 + 1, y0 - 3, C["grass_hi"])

plank(14, 120)                       # 제목 글자(y=6~12) 아래, 도감 칸(y=17) 바로 위
for sx in (150, 158, 166): sprout(sx, 12)

# 연결부: 도감 칸 아래 나무 선반(y=71~72) + 밀짚색이 회색으로 4단계로 바뀜(y=73~83)
STEPS = [(73, 0.25), (76, 0.5), (79, 0.75), (82, 1.0)]  # (시작 y, 회색 비율)
for y in range(73, H):
    t = max(s for y0, s in STEPS if y >= y0)
    for x in range(3, 173):
        put(x, y, mix(C["bg"], C["gray"], t))
for x in range(3, 173):
    put(x, 71, C["plank_hi"]); put(x, 72, C["plank_lo"])
    if x % 29 == 0: put(x, 71, C["plank"])
# 선반 아래로 늘어진 풀/뿌리. "인벤토리" 글자 자리(x<64)는 피함
for x in range(64, 172):
    n = (x * 37) % 11
    if n < 3:
        for dy in range(n + 1): put(x, 73 + dy, C["grass"] if dy < n else C["grass_lo"])

def slot(i, kind, item):
    x0, y0 = 7 + 18 * (i % 9), 17 + 18 * (i // 9)
    dark, light, fill, sil = SLOT[kind]
    for y in range(y0, y0 + 18):
        for x in range(x0, x0 + 18):
            put(x, y, fill)
    for k in range(17):
        put(x0 + k, y0, dark); put(x0, y0 + k, dark)
        put(x0 + 1 + k, y0 + 17, light); put(x0 + 17, y0 + 1 + k, light)
    tex = Image.open(HERE / "items" / f"{item}.png").convert("RGBA")
    for ty in range(16):
        for tx in range(16):
            if tex.getpixel((tx, ty))[3] > 0:
                put(x0 + 1 + tx, y0 + 1 + ty, sil)

for i, (item, kind) in SLOTS.items():   # 빈 번호(22, 26)는 칸을 그리지 않음
    slot(i, kind, item)

img.save(OUT)
print("saved", OUT)

# 미리보기: 바닐라 3줄 상자 창을 흉내 내 그리고 그 위에 글리프를 얹음. 진한 선은 제목/"인벤토리" 글자 자리.
if __name__ == "__main__":
    prev = Image.new("RGBA", (W, VANILLA_H), (0, 0, 0, 0))
    vp = prev.load()
    for y in range(VANILLA_H):
        for x in range(W):
            vp[x, y] = (*C["gray"], 255)
    for y in range(VANILLA_H):
        vp[0, y] = vp[175, y] = (0, 0, 0, 255); vp[1, y] = vp[2, y] = (255, 255, 255, 255)
        vp[173, y] = vp[174, y] = (*C["shadow"], 255)
    for x in range(W):
        vp[x, VANILLA_H - 1] = (0, 0, 0, 255); vp[x, VANILLA_H - 2] = vp[x, VANILLA_H - 3] = (*C["shadow"], 255)
    def vslot(x0, y0):
        for y in range(y0, y0 + 18):
            for x in range(x0, x0 + 18):
                vp[x, y] = (139, 139, 139, 255)
        for k in range(17):
            vp[x0 + k, y0] = vp[x0, y0 + k] = (55, 55, 55, 255)
            vp[x0 + 1 + k, y0 + 17] = vp[x0 + 17, y0 + 1 + k] = (255, 255, 255, 255)
    for r in range(3):
        for c in range(9): vslot(7 + 18 * c, 84 + 18 * r)
    for c in range(9): vslot(7 + 18 * c, 142)
    prev.paste(img, (0, 0), img)
    for x in range(8, 48): vp[x, 9] = vp[x, 77] = (64, 64, 64, 255)
    prev.resize((W * 4, VANILLA_H * 4), Image.NEAREST).save(HERE / "catalog_farm_preview.png")
