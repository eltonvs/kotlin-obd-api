# Releasing

Git tags are releases. A tag `vX.Y.Z` (or a pre-release such as `v2.0.0-beta.1`)
is the only place a version is written down: `build.gradle.kts` computes the
version from the tag at build time, and pull requests never edit it.

Each release produces:

| Channel | Coordinates | When |
| :- | :- | :- |
| JitPack | `com.github.eltonvs:kotlin-obd-api:X.Y.Z` | Always. JitPack builds the tag the first time someone requests it. |
| GitHub release | `vX.Y.Z` with the jar, sources jar and javadoc jar attached | Always. |
| Maven Central | `io.github.eltonvs:kotlin-obd-api:X.Y.Z` | Only after the one-time setup below and `MAVEN_CENTRAL_PUBLISHING=true`. |

## Branches

- `master` is the trunk. Every pull request targets it and the next release
  (including pre-releases of the next major) is cut from it.
- `N.x` (for example `1.x`) is a maintenance branch for an older major. It is
  created only when that line needs a patch after `master` has moved on to
  the next major. It is cut from the last tag of that line.
- There is no `develop` branch and no release branch per version.

Fixes always land on `master` first and are cherry-picked to `N.x`. CI runs on
pushes and pull requests for `master` and `*.x`.

## How the version is computed

First match wins:

1. JitPack (`JITPACK=true`): the version JitPack was asked for (`VERSION`).
2. GitHub Actions tag runs (`GITHUB_REF_TYPE=tag`): the tag without the `v`.
3. The highest SemVer `v*` tag pointing at `HEAD`, without the `v`.
4. `0.0.0-SNAPSHOT` for everything else (branches, pull requests, local work).

Snapshots are never published, so the fallback does not try to guess the next
version. To check a build locally, check out a tag and run
`./gradlew publishToMavenLocal`.

## Cutting a release

1. Make sure the branch is green and contains everything that should ship.
2. Open **Actions → Release → Run workflow**, choose the branch (`master` or
   `N.x`) and enter the version without the `v`, for example `1.4.2`.
   Until the release commit is allowed to bypass the branch ruleset (one-time
   setup, item 1), uncheck **update_docs**: with it checked, the push is
   rejected and nothing is released.
3. The workflow:
   - refuses invalid SemVer, versions whose tag already exists, versions that
     do not move the branch's line forward, branches other than `master` and
     `N.x`, a version on `N.x` whose major is not `N`, and a release from
     `master` of a major that already has an `N.x` branch;
   - runs `./gradlew clean test ktlintCheck detekt`;
   - for stable releases with **update_docs** checked, commits the README and
     `llms.txt` install snippets as `chore(release): vX.Y.Z`;
   - pushes the branch and an annotated tag `vX.Y.Z` in one atomic push, so if
     the branch moved or rejects the push, no tag is created;
   - starts the **Publish** workflow on the tag.
4. **Publish** checks that the tagged commit is on the branch its major is
   released from (`N.x` once it exists, `master` before that) and that the
   version is greater than every release in the commit's history. It then runs
   the checks again, builds the jars, uploads to Maven Central (if enabled),
   and creates the GitHub release with generated notes.
5. Optionally warm JitPack by requesting
   `https://jitpack.io/com/github/eltonvs/kotlin-obd-api/X.Y.Z/kotlin-obd-api-X.Y.Z.pom`
   and check the build log at `https://jitpack.io/#eltonvs/kotlin-obd-api`.

Pushing a tag by hand (`git tag -a vX.Y.Z -m "Release vX.Y.Z" && git push origin vX.Y.Z`)
also triggers **Publish**. Push the branch first, because the tagged commit has to
be on its release branch. This path does not update the README, and **Publish**
repeats only the branch and ordering checks above. It does not refuse a version
whose bare tag (`X.Y.Z`) exists, or a version below a release on `N.x` that is
not in the tagged commit's history. Before pushing the tag, run
`./scripts/release-tool.sh release-plan <branch> X.Y.Z` on an up-to-date checkout
of that branch (`git fetch origin --tags`).

### Pre-release (for example `2.0.0-beta.1`)

Run **Release** from `master` with `2.0.0-beta.1`. The README is not touched,
and the GitHub release is marked as a pre-release and never takes the "Latest"
badge. Maven, Gradle and the release checks all order these versions as
`2.0.0-beta.1 < 2.0.0-rc.1 < 2.0.0`. Use dot-separated numeric suffixes
(`-beta.2`, `-rc.10`) so that they sort correctly.

### Major release (for example `2.0.0` after the KMP merge)

1. If the 1.x line will still need patches, cut the maintenance branch from the
   last 1.x tag. This can also be done later, the first time a patch is needed:

   ```shell
   git fetch origin --tags
   git push origin refs/tags/v1.4.2^{commit}:refs/heads/1.x
   ```

   Protect `1.x` the same way as `master` (see the setup below).
2. Merge the major change into `master`. Ship `2.0.0-beta.N` and `2.0.0-rc.N`
   from `master` as needed, then `2.0.0`.
3. From then on, `master` refuses to release `1.y.z`. 1.x releases come from
   `1.x`.

If the KMP migration changes what gets published (for example extra
`-jvm` or platform artifacts), update the jar check and artifact upload in
`publish.yml`, and the install snippets, in the same pull request.

