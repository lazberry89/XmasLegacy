"""8x8 hand-placed X (1 texture px = 1 GUI px for the 8px 'required' glyph).
Shape is the classic 8px X; top/left edges get a highlight, bottom/right edges a shade, so it keeps volume
without an outline eating the 8 pixels."""
import sys
from PIL import Image
R, q, r, K = (214, 40, 46), (246, 104, 108), (160, 20, 32), (96, 10, 22)
shapes = {
 "c": ["XX....XX",
       "XXX..XXX",
       ".XXXXXX.",
       "..XXXX..",
       "..XXXX..",
       ".XXXXXX.",
       "XXX..XXX",
       "XX....XX"],
 "d": ["X......X",
       "XX....XX",
       ".XX..XX.",
       "..XXXX..",
       "..XXXX..",
       ".XX..XX.",
       "XX....XX",
       "X......X"],
}
row = Image.new("RGBA", (len(shapes) * 14 + 4, 12), (60, 64, 72, 255))
btn = Image.new("RGBA", (len(shapes) * 14 + 4, 12), (110, 110, 110, 255))   # on a grey menu button
for i, (k, art) in enumerate(shapes.items()):
    on = lambda x, y: 0 <= x < 8 and 0 <= y < 8 and art[y][x] == "X"
    img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
    for y in range(8):
        for x in range(8):
            if not on(x, y): continue
            c = R
            if not on(x, y - 1) or not on(x - 1, y): c = q
            if not on(x, y + 1) and not on(x + 1, y): c = K
            elif not on(x, y + 1) or not on(x + 1, y): c = r
            img.putpixel((x, y), (*c, 255))
    img.save(f"exit_x8_{k}.png")
    row.alpha_composite(img, (4 + i * 14, 2)); btn.alpha_composite(img, (4 + i * 14, 2))
out = Image.new("RGBA", (row.width, 26)); out.paste(row, (0, 0)); out.paste(btn, (0, 13))
out.resize((out.width * 20, out.height * 20), Image.NEAREST).save(sys.argv[1])
