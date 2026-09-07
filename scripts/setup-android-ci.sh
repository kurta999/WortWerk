#!/usr/bin/env bash
set -euo pipefail

: "${ANDROID_HOME:?ANDROID_HOME must point to the Android SDK}"
: "${RUNNER_TEMP:?RUNNER_TEMP must be set}"
tools_dir="$RUNNER_TEMP/wortwerk-android-tools"
mkdir -p "$tools_dir"

# Fixed Google CLI version avoids changes in the sdkmanager compatibility wrapper.
curl --fail --location --retry 3 \
  https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip \
  --output "$tools_dir/tools.zip"
# SHA-1 as published in Google's SDK repository2-1.xml for cmdline-tools;19.0.
echo "5fdcc763663eefb86a5b8879697aa6088b041e70  $tools_dir/tools.zip" | sha1sum --check
unzip -q -o "$tools_dir/tools.zip" -d "$tools_dir"
"$tools_dir/cmdline-tools/bin/sdkmanager" --sdk_root="$ANDROID_HOME" \
  "platforms;android-36.1" "build-tools;36.0.0" "platform-tools"
