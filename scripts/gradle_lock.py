"""Serialize Gradle writers sharing this pack's dependency cache."""
from contextlib import contextmanager
from pathlib import Path
import os,time
ROOT=Path(__file__).resolve().parents[1]

@contextmanager
def gradle_writer(timeout=1800):
    path=ROOT/'.local/gradle-writer.lock';path.parent.mkdir(parents=True,exist_ok=True)
    with path.open('a+b') as lock:
        lock.seek(0,2)
        if lock.tell()==0:lock.write(b'0');lock.flush()
        start=time.monotonic();announced=False
        while True:
            try:
                lock.seek(0)
                if os.name=='nt':
                    import msvcrt
                    msvcrt.locking(lock.fileno(),msvcrt.LK_NBLCK,1)
                else:
                    import fcntl
                    fcntl.flock(lock.fileno(),fcntl.LOCK_EX|fcntl.LOCK_NB)
                break
            except (BlockingIOError,OSError):
                if time.monotonic()-start>timeout:raise TimeoutError('Waiting for the pack Gradle writer')
                if not announced:print('Waiting for the active pack Gradle build...',flush=True);announced=True
                time.sleep(1)
        try:yield
        finally:
            lock.seek(0)
            if os.name=='nt':msvcrt.locking(lock.fileno(),msvcrt.LK_UNLCK,1)
            else:fcntl.flock(lock.fileno(),fcntl.LOCK_UN)
