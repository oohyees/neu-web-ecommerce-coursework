SET NAMES utf8mb4;

-- ═══ Delete old banners and recreate with correct product references ═══
DELETE FROM banner;

-- Use real dummyjson product images that match the banner titles
INSERT INTO banner (id, title, image_url, link_url, sort_order) VALUES
(1, 'Essence 睫毛膏 限时特惠', 'https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/1.webp', '/products/5000', 1),
(2, 'Dior J''adore 香水 新品上市', 'https://cdn.dummyjson.com/product-images/fragrances/dior-j''adore/thumbnail.webp', '/products/5007', 2),
(3, 'Annibale Colombo 精品沙发', 'https://cdn.dummyjson.com/product-images/furniture/annibale-colombo-sofa/1.webp', '/products/5011', 3),
(4, 'Calvin Klein 经典香水', 'https://cdn.dummyjson.com/product-images/fragrances/calvin-klein-ck-one/1.webp', '/products/5005', 4),
(5, '家居相框 畅玩无限', 'https://cdn.dummyjson.com/product-images/home-decoration/family-tree-photo-frame/thumbnail.webp', '/products/5043', 5),
(6, 'Samsung Galaxy S8', 'https://cdn.dummyjson.com/product-images/smartphones/samsung-galaxy-s8/1.webp', '/products/5044', 6),
(7, '高品质厨房好物', 'https://cdn.dummyjson.com/product-images/kitchen-accessories/silver-pot-with-glass-cap/thumbnail.webp', '/products/5070', 7),
(8, 'Apple iPhone X', 'https://cdn.dummyjson.com/product-images/smartphones/iphone-x/1.webp', '/products/5046', 8),
(9, '厨房削皮器 创意利器', 'https://cdn.dummyjson.com/product-images/kitchen-accessories/yellow-peeler/thumbnail.webp', '/products/5076', 9);
