# Create 26.3 Modpack source workspace

- Publication authorization (2026-11-04): submit reviewed updates, merge the latest work into main, create an experimental release, and publish reviewed source publicly. Do not expose restricted assets or private history; changing repository identity or rewriting history requires a concrete approved plan. Preserve local source workspaces.
- Preserve separate upstream identities/licenses. Imported worktrees are honest source snapshots with source hashes, not fabricated historical commits.
- Never access or launch original HMCL worlds. Tests only use new runtime directories created under this workspace.
- Keep .local, runtime, artifacts, caches, raw/curated AI records, personal identifiers and machine-specific configuration out of remote Git and release archives.
- Keep restricted upstream assets local and obtain them from fixed upstream sources during bootstrap. Do not mirror those assets in GitHub attachments.
- One Gradle writer. Java25 / Minecraft26.3 / NeoForge26.3.0.22-beta / Gradle9.5.0 is the initial integration baseline; source changes must pass relevant compile and real runtime checks.
- Maintain root project records and machine-readable provenance. No source deletion, resets or history rewriting.

## Ponder regression sampling

- Test one representative for Ponder entries in the same module with the same actual logic.
- Group by storyboard implementation and meaningful captured parameters, not callback object identity. Item/material aliases using the same logic do not need separate playback.
- Keep separate samples only for materially different rendering, data formats, configuration branches or changed behavior relevant to the fix.
- After a change, rerun affected representative groups plus a small cross-module smoke check. An unrelated JAR hash change alone does not require replaying the entire pack.
- Keep the full registration index for inexpensive static checks; record the representative groups, selection and limits of runtime coverage.

## Public snapshot publication

- The approved public repository is Flowfangfei/create-263-modpack-public. Preserve Flowfangfei/create-263-modpack as a private maintenance archive, including its existing refs and releases.
- Import only an audited current-tree snapshot into independent public Git history. Never push the private repository history to the public remote. Record the real snapshot source commit and tree; do not invent upstream or public historical commits.

## Documentation evidence coverage

- Review documentation against the full development path: source provenance and differential recovery; loader, dependency ABI and namespaces; foundation physics/railways; addon integration; transactions, persistence and networking; data/resources; functional and client validation; reproducible distribution.
- Choose emphasis by engineering responsibility and impact, using original records and current code. Recent log volume or larger scene/frame counts do not establish greater technical importance or broader coverage.
- Keep the README focused on players and port developers. Put general methods in MOD-UPGRADE-AND-TESTING, project implementations in DEVELOPMENT-OVERVIEW, and detailed subsystem regressions in their topic pages.
- Label historical compile/pending states by their phase and link later evidence; preserve remaining limitations. Keep localized claims, commands and links aligned.
