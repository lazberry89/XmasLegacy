"""16x16 exit icon (Oraxen required/exit_icon): red rounded button with a white X and a snow cap, matching the logo."""
import sys
from PIL import Image
C = {"K": (58, 6, 14), "R": (214, 40, 46), "q": (242, 96, 100), "r": (160, 20, 32),
     "W": (255, 255, 255), "w": (206, 214, 226), "s": (214, 236, 250), "S": (160, 204, 236)}
art = [
"................",
"..WWWW....WWW...",
".WWWWWWWWWWWWWW.",
"KsSsWWsSWWWsSsSK",
"KqqqqqqqqqqqqqqK",
"KRRRRRRRRRRRRRRK",
"KRRWWRRRRRRWWRRK",
"KRRWWWRRRRWWWRRK",
"KRRRWWWRRWWWRRRK",
"KRRRRWWWWWWRRRRK",
"KRRRRRWWWWRRRRRK",
"KRRRRWWWWWWRRRRK",
"KRRRWWWRRWWWRRRK",
"KRRWWWRRRRWWWRRK",
"KrrrWwrrrrrwWrrK",
".KKKKKKKKKKKKKK.",
]
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for y, row in enumerate(art):
    assert len(row) == 16, (y, len(row))
    for x, ch in enumerate(row):
        if ch != ".": img.putpixel((x, y), (*C[ch], 255))
img.save(sys.argv[1])
bg = Image.new("RGBA", (16 * 3 + 20, 16 + 10), (60, 64, 72, 255))
bg.alpha_composite(img, (5, 5))
bg.alpha_composite(img.resize((8, 8), Image.NEAREST), (30, 9))           # how it looks as the 8px glyph at GUI scale 1
bg.resize((bg.width * 12, bg.height * 12), Image.NEAREST).save(sys.argv[2])
