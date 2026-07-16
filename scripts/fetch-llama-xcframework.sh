#!/usr/bin/env bash
# llama.cpp 公式プレビルト XCFramework を取得して iosApp/Frameworks/ に配置する。
# （リポジトリには含めない: 約780MB。リリースタグはピン留め）
set -euo pipefail

TAG="b10038"
DEST="$(cd "$(dirname "$0")/.." && pwd)/iosApp/Frameworks"

if [ -d "$DEST/llama.xcframework" ]; then
  echo "llama.xcframework は既に存在します: $DEST"
  exit 0
fi

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

echo "Downloading llama-$TAG-xcframework.zip ..."
gh release download "$TAG" -R ggml-org/llama.cpp -p "llama-$TAG-xcframework.zip" -D "$TMP" ||
  curl -fL -o "$TMP/llama-$TAG-xcframework.zip" \
    "https://github.com/ggml-org/llama.cpp/releases/download/$TAG/llama-$TAG-xcframework.zip"

unzip -q "$TMP/llama-$TAG-xcframework.zip" -d "$TMP/x"
mkdir -p "$DEST"
mv "$TMP/x/build-apple/llama.xcframework" "$DEST/"
echo "done: $DEST/llama.xcframework"
