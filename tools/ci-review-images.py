"""Expose disposable game-test screenshots to reviewers in bounded log chunks."""
import base64
from pathlib import Path
root = Path("build/run/clientGameTest/screenshots")
for name in ("pvpse-fullbright-off", "pvpse-fullbright-on"):
    matches = sorted(root.glob("*_" + name + ".png"))
    if not matches:
        continue
    encoded = base64.b64encode(matches[-1].read_bytes()).decode("ascii")
    for offset in range(0, len(encoded), 8000):
        print("PVPSE_REVIEW|" + name + "|" + str(offset) + "|" + encoded[offset:offset + 8000])
