"""Launch a real client on CI; require resource upload without loader/render crashes."""
import os
from pathlib import Path
import re
import subprocess
import time

log = Path("build/client-smoke.log")
log.parent.mkdir(exist_ok=True)
environment = dict(os.environ, LIBGL_ALWAYS_SOFTWARE="true", ALSOFT_DRIVERS="null")
with log.open("w") as output:
    client = subprocess.Popen(
        ["xvfb-run", "-a", "gradle", "--no-daemon", "runClient", "--args=--username PvpseSmoke"],
        stdout=output, stderr=subprocess.STDOUT, env=environment, start_new_session=True,
    )
    ready_at = None
    try:
        deadline = time.monotonic() + 210
        while time.monotonic() < deadline:
            text = log.read_text(errors="replace")
            fatal = ["Failed to execute entrypoint", "Crash report saved", "ReportedException",
                     "Unexpected error", "NoSuchMethodError", "NoClassDefFoundError", "Exception in thread"]
            if any(marker in text for marker in fatal):
                raise RuntimeError("Client startup failure; inspect client-smoke.log")
            if re.search(r"Created:.*(?:atlas|textures)", text) and "pvp_hud_client" in text:
                if ready_at is None: ready_at = time.monotonic()
                if time.monotonic() - ready_at > 10:
                    print("Client initialized PVPSE, uploaded atlases and ran for 10 seconds without a fatal error")
                    break
            if client.poll() is not None:
                raise RuntimeError("Client exited before the startup check completed")
            time.sleep(2)
        else:
            raise RuntimeError("Client did not finish initializing within 210 seconds")
    finally:
        import signal
        os.killpg(client.pid, signal.SIGTERM) if client.poll() is None else None
        try: client.wait(timeout=15)
        except subprocess.TimeoutExpired: os.killpg(client.pid, signal.SIGKILL)
