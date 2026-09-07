#!/usr/bin/env bash
set -euo pipefail

: "${GH_REPO:?GH_REPO must identify the repository}"
: "${GITHUB_SHA:?GITHUB_SHA must identify the tested commit}"
[[ -s release/WortWerk-debug.apk && -s release/SHA256SUMS.txt ]] || {
  echo "Tested APK or checksum is missing." >&2
  exit 1
}

tag_options=()
if [[ "${GITHUB_REF_TYPE:-}" == "tag" ]]; then
  release_tag="${GITHUB_REF_NAME:?Missing version tag}"
  [[ "$release_tag" == v* ]] || { echo "Only v* tags are published." >&2; exit 1; }
  tag_options=(--verify-tag)
else
  release_tag="preview-${GITHUB_RUN_ID:?Missing run ID}-${GITHUB_RUN_ATTEMPT:?Missing run attempt}"
  tag_options=(--target "$GITHUB_SHA")
fi

cat > release/notes.md <<'NOTES'
WortWerk Android preview build.

Install WortWerk-debug.apk on Android 8.0 or newer. This is a debug-signed
test build, not a production or Play Store release. CI uses a temporary
debug key: installing over a build signed with another key may require
uninstalling that build first, which removes its local learning progress.

Unit tests, Android lint and APK build passed in this workflow.
SHA256SUMS.txt contains the APK checksum.
NOTES
printf '\nTested commit: %s\n' "$GITHUB_SHA" >> release/notes.md

if gh release view "$release_tag" --json isDraft > release/existing.json; then
  if ! jq -e '.isDraft == true' release/existing.json > /dev/null; then
    echo "Release already published; leaving it unchanged."
    exit 0
  fi
  gh release upload "$release_tag" release/WortWerk-debug.apk release/SHA256SUMS.txt --clobber
  gh release edit "$release_tag" --draft=false --prerelease --latest=false \
    --title "WortWerk $release_tag preview" --notes-file release/notes.md
else
  # Assemble both assets in a draft, then publish atomically after upload succeeds.
  gh release create "$release_tag" release/WortWerk-debug.apk release/SHA256SUMS.txt \
    "${tag_options[@]}" --draft --prerelease --latest=false \
    --title "WortWerk $release_tag preview" --notes-file release/notes.md
  gh release edit "$release_tag" --draft=false --prerelease --latest=false
fi

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  printf 'Published prerelease: **%s** with WortWerk-debug.apk and SHA256SUMS.txt.\n' \
    "$release_tag" >> "$GITHUB_STEP_SUMMARY"
fi
