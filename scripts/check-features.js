#!/usr/bin/env node
/**
 * 前后端功能注册表一致性校验（零依赖）
 * ============================================================
 * 新增功能最容易漏的就是「后端加了、前端没加」或反过来 —— 两边都能编译，
 * 但菜单点了 404 / 后端接口没人调。本脚本把 key 集合、重复项、路径格式一次查完。
 *
 * 用法：node scripts/check-features.js
 */
'use strict'

const fs = require('fs')
const path = require('path')
const ROOT = path.resolve(__dirname, '..')

const fePath = path.join(ROOT, 'apps/frontend/src/config/features.js')
const bePath = path.join(ROOT, 'apps/backend/core/src/main/java/com/example/base/core/FeatureRegistry.java')

const fe = fs.readFileSync(fePath, 'utf8')
const be = fs.readFileSync(bePath, 'utf8')

// 前端：registry 数组项与 registry.push(...) 两种写法都收
const local = [...fe.matchAll(/key:\s*'([^']+)'/g)].map((m) => m[1])
// 后端：new Feature("key", ...)
const remote = [...be.matchAll(/new\s+Feature\("([^"]+)"/g)].map((m) => m[1])

const errors = []
const dup = (arr) => arr.filter((k, i) => arr.indexOf(k) !== i)
if (dup(local).length) errors.push(`前端注册表有重复 key：${[...new Set(dup(local))].join(', ')}`)
if (dup(remote).length) errors.push(`后端注册表有重复 key：${[...new Set(dup(remote))].join(', ')}`)
if (!local.length) errors.push('前端注册表为空（或 key 写法不匹配）')
if (!remote.length) errors.push('后端注册表为空（或 new Feature 写法不匹配）')

const sorted = (a) => [...a].sort().join(',')
if (sorted(local) !== sorted(remote)) {
  errors.push(`前后端 key 集合不一致：前端 [${sorted(local)}] / 后端 [${sorted(remote)}]`)
}
for (const line of fe.matchAll(/path:\s*'([^']+)'/g)) {
  if (!line[1].startsWith('/')) errors.push(`前端路由必须以 / 开头：${line[1]}`)
}
for (const line of be.matchAll(/new\s+Feature\("[^"]+",\s*"[^"]*",\s*"([^"]+)",\s*"([^"]+)"/g)) {
  if (!line[2].startsWith('/api/')) errors.push(`后端接口前缀必须以 /api/ 开头：${line[2]}`)
}

// 锚点守护：生成器依赖这些锚点，删了以后新增功能会静默漏登记
const anchors = [
  ['apps/backend/pom.xml', '<!-- @NEW_MODULE -->'],
  ['apps/backend/pom.xml', '<!-- @NEW_DEPENDENCY -->'],
  ['apps/backend/main/pom.xml', '<!-- @NEW_DEPENDENCY -->'],
  ['apps/frontend/src/config/features.js', '// @NEW_FEATURE'],
  ['apps/backend/core/src/main/java/com/example/base/core/FeatureRegistry.java', '// @NEW_FEATURE'],
  ['packages/api-client/src/index.js', '// @NEW_EXPORT']
]
for (const [file, anchor] of anchors) {
  if (!fs.readFileSync(path.join(ROOT, file), 'utf8').includes(anchor)) {
    errors.push(`扩展锚点被删除：${file} :: ${anchor}`)
  }
}

if (errors.length) {
  console.error('❌ 功能注册表校验失败：')
  errors.forEach((e) => console.error('   - ' + e))
  process.exit(1)
}

console.log(`✅ 功能注册表一致（${local.length} 个）：${sorted(local)}`)
console.log('✅ 6 处扩展锚点完好，node scripts/new-module.js 可正常生成新功能')