### Patch on an older line (for example `1.4.3` on `1.x`)

1. Fix the bug on `master` through a normal pull request.
2. Backport it:

   ```shell
   git fetch origin
   git switch -c backport/fix-name origin/1.x
   git cherry-pick -x <commit-on-master>
   git push origin backport/fix-name
   ```

   Open a pull request into `1.x`. CI runs on it. If the code no longer
   exists on `master`, fix it directly in a pull request to `1.x`.
3. Run **Release** on the `1.x` branch with `1.4.3`. Once `1.x` is protected
   like `master` (one-time setup, item 2), uncheck **update_docs** unless the
   release commit can bypass the ruleset.

The GitHub release for `v1.4.3` is created with `--latest=false` because `2.0.0`
is higher, so the "Latest" badge stays on the newest stable release.

A maintenance branch has to contain this release automation. Branches cut from
`v1.4.2` or later have it. If a branch is ever cut from an older tag, first
cherry-pick the release automation commits (`build:`/`ci:` changes to
`build.gradle.kts`, `gradle/`, `.github/` and `scripts/`) onto it.

## The "Latest" badge

**Publish** marks a release as Latest only when it is stable and is the highest
stable version among the `v*` tags on their release branch (`master` or `N.x`).
Pre-releases, older-line patches and stray tags on other branches never take it.
To fix the badge by hand, run `gh release edit vX.Y.Z --latest`.

## Retrying and mistakes

- If **Publish** fails, fix the cause and use **Re-run failed jobs** on that
  run. Run **Publish** manually on the tag only when the `maven-central` job did
  not succeed (or was skipped): a new run uploads to Maven Central again, which
  fails for a version Central already released, and the GitHub release is then
  skipped. An existing GitHub release is left as it is.
- Never move, delete or reuse a published tag or version. Maven Central and
  JitPack caches make both permanent. Fix forward with a new patch version.
- `./scripts/release-tool.sh release-plan master 1.4.2` (after `git fetch --tags`)
  previews the release checks locally.

## One-time setup

The automation needs these repository settings. Today only the `release`
environment (restricted to the `master` branch) exists, and it has no secrets.

1. **Pushing the release commit.** The `master` ruleset requires a pull request
   and the `verify` check and has no bypass actors, so the `github-actions[bot]`
   push of the `chore(release)` commit is rejected. The same applies to `N.x`
   once item 2 is done. Because the push is atomic, nothing is created when that
   happens. Pick one:
   - Run **Release** with **update_docs** unchecked. Only the tag is pushed;
     tags are not covered by the branch ruleset. Then bump the README
     install snippets in a normal pull request (`./scripts/release-tool.sh update-docs X.Y.Z`).
   - Or allow the release commit, for example by pushing with a GitHub App
     token whose app is a ruleset bypass actor.
     The workflow uses `GITHUB_TOKEN` today. If you switch to an app or
     personal token, tag pushes start triggering **Publish** on their own,
     so remove the dispatch step in `release.yml`.
2. **Maintenance branches.** Add `*.x` to the branch ruleset (or a copy of it)
   so `1.x` gets the same protection and required `verify` check as `master`.
3. **Tags.** Add a tag ruleset for `v*` that blocks updates and deletions.
   Anyone with write access can still create a `v*` tag, and **Publish** runs
   the workflow and `scripts/release-tool.sh` from the tagged commit. Its branch
   check therefore guards against mistakes, not against a tag on an unmerged
   commit that edits those files. Adding **Restrict creations** (with an admin
   bypass) closes that, but it also blocks the `GITHUB_TOKEN` tag push from
   **Release**, so it fits only with the app token from item 1.
4. **Maven Central (optional).**
   1. Verify the `io.github.eltonvs` namespace in the Central Portal and create
      a user token.
   2. Add these secrets to the `release` environment:

      | Secret | Value |
      | :- | :- |
      | `MAVEN_CENTRAL_USERNAME` | Central Portal user token name |
      | `MAVEN_CENTRAL_PASSWORD` | Central Portal user token password |
      | `SIGNING_KEY` | ASCII-armored private GPG key |
      | `SIGNING_KEY_PASSWORD` | Its passphrase |
      | `SIGNING_KEY_ID` | Optional: short key ID, needed only when signing with a subkey |

      Publish the public key to `keys.openpgp.org` or `keyserver.ubuntu.com`.
   3. In the `release` environment's deployment rules, add the tag pattern `v*`.
      The Maven Central job runs on tag refs, and today only `master` is allowed.
   4. Set the repository variable `MAVEN_CENTRAL_PUBLISHING` to `true`.
   5. Whoever approves the `release` environment: before approving, check that
      the run's commit is on `master` or the matching `N.x` branch (see item 3).

   Until the variable is set, the Maven Central job is skipped and does not wait
   for environment approval. If the variable is set but secrets are missing, the
   job logs a warning and skips the upload instead of failing. The job runs
   `publishAndReleaseToMavenCentral`. To review deployments in the portal before
   they go live, switch it to `publishToMavenCentral` and click **Publish** in the
   Central Portal.
   Once Central has a release, the README can list the `io.github.eltonvs`
   coordinates next to the JitPack ones.
