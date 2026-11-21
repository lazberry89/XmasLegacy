"""Two more 16x16 exit icon variants: power symbol and a plain X."""
import sys
from PIL import Image
C = {"K": (58, 6, 14), "R": (214, 40, 46), "q": (242, 96, 100), "r": (160, 20, 32), "W": (255, 255, 255)}
power = [
"................",
"......KKKK......",
"...KK.KqqK.KK...",
"..KqRKKqRKKRqK..",
".KqRRKKRRKKRRrK.",
".KqRK.KRRK.KRrK.",
"KqRK..KRRK..KRrK",
"KqRK..KRRK..KRrK",
"KRRK..KKKK..KRrK",
"KRRK........KRrK",
".KRRK......KRrK.",
".KRRRKK..KKRrrK.",
"..KRRRRKKRRRrK..",
"...KKrrrrrrKK...",
".....KKKKKK.....",
"................",
]
xmark = [
"................",
"................",
"..KKK......KKK..",
".KqqRK....KqqRK.",
".KqRRRK..KqRRrK.",
"..KRRRRKKqRRrK..",
"...KRRRRRRRrK...",
"....KRRRRRrK....",
"....KqRRRRRK....",
"...KqRRRRRRRK...",
"..KqRRrKKRRRRK..",
".KqRRrK..KRRRrK.",
".KRRrK....KRrrK.",
"..KKK......KKK..",
"................",
"................",
]
def make(art):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(art):
        assert len(row) == 16, (y, len(row))
        for x, ch in enumerate(row):
            if ch != ".": img.putpixel((x, y), (*C[ch], 255))
    return img
p, x = make(power), make(xmark)
p.save("exit_icon_power.png"); x.save("exit_icon_x.png")
cur = Image.open("exit_icon.png")
row = Image.new("RGBA", (3 * 16 + 4 * 6 + 3 * 8, 30), (60, 64, 72, 255))
for i, im in enumerate((cur, p, x)):
    ox = 6 + i * 30
    row.alpha_composite(im, (ox, 3)); row.alpha_composite(im.resize((8, 8), Image.NEAREST), (ox + 4, 21))
row.resize((row.width * 10, row.height * 10), Image.NEAREST).save(sys.argv[1])
