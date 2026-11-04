#!/usr/bin/env python3
"""Run a module's Gradle tasks under the shared exclusive writer lock."""
import argparse
from build import run_gradle, MODULES
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('module',choices=[d.name for d in MODULES.iterdir() if d.is_dir()]+['runtime-probe'])
p.add_argument('tasks',nargs='+')
a=p.parse_args()
run_gradle(a.module,a.tasks)
