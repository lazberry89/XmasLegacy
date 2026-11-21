"""Animated sidebar title: snow sheet over the title row + holly/bell on top (static) and snowflakes falling
(one row per frame, seamless over 16 frames). Oraxen animated glyph rules:
- frame width W = 105; content uses cols 0..103 and col 104 stays empty in every frame, so each frame's
  advance (last opaque col + 1 + 1px) is exactly 105 = Oraxen's reset (-height*W/H).
- col 103 must be opaque in every frame (the snow sheet covers it), otherwise the advance shrinks.
Rows vs title baseline B: glyph row TOP = top of title row (B-8); ascent TOP+8; logo above the ornament."""
import json, math, random, sys
from pathlib import Path
from PIL import Image, ImageDraw

TW = 100                        # title advance after shifts = sidebar content width when it is the widest line
W, CW = TW + 5, TW + 4          # frame width, content width (covers the box's 2px padding on both sides)
LOGO_H = 14                     # logo rows on top (2 rows of jump room + snow cap + outlined bold text)
JUMP = 2
ART_DY = LOGO_H - 3              # holly/bell art rows 3..10 drawn right under the logo
TOP, HANG = 13 + ART_DY, 9
FALL = 32                      # flakes fall through rows TOP+2 .. TOP+33, 2 rows per frame -> seamless over 16 frames
H = TOP + 2 + FALL + 1
ASCENT = TOP + 8
FRAMES = 16
P = {"W": (255, 255, 255), "s": (226, 242, 252), "S": (184, 216, 240), "o": (128, 168, 206),
     "g": (40, 120, 56), "G": (86, 178, 92), "d": (22, 74, 38), "r": (210, 34, 46), "R": (255, 136, 136),
     "k": (128, 14, 28), "y": (255, 214, 92), "Y": (255, 246, 200), "b": (190, 120, 30), "c": (196, 236, 255),
     "f": (232, 246, 255), "F": (170, 214, 242)}

sk = [(320,256),(345,300),(375,338),(400,340),(430,318),(445,310),(470,310),(500,340),(530,372),(560,345),
      (600,305),(640,288),(660,300),(675,318),(690,305),(700,302),(715,315),(740,342),(760,330),(780,295),(792,256)]
def depth(x):
    sx = 320 + x / (CW - 1) * 472
    for (x0, y0), (x1, y1) in zip(sk, sk[1:]):
        if x0 <= sx <= x1: y = y0 + (y1 - y0) * (sx - x0) / (x1 - x0); break
    return max(1, min(9 + HANG, round((y - 255) / 57 * 9 * (18 / 18.6))))
d0 = [depth(x) for x in range(CW)]
dep = [round((d0[max(0, x-1)] + 2 * d0[x] + d0[min(CW-1, x+1)]) / 4) for x in range(CW)]
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
rng = random.Random(7)
low = [(rng.randrange(2, CW - 2), rng.randrange(16), rng.random() * 6.28, rng.choice("WWf")) for _ in range(34)]

