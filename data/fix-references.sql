SET NAMES utf8mb4;

-- ═══ Banners: update to reference new product IDs ═══
UPDATE banner SET link_url='/products/5000', title='Essence 睫毛膏 限时特惠' WHERE id=1;
UPDATE banner SET link_url='/products/5007', title='Dior J''adore 香水 新品上市' WHERE id=2;
UPDATE banner SET link_url='/products/5011', title='Annibale Colombo 精品沙发' WHERE id=3;
UPDATE banner SET link_url='/products/5005', title='Calvin Klein 经典香水' WHERE id=4;
UPDATE banner SET link_url='/products/5043', title='iPhone 15 畅玩无限' WHERE id=5;
UPDATE banner SET link_url='/products/5058', title='Samsung 便携音箱' WHERE id=6;
UPDATE banner SET link_url='/products/5070', title='高品质真皮手提包' WHERE id=7;
UPDATE banner SET link_url='/products/5043', title='Apple iPhone 15 Pro' WHERE id=8;
UPDATE banner SET link_url='/products/5060', title='Seagate 移动硬盘' WHERE id=9;

-- ═══ Promotions: remap to new product IDs ═══
UPDATE promotion SET product_id=5000 WHERE id=1;
UPDATE promotion SET product_id=5001 WHERE id=2;
UPDATE promotion SET product_id=5002 WHERE id=3;
UPDATE promotion SET product_id=5003 WHERE id=4;
UPDATE promotion SET product_id=5004 WHERE id=5;
UPDATE promotion SET product_id=5008 WHERE id=6;
UPDATE promotion SET product_id=5010 WHERE id=7;
UPDATE promotion SET product_id=5011 WHERE id=8;
UPDATE promotion SET product_id=5012 WHERE id=9;
UPDATE promotion SET product_id=5016 WHERE id=10;

-- ═══ Reviews: delete old and insert new referencing real products ═══
DELETE FROM product_review;
INSERT INTO product_review (user_id, product_id, rating, content, image_url, created_at) VALUES
(1, 5000, 4, 'Great mascara, really volumizes my lashes! Would buy again.', '', NOW() - INTERVAL 2 DAY),
(1, 5001, 5, 'Beautiful eyeshadow palette with great pigmentation.', '', NOW() - INTERVAL 3 DAY),
(1, 5002, 3, 'Good setting powder but a bit too matte for my taste.', '', NOW() - INTERVAL 4 DAY),
(1, 5003, 5, 'Classic red lipstick, very creamy and long-lasting!', '', NOW() - INTERVAL 5 DAY),
(1, 5004, 5, 'Love this nail polish, dries quickly and looks great.', '', NOW() - INTERVAL 6 DAY),
(1, 5005, 5, 'CK One is my go-to everyday fragrance.', '', NOW() - INTERVAL 1 DAY),
(1, 5006, 4, 'Chanel Coco Noir is elegant and mysterious. Perfect for evenings.', '', NOW() - INTERVAL 2 DAY),
(1, 5007, 4, 'Dior J''adore is simply luxurious and feminine.', '', NOW() - INTERVAL 3 DAY),
(1, 5008, 5, 'Dolce Shine is a joyful and youthful scent!', '', NOW() - INTERVAL 4 DAY),
(1, 5009, 4, 'Gucci Bloom is a modern and romantic fragrance.', '', NOW() - INTERVAL 5 DAY),
(1, 5010, 5, 'This bed frame is incredibly comfortable and well-made.', '', NOW() - INTERVAL 1 DAY),
(1, 5011, 5, 'The sofa is the centerpiece of our living room now!', '', NOW() - INTERVAL 2 DAY),
(1, 5012, 3, 'Nice bedside table, but assembly was a bit tricky.', '', NOW() - INTERVAL 3 DAY),
(1, 5013, 5, 'Best conference chair I''ve ever sat in.', '', NOW() - INTERVAL 4 DAY),
(1, 5014, 4, 'Unique design, looks great in our bathroom.', '', NOW() - INTERVAL 5 DAY),
(1, 5043, 5, 'iPhone 15 is fast and the camera is amazing!', '', NOW() - INTERVAL 1 DAY),
(1, 5044, 4, 'Samsung Galaxy S8 still holds up well.', '', NOW() - INTERVAL 2 DAY),
(1, 5045, 5, 'OPPO F11 Pro has an incredible pop-up camera!', '', NOW() - INTERVAL 3 DAY),
(1, 5058, 4, 'Great sound quality for the price.', '', NOW() - INTERVAL 4 DAY),
(1, 5060, 5, 'Plenty of storage space and very fast transfer speeds.', '', NOW() - INTERVAL 5 DAY),
(1, 5070, 5, 'Beautiful leather bag, worth every penny!', '', NOW() - INTERVAL 1 DAY),
(1, 5071, 4, 'Chic and practical handbag for everyday use.', '', NOW() - INTERVAL 2 DAY),
(1, 5072, 5, 'Prada keeps delivering quality products.', '', NOW() - INTERVAL 3 DAY),
(1, 5073, 4, 'Stylish sunglasses with great UV protection.', '', NOW() - INTERVAL 4 DAY),
(1, 5074, 5, 'Lightweight and comfortable running shoes!', '', NOW() - INTERVAL 5 DAY),
(1, 5075, 4, 'Elegant watch, perfect for formal occasions.', '', NOW() - INTERVAL 1 DAY),
(1, 5076, 5, 'The MacBook Pro is a powerhouse for creative work!', '', NOW() - INTERVAL 2 DAY),
(1, 5077, 4, 'Microsoft Surface Laptop is sleek and versatile.', '', NOW() - INTERVAL 3 DAY),
(1, 5078, 4, 'ThinkPad is reliable for business use.', '', NOW() - INTERVAL 4 DAY),
(1, 5079, 5, 'Razer Blade is the best gaming laptop I''ve owned!', '', NOW() - INTERVAL 5 DAY);
