#!/usr/bin/env bash
# Prints the CHANGELOG.md section of one version, to be used as the release notes.
#
#   tools/release-notes.sh 0.1.0                   # fails if the entry is missing or still says "Unreleased"
#   tools/release-notes.sh 0.1.0 --allow-unreleased  # dry runs: only warns about "Unreleased"
set -euo pipefail

version="${1:?usage: release-notes.sh <version> [--allow-unreleased]}"
allow_unreleased="${2:-}"
changelog="$(dirname "$0")/../CHANGELOG.md"

header="$(grep -m1 -F "## [${version}]" "$changelog" || true)"
if [ -z "$header" ]; then
	echo "CHANGELOG.md has no entry for version ${version}" >&2
	exit 1
fi

if [[ "$header" == *Unreleased* ]]; then
	if [ "$allow_unreleased" = "--allow-unreleased" ]; then
		echo "warning: the entry for ${version} still says Unreleased; date it before releasing" >&2
	else
		echo "the entry for ${version} still says Unreleased: put the release date in CHANGELOG.md first" >&2
		exit 1
	fi
fi

# Everything between this version's heading and the next version heading, without the heading itself.
awk -v h="## [${version}]" '
	index($0, h) == 1 { inside = 1; next }
	inside && /^## \[/ { exit }
	inside { print }
' "$changelog" | sed -e '/./,$!d'
