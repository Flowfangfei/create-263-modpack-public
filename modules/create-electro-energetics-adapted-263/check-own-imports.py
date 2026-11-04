from pathlib import Path
import re
r=Path(__file__).parent; src=r/'src/neoforge/java'
missing={}
for p in src.rglob('*.java'):
    t=p.read_text(encoding='utf-8')
    for cls in re.findall(r'^import (com\.george_vi\.electroenergetics\.[\w.]+);',t,re.M):
        path=src/Path(cls.replace('.','/')+'.java')
        if path.exists():continue
        # Nested Java types may legitimately have no source file.
        outer=src/Path(cls.rsplit('.',1)[0].replace('.','/')+'.java')
        if outer.exists():continue
        missing.setdefault(cls,[]).append(str(p.relative_to(src)))
for cls,paths in missing.items():print(cls,':',len(paths),':',paths[:2])
