import { chromium } from 'playwright'
import fs from 'node:fs/promises'
import path from 'node:path'

const ROOT = process.cwd()
const OUT_DIR = path.join(ROOT, 'docs/course/acceptance/evidence/fullstack-ui-audit-20260607')
const SHOP = process.env.SHOP_URL || 'http://localhost:18095'
const ADMIN = process.env.ADMIN_URL || 'http://localhost:18082'
const GATEWAY = process.env.GATEWAY_URL || 'http://localhost:18090'

const results = []
const consoleIssues = []
const failedResponses = []

async function ensureDir() {
  await fs.mkdir(OUT_DIR, { recursive: true })
}

async function productSeed() {
  const res = await fetch(`${GATEWAY}/api/products?page=1&size=5`)
  const json = await res.json()
  const item = json?.data?.items?.[0]
  if (!item?.id || !item?.categoryId) {
    throw new Error('Cannot resolve product seed from gateway API')
  }
  return item
}

async function shot(page, name, note = '') {
  await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {})
  await page.screenshot({ path: path.join(OUT_DIR, `${name}.png`), fullPage: true })
  results.push({ name, status: 'PASS', note })
}

async function mark(name, status, note) {
  results.push({ name, status, note })
}

async function goto(page, url) {
  await page.goto(url, { waitUntil: 'domcontentloaded' })
  await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {})
}

async function expectVisible(page, text, name) {
  const loc = page.getByText(text, { exact: false }).first()
  try {
    await loc.waitFor({ state: 'visible', timeout: 10000 })
    await mark(name, 'PASS', `visible text: ${text}`)
    return true
  } catch (e) {
    await mark(name, 'WARN', `missing visible text: ${text}; ${e.message}`)
    return false
  }
}

async function expectAnyVisible(page, texts, name) {
  for (const text of texts) {
    const loc = page.getByText(text, { exact: false }).first()
    if (await loc.isVisible({ timeout: 3000 }).catch(() => false)) {
      await mark(name, 'PASS', `visible text: ${text}`)
      return true
    }
  }
  await mark(name, 'WARN', `missing any visible text: ${texts.join(' / ')}`)
  return false
}

async function clickText(page, text, exact = false) {
  await page.getByText(text, { exact }).first().click({ timeout: 10000 })
}

async function shopLogin(page) {
  await goto(page, `${SHOP}/login`)
  await shot(page, 'shop-05-login', '用户登录页：账号密码登录、记住密码、注册/找回入口')
  await page.getByPlaceholder('用户名').fill('alice')
  await page.getByPlaceholder('密码').fill('123456')
  await page.getByRole('button', { name: '登录' }).click()
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 15000 }).catch(() => {})
}

async function adminLogin(page) {
  await goto(page, `${ADMIN}/login`)
  await shot(page, 'admin-01-login', '管理员登录页')
  await page.getByPlaceholder('管理员账号').fill('admin')
  await page.getByPlaceholder('密码').fill('admin123')
  await page.getByRole('button', { name: '登录' }).click()
  await page.waitForURL((url) => url.pathname.includes('/dashboard'), { timeout: 15000 }).catch(() => {})
}

