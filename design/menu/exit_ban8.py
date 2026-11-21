"""8x8 'prohibited' sign, hand-coloured: ring lit from the top-left, shaded bottom-right; diagonal bar."""
import sys
from PIL import Image
C = {"R": (214, 40, 46), "q": (246, 104, 108), "r": (160, 20, 32), "K": (110, 12, 24)}
art = ["..qqRR..",
       ".qR..Rr.",
       "qRR...Rr",
       "q.RR...r",
       "R..RR..r",
       "Rr..RRrr",
       ".rr..rK.",
       "..rrKK.."]
img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
for y, line in enumerate(art):
    assert len(line) == 8
    for x, ch in enumerate(line):
        if ch != ".": img.putpixel((x, y), (*C[ch], 255))
img.save("exit_ban8.png")
x8 = Image.open("exit_x8_c.png")
out = Image.new("RGBA", (32, 26))
for row_i, bg in enumerate([(60, 64, 72, 255), (110, 110, 110, 255)]):
    out.paste(Image.new("RGBA", (32, 12), bg), (0, row_i * 13))
    out.alpha_composite(x8, (4, 2 + row_i * 13)); out.alpha_composite(img, (18, 2 + row_i * 13))
out.resize((out.width * 20, out.height * 20), Image.NEAREST).save(sys.argv[1])
