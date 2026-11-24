#!/usr/bin/env node
/**
 * 新增功能生成器 —— 本工程的「扩展入口」
 * ============================================================
 * 一条命令把「后端模块 + 前端功能域 + 各处登记 + 建表脚本」全部生成好，
 * 避免新增功能时漏改 POM / 注册表 / 路由这类不会立刻报错、上线才 404 的地方。
 *
 * 用法：
 *   node scripts/new-module.js --name order --title 订单
 *   node scripts/new-module.js --name order --title 订单 --placeholder   # 只占位，菜单显示「待实现」
 *   node scripts/new-module.js --name order --title 订单 --dry-run        # 只打印计划，不落盘
 *
 * 零外部依赖：只用 Node 内置模块，pnpm/npm 装不上也能跑。
 */
'use strict'

const fs = require('fs')
const path = require('path')

const ROOT = path.resolve(__dirname, '..')

// ---------- 参数解析 ----------
function parseArgs(argv) {
  const out = { name: '', title: '', dryRun: false, placeholder: false }
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i]
    if (a === '--dry-run') out.dryRun = true
    else if (a === '--placeholder') out.placeholder = true
    else if (a === '--name') out.name = argv[++i] || ''
    else if (a === '--title') out.title = argv[++i] || ''
    else if (a === '--help' || a === '-h') out.help = true
  }
  return out
}

function usage() {
  return [
    '用法：node scripts/new-module.js --name <小写域名> --title <中文名> [选项]',
    '',
    '  --name order        必填，小写字母/数字，作为包名、目录名、注册表 key（如 order）',
    '  --title 订单        可选，菜单显示名，缺省用 --name',
    '  --placeholder       可选，注册表 ready=false（只占位不实现），缺省生成可运行骨架',
    '  --dry-run           可选，只打印将要创建/修改的文件，不写盘',
    '',
    '示例：node scripts/new-module.js --name order --title 订单',
    '      node scripts/new-module.js --name order --title 订单 --dry-run'
  ].join('\n')
}

// ---------- 占位符 ----------
function derive(name, title) {
  const plural = name.endsWith('s') ? name + 'es' : name + 's'   // 够用即可，需要不规则复数可手工改
  return {
    __domain__: name,
    __Domain__: name.charAt(0).toUpperCase() + name.slice(1),
    __DOMAIN__: name.toUpperCase(),
    __title__: title || name,
    __table__: 't_' + name,
    __api__: '/api/' + plural,
    __relapi__: '/' + plural,
    __path__: '/' + plural,
    __version__: ''   // 版本号单独计算，见 nextVersion()
  }
}

function render(text, vars) {
  // 逐个 split/join，避免正则转义；占位符互不重叠
  let out = text
  for (const [k, v] of Object.entries(vars)) out = out.split(k).join(v)
  return out
}

function renderPath(rel, vars) {
  return render(rel, vars).replace(/\.tpl$/, '')
}

// ---------- 迁移版本号 ----------
function nextVersion(migrationDir) {
  const names = fs.existsSync(migrationDir) ? fs.readdirSync(migrationDir) : []
  const versions = names
    .map((n) => /^V(\d+)__/.exec(n))
    .filter(Boolean)
    .map((m) => Number(m[1]))
  return (versions.length ? Math.max(...versions) : 0) + 1
}

// ---------- 登记点：锚点前插一行 ----------
function insertBefore(rel, anchor, addition, dryRun, edits) {
  const file = path.join(ROOT, rel)
  if (!fs.existsSync(file)) throw new Error('登记文件不存在：' + rel)
  const text = fs.readFileSync(file, 'utf8')
  const lines = text.split('\n')
  const idx = lines.findIndex((l) => l.includes(anchor))
  if (idx < 0) throw new Error('锚点缺失：' + rel + ' :: ' + anchor)
  // 按「整行」插入：复用锚点行的缩进，addition 只写相对缩进，
  // 避免「锚点原缩进 + 内容自带缩进」叠加成双倍空格
  const indent = (lines[idx].match(/^\s*/) || [''])[0]
  const addLines = addition.split('\n').map((l) => indent + l)
  lines.splice(idx, 0, ...addLines)
  const next = lines.join('\n')
  edits.push({ file: rel, before: text, after: next })
  if (!dryRun) fs.writeFileSync(file, next, 'utf8')
}

// ---------- 递归复制模板目录 ----------
function copyTemplateDir(srcRel, destRel, vars, dryRun, creates, opts = {}) {
  const srcDir = path.join(ROOT, srcRel)
  const skip = opts.skip || []
  const walk = (cur, relPrefix) => {
    for (const entry of fs.readdirSync(cur, { withFileTypes: true })) {
      const rel = relPrefix ? relPrefix + '/' + entry.name : entry.name
      if (skip.some((s) => rel === s || rel.startsWith(s + '/'))) continue
      const full = path.join(cur, entry.name)
      if (entry.isDirectory()) {
        walk(full, rel)
      } else {
        const outRel = destRel + '/' + renderPath(rel, vars)
        const content = render(fs.readFileSync(full, 'utf8'), vars)
        creates.push({ file: outRel, content })
        if (!dryRun) {
          const abs = path.join(ROOT, outRel)
          fs.mkdirSync(path.dirname(abs), { recursive: true })
          fs.writeFileSync(abs, content, 'utf8')
        }
      }
    }
  }
  walk(srcDir, '')
}

