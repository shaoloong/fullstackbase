#!/usr/bin/env bash
# 后端冒烟（L2）：对运行中的后端做接口断言。
# 用法：bash scripts/backend-smoke.sh [BASE_URL]     默认 http://127.0.0.1:8082
# 前置：mvn -f apps/backend/pom.xml -q -DskipTests install -T 1C && mvn -f apps/backend/pom.xml -pl main spring-boot:run
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="${1:-http://127.0.0.1:8082}"
NODE="${NODE_BIN:-node}"
APIS_FILE="$ROOT/.fb_apis.tmp"
trap 'rm -f "$APIS_FILE"' EXIT

# 环境可能注入 HTTP 代理，本机回环请求必须绕过
curl_() { curl -s --noproxy '*' --max-time 10 "$@"; }

echo "==> 检查服务可达性：$BASE"
if ! curl_ -o /dev/null -w '%{http_code}' "$BASE/api/features" | grep -q '^200$'; then
  echo "❌ 后端未启动或不可达：$BASE"
  echo "   先运行：pnpm backend:dev（或 mvn -f apps/backend/pom.xml -pl main spring-boot:run）"
  exit 1
fi

# 用 Node 断言 JSON，比 grep 字符串可靠（字段顺序/空格变化不会误判）
assert_js() { "$NODE" -e "$1"; }

echo "==> [1/5] 功能目录 /api/features"
FEATURES_JSON="$(curl_ "$BASE/api/features")"
echo "$FEATURES_JSON" | assert_js '
let s=""; process.stdin.on("data",d=>s+=d).on("end",()=>{
  const j=JSON.parse(s);
  if(j.code!==0 || !Array.isArray(j.data) || j.data.length===0) throw new Error("功能目录为空或格式异常");
  console.log("   ✅ 已注册功能：" + j.data.map(f=>f.key).join(", "));
});'

echo "==> [2/5] 对功能目录里每个 ready 功能的列表接口做可达性自检"
# 新增功能后无需改本脚本：注册表里 ready=true 的功能会自动纳入冒烟
echo "$FEATURES_JSON" | FB_APIS_FILE="$APIS_FILE" "$NODE" -e '
let s=""; process.stdin.on("data",d=>s+=d).on("end",()=>{
  const j=JSON.parse(s);
  const ready=j.data.filter(f=>f.ready).map(f=>f.api);
  require("fs").writeFileSync(process.env.FB_APIS_FILE, ready.join("\n"));
  console.log("   ✅ 待自检接口：" + ready.join(", "));
});'
while IFS= read -r api; do
  [ -z "$api" ] && continue
  code="$(curl_ -o /dev/null -w '%{http_code}' "$BASE$api")"
  if [ "$code" != "200" ]; then echo "   ❌ $api → HTTP $code"; exit 1; fi
done < "$APIS_FILE"
rm -f "$APIS_FILE"
echo "   ✅ 全部 ready 功能的列表接口返回 200"

echo "==> [3/5] 用户列表：分页 / 总数 / 逻辑删除 / id 类型"
curl_ "$BASE/api/users?page=1&size=3" | assert_js '
let s=""; process.stdin.on("data",d=>s+=d).on("end",()=>{
  const j=JSON.parse(s), d=j.data;
  if(j.code!==0) throw new Error("业务码非 0");
  if(d.total!==23) throw new Error("期望 total=23（24 条减 1 条逻辑删除），实际 "+d.total);
  if(d.items.length!==3) throw new Error("分页 size=3 未生效");
  if(typeof d.items[0].id!=="string") throw new Error("id 必须是 String（Long 会让前端丢精度）");
  console.log("   ✅ total=23 / 分页 3 条 / id 为字符串 / 首条="+d.items[0].name);
});'

echo "==> [4/5] 条件查询：关键词 / 状态筛选"
curl_ "$BASE/api/users?keyword=%E5%BC%A0" | assert_js '
let s=""; process.stdin.on("data",d=>s+=d).on("end",()=>{
  const j=JSON.parse(s);
  if(j.data.total!==1 || !j.data.items[0].name.includes("张")) throw new Error("关键词筛选异常");
  console.log("   ✅ keyword=张 命中 1 条");
});'
curl_ "$BASE/api/users?status=0&size=100" | assert_js '
let s=""; process.stdin.on("data",d=>s+=d).on("end",()=>{
  const j=JSON.parse(s);
  if(!j.data.items.every(x=>x.status===0)) throw new Error("状态筛选串了条件");
  console.log("   ✅ status=0 命中 "+j.data.total+" 条且全为停用");
});'

echo "==> [5/5] 参数校验 / 契约文档"
curl_ "$BASE/api/users?page=0" | assert_js '
let s=""; process.stdin.on("data",d=>s+=d).on("end",()=>{
  const j=JSON.parse(s);
  if(j.code!==400) throw new Error("page=0 应返回业务码 400，实际 "+j.code);
  console.log("   ✅ page=0 → 400：" + j.message);
});'
code="$(curl_ -o /dev/null -w '%{http_code}' "$BASE/v3/api-docs")"
[ "$code" = "200" ] || { echo "   ❌ /v3/api-docs → HTTP $code"; exit 1; }
echo "   ✅ /v3/api-docs 可用（契约可导出）"

echo ""
echo "✅ L2 冒烟全部通过。功能注册表一致性另跑：node scripts/check-features.js"
