---
name: bump-version
description: >
  Bumps the mesh-android-sdk version using semantic versioning, then updates CHANGELOG.md.
  Use this skill whenever the user asks to bump the version, release a new version, update
  the version number, prepare a release, or update the changelog. Also trigger when the user
  says things like "we're ready to release" or "what version should this be?".

  Steps: diff HEAD against the latest git tag → classify changes as MAJOR/MINOR/PATCH →
  increment the `mesh-link` version in gradle/libs.versions.toml → prepend a new entry to
  CHANGELOG.md.
---

## bump-version skill

### Step 1 — Sync with remote, find the latest tag, and build the diff

First, fetch the latest commits and all tags from origin so the diff reflects the true
published state rather than a stale local snapshot:

```bash
git fetch origin main --tags
```

Then find the latest tag:

```bash
git describe --tags --abbrev=0
```

If no tags exist, treat the full history as the diff (use `git log --oneline` for context) and
assume the current version in `gradle/libs.versions.toml` is the starting point.

```bash
git diff <latest-tag>..HEAD --stat
git diff <latest-tag>..HEAD -- \
  'link/src/main/java/com/meshconnect/link/entity/' \
  'link/src/main/java/com/meshconnect/link/ui/LaunchLink.kt' \
  'link/src/main/java/com/meshconnect/link/ui/LinkActivity.kt' \
  'link/src/main/java/com/meshconnect/link/ui/LinkViewModel.kt' \
  'link/src/main/java/com/meshconnect/link/utils/' \
  'link/proguard-rules.pro'
```

Also capture the commit log for the changelog summary:

```bash
git log <latest-tag>..HEAD --oneline
```

---

### Step 2 — Classify the bump

Analyse the diff output against these rules (apply the highest matching level):

#### MAJOR — any of:
- A public class, interface, or `data class` in `entity/` is **deleted or renamed**
- A field is **removed** from a public `data class` (e.g. `LinkConfiguration`, `LinkPayload` subtypes,
  `AccountToken`, `IntegrationAccessToken`)
- A method signature in `LaunchLink.kt` or `JSBridge.kt` is changed in a breaking way
- `proguard-rules.pro` removes a `-keep` rule for a previously public class

#### MINOR — any of (and no MAJOR):
- A **new field** is added to `LinkConfiguration`, `LinkPayload`, or another public `data class`
- A **new sealed subtype** is added to `LinkPayload` or `LinkResult`
- A **new public utility file** is added under `utils/`
- A **new enum value** is added to any public enum (e.g. `LinkTheme`)
- `LaunchLink.kt` gains new optional parameters

#### PATCH — everything else:
- Bug fixes, internal refactors, test additions or changes
- README, CHANGELOG, or `CLAUDE.md` edits
- Dependency version bumps that don't affect the public API
- New or changed private/internal functions
- Changes confined to `usecase/`, `converter/`, or the `app/` demo module

When in doubt between MINOR and PATCH, prefer MINOR. When in doubt between MAJOR and MINOR,
**ask the user** before proceeding — a wrong MAJOR bump is hard to undo after publishing.

---

### Step 3 — Read and bump the version

Read the current version:

```bash
grep 'mesh-link' gradle/libs.versions.toml
```

The line looks like:
```
mesh-link = "3.2.0"
```

Apply the bump:
- **MAJOR**: increment first number, reset others to 0  →  `3.2.0` → `4.0.0`
- **MINOR**: increment second number, reset third to 0  →  `3.2.0` → `3.3.0`
- **PATCH**: increment third number                     →  `3.2.0` → `3.2.1`

Write the new version back using Edit — change only the `mesh-link` line, nothing else.

---

### Step 4 — Update CHANGELOG.md

Read `CHANGELOG.md` if it exists (it may not).

Prepend a new section at the very top (above any existing content):

```markdown
## X.Y.Z

### Added
- ...

### Changed
- ...

### Fixed
- ...

### Removed
- ...
```

Rules for the summary:
- Do **not** include a date — use only the version number: `## X.Y.Z`.
- Omit any section heading (`Added`, `Changed`, etc.) that has no items.
- Keep each bullet to one line — describe **what changed and why it matters** to a consumer of
  the SDK, not internal implementation details. Example:
  - ✅ `Added \`theme\` parameter to \`LinkConfiguration\` for light/dark/system UI theming`
  - ❌ `Modified getLinkIntent() to pass THEME extra to LinkActivity`
- Changes in `app/`, tests, `CLAUDE.md`, and `README.md` go under **Changed** only if they
  affect the developer experience; skip purely internal ones.
- If CHANGELOG.md did not exist, create it with a short header before the first entry:

```markdown
# Changelog

All notable changes to the Mesh Connect Android SDK are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## X.Y.Z
...
```

---

### Step 5 — Report to the user

After writing the files, summarise:

```
Bumped version: 3.2.0 → 3.3.0  (MINOR)

Reason: new fields added to LinkConfiguration (language, displayFiatCurrency, theme),
        new LinkTheme enum, new Language.kt and Theme.kt utility files.

Files updated:
  • gradle/libs.versions.toml  (mesh-link version)
  • CHANGELOG.md               (new entry prepended)
```

Then ask the user:

> Would you like me to commit these changes?

If they say yes, stage only the two modified files and create a commit:

```bash
git add gradle/libs.versions.toml CHANGELOG.md
git commit -m "chore: bump version to X.Y.Z"
```

If they say no, leave the files as-is.