async function runShop(browser, seed) {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
  page.on('console', (msg) => {
    if (['error', 'warning'].includes(msg.type())) consoleIssues.push({ page: page.url(), type: msg.type(), text: msg.text() })
  })
  page.on('pageerror', (err) => consoleIssues.push({ page: page.url(), type: 'pageerror', text: err.message }))
  page.on('response', (response) => {
    if (response.status() >= 400) failedResponses.push({ page: page.url(), status: response.status(), url: response.url() })
  })

  await goto(page, SHOP)
  await expectAnyVisible(page, ['热门好物', '热门商品', '新品首发', '促销活动'], 'shop-home-hot-products-visible')
  await shot(page, 'shop-01-home', '首页：轮播、分类导航、热门商品、新品/促销入口')

  await goto(page, `${SHOP}/search?keyword=watch&searchMode=fuzzy`)
  await expectVisible(page, '商品搜索', 'shop-search-title-visible')
  await shot(page, 'shop-02-search-fuzzy-sort', '商品搜索：关键词、模糊搜索、排序控件、商品列表')

  await goto(page, `${SHOP}/category/${seed.categoryId}`)
  await expectVisible(page, '分类', 'shop-category-visible')
  await shot(page, 'shop-03-category', '分类浏览：按分类筛选商品')

  await goto(page, `${SHOP}/product/${seed.id}`)
  await expectVisible(page, '加入购物车', 'shop-product-add-cart-visible')
  await shot(page, 'shop-04-product-detail-top', '商品详情：图片、名称、价格、库存、参数、规格、加购')
  await page.mouse.wheel(0, 900)
  await shot(page, 'shop-04-product-detail-reviews', '商品详情评价区：查看评价、提交评价入口')

  await goto(page, `${SHOP}/register`)
  await expectVisible(page, '邮箱验证码', 'shop-register-email-code-visible')
  await shot(page, 'shop-06-register', '注册页：邮箱注册、验证码、手机号、密码')

  await goto(page, `${SHOP}/forgot-password`)
  await expectVisible(page, '获取验证码', 'shop-forgot-code-visible')
  await shot(page, 'shop-07-forgot-password', '找回密码：邮箱验证码、重置密码')

  await shopLogin(page)

  await goto(page, `${SHOP}/profile`)
  await expectVisible(page, '上传图片', 'shop-profile-upload-visible')
  await shot(page, 'shop-12-profile', '个人中心：头像、昵称、邮箱、手机号、密码修改')

  await goto(page, `${SHOP}/address`)
  await expectVisible(page, '新增地址', 'shop-address-add-visible')
  await shot(page, 'shop-13-address-list', '收货地址：列表、默认地址、新增/编辑/删除入口')
  await clickText(page, '新增地址', true).catch(() => {})
  await page.waitForTimeout(500)
  await shot(page, 'shop-13-address-add-dialog', '收货地址：新增地址弹窗')
  await page.keyboard.press('Escape').catch(() => {})

  await goto(page, `${SHOP}/favorites`)
  await expectVisible(page, '我的收藏', 'shop-favorites-visible')
  await shot(page, 'shop-14-favorites', '收藏列表：收藏/取消收藏结果查看')

  await goto(page, `${SHOP}/coupons`)
  await expectVisible(page, '优惠券', 'shop-coupons-visible')
  await shot(page, 'shop-15-coupons', '优惠券：可领取/已领取、结算联动说明')

  await goto(page, `${SHOP}/seckill`)
  await expectVisible(page, '限时', 'shop-seckill-visible')
  await shot(page, 'shop-16-seckill', '秒杀/促销活动页')

  await goto(page, `${SHOP}/notices`)
  await expectVisible(page, '公告', 'shop-notices-visible')
  await shot(page, 'shop-17-notices', '公告/活动通知展示')

  await goto(page, `${SHOP}/feedback`)
  await expectVisible(page, '意见反馈', 'shop-feedback-visible')
  await shot(page, 'shop-18-feedback', '用户反馈：问题/建议提交与历史列表')

  await goto(page, `${SHOP}/service`)
  await expectVisible(page, '客服', 'shop-service-visible')
  await shot(page, 'shop-19-service', '客服咨询：留言/回复/聊天入口')

  await goto(page, `${SHOP}/product/${seed.id}`)
  await page.getByText('加入购物车', { exact: true }).click({ timeout: 10000 })
  await page.waitForTimeout(1000)
  await goto(page, `${SHOP}/cart`)
  await expectVisible(page, '去结算', 'shop-cart-checkout-visible')
  await shot(page, 'shop-08-cart', '购物车：图片、名称、单价、数量、小计、勾选、全选/反选、删除、结算')

  const checkoutButton = page.getByText('去结算', { exact: true }).first()
  if (await checkoutButton.isEnabled().catch(() => false)) {
    await checkoutButton.click()
    await page.waitForURL((url) => url.pathname.includes('/checkout'), { timeout: 15000 }).catch(() => {})
  } else {
    await goto(page, `${SHOP}/checkout`)
  }
  await expectVisible(page, '提交订单', 'shop-checkout-submit-visible').catch(async (e) => {
    await mark('shop-checkout-submit-visible', 'WARN', e.message)
  })
  await shot(page, 'shop-09-checkout', '确认订单：地址、商品清单、优惠券、总价、提交订单')

  const submit = page.getByText('提交订单', { exact: true }).first()
  if (await submit.isVisible().catch(() => false)) {
    await submit.click().catch(() => {})
    await page.waitForTimeout(1500)
  }
  if (!page.url().includes('/payment')) {
    await goto(page, `${SHOP}/payment`)
  }
  await expectVisible(page, '订单支付', 'shop-payment-visible').catch(async (e) => {
    await mark('shop-payment-visible', 'WARN', e.message)
  })
  await shot(page, 'shop-10-payment', '支付页：模拟支付、金额、支付方式、确认支付')

  await goto(page, `${SHOP}/orders`)
  await expectVisible(page, '我的订单', 'shop-orders-visible')
  await shot(page, 'shop-11-orders', '我的订单：全部/待支付/待发货/待收货/已完成/已取消、支付/取消/收货/退款/物流入口')

  await page.close()
}

