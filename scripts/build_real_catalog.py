import csv
import io
import json
import re
import textwrap
import urllib.request
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE_URL = "https://raw.githubusercontent.com/luminati-io/eCommerce-dataset-samples/main/amazon-products.csv"
CATALOG_DIR = ROOT / "frontend" / "shop-web" / "public" / "catalog"
DATA_DIR = ROOT / ".refs" / "real_catalog"
SQL_PATH = ROOT / "backend" / "legacy-web" / "src" / "main" / "resources" / "data.sql"
JSON_PATH = DATA_DIR / "real_catalog.json"
PROVENANCE_PATH = ROOT / "docs" / "archive" / "DATA_PROVENANCE.md"


CATEGORY_RULES = [
    ("输入设备", ["keyboard", "mouse", "mouse pad"]),
    ("音视频", ["headset", "speaker", "microphone", "headphones"]),
    ("存储网络", ["hard drive", "ssd", "router", "ethernet cable", "hdmi cable", "kvm switch", "usb c", "usb-c"]),
    ("桌面办公", ["desk pad", "stapler", "binder", "ballpoint pen", "desktop organizer", "table lamp", "toner", "printer", "lamp", "monitor stand", "fan"]),
]

EXCLUDE_TERMS = [
    "pelvic",
    "iphone",
    "macbook case",
    "door",
    "hair",
    "shoe",
    "vest",
    "exercise",
    "headlamp",
]

TARGET_PER_CATEGORY = {
    "输入设备": 10,
    "音视频": 8,
    "存储网络": 10,
    "桌面办公": 12,
}

PRODUCT_ALLOWLIST = [
    ("B076LRJ528", "输入设备"),
    ("B08F2Z6RJB", "输入设备"),
    ("B0B572FFKQ", "输入设备"),
    ("B0036DDT5G", "输入设备"),
    ("B07W58LMND", "输入设备"),
    ("B07S8QPD3S", "输入设备"),
    ("B0CQTMGXHD", "输入设备"),
    ("B0BK8QY1JB", "音视频"),
    ("B09Q2R6FSP", "音视频"),
    ("B089K5PVBF", "音视频"),
    ("B094TJ5FBF", "音视频"),
    ("B07WWN97S7", "音视频"),
    ("B01J4C2O4Y", "音视频"),
    ("B004YCUDMU", "存储网络"),
    ("B08B3HYC45", "存储网络"),
    ("B092R6HW7L", "存储网络"),
    ("B00XIG5IV4", "存储网络"),
    ("B0B7QC7HP1", "存储网络"),
    ("B088CTW44F", "存储网络"),
    ("B09NBLVKXJ", "存储网络"),
    ("B00I28F9UI", "桌面办公"),
    ("B091Y7B6N2", "桌面办公"),
    ("B09QM2BMPG", "桌面办公"),
    ("B016MGVQ6C", "桌面办公"),
    ("B002XK2OW2", "桌面办公"),
    ("B00INKVS82", "桌面办公"),
    ("B01EZ9WR34", "桌面办公"),
    ("B09MDNKLM7", "桌面办公"),
    ("B00G7QRJKK", "桌面办公"),
    ("B00JPBZOJ2", "桌面办公"),
]


def fetch_csv():
    csv.field_size_limit(10_000_000)
    text = urllib.request.urlopen(SOURCE_URL, timeout=60).read().decode("utf-8-sig")
    return list(csv.DictReader(io.StringIO(text)))


def clean_price(value):
    if not value or value == "null":
        return None
    value = value.strip().strip('"')
    try:
        return round(float(value), 2)
    except ValueError:
        return None


def clean_text(value):
    if not value or value == "null":
        return ""
    return re.sub(r"\s+", " ", value).strip()


def classify(row):
    title = row["title"].lower()
    if any(term in title for term in EXCLUDE_TERMS):
        return None
    for category, terms in CATEGORY_RULES:
        if any(term in title for term in terms):
            return category
    return None


def ext_from_content_type(content_type):
    if content_type == "image/png":
        return ".png"
    if content_type == "image/webp":
        return ".webp"
    return ".jpg"


