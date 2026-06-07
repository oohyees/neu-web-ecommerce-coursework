#!/usr/bin/env python3
"""Generate product seed from real dummyjson product data.
Uses actual product names, brands, prices, images, reviews, etc."""
import json
import random

random.seed(42)

# Load real products
with open("/Users/oohyees/Projects/NEU/sem6/web/data/dummyjson-products.json", "r") as f:
    dummyjson_data = json.load(f)

real_products = dummyjson_data.get("products", [])
print(f"Loaded {len(real_products)} real products from dummyjson")

# Map dummyjson categories to our DB category IDs
# Our categories: 1-数码办公, 2-输入设备, 3-影音与会议, 5-耳机音箱,
# 6-扩展与连接, 7-存储网络, 8-线材配件, 9-桌面效率, 12-美妆个护,
# 14-护肤, 15-香水香氛, 18-家居家具, 21-鞋靴箱包, 24-智能设备,
# 25-智能手机, 26-智能手表, 27-笔记本电脑

CATEGORY_MAP = {
    "beauty": 12,
    "fragrances": 15,
    "furniture": 18,
    "groceries": 12,
    "home-decoration": 18,
    "kitchen-accessories": 12,
    "laptops": 27,
    "mens-shirts": 21,
    "mens-shoes": 21,
    "mens-watches": 26,
    "mobile-accessories": 8,
    "motorcycle": 24,
    "skin-care": 14,
    "smartphones": 25,
    "sports-accessories": 24,
    "sunglasses": 24,
    "tablets": 24,
    "tops": 21,
    "vehicle": 24,
    "womens-bags": 21,
    "womens-dresses": 21,
    "womens-jewellery": 24,
    "womens-shoes": 21,
    "womens-watches": 26,
}

# Color options per category type for SKU generation
COLOR_OPTIONS = {
    "electronics": ["黑色", "白色", "银色", "深空灰", "午夜蓝", "玫瑰金"],
    "fashion": ["黑色", "白色", "红色", "蓝色", "粉色", "米色", "灰色", "棕色"],
    "home": ["原木色", "白色", "黑色", "灰色", "胡桃木色"],
    "beauty": ["自然色", "象牙白", "蜜桃粉", "玫瑰红", "裸色", "珊瑚色"],
}

SIZE_OPTIONS = {
    "electronics": ["标准版", "升级版", "旗舰版"],
    "fashion_shoes": ["38", "39", "40", "41", "42", "43", "44"],
    "fashion_bag": ["小号", "中号", "大号"],
    "beauty": ["30ml", "50ml", "100ml"],
    "home": ["单件装", "套装"],
}

# Category type mapping
CAT_TYPE = {}
for cid in [1,2,3,5,6,7,8,9,24,25,26,27]:
    CAT_TYPE[cid] = "electronics"
for cid in [21]:
    CAT_TYPE[cid] = "fashion"
for cid in [18]:
    CAT_TYPE[cid] = "home"
for cid in [12,14,15]:
    CAT_TYPE[cid] = "beauty"

CAT_SIZE_TYPE = {}
for cid in [21]:
    CAT_SIZE_TYPE[cid] = "fashion_shoes"
for cid in [12,14,15]:
    CAT_SIZE_TYPE[cid] = "beauty"
for cid in [18]:
    CAT_SIZE_TYPE[cid] = "home"

# Category name lookup
CAT_NAMES = {
    1: "数码办公", 2: "输入设备", 3: "影音与会议", 5: "耳机音箱",
    6: "扩展与连接", 7: "存储网络", 8: "线材配件", 9: "桌面效率",
    12: "美妆个护", 14: "护肤", 15: "香水香氛", 18: "家居家具",
    21: "鞋靴箱包", 24: "智能设备", 25: "智能手机", 26: "智能手表",
    27: "笔记本电脑",
}


def esc(s):
    if s is None:
        return ""
    return str(s).replace("\\", "\\\\").replace("'", "''")


