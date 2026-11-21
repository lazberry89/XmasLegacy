import sys
from PIL import Image
import numpy as np
src = Image.open('logo_only.png').convert('RGB')
a = np.asarray(src).astype(float)
r, g, b = a[..., 0], a[..., 1], a[..., 2]
# magenta-ness: high r & b, low g
mag = np.clip(((np.minimum(r, b) - g) - 60) / 120, 0, 1)
alpha = 1 - mag
ys, xs = np.where(alpha > 0.5)
y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
a, alpha, mag = a[y0:y1, x0:x1], alpha[y0:y1, x0:x1], mag[y0:y1, x0:x1]
# interior palette: confidently opaque pixels
interior = a[(alpha > 0.98)]
pal_img = Image.fromarray(interior.reshape(-1, 1, 3).astype(np.uint8)).quantize(colors=int(sys.argv[2]), method=Image.Quantize.MEDIANCUT)
pal = np.array(pal_img.getpalette()[:int(sys.argv[2]) * 3]).reshape(-1, 3).astype(float)
TW = int(sys.argv[1]); TH = round((y1 - y0) * TW / (x1 - x0))
rgb = Image.fromarray(a.astype(np.uint8)).resize((TW, TH), Image.BOX)
al = Image.fromarray((alpha * 255).astype(np.uint8)).resize((TW, TH), Image.BOX)
# colour: average only over opaque source pixels (avoid magenta bleed)
w = Image.fromarray(((alpha > 0.98) * 255).astype(np.uint8)).resize((TW, TH), Image.BOX)
prem = Image.fromarray((a * (alpha > 0.98)[..., None]).astype(np.uint8)).resize((TW, TH), Image.BOX)
P = np.asarray(prem).astype(float); Wt = np.asarray(w).astype(float)[..., None] / 255
col = np.where(Wt > 0.05, P / np.maximum(Wt, 1e-3), np.asarray(rgb).astype(float))
A = np.asarray(al) > 140
d = ((col[..., None, :] - pal[None, None]) ** 2).sum(-1)
q = pal[d.argmin(-1)]
out = np.zeros((TH, TW, 4), np.uint8); out[..., :3] = q; out[..., 3] = A * 255
Image.fromarray(out).save(sys.argv[3]); print(TW, TH)
bg = Image.new('RGBA', (TW, TH), (40, 44, 52, 255)); bg.alpha_composite(Image.fromarray(out))
bg.resize((TW * (1200 // TW), TH * (1200 // TW)), Image.NEAREST).save(sys.argv[3].replace('.png', '_view.png'))
