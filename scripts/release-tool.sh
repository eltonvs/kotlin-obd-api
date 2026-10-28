#!/usr/bin/env bash
#
# Release helpers shared by .github/workflows/release.yml and publish.yml.
# See RELEASING.md for the process these checks enforce.
#
# Usage:
#   release-tool.sh validate-version <version>
#   release-tool.sh compare <version-a> <version-b>      # prints -1, 0 or 1 (SemVer precedence)
#   release-tool.sh release-plan <branch> <version>      # gates the Release workflow
#   release-tool.sh publish-plan <tag>                   # gates the Publish workflow
#   release-tool.sh update-docs <version>                # rewrites README.md/llms.txt install snippets
#
# release-plan and publish-plan print key=value lines meant for "$GITHUB_OUTPUT".
# They read tags and remote-tracking branches from the local clone, so run them
# after a full fetch (actions/checkout with fetch-depth: 0).
#
# Works with bash 3.2 (macOS) and later.

set -euo pipefail
export LC_ALL=C

REMOTE="${RELEASE_REMOTE:-origin}"
SEMVER_RE='^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-([0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*))?$'

die() {
    echo "::error::$*" >&2
    exit 1
}

# SemVer 2.0.0 without build metadata (Maven versions must not contain "+").
is_semver() {
    local version="$1" pre identifier
    [[ "$version" =~ $SEMVER_RE ]] || return 1
    pre="${BASH_REMATCH[5]}"
    [[ -z "$pre" ]] && return 0
    local IFS=.
    for identifier in $pre; do
        # Numeric pre-release identifiers must not have leading zeros.
        if [[ "$identifier" =~ ^[0-9]+$ && "$identifier" =~ ^0[0-9] ]]; then
            return 1
        fi
    done
    return 0
}

is_prerelease() {
    [[ "$1" == *-* ]]
}

major_of() {
    echo "${1%%.*}"
}

