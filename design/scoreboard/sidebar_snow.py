"""Sidebar title = snow sheet draped over the title row (no text), holly + bell on top, lumps hanging below.
Title row = 9 rows (baseline B-8..B). Glyph row 13 = top of title row. ascent 21.
Drawn width = TW + 4 so it covers the 2px padding on both sides of the sidebar box when TW is the widest line."""
import json, math, sys
from pathlib import Path
from PIL import Image, ImageDraw

TW = 100                      # advance the title takes (sidebar content width when it's the widest)
W = TW + 4
TOP = 13                      # glyph row of the title row's top edge
HANG = 9                      # max rows hanging below the title row (stays inside the blank first line)
H = TOP + 9 + HANG
ASCENT = TOP + 8
img = Image.new("RGBA", (W, H), (0, 0, 0, 0)); px = img.load()
def put(x, y, c):
    if 0 <= x < W and 0 <= y < H: px[x, y] = (*c, 255)
P = {"W": (255, 255, 255), "s": (226, 242, 252), "S": (184, 216, 240), "o": (128, 168, 206),
     "g": (40, 120, 56), "G": (86, 178, 92), "d": (22, 74, 38), "r": (210, 34, 46), "R": (255, 136, 136),
     "k": (128, 14, 28), "y": (255, 214, 92), "Y": (255, 246, 200), "b": (190, 120, 30), "c": (196, 236, 255)}

# bottom edge traced from the sketch: (x on box 320..792, y with box 255..312)
sk = [(320,256),(345,300),(375,338),(400,340),(430,318),(445,310),(470,310),(500,340),(530,372),(560,345),
      (600,305),(640,288),(660,300),(675,318),(690,305),(700,302),(715,315),(740,342),(760,330),(780,295),(792,256)]
def depth(x):                 # rows below the title-row top where the snow ends, at glyph column x
    sx = 320 + x / (W - 1) * 472
    for (x0, y0), (x1, y1) in zip(sk, sk[1:]):
        if x0 <= sx <= x1:
            y = y0 + (y1 - y0) * (sx - x0) / (x1 - x0); break
    rows = (y - 255) / 57 * 9
    return max(1, min(9 + HANG, round(rows * (18 / 18.6))))
d = [depth(x) for x in range(W)]
d = [round((d[max(0, x-1)] + 2 * d[x] + d[min(W-1, x+1)]) / 4) for x in range(W)]   # soften steps

for x in range(W):
    bottom = TOP + d[x] - 1
    for y in range(TOP, bottom + 1): put(x, y, P["W"])
    put(x, bottom, P["o"])                                   # outline
    if bottom - 1 >= TOP: put(x, bottom - 1, P["S"])         # shading towards the edge
    if bottom - 2 >= TOP: put(x, bottom - 2, P["s"])
    if bottom - 3 > TOP and x % 11 in (3, 4): put(x, bottom - 3, P["s"])
# a little snow piled on the top edge: small bumps (rows 11..12)
for x in range(W):
    b = 0.9 + 0.9 * math.sin(x * 0.42) + 0.6 * math.sin(x * 0.17 + 1.3)
    for k in range(max(0, min(2, round(b)))): put(x, TOP - 1 - k, P["W"])
for x in range(W):                                           # sparkle on the top
    if x % 13 == 6:
        for y in range(TOP - 2, TOP + 1):
            if px[x, y][3]: put(x, y, P["c"]); break

# holly + bell (approved part), resting on the top bumps
art = [
"..............c.......................c.................",
".............ccc.....................ccc................",
"..............c.......................c.................",
"..........................y.............................",
".....c...................yYy................c...........",
"....................d....yyy.....d......................",
"..................dGGd..yyyyy..dGGd.....................",
".................dgGGgd.bybyb.dgGGgd....................",
"................dggGGgggdRr.rRdggGGggd..................",
"...............dggg..ggrRRrrRRrggg..gggd................",
"................d......krrkkrrk......d..................",
]
ox = W // 2 - art[3].index("y")
for y, row in enumerate(art):
    for x, ch in enumerate(row):
        if ch != ".": put(x + ox, y, P[ch])
img.save(sys.argv[1])

# ---------- preview on a mock sidebar (vanilla layout: box = content width + 2px padding each side) ----------
mc = Path("mcfont/assets/minecraft")
prov = [p for p in json.load(open(mc / "font/include/default.json", encoding="utf-8"))["providers"] if p["file"].endswith("ascii.png")][0]
atlas = Image.open(mc / "textures/font/ascii.png").convert("RGBA")
cw = atlas.width // 16; chh = atlas.height // len(prov["chars"]); cell = {}
for r, row in enumerate(prov["chars"]):
    for c, ch in enumerate(row):
        g = atlas.crop((c*cw, r*chh, (c+1)*cw, (r+1)*chh)); xs = [x for x in range(cw) for y in range(chh) if g.getpixel((x, y))[3]]
        cell[ch] = (g, (max(xs)+1) if xs else 0)
def tw(s): return sum(4 if ch == " " else cell[ch][1] + 1 for ch in s)
def text(can, x, top, s, col):
    for ch in s:
        if ch == " ": x += 4; continue
        g, w = cell[ch]; t = Image.new("RGBA", g.size, (*col, 255)); t.putalpha(g.getchannel("A"))
        can.alpha_composite(t, (x, top)); x += w + 1
def sidebar(lines):
    k = max([TW] + [tw(l) for l in lines])
    can = Image.new("RGBA", (k + 40, 9 * (len(lines) + 1) + 50), (70, 90, 70, 255))
    l = 20; k1 = 40 + 9                              # k1 = first line's top (vanilla naming)
    dr = ImageDraw.Draw(can)
    dr.rectangle([l - 2, k1 - 10, l + k + 1, k1 - 2], fill=(0, 0, 0, 102))
    dr.rectangle([l - 2, k1 - 1, l + k + 1, k1 - 1 + 9 * len(lines)], fill=(0, 0, 0, 77))
    B = k1 - 2
    tx = l + k // 2 - TW // 2                         # title start (vanilla centring)
    can.alpha_composite(img, (tx - 2, B - ASCENT))      # <shift:-2> before the glyph
    for i, s in enumerate(lines): text(can, l, k1 + 9 * i, s, (255, 255, 255))
    return can
a = sidebar(["", "Money 0 $", "", "Exp 170 (5)", "", "Job Blacksmith", "", "Mastery 3487 Rxp"])
b = sidebar(["", "Money 12345678901234 $", "", "Exp 9999 (42)"])
prev = Image.new("RGBA", (a.width + b.width + 10, max(a.height, b.height)), (40, 40, 40, 255))
prev.alpha_composite(a, (0, 0)); prev.alpha_composite(b, (a.width + 10, 0))
prev.resize((prev.width * 4, prev.height * 4), Image.NEAREST).save(sys.argv[2]); print("ok", img.size)
