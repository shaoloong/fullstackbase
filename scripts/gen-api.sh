#!/usr/bin/env bash
# 契约生成：后端 /v3/api-docs → packages/api-client/openapi.json →（可选）orval 类型化客户端
# 改接口后必跑；提交时把 openapi.json 与生成物一起入库（契约即代码，PR 可 diff）。
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="${BASE_URL:-http://127.0.0.1:8082}"
CLIENT="$ROOT/packages/api-client"
NODE="${NODE_BIN:-node}"

echo "==> [1/4] 版本闸门：根 contractVersion / OpenApiConfig / 现有 openapi.json 必须一致"
ROOT_CV="$("$NODE" -p "require('$ROOT/package.json').contractVersion" 2>/dev/null || echo '')"
JAVA_CV="$(grep -oP 'CONTRACT_VERSION\s*=\s*"\K[^"]+' "$ROOT/apps/backend/core/src/main/java/com/example/base/core/OpenApiConfig.java" || echo '')"
SNAP_CV="$("$NODE" -p "require('$CLIENT/openapi.json').info.version" 2>/dev/null || echo '')"
if ! echo "$ROOT_CV" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$'; then
  echo "❌ 根 package.json 的 contractVersion 缺失或不是合法 semver：$ROOT_CV"; exit 1
fi
if [ "$ROOT_CV" != "$JAVA_CV" ]; then
  echo "❌ 版本不一致：package.json=$ROOT_CV / OpenApiConfig=$JAVA_CV"; exit 1
fi
if [ "$ROOT_CV" != "$SNAP_CV" ]; then
  echo "❌ 版本不一致：package.json=$ROOT_CV / openapi.json=$SNAP_CV"; exit 1
fi
echo "   ✅ 三处版本对齐：$ROOT_CV"

echo "==> [2/4] 从运行中的后端导出契约"
OUT="$(mktemp --suffix=.json)"   # node require() 只按 .json 扩展名解析 JSON，无后缀会当 JS 解析失败
if ! curl -s --noproxy '*' --max-time 15 -o "$OUT" "$BASE/v3/api-docs"; then
  echo "❌ 无法连接 $BASE/v3/api-docs，请先启动后端：pnpm backend:dev"; rm -f "$OUT"; exit 1
fi
FRESH_CV="$("$NODE" -p "(()=>{try{return require('$OUT').info.version}catch(e){return ''}})()" 2>/dev/null || echo '')"
if [ "$FRESH_CV" != "$ROOT_CV" ]; then
  echo "❌ 后端导出版本 $FRESH_CV != contractVersion $ROOT_CV（改 OpenApiConfig 后需重启后端）"
  rm -f "$OUT"; exit 1
fi
cp "$OUT" "$CLIENT/openapi.json"
rm -f "$OUT"
echo "   ✅ 已写入 $CLIENT/openapi.json"

echo "==> [3/4] orval 生成类型化客户端（未安装则跳过，不阻断）"
if { [ -x "$ROOT/node_modules/.bin/orval" ] || [ -x "$CLIENT/node_modules/.bin/orval" ]; } \
   && [ -f "$CLIENT/orval.config.js" ]; then
  (cd "$CLIENT" && "$ROOT/node_modules/.bin/orval" --config ./orval.config.js 2>/dev/null \
    || ./node_modules/.bin/orval --config ./orval.config.js)
  echo "   ✅ 已生成 src/gen/"
else
  echo "   ⏭  未安装 orval 或缺少 orval.config.js；当前契约层仍是零依赖手写出口"
  echo "      前端各域 api.js 继续可用，不影响构建与运行"
fi

echo "==> [4/4] 一致性自检"
"$NODE" "$ROOT/scripts/check-features.js"

echo ""
echo "✅ 契约已同步。别忘了把 packages/api-client/ 的变更一起提交。"