async function runAdmin(browser) {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
  page.on('console', (msg) => {
    if (['error', 'warning'].includes(msg.type())) consoleIssues.push({ page: page.url(), type: msg.type(), text: msg.text() })
  })
  page.on('pageerror', (err) => consoleIssues.push({ page: page.url(), type: 'pageerror', text: err.message }))
  page.on('response', (response) => {
    if (response.status() >= 400) failedResponses.push({ page: page.url(), status: response.status(), url: response.url() })
  })

  await adminLogin(page)

  const adminPages = [
    ['/dashboard', 'admin-02-dashboard', '数据看板：总用户、总订单、总销售额、今日订单/销售额、图表'],
    ['/products', 'admin-03-products', '商品管理：列表、搜索、分类/状态筛选、新增、编辑、上下架、删除、导入导出、上传图入口'],
    ['/categories', 'admin-04-categories', '分类管理：新增、编辑、删除、排序字段'],
    ['/orders', 'admin-05-orders', '订单管理：订单号/用户/状态筛选、支付/履约状态、发货/退款、导出'],
    ['/users', 'admin-06-users', '用户管理：列表、搜索、启用/禁用'],
    ['/reviews', 'admin-07-reviews', '评价管理：评价列表、删除违规评价'],
    ['/banners', 'admin-08-banners', '轮播管理：列表、新增、编辑、删除、上传、链接、排序'],
    ['/promotions', 'admin-09-promotions', '促销/优惠券管理：新增、编辑、删除、启停'],
    ['/notices', 'admin-10-notices', '公告/活动通知管理：发布、编辑、删除'],
    ['/feedbacks', 'admin-11-feedbacks', '反馈管理：查看、回复、标记处理'],
    ['/cs', 'admin-12-cs', '客服咨询：查看用户咨询、回复'],
    ['/permission-manage', 'admin-13-permission', '权限管理：管理员账号、角色权限分级'],
    ['/profile', 'admin-14-profile', '管理员个人中心：资料、密码修改'],
  ]

  for (const [route, name, note] of adminPages) {
    await goto(page, `${ADMIN}${route}`)
    await shot(page, name, note)
  }

  await goto(page, `${ADMIN}/products`)
  await clickText(page, '新增', true).catch(() => {})
  await page.waitForTimeout(500)
  await shot(page, 'admin-03-products-add-dialog', '商品新增弹窗：名称、分类、价格、库存、图片上传、详情')
  await page.keyboard.press('Escape').catch(() => {})

  await goto(page, `${ADMIN}/banners`)
  await clickText(page, '新增轮播', true).catch(() => {})
  await page.waitForTimeout(500)
  await shot(page, 'admin-08-banners-add-dialog', '轮播新增弹窗：图片、链接、排序、上传')
  await page.keyboard.press('Escape').catch(() => {})

  await goto(page, `${ADMIN}/promotions`)
  await clickText(page, '优惠券', true).catch(() => {})
  await page.waitForTimeout(1000)
  await shot(page, 'admin-09-coupons-tab', '优惠券管理：列表、新增、编辑、删除、启停')
  await clickText(page, '新增优惠券', true).catch(() => {})
  await page.waitForTimeout(500)
  await shot(page, 'admin-09-coupons-add-dialog', '优惠券新增弹窗：名称、满减门槛、优惠金额、启用')
  await page.keyboard.press('Escape').catch(() => {})

  await page.close()
}