def render(fr):
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0)); px = img.load()
    def put(x, y, c):
        if 0 <= x < CW and 0 <= y < H: px[x, y] = (*c, 255)
    # flakes first: the snow sheet and ornament are drawn over them
    for fx, ph, sw, col in low:                       # come out from under the sheet and fall down past the lines
        y = TOP + 2 + 2 * ((ph + fr) % 16)
        c = P[col] if y < TOP + 2 + FALL * 0.6 else P["F"]    # dimmer in the lower part, as if fading out
        put(fx + round(math.sin(sw + fr * 0.39)), y, c)
    for x in range(CW):                               # snow sheet over the title row
        bottom = TOP + dep[x] - 1
        for y in range(TOP, bottom + 1): put(x, y, P["W"])
        put(x, bottom, P["o"])
        if bottom - 1 >= TOP: put(x, bottom - 1, P["S"])
        if bottom - 2 >= TOP: put(x, bottom - 2, P["s"])
        if bottom - 3 > TOP and x % 11 in (3, 4): put(x, bottom - 3, P["s"])
    for x in range(CW):                               # a little snow on the top edge
        b = 0.9 + 0.9 * math.sin(x * 0.42) + 0.6 * math.sin(x * 0.17 + 1.3)
        for k in range(max(0, min(2, round(b)))): put(x, TOP - 1 - k, P["W"])
    for x in range(CW):
        if x % 13 == 6:
            for y in range(TOP - 2, TOP + 1):
                if px[x, y][3]: put(x, y, P["c"]); break
    ox = CW // 2 - art[3].index("y")
    for y, row in enumerate(art):
        if y < 3: continue                            # sparkle rows dropped: the logo sits there now
        for x, ch in enumerate(row):
            if ch != ".": put(x + ox, y + ART_DY, P[ch])
    img.alpha_composite(LOGOS[fr], ((CW - LOGOS[fr].width) // 2, 0))
    return img

def lifts(fr, n=10):
    """frames 0..6: wave peak runs X..y, 6..12: back y..X (1.5 letters per frame), 13..15: logo rests.
    The letter under the peak is 2px up, its neighbours 1px (16 frames, same loop as the snowfall)"""
    if fr > 12: return [0] * n
    p = 1.5 * fr if fr <= 6 else 9 - 1.5 * (fr - 6)
    return [JUMP if abs(i - p) <= 0.75 else 1 if abs(i - p) <= 1.75 else 0 for i in range(n)]

def build_logo(lift):
    mc = Path("mcfont/assets/minecraft")
    prov = [p for p in json.load(open(mc / "font/include/default.json", encoding="utf-8"))["providers"] if p["file"].endswith("ascii.png")][0]
    atlas = Image.open(mc / "textures/font/ascii.png").convert("RGBA")
    cw = atlas.width // 16; chh = atlas.height // len(prov["chars"]); glyph = {}
    for r, row in enumerate(prov["chars"]):
        for c, ch in enumerate(row):
            g = atlas.crop((c*cw, r*chh, (c+1)*cw, (r+1)*chh)); xs = [x for x in range(cw) for y in range(chh) if g.getpixel((x, y))[3]]
            glyph[ch] = (g, (max(xs)+1) if xs else 0)
    parts = [("Xmas", (214, 40, 40), (166, 18, 30), (70, 8, 16)), ("Legacy", (252, 253, 255), (206, 230, 248), (110, 160, 205))]
    tw = sum(glyph[ch][1] + 3 for t, *_ in parts for ch in t)
    ow, oh = tw + 4, LOGO_H
    fill = {}; x = 2; li = 0
    for text, c1, c2, oc in parts:
        for ch in text:
            g, w = glyph[ch]
            for gx in range(w):
                for gy in range(chh):
                    if g.getpixel((gx, gy))[3]:
                        for b in (0, 1):                      # bold: drawn twice, 1px apart
                            fill[(x + gx + b, gy + 3 + JUMP - lift[li])] = (c1 if gy < 5 else c2, oc)
            x += w + 3                                  # bold width + 1px gap so outlines don't merge letters
            li += 1
    logo = Image.new("RGBA", (ow, oh), (0, 0, 0, 0)); lp = logo.load()
    for (fx, fy), (_, oc) in fill.items():                     # outline (8-neighbourhood)
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1)):   # 4-neighbourhood + drop shadow corner
                q = (fx + dx, fy + dy)
                if q not in fill and 0 <= q[0] < ow and 0 <= q[1] < oh: lp[q] = (*oc, 255)
    for (fx, fy), (c, _) in fill.items(): lp[fx, fy] = (*c, 255)
    for xx in range(ow):                                       # snow resting on top of the letters
        ys = [yy for yy in range(oh) if lp[xx, yy][3]]
        if not ys: continue
        top = ys[0]; lump = 1 + (1 if math.sin(xx * 0.7) > 0.3 else 0)
        for k in range(1, lump + 1):
            if top - k >= 0: lp[xx, top - k] = (255, 255, 255, 255)
        if top - lump - 1 >= 0 and xx % 6 == 2: lp[xx, top - lump] = (196, 236, 255, 255)
    return logo

LOGOS = [build_logo(lifts(f)) for f in range(FRAMES)]
assert LOGOS[0].width <= CW, LOGOS[0].width
frames = [render(f) for f in range(FRAMES)]
for f in frames:                                      # Oraxen advance rule
    assert all(f.getpixel((W - 1, y))[3] == 0 for y in range(H))
    assert any(f.getpixel((W - 2, y))[3] for y in range(H))
sheet = Image.new("RGBA", (W, H * FRAMES))
for i, f in enumerate(frames): sheet.alpha_composite(f, (0, H * i))
sheet.save(sys.argv[1]); frames[0].save(sys.argv[2])

# ---------- GIF preview on a mock sidebar ----------
mc = Path("mcfont/assets/minecraft")
prov = [p for p in json.load(open(mc / "font/include/default.json", encoding="utf-8"))["providers"] if p["file"].endswith("ascii.png")][0]
atlas = Image.open(mc / "textures/font/ascii.png").convert("RGBA")
cw = atlas.width // 16; chh = atlas.height // len(prov["chars"]); cell = {}
for r, row in enumerate(prov["chars"]):
    for c, ch in enumerate(row):
        g = atlas.crop((c*cw, r*chh, (c+1)*cw, (r+1)*chh)); xs = [x for x in range(cw) for y in range(chh) if g.getpixel((x, y))[3]]
        cell[ch] = (g, (max(xs)+1) if xs else 0)
def text(can, x, top, s, col):
    for ch in s:
        if ch == " ": x += 4; continue
        g, w = cell[ch]; t = Image.new("RGBA", g.size, (*col, 255)); t.putalpha(g.getchannel("A"))
        can.alpha_composite(t, (x, top)); x += w + 1
lines = ["", "Money 0 $", "", "Exp 170 (5)", "", "Job Blacksmith", "", "Mastery 3487 Rxp", "", "Ice", "[##########]"]
gif = []
for f in frames:
    k = TW; can = Image.new("RGBA", (k + 40, 9 * (len(lines) + 1) + 50), (70, 90, 70, 255))
    l = 20; k1 = 49; dr = ImageDraw.Draw(can)
    dr.rectangle([l - 2, k1 - 10, l + k + 1, k1 - 2], fill=(0, 0, 0, 102))
    dr.rectangle([l - 2, k1 - 1, l + k + 1, k1 - 1 + 9 * len(lines)], fill=(0, 0, 0, 77))
    can.alpha_composite(f, (l - 2, k1 - 2 - ASCENT))
    for i, s in enumerate(lines): text(can, l, k1 + 9 * i, s, (255, 255, 255))
    gif.append(can.resize((can.width * 4, can.height * 4), Image.NEAREST).convert("P", palette=Image.ADAPTIVE))
gif[0].save(sys.argv[3], save_all=True, append_images=gif[1:], duration=167, loop=0)
print("ok sheet", sheet.size)