def download_image(url, asin):
    existing = list(CATALOG_DIR.glob(f"{asin}.*"))
    if existing:
        return f"/catalog/{existing[0].name}"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        content_type = resp.headers.get_content_type()
        if not content_type.startswith("image/"):
            raise ValueError(f"not an image: {content_type}")
        data = resp.read()
    ext = ext_from_content_type(content_type)
    path = CATALOG_DIR / f"{asin}{ext}"
    path.write_bytes(data)
    return f"/catalog/{path.name}"


def pick_products(rows):
    by_asin = {row.get("asin"): row for row in rows}
    picked = []
    for asin, category in PRODUCT_ALLOWLIST:
        row = by_asin.get(asin)
        if not row:
            print(f"missing row: {asin}")
            continue
        try:
            image_path = download_image(row["image_url"], row["asin"])
        except Exception as exc:
            print(f"image download failed: {asin} {exc}")
            continue
        picked.append({
            "source_dataset": "luminati-io/eCommerce-dataset-samples amazon-products.csv",
            "source_url": SOURCE_URL,
            "asin": row["asin"],
            "category": category,
            "title": clean_text(row["title"]),
            "description": clean_text(row.get("description")),
            "price": clean_price(row.get("final_price")),
            "brand": clean_text(row.get("brand")),
            "manufacturer": clean_text(row.get("manufacturer")),
            "model_number": clean_text(row.get("model_number")),
            "rating": clean_text(row.get("rating")),
            "top_review": clean_text(row.get("top_review")),
            "image_source_url": row["image_url"],
            "image_path": image_path,
        })
    return picked


def sql_escape(value):
    return value.replace("\\", "\\\\").replace("'", "''")


def short_description(product):
    description = product["description"] or "暂无公开描述"
    return textwrap.shorten(description, width=420, placeholder="...")


def short_title(product):
    return textwrap.shorten(product["title"], width=120, placeholder="...")


def params_text(product):
    parts = []
    for label, key in [("品牌", "brand"), ("型号", "model_number"), ("制造商", "manufacturer")]:
        if product[key]:
            parts.append(f"{label}: {product[key]}")
    return "; ".join(parts) if parts else "暂无公开参数"


def product_specs(product):
    specs = []
    if product["brand"]:
        specs.append(("品牌", product["brand"]))
    if product["model_number"]:
        specs.append(("型号", product["model_number"]))
    specs.append(("来源", "公开商品样本"))
    return specs


