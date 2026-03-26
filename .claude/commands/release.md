---
name: release
description: >
  Run pre-flight checks and trigger the release workflow.
  Use this when you want to release the current version to Maven Central.
  Pre-conditions: version in gradle/libs.versions.toml must differ from the
  latest git tag, and CHANGELOG.md must have a matching entry.
---

Run pre-flight checks and trigger the release workflow.

## Steps

1. **Read the current version** from `gradle/libs.versions.toml`:
   ```
   grep "mesh-link" gradle/libs.versions.toml -m 1 | cut -d "=" -f2 | xargs
   ```

2. **Get the latest git tag**:
   ```
   git tag --sort=-v:refname | head -n 1
   ```

3. **Check for a new version** — if the version matches the latest tag, stop and tell the user there is nothing to release.

4. **Validate the changelog** — check that `CHANGELOG.md` contains a `## {VERSION}` header. If it is missing, stop and tell the user to add a changelog entry before releasing.

5. **Trigger the release workflow**:
   ```
   gh workflow run release.yaml --ref main
   ```

6. **Get the run ID** (wait 3 seconds for it to register, then fetch):
   ```
   gh run list --workflow=release.yaml --limit 1 --json databaseId,url
   ```

7. **Report** the run URL to the user and watch it:
   ```
   gh run watch <databaseId> --exit-status
   ```

8. On success, tell the user the release completed and show the GitHub Release URL:
   `https://github.com/FrontFin/mesh-android-sdk/releases/tag/{VERSION}`

   On failure, tell the user which step failed and suggest checking the run logs at the URL from step 6.