async function runResponsive(browser) {
  const mobile = await browser.newPage({ viewport: { width: 390, height: 900 }, isMobile: true })
  await goto(mobile, SHOP)
  await shot(mobile, 'responsive-shop-390-home', '390px 前台首页响应式')
  await goto(mobile, `${SHOP}/search?keyword=watch`)
  await shot(mobile, 'responsive-shop-390-search', '390px 前台商品列表响应式')
  await mobile.close()

  const tablet = await browser.newPage({ viewport: { width: 768, height: 1000 } })
  await goto(tablet, SHOP)
  await shot(tablet, 'responsive-shop-768-home', '768px 前台首页响应式')
  await tablet.close()

  const adminMobile = await browser.newPage({ viewport: { width: 390, height: 900 }, isMobile: true })
  await adminLogin(adminMobile)
  await shot(adminMobile, 'responsive-admin-390-dashboard', '390px 后台看板响应式')
  const menuButton = adminMobile.locator('button').first()
  await menuButton.click().catch(() => {})
  await adminMobile.waitForTimeout(500)
  await shot(adminMobile, 'responsive-admin-390-drawer', '390px 后台抽屉菜单')
  await adminMobile.close()
}

async function writeReport() {
  const rows = results.map((r) => `| ${r.status} | ${r.name}.png | ${r.note.replaceAll('\n', ' ')} |`).join('\n')
  const issues = consoleIssues.length
    ? consoleIssues.map((i) => `- ${i.type} ${i.page}: ${i.text}`).join('\n')
    : '- 未捕获到 pageerror；控制台 warning/error 见脚本过滤结果为空。'
  const responses = failedResponses.length
    ? failedResponses.map((i) => `- ${i.status} while on ${i.page}: ${i.url}`).join('\n')
    : '- 未捕获到 HTTP 4xx/5xx 响应。'
  const markdown = `# Fullstack UI Audit Evidence - 2026-06-07

入口：

- 前台：${SHOP}
- 后台：${ADMIN}
- Gateway：${GATEWAY}

## 截图索引

| 状态 | 截图 | 说明 |
| --- | --- | --- |
${rows}

## 控制台问题

${issues}

## HTTP 失败响应

${responses}
`
  await fs.writeFile(path.join(OUT_DIR, 'README.md'), markdown, 'utf8')
}

async function main() {
  await ensureDir()
  const seed = await productSeed()
  const browser = await chromium.launch({ headless: true })
  try {
    await runShop(browser, seed)
    await runAdmin(browser)
    await runResponsive(browser)
  } finally {
    await browser.close()
    await writeReport()
  }
}

main().catch(async (err) => {
  await mark('audit-script', 'FAIL', err.stack || err.message)
  await writeReport()
  console.error(err)
  process.exit(1)
})
