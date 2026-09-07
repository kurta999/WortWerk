#!/usr/bin/env bash
set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
test_root="$project_root/app/build/prerelease-script-tests"
mkdir -p "$test_root"
export GH_REPO=example/wortwerk GITHUB_SHA=0123456789abcdef
export GITHUB_RUN_ID=12345 GITHUB_RUN_ATTEMPT=2 GITHUB_STEP_SUMMARY=""

# Functions shadow gh and jq. No GitHub/network commands run in these tests.
gh() {
  printf '%s\n' "$*" >> "$LOG_FILE"
  case "$1 $2" in
    "release view")
      case "$TEST_CASE" in
        draft|upload-failure) printf '{"isDraft":true}\n'; return 0 ;;
        published) printf '{"isDraft":false}\n'; return 0 ;;
        *) return 1 ;;
      esac ;;
    "release upload") [[ "$TEST_CASE" != upload-failure ]] ;;
    *) return 0 ;;
  esac
}
jq() { grep -q '"isDraft":true' "${@: -1}"; }
export -f gh jq

for scenario in branch version draft published upload-failure missing-apk; do
  export TEST_CASE="$scenario"
  export GITHUB_REF_TYPE=branch GITHUB_REF_NAME=main
  [[ "$scenario" != version ]] || { export GITHUB_REF_TYPE=tag GITHUB_REF_NAME=v1.0.0; }
  case_dir="$test_root/$scenario"
  mkdir -p "$case_dir/release"
  export LOG_FILE="$case_dir/calls.log"
  : > "$LOG_FILE"
  if [[ "$scenario" != missing-apk ]]; then
    printf 'fixture' > "$case_dir/release/WortWerk-debug.apk"
  fi
  printf 'fixture-checksum' > "$case_dir/release/SHA256SUMS.txt"
  status=0
  (cd "$case_dir" && bash "$project_root/scripts/publish-prerelease.sh") > "$case_dir/output.log" 2>&1 || status=$?
  case "$scenario" in
    branch)
      [[ "$status" == 0 ]]
      grep -q 'release create preview-12345-2 .*--target 0123456789abcdef' "$LOG_FILE"
      grep -q 'release edit preview-12345-2 --draft=false --prerelease --latest=false' "$LOG_FILE" ;;
    version)
      [[ "$status" == 0 ]]
      grep -q 'release create v1.0.0 .*--verify-tag' "$LOG_FILE"
      grep -q 'release edit v1.0.0 --draft=false --prerelease' "$LOG_FILE" ;;
    draft)
      [[ "$status" == 0 ]]
      grep -q 'release upload .*--clobber' "$LOG_FILE"
      grep -q 'release edit .*--draft=false' "$LOG_FILE"
      ! grep -q 'release create' "$LOG_FILE" ;;
    published)
      [[ "$status" == 0 ]]
      ! grep -Eq 'release (create|upload|edit)' "$LOG_FILE" ;;
    upload-failure)
      [[ "$status" != 0 ]]
      ! grep -q 'release edit' "$LOG_FILE" ;;
    missing-apk)
      [[ "$status" != 0 ]]
      [[ ! -s "$LOG_FILE" ]] ;;
  esac
  echo "PASS: $scenario"
done