def gen_all():
    product_full = []
    product_minimal = []
    sku_lines = []
    image_lines = []
    spec_lines = []

    sku_id = 5000
    img_id = 5000
    spec_id = 5000

    pid = 5000
    for rp in real_products:
        cat_id = CATEGORY_MAP.get(rp["category"], 24)

        name = rp["title"]
        brand = rp.get("brand", "")
        price = rp.get("price", 0)
        original_price = round(price * (1 + rp.get("discountPercentage", 10) / 100), 2)
        stock = rp.get("stock", 100)
        rating = rp.get("rating", 0)
        description = rp.get("description", "")
        sku_code = rp.get("sku", f"SKU-{pid}")
        warranty = rp.get("warrantyInformation", "")
        shipping = rp.get("shippingInformation", "")
        tags = rp.get("tags", [])

        # Images
        images = rp.get("images", [])
        thumbnail = rp.get("thumbnail", "")
        main_img = thumbnail or (images[0] if images else "")

        # Detail HTML from description
        detail_html = f"<p>{esc(description)}</p>"
        if tags:
            detail_html += f"<p>标签: {esc(', '.join(tags))}</p>"

        # Params text
        params_parts = []
        if brand:
            params_parts.append(f"品牌: {brand}")
        params_parts.append(f"分类: {CAT_NAMES.get(cat_id, '其他')}")
        if warranty:
            params_parts.append(f"保修: {warranty}")
        if shipping:
            params_parts.append(f"配送: {shipping}")
        params_text = "; ".join(params_parts)

        # Subtitle
        subtitle_parts = []
        if brand:
            subtitle_parts.append(brand)
        if rp.get("discountPercentage"):
            subtitle_parts.append(f"立省{rp['discountPercentage']:.0f}%")
        if rating:
            subtitle_parts.append(f"{rating}分好评")
        subtitle = " | ".join(subtitle_parts) if subtitle_parts else description[:30]

        # Product rows
        product_full.append(
            f"INSERT INTO product (id, category_id, name, subtitle, image_url, price, original_price, stock, sales, is_on_sale, detail_html, params_text) "
            f"VALUES ({pid}, {cat_id}, '{esc(name)}', '{esc(subtitle)}', '{esc(main_img)}', {price}, {original_price}, {stock}, {random.randint(0, 5000)}, 1, '{esc(detail_html)}', '{esc(params_text)}');"
        )
        product_minimal.append(
            f"INSERT INTO product (id, category_id, name, image_url, price, stock, sales, is_on_sale, detail_html, params_text) "
            f"VALUES ({pid}, {cat_id}, '{esc(name)}', '{esc(main_img)}', {price}, {stock}, {random.randint(0, 5000)}, 1, '{esc(detail_html)}', '{esc(params_text)}');"
        )

        # Images - use real URLs
        for si, url in enumerate(images):
            image_lines.append(
                f"INSERT INTO product_image (id, product_id, url, sort_order, deleted) "
                f"VALUES ({img_id}, {pid}, '{esc(url)}', {si}, 0);"
            )
            img_id += 1

        # SKU - generate color/size variants based on category
        cat_type = CAT_TYPE.get(cat_id, "electronics")
        colors = random.sample(COLOR_OPTIONS[cat_type], k=random.randint(2, 3))
        size_type = CAT_SIZE_TYPE.get(cat_id)
        sizes = random.sample(SIZE_OPTIONS[size_type], k=random.randint(1, 2)) if size_type else []

        for color in colors:
            if sizes:
                for size in sizes:
                    s_code = f"{sku_code}-{color[:2]}-{size[:2]}"
                    s_price = round(price * random.uniform(0.9, 1.15), 2)
                    s_stock = random.randint(0, 300)
                    sku_lines.append(
                        f"INSERT INTO product_sku (id, product_id, sku_code, color, size, price, stock, image, deleted) "
                        f"VALUES ({sku_id}, {pid}, '{esc(s_code)}', '{esc(color)}', '{esc(size)}', {s_price}, {s_stock}, '{esc(main_img)}', 0);"
                    )
                    sku_id += 1
            else:
                s_code = f"{sku_code}-{color[:2]}"
                s_price = round(price * random.uniform(0.9, 1.15), 2)
                s_stock = random.randint(0, 300)
                sku_lines.append(
                    f"INSERT INTO product_sku (id, product_id, sku_code, color, size, price, stock, image, deleted) "
                    f"VALUES ({sku_id}, {pid}, '{esc(s_code)}', '{esc(color)}', NULL, {s_price}, {s_stock}, '{esc(main_img)}', 0);"
                )
                sku_id += 1

        # Specs from real product data
        spec_data = []
        if brand:
            spec_data.append(("品牌", brand))
        spec_data.append(("分类", CAT_NAMES.get(cat_id, "其他")))
        if rp.get("weight"):
            spec_data.append(("重量", f"{rp['weight']}kg"))
        if warranty:
            spec_data.append(("保修", warranty))
        if shipping:
            spec_data.append(("配送", shipping))
        if rp.get("discountPercentage"):
            spec_data.append(("折扣", f"{rp['discountPercentage']:.1f}%"))
        if rating:
            spec_data.append(("评分", f"{rating}"))
        if rp.get("minimumOrderQuantity"):
            spec_data.append(("起订量", str(rp["minimumOrderQuantity"])))
        if rp.get("returnPolicy"):
            spec_data.append(("退换政策", rp["returnPolicy"]))

        for sname, sval in spec_data:
            spec_lines.append(
                f"INSERT INTO product_spec (id, product_id, spec_name, spec_value) "
                f"VALUES ({spec_id}, {pid}, '{esc(sname)}', '{esc(sval)}');"
            )
            spec_id += 1

        pid += 1

    return product_full, product_minimal, sku_lines, image_lines, spec_lines


product_full, product_minimal, sku_lines, image_lines, spec_lines = gen_all()

# Full version (for ecommerce_product - microservices)
full_sql = "\n".join([
    "SET NAMES utf8mb4;",
    "USE ecommerce_product;",
    "",
    "DELETE FROM product WHERE id >= 5000;",
    "DELETE FROM product_sku WHERE product_id >= 5000;",
    "DELETE FROM product_image WHERE product_id >= 5000;",
    "DELETE FROM product_spec WHERE product_id >= 5000;",
    "",
] + product_full + [""] + sku_lines + [""] + image_lines + [""] + spec_lines + [""])

with open("/Users/oohyees/Projects/NEU/sem6/web/data/seed-3000-products.sql", "w", encoding="utf-8") as f:
    f.write(full_sql + "\n")

# Minimal version (for ecommerce_minimal - monolith)
minimal_sql = "\n".join([
    "SET NAMES utf8mb4;",
    "USE ecommerce_minimal;",
    "",
    "DELETE FROM product WHERE id >= 5000;",
    "DELETE FROM product_sku WHERE product_id >= 5000;",
    "DELETE FROM product_image WHERE product_id >= 5000;",
    "DELETE FROM product_spec WHERE product_id >= 5000;",
    "",
] + product_minimal + [""] + sku_lines + [""] + image_lines + [""] + spec_lines + [""])

with open("/Users/oohyees/Projects/NEU/sem6/web/data/seed-3000-products-minimal.sql", "w", encoding="utf-8") as f:
    f.write(minimal_sql + "\n")

print(f"Generated: {len(product_full)} real products, {len(sku_lines)} SKUs, {len(image_lines)} images, {len(spec_lines)} specs")
print("All data from real dummyjson products")