# Prints -1, 0 or 1 following SemVer 2.0.0 precedence rules.
semver_compare() {
    local a="$1" b="$2"
    local a_core="${a%%-*}" b_core="${b%%-*}" a_pre="" b_pre=""
    [[ "$a" == *-* ]] && a_pre="${a#*-}"
    [[ "$b" == *-* ]] && b_pre="${b#*-}"

    local IFS=.
    local -a ac bc ap bp
    ac=($a_core)
    bc=($b_core)
    local i
    for i in 0 1 2; do
        if ((10#${ac[i]} < 10#${bc[i]})); then
            echo -1
            return
        elif ((10#${ac[i]} > 10#${bc[i]})); then
            echo 1
            return
        fi
    done

    # A version without pre-release has higher precedence than one with it.
    if [[ -z "$a_pre" && -z "$b_pre" ]]; then
        echo 0
        return
    elif [[ -z "$a_pre" ]]; then
        echo 1
        return
    elif [[ -z "$b_pre" ]]; then
        echo -1
        return
    fi

    ap=($a_pre)
    bp=($b_pre)
    local x y
    i=0
    while ((i < ${#ap[@]} && i < ${#bp[@]})); do
        x="${ap[i]}"
        y="${bp[i]}"
        if [[ "$x" =~ ^[0-9]+$ && "$y" =~ ^[0-9]+$ ]]; then
            if ((10#$x < 10#$y)); then
                echo -1
                return
            elif ((10#$x > 10#$y)); then
                echo 1
                return
            fi
        elif [[ "$x" =~ ^[0-9]+$ ]]; then
            echo -1 # numeric identifiers sort before alphanumeric ones
            return
        elif [[ "$y" =~ ^[0-9]+$ ]]; then
            echo 1
            return
        elif [[ "$x" < "$y" ]]; then
            echo -1
            return
        elif [[ "$x" > "$y" ]]; then
            echo 1
            return
        fi
        i=$((i + 1))
    done
    if ((${#ap[@]} < ${#bp[@]})); then
        echo -1
    elif ((${#ap[@]} > ${#bp[@]})); then
        echo 1
    else
        echo 0
    fi
}

# Release versions of every vX.Y.Z[-pre] tag in the clone, one per line.
# With a commit argument, only tags reachable from that commit are listed.
release_versions() {
    local tag version
    git tag --list ${1:+--merged "$1"} 'v*' | while IFS= read -r tag; do
        version="${tag#v}"
        if is_semver "$version"; then
            echo "$version"
        fi
    done
}

# Reads versions on stdin and prints the highest one (empty if none).
# Optional filters: --major N, --stable, --below VERSION.
highest_version() {
    local major="" stable=false below="" best="" version
    while (($# > 0)); do
        case "$1" in
            --major) major="$2"; shift 2 ;;
            --stable) stable=true; shift ;;
            --below) below="$2"; shift 2 ;;
            *) die "highest_version: unknown option $1" ;;
        esac
    done
    while IFS= read -r version; do
        [[ -z "$version" ]] && continue
        [[ -n "$major" && "$(major_of "$version")" != "$major" ]] && continue
        [[ "$stable" == true ]] && is_prerelease "$version" && continue
        [[ -n "$below" && "$(semver_compare "$version" "$below")" != -1 ]] && continue
        if [[ -z "$best" || "$(semver_compare "$version" "$best")" == 1 ]]; then
            best="$version"
        fi
    done
    echo "$best"
}

remote_branch_exists() {
    git show-ref --verify --quiet "refs/remotes/$REMOTE/$1"
}

cmd_validate_version() {
    is_semver "${1:?version required}" || die "'$1' is not a SemVer version (MAJOR.MINOR.PATCH[-PRERELEASE], no 'v' prefix)."
}

# Decides whether <version> may be released from <branch>, whose tip is checked out.
cmd_release_plan() {
    local branch="${1:?branch required}" version="${2:?version required}"
    version="${version#v}"
    cmd_validate_version "$version"
    local major
    major="$(major_of "$version")"

    if [[ "$branch" == master ]]; then
        if remote_branch_exists "$major.x"; then
            die "Major version $major is maintained on the $major.x branch; release $version from there."
        fi
    elif [[ "$branch" =~ ^([0-9]+)\.x$ ]]; then
        if [[ "${BASH_REMATCH[1]}" != "$major" ]]; then
            die "Branch $branch only releases $(echo "$branch" | cut -d. -f1).y.z versions, not $version."
        fi
    else
        die "Releases are cut from master or a maintenance branch named N.x, not '$branch'."
    fi

    if git rev-parse --quiet --verify "refs/tags/v$version" >/dev/null ||
        git rev-parse --quiet --verify "refs/tags/$version" >/dev/null; then
        die "Tag v$version (or $version) already exists."
    fi

    # Versions only move forward on a line. On master compare with the releases reachable
    # from HEAD (the commit being released); on N.x with every N.* release.
    local highest
    if [[ "$branch" == master ]]; then
        highest="$(release_versions HEAD | highest_version)"
    else
        highest="$(release_versions | highest_version --major "$major")"
    fi
    if [[ -n "$highest" && "$(semver_compare "$version" "$highest")" != 1 ]]; then
        die "$version must be greater than the latest release on $branch ($highest)."
    fi

    echo "version=$version"
    echo "tag=v$version"
    if is_prerelease "$version"; then
        echo "prerelease=true"
    else
        echo "prerelease=false"
    fi
}

# Decides how a pushed tag is published and presented on GitHub.
cmd_publish_plan() {
    local tag="${1:?tag required}"
    [[ "$tag" == v* ]] || die "Release tags start with 'v': $tag"
    local version="${tag#v}"
    is_semver "$version" || die "Tag $tag is not vMAJOR.MINOR.PATCH[-PRERELEASE]."
    local major
    major="$(major_of "$version")"

    local sha ref allowed=false
    sha="$(git rev-parse --verify "refs/tags/$tag^{commit}")" || die "Tag $tag not found."
    while IFS= read -r ref; do
        if [[ "$ref" == "refs/remotes/$REMOTE/master" || "$ref" == "refs/remotes/$REMOTE/$major.x" ]]; then
            allowed=true
        fi
    done < <(git for-each-ref --contains "$sha" --format='%(refname)' "refs/remotes/$REMOTE/")
    if [[ "$allowed" != true ]]; then
        die "Tag $tag ($sha) is not reachable from $REMOTE/master or $REMOTE/$major.x. Push the branch before the tag."
    fi

    # "Latest" goes to the highest stable version only, so a 1.x patch published after
    # 2.0.0 or a pre-release never takes the badge. Release notes start at the previous
    # release that is an ancestor of this tag (previous stable one for stable releases).
    local prerelease=false latest=false notes_start
    if is_prerelease "$version"; then
        prerelease=true
        notes_start="$(release_versions "$sha" | highest_version --below "$version")"
    else
        local highest_stable
        highest_stable="$(release_versions | highest_version --stable)"
        if [[ -z "$highest_stable" || "$(semver_compare "$version" "$highest_stable")" != -1 ]]; then
            latest=true
        fi
        notes_start="$(release_versions "$sha" | highest_version --stable --below "$version")"
    fi

    echo "version=$version"
    echo "tag=$tag"
    echo "prerelease=$prerelease"
    echo "latest=$latest"
    echo "notes_start_tag=${notes_start:+v$notes_start}"
}

# Points the README/llms.txt install snippets at <version>; prints changed=true|false.
cmd_update_docs() {
    local version="${1:?version required}"
    cmd_validate_version "$version"
    local file
    for file in README.md llms.txt; do
        [[ -f "$file" ]] || die "$file not found; run from the repository root."
        RELEASE_VERSION="$version" perl -0pi -e '
            s{(eltonvs:kotlin-obd-api:)[0-9A-Za-z.-]+}{$1$ENV{RELEASE_VERSION}}g;
            s{(<artifactId>kotlin-obd-api</artifactId>\s*<version>)[^<]*(</version>)}{$1$ENV{RELEASE_VERSION}$2}g;
        ' "$file"
        grep -q "eltonvs:kotlin-obd-api:$version" "$file" || die "No install snippet found in $file."
    done
    if git diff --quiet -- README.md llms.txt; then
        echo "changed=false"
    else
        echo "changed=true"
    fi
}

main() {
    local command="${1:-}"
    shift || true
    case "$command" in
        validate-version) cmd_validate_version "$@" ;;
        compare)
            cmd_validate_version "${1:?}"
            cmd_validate_version "${2:?}"
            semver_compare "$1" "$2"
            ;;
        release-plan) cmd_release_plan "$@" ;;
        publish-plan) cmd_publish_plan "$@" ;;
        update-docs) cmd_update_docs "$@" ;;
        *)
            sed -n '3,16p' "$0" >&2
            exit 2
            ;;
    esac
}

main "$@"