def render_sql(products):
    category_ids = {
        "输入设备": 2,
        "音视频": 4,
        "存储网络": 6,
        "桌面办公": 8,
    }
    lines = [
        "SET NAMES utf8mb4;",
        "USE ecommerce_minimal;",
        "",
        "INSERT INTO user (username, password, nickname, email, phone, avatar_url, enabled) VALUES",
        "('alice', '123456', 'Alice', 'alice@example.com', '13800000000', '/catalog/avatar-alice.svg', 1);",
        "",
        "INSERT INTO admin_user (username, password, nickname, email, phone) VALUES",
        "('admin', 'admin123', '系统管理员', 'admin@example.com', '13900000000');",
        "",
        "INSERT INTO product_category (parent_id, name, sort_order) VALUES",
        "(NULL, '数码办公', 1),",
        "(1, '输入设备', 1),",
        "(NULL, '影音与会议', 2),",
        "(3, '音视频', 1),",
        "(NULL, '扩展与连接', 3),",
        "(5, '存储网络', 1),",
        "(NULL, '桌面效率', 4),",
        "(7, '桌面办公', 1);",
        "",
    ]

    hero = products[:2]
    lines += [
        "INSERT INTO banner (title, image_url, link_url, sort_order) VALUES",
        f"('真实商品精选', '{hero[0]['image_path']}', '/products/{1}', 1),",
        f"('办公桌面推荐', '{hero[1]['image_path']}', '/products/{2}', 2);",
        "",
        "INSERT INTO announcement (title, content, created_at) VALUES",
        "('真实商品目录已更新', '当前目录使用公开商品样本整理，商品图与商品信息均可追溯来源。', NOW());",
        "",
        "INSERT INTO product (category_id, name, price, stock, sales, is_on_sale, image_url, detail_html, params_text) VALUES",
    ]
    product_rows = []
    for i, p in enumerate(products, start=1):
        product_rows.append(
            f"({category_ids[p['category']]}, '{sql_escape(short_title(p))}', {p['price']:.2f}, {30 + (i % 17)}, {5 + (i * 3 % 40)}, 1, "
            f"'{p['image_path']}', '{sql_escape(short_description(p))}', '{sql_escape(params_text(p))}')"
        )
    lines.append(",\n".join(product_rows) + ";")
    lines.append("")

    spec_rows = []
    for i, p in enumerate(products, start=1):
        for name, value in product_specs(p):
            spec_rows.append(f"({i}, '{sql_escape(name)}', '{sql_escape(value)}')")
    lines += [
        "INSERT INTO product_spec (product_id, spec_name, spec_value) VALUES",
        ",\n".join(spec_rows) + ";",
        "",
        "INSERT INTO coupon (name, threshold_amount, discount_amount, enabled) VALUES",
        "('满300减30', 300.00, 30.00, 1),",
        "('满500减80', 500.00, 80.00, 1);",
        "",
        "INSERT INTO user_coupon (user_id, coupon_id, status, claimed_at) VALUES",
        "(1, 1, 'UNUSED', NOW());",
        "",
        "INSERT INTO promotion (product_id, title, promotion_type, promotion_price, promotion_stock, start_at, end_at, enabled) VALUES",
        f"(1, '限时优惠', 'FLASH_SALE', {max(products[0]['price'] - 10, 1):.2f}, 10, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),",
        f"(2, '精选直降', 'PROMOTION', {max(products[1]['price'] - 5, 1):.2f}, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);",
        "",
        "INSERT INTO user_address (user_id, receiver_name, phone, province, city, district, detail_address, is_default) VALUES",
        "(1, 'Alice', '13800000000', '辽宁省', '沈阳市', '和平区', '创新路 1 号', 1);",
        "",
    ]

    review_rows = []
    for i, p in enumerate(products, start=1):
        if p["top_review"]:
            rating = int(round(float(p["rating"]))) if p["rating"] else 5
            review = textwrap.shorten(p["top_review"], width=480, placeholder="...")
            review_rows.append(f"(1, {i}, {rating}, '{sql_escape(review)}', '', NOW())")
    if review_rows:
        lines += [
            "INSERT INTO product_review (user_id, product_id, rating, content, image_url, created_at) VALUES",
            ",\n".join(review_rows) + ";",
            "",
        ]
    return "\n".join(lines)


def write_avatar():
    (CATALOG_DIR / "avatar-alice.svg").write_text(
        '<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120"><rect width="120" height="120" rx="60" fill="#dbeafe"/><text x="60" y="73" text-anchor="middle" font-size="52" fill="#1e3a8a" font-family="Arial">A</text></svg>',
        encoding="utf-8",
    )


def write_provenance(products):
    lines = [
        "# Data Provenance",
        "",
        f"- Generated at: {datetime.now().isoformat(timespec='seconds')}",
        "- Product source: `luminati-io/eCommerce-dataset-samples` → `amazon-products.csv`",
        "- Image handling: downloaded locally from each row's `image_url` field",
        "- Review handling: only `top_review` text from the same source row is attached to that exact product",
        "- Operational values such as stock, sales, coupons, and promotions remain demo data for coursework flow testing",
        "",
        "| Local ID | ASIN | Category | Title | Local Image | Source Image |",
        "| --- | --- | --- | --- | --- | --- |",
    ]
    for i, p in enumerate(products, start=1):
        lines.append(
            f"| {i} | {p['asin']} | {p['category']} | {p['title'].replace('|', '/')} | `{p['image_path']}` | {p['image_source_url']} |"
        )
    PROVENANCE_PATH.write_text("\n".join(lines), encoding="utf-8")


def main():
    CATALOG_DIR.mkdir(parents=True, exist_ok=True)
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    products = pick_products(fetch_csv())
    if len(products) < 30:
        raise RuntimeError(f"only collected {len(products)} products")
    write_avatar()
    JSON_PATH.write_text(json.dumps(products, ensure_ascii=False, indent=2), encoding="utf-8")
    SQL_PATH.write_text(render_sql(products), encoding="utf-8")
    write_provenance(products)
    print(f"collected {len(products)} products")


if __name__ == "__main__":
    main()