// ---------- 主流程 ----------
function main() {
  const args = parseArgs(process.argv.slice(2))
  if (args.help) { console.log(usage()); return }
  if (!args.name) { console.error('❌ 缺少 --name\n\n' + usage()); process.exit(1) }
  if (!/^[a-z][a-z0-9]*$/.test(args.name)) {
    console.error('❌ --name 必须是小写字母开头的小写字母/数字（会作为 Java 包名）：' + args.name)
    process.exit(1)
  }

  const name = args.name
  const vars = derive(name, args.title)
  const ready = !args.placeholder
  const version = nextVersion(path.join(ROOT, 'apps/backend/main/src/main/resources/db/migration'))
  vars.__version__ = String(version)

  const moduleDir = 'apps/backend/modules/' + name
  const featureDir = 'apps/frontend/src/features/' + name
  if (fs.existsSync(path.join(ROOT, moduleDir)) || fs.existsSync(path.join(ROOT, featureDir))) {
    console.error('❌ 目标已存在：' + moduleDir + ' 或 ' + featureDir + '（不覆盖已有代码）')
    process.exit(1)
  }

  const creates = []
  const edits = []

  // ① 后端业务模块（跳过模板 README 与 src/test：测试要放到 main 模块才能看到启动类）
  copyTemplateDir('templates/backend-module', moduleDir, vars, args.dryRun, creates,
    { skip: ['README.md', 'src/test'] })

  // ② 集成测试：放 main/src/test，因为业务模块 classpath 里没有 @SpringBootConfiguration
  const testTpl = 'templates/backend-module/src/test/java/com/example/base/modules/__domain__/__Domain__ServiceTest.java.tpl'
  const testRel = `apps/backend/main/src/test/java/com/example/base/modules/${name}/${vars.__Domain__}ServiceTest.java`
  creates.push({
    file: testRel,
    content: render(fs.readFileSync(path.join(ROOT, testTpl), 'utf8'), vars)
  })

  // ③ 前端功能域（跳过模板 README）
  copyTemplateDir('templates/frontend-feature', featureDir, vars, args.dryRun, creates,
    { skip: ['README.md'] })

  // ④ Flyway 建表脚本：版本号自动取下一个，文件名由生成器构造
  const sqlRel = `apps/backend/main/src/main/resources/db/migration/V${version}__${name}.sql`
  creates.push({
    file: sqlRel,
    content: render(fs.readFileSync(path.join(ROOT, 'templates/migration/V__tpl.sql'), 'utf8'), vars)
  })

  // ⑤ 四处登记（漏一处就编译不过或接口 404）
  insertBefore('apps/backend/pom.xml', '<!-- @NEW_MODULE -->',
    `<module>modules/${name}</module>`, args.dryRun, edits)

  insertBefore('apps/backend/pom.xml', '<!-- @NEW_DEPENDENCY -->',
    `<dependency>\n` +
    `  <groupId>com.example.base</groupId>\n` +
    `  <artifactId>base-module-${name}</artifactId>\n` +
    `  <version>\${project.version}</version>\n` +
    `</dependency>`, args.dryRun, edits)

  insertBefore('apps/backend/main/pom.xml', '<!-- @NEW_DEPENDENCY -->',
    `<dependency>\n` +
    `  <groupId>com.example.base</groupId>\n` +
    `  <artifactId>base-module-${name}</artifactId>\n` +
    `</dependency>`, args.dryRun, edits)

  insertBefore('apps/backend/core/src/main/java/com/example/base/core/FeatureRegistry.java',
    '// @NEW_FEATURE',
    `FEATURES.add(new Feature("${name}", "${vars.__title__}", "${vars.__path__}", ` +
    `"${vars.__api__}", "🧩", ${ready}));`, args.dryRun, edits)

  insertBefore('apps/frontend/src/config/features.js', '// @NEW_FEATURE',
    `registry.push({\n` +
    `  key: '${name}',\n` +
    `  title: '${vars.__title__}',\n` +
    `  path: '${vars.__path__}',\n` +
    `  icon: '🧩',\n` +
    `  ready: ${ready},\n` +
    `  component: () => import('../features/${name}/Page.vue')\n` +
    `})`, args.dryRun, edits)

  insertBefore('packages/api-client/src/index.js', '// @NEW_EXPORT',
    `// ${vars.__title__} 域接口：生成契约后改为 export * from './gen/${name}/${name}'`,
    args.dryRun, edits)

  // 落盘（dry-run 只收集不写）
  if (!args.dryRun) {
    for (const c of creates) {
      const abs = path.join(ROOT, c.file)
      fs.mkdirSync(path.dirname(abs), { recursive: true })
      fs.writeFileSync(abs, c.content, 'utf8')
    }
  }

  // ---------- 输出计划 ----------
  console.log(`${args.dryRun ? '🔍 DRY-RUN（未写盘）' : '✅ 已生成'}：${name} / ${vars.__title__}`)
  console.log(`   后端接口 ${vars.__api__} | 前端路由 ${vars.__path__} | 表 ${vars.__table__} | 迁移 V${version}`)
  console.log(`\n📄 新建文件 ${creates.length} 个：`)
  creates.forEach((c) => console.log('   + ' + c.file))
  console.log(`\n✏️  修改登记 ${edits.length} 处：`)
  edits.forEach((e) => console.log('   ~ ' + e.file))

  console.log(`
📌 生成后必做（按 docs/extend-guide.md）：
   1. 改字段：entity / VO / Query / ServiceImpl 的 toVO 与条件构造器
   2. 补建表脚本列与索引：${sqlRel}
   3. 自证：mvn -f apps/backend/pom.xml -q test  →  bash scripts/backend-smoke.sh
   4. 改了接口：bash scripts/gen-api.sh（重新生成契约与前端类型）
   5. 一致性：node scripts/check-features.js
${args.placeholder ? '   6. 当前为占位模式（ready=false），实现完把两处注册表的 ready 改成 true\n' : ''}`)
}

try {
  main()
} catch (e) {
  console.error('❌ 生成失败：' + e.message)
  process.exit(1)
}
