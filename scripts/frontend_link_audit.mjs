import { chromium } from 'playwright'
import fs from 'node:fs/promises'
import path from 'node:path'

const SHOP = process.env.SHOP_URL || 'http://localhost:18095'
const ADMIN = process.env.ADMIN_URL || 'http://localhost:18082'
const OUT_DIR = process.env.OUT_DIR || 'docs/course/acceptance/evidence/frontend-link-audit-20260607'

const shopPublicRoutes = [
  '/',
  '/login',
  '/register',
  '/forgot-password',
  '/search?keyword=牛肉',
  '/category/1',
  '/product/1',
  '/seckill',
  '/notices',
  '/activities',
]

const shopAuthRoutes = [
  '/cart',
  '/checkout',
  '/payment?orderNo=DEMO&id=1',
  '/orders',
  '/orders/1',
  '/profile',
  '/address',
  '/favorites',
  '/coupons',
  '/feedback',
  '/service',
]

const shopSuspectRoutes = [
  '/products',
  '/products?sort=sales_desc',
  '/products?sort=newest',
]

const adminRoutes = [
  '/',
  '/dashboard',
  '/users',
  '/categories',
  '/products',
  '/promotions',
  '/reviews',
  '/orders',
  '/banners',
  '/notices',
  '/feedbacks',
  '/cs',
  '/permission-manage',
  '/roles',
  '/admins',
  '/profile',
]

function slug(label) {
  return label.replace(/^https?:\/\//, '').replace(/[^a-z0-9]+/gi, '-').replace(/^-|-$/g, '').slice(0, 90) || 'root'
}

async function capture(page, kind, route) {
  const file = `${kind}-${slug(route)}.png`
  await page.screenshot({ path: path.join(OUT_DIR, file), fullPage: true })
  return file
}

async function visit(page, base, route, kind) {
  const failures = []
  const pageErrors = []
  const consoleErrors = []
  const onRequestFailed = (request) => failures.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`)
  const onResponse = (response) => {
    if (response.status() >= 400) failures.push(`${response.status()} ${response.url()}`)
  }
  const onPageError = (error) => pageErrors.push(error.message)
  const onConsole = (message) => {
    if (message.type() === 'error') consoleErrors.push(message.text())
  }
  page.on('requestfailed', onRequestFailed)
  page.on('response', onResponse)
  page.on('pageerror', onPageError)
  page.on('console', onConsole)
  const url = `${base}${route}`
  let status = 'PASS'
  let error = ''
  try {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 20000 })
  } catch (err) {
    status = 'FAIL'
    error = err.message
  }
  await page.waitForTimeout(350)
  const title = await page.title().catch(() => '')
  const bodyText = await page.locator('body').innerText({ timeout: 5000 }).catch(() => '')
  const bodyLength = bodyText.replace(/\s+/g, '').length
  const hasRouterHole = bodyLength < 40 && !route.includes('/login')
  if (hasRouterHole || pageErrors.length || failures.some((line) => /^5\d\d|^404/.test(line))) status = 'FAIL'
  const screenshot = await capture(page, kind, route)
  page.off('requestfailed', onRequestFailed)
  page.off('response', onResponse)
  page.off('pageerror', onPageError)
  page.off('console', onConsole)
  return {
    status,
    kind,
    route,
    finalUrl: page.url(),
    title,
    bodyLength,
    screenshot,
    error,
    failures,
    pageErrors,
    consoleErrors,
    bodyPreview: bodyText.replace(/\s+/g, ' ').trim().slice(0, 160),
  }
}

async function loginShop(page) {
  await page.goto(`${SHOP}/login`, { waitUntil: 'networkidle' })
  await page.getByPlaceholder('用户名').fill('alice')
  await page.getByPlaceholder('密码').fill('123456')
  await page.getByRole('button', { name: '登录' }).click()
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 15000 }).catch(() => {})
}

async function loginAdmin(page) {
  await page.goto(`${ADMIN}/login`, { waitUntil: 'networkidle' })
  await page.getByPlaceholder(/用户名|管理员账号/).fill('admin')
  await page.getByPlaceholder('密码').fill('admin123')
  await page.getByRole('button', { name: '登录' }).click()
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 15000 }).catch(() => {})
}

async function collectLinks(page, base, kind) {
  await page.goto(base, { waitUntil: 'networkidle' })
  await page.waitForTimeout(500)
  const links = await page.$$eval('a[href]', (anchors) => anchors.map((a) => ({
    text: a.textContent?.replace(/\s+/g, ' ').trim() || '',
    href: a.href,
  })))
  return links.filter((link) => link.href.startsWith(base)).map((link) => ({ kind, ...link }))
}

async function main() {
  await fs.mkdir(OUT_DIR, { recursive: true })
  const browser = await chromium.launch({ headless: true })
  const shopPage = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
  const adminPage = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
  const results = []

  for (const route of shopPublicRoutes) results.push(await visit(shopPage, SHOP, route, 'shop-public'))
  await loginShop(shopPage)
  for (const route of shopAuthRoutes) results.push(await visit(shopPage, SHOP, route, 'shop-auth'))
  for (const route of shopSuspectRoutes) results.push(await visit(shopPage, SHOP, route, 'shop-suspect'))

  await loginAdmin(adminPage)
  for (const route of adminRoutes) results.push(await visit(adminPage, ADMIN, route, 'admin'))

  const links = [
    ...(await collectLinks(shopPage, SHOP, 'shop')),
    ...(await collectLinks(adminPage, ADMIN, 'admin')),
  ]

  const rows = [
    '# Frontend Link Audit',
    '',
    `- Shop: ${SHOP}`,
    `- Admin: ${ADMIN}`,
    `- Generated: ${new Date().toISOString()}`,
    '',
    '## Route Results',
    '',
    '| Status | Kind | Route | Final URL | Text Len | Screenshot | Notes |',
    '| --- | --- | --- | --- | ---: | --- | --- |',
    ...results.map((r) => {
      const notes = [
        r.error && `nav: ${r.error}`,
        r.failures.length && `http: ${r.failures.slice(0, 3).join('; ')}`,
        r.pageErrors.length && `pageerror: ${r.pageErrors.slice(0, 2).join('; ')}`,
        r.bodyLength < 40 && `short body: ${r.bodyPreview}`,
      ].filter(Boolean).join('<br>')
      return `| ${r.status} | ${r.kind} | \`${r.route}\` | ${r.finalUrl} | ${r.bodyLength} | [${r.screenshot}](./${r.screenshot}) | ${notes} |`
    }),
    '',
    '## In-Page Anchors',
    '',
    '| Kind | Text | Href |',
    '| --- | --- | --- |',
    ...links.map((link) => `| ${link.kind} | ${link.text || '(empty)'} | ${link.href} |`),
    '',
  ]
  await fs.writeFile(path.join(OUT_DIR, 'README.md'), rows.join('\n'))
  await browser.close()
  const failed = results.filter((r) => r.status !== 'PASS')
  console.log(JSON.stringify({ outDir: OUT_DIR, total: results.length, failed: failed.length, failedRoutes: failed.map((r) => r.route) }, null, 2))
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
