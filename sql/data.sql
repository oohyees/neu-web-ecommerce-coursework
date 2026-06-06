SET NAMES utf8mb4;
USE ecommerce_minimal;

INSERT INTO user (username, password, nickname, email, phone, avatar_url, enabled) VALUES
('alice', '123456', 'Alice', 'alice@example.com', '13800000000', '/catalog/avatar-alice.svg', 1);

INSERT INTO admin_user (username, password, nickname, email, phone, role) VALUES
('admin', 'admin123', '系统管理员', 'admin@example.com', '13900000000', 'SUPER_ADMIN'),
('operator', 'operator123', '普通管理员', 'operator@example.com', '13900000001', 'ADMIN'),
('manager', 'manager123', '演示普通管理员', 'manager@example.com', '13900000002', 'ADMIN');

INSERT INTO admin_permission (id, code, name, group_name) VALUES
(1,  'dashboard:view',     '查看数据看板',   '数据看板'),
(2,  'product:manage',     '商品管理',       '商品管理'),
(3,  'category:manage',    '分类管理',       '商品管理'),
(4,  'review:manage',      '评价管理',       '商品管理'),
(5,  'order:manage',       '订单管理',       '订单管理'),
(6,  'order:export',       '订单导出',       '订单管理'),
(7,  'user:manage',        '用户管理',       '用户管理'),
(8,  'banner:manage',      '轮播管理',       '内容管理'),
(9,  'announcement:manage','公告管理',       '内容管理'),
(10, 'activity:manage',    '活动通知管理',   '内容管理'),
(11, 'feedback:manage',    '反馈管理',       '内容管理'),
(12, 'consultation:manage','咨询管理',       '内容管理'),
(13, 'coupon:manage',      '优惠券管理',     '营销管理'),
(14, 'promotion:manage',   '促销管理',       '营销管理'),
(15, 'admin:manage',       '管理员管理',     '系统管理'),
(16, 'import:product',     '商品导入',       '数据管理'),
(17, 'export:product',     '商品导出',       '数据管理'),
(18, 'export:stats',       '统计导出',       '数据管理');

-- SUPER_ADMIN has ALL permissions
INSERT INTO admin_role_permission (role, permission_id)
SELECT 'SUPER_ADMIN', id FROM admin_permission;

-- ADMIN has most permissions except admin management
INSERT INTO admin_role_permission (role, permission_id)
SELECT 'ADMIN', id FROM admin_permission WHERE code NOT IN ('admin:manage');

INSERT INTO hot_search (keyword, search_count, enabled, sort_order) VALUES
('机械键盘', 156, 1, 1),
('无线鼠标', 143, 1, 2),
('蓝牙耳机', 128, 1, 3),
('显示器', 112, 1, 4),
('Type-C数据线', 98, 1, 5);

INSERT INTO product_category (parent_id, name, sort_order) VALUES
(NULL, '数码办公', 1),
(1, '输入设备', 1),
(NULL, '影音与会议', 2),
(3, '音视频', 1),
(NULL, '扩展与连接', 3),
(5, '存储网络', 1),
(NULL, '桌面效率', 4),
(7, '桌面办公', 1);

INSERT INTO banner (title, image_url, link_url, sort_order) VALUES
('机械键盘限时特惠', '/catalog/B076LRJ528.webp', '/products/1', 1),
('电竞鼠标办公升级', '/catalog/B08F2Z6RJB.webp', '/products/2', 2);

INSERT INTO announcement (title, content, created_at) VALUES
('真实商品目录已更新', '当前目录使用公开商品样本整理，商品图与商品信息均可追溯来源。', NOW());

INSERT INTO activity_notice (title, content, enabled, created_at) VALUES
('618 预热活动', '指定商品限时直降，优惠券可叠加使用。', 1, NOW());

INSERT INTO customer_consultation (user_id, subject, content, reply, status, created_at) VALUES
(1, '发货时效', '下单后通常多久发货？', '工作日订单通常在 24 小时内发出。', 'REPLIED', NOW());

INSERT INTO product (category_id, name, price, stock, sales, is_on_sale, image_url, detail_html, params_text) VALUES
(2, 'Koolertron One Handed Programmable Mechanical Keyboard with OEM Gateron Red Switch,All 48 Programmable Keys Tools...', 85.99, 31, 8, 1, '/catalog/B076LRJ528.webp', 'Read more Macro Keyboard Specifications: All 48 keys are programmable, 8 Macro Keys,No backlit Key number: 48 Keyboard switch: OEM Gateron Red Keystroke force: 45±15cN Interface: 2 MINI USB Interfaces, USB2.1M detachable USB cable Rating: DC 5V, &lt;100mA 0Key speed: 1-100ms The Macro Keyboard Has 2 Working Modes Standard operating mode and configuration management mode . Configuration management mode: This mode...', '品牌: Koolertron; 型号: AE-SMKD7; 制造商: Koolertron'),
(2, 'Razer DeathAdder V2 Gaming Mouse: 20K DPI Optical Sensor - Fastest Gaming Mouse Switch - Chroma RGB Lighting - 8...', 58.20, 32, 11, 1, '/catalog/B08F2Z6RJB.webp', 'With over 10 million Razer DeathAdders sold, the most celebrated and awarded gaming mouse in the world has earned its popularity through its exceptional ergonomic design. Perfectly suited for a palm grip, it also works well with claw and fingertip styles. The Razer DeathAdder V2 continues this legacy, retaining its signature shape while shedding more weight for quicker handling to improve your gameplay. Going...', '品牌: Razer; 型号: ‎RZ01-03210300-R3M1; 制造商: ‎Razer Inc.'),
(2, 'R-Go Split Ergonomic Keyboard, QWERTY (US), Black, Wired USB Keyboard (QWERTY (US) / Spilt, Wired/Windows, Linux)', 102.91, 33, 14, 1, '/catalog/B0B572FFKQ.jpg', 'R-GO TOOLS ERGONOMIC SPLIT KEYBOARD R-GO TOOLS ERGONOMIC SPLIT KEYBOARD Chances are you have a kink in the wrists while typing. That happens automatically when you use a standard keyboard. In order to be able to type with straight wrists, we have developed an ergonomic split keyboard that consists of two separate parts. These parts can be placed freely on the desk so that you can type in a healthy way in any...', '品牌: R-Go Tools; 型号: ‎RGOSP-USWIBL; 制造商: ‎R-Go Tools'),
(2, 'Allsop Mouse Pad Pro Memory Foam Mouse Pad - Black (30203)', 10.99, 34, 17, 1, '/catalog/B0036DDT5G.jpg', 'Mouse pad is designed with a raised wrist rest and a large round mousing area for comfortable mousing. Multiple layers of memory foam are combined to ensure maximum air circulation, conformity and wrist support. This ensures your hand and wrist stay in a more natural position while using your mouse. The memory foam also conforms and adjusts to the contours of your wrist to relieve stress on pressure points....', '品牌: Allsop; 型号: ASP30203; 制造商: Allsop'),
(2, 'YSAGi Desk Pad, Desk Mat, 31.5" x 15.7" Laptop Leather Desk Pad Protector, Large Leather Desk Blotter for Keyboard...', 12.59, 35, 20, 1, '/catalog/B07W58LMND.webp', 'About this item No Need for Mouse Pad and Protect Your Desk : No unpleasant smell, Waterproof, oil-proof, protect your glass/wooden desktop from scratches, stains,and spills. Surface can be used as a large mouse pad, comfortable resting surface for your hands while writing, typing, and using the mouse. Necessary office supplies. Dual Side Usage and Multi Size and Multi Color Selection:Different color in each side,...', '品牌: YSAGi; 型号: v40; 制造商: YSAGi'),
(2, 'EMINTA Dual Sided Desk Pad, New Upgrade Sewing PU Leather Office Desk Mat, Waterproof Desk Blotter Protector, Desk...', 13.39, 36, 23, 1, '/catalog/B07S8QPD3S.webp', 'About this item NEW UPGRADE SEWING REINFORCEMENT: Made of durable PU leather material, which protects your glass/wooden desktop from scratches, stains, spills, heat and scuffs. NEW DESIGN SEWING mats are more durable than the normal desk protector, which are more fashion for daily use! Perfect for both office and home. WATERPROOF DOUBLE-SIDED USE: Water resistant and heat resistant material makes this desk blotter...', '品牌: EMINTA; 型号: E00BU; 制造商: EMINTA'),
(2, 'Aothia Dual-Sided Desk Pad - Leather Desk Mat, Natural Cork Desk Pad Protector, Large Mouse Pad for Desk, Waterproof...', 18.95, 37, 26, 1, '/catalog/B0CQTMGXHD.jpg', 'Aothia Cork Dual-Sided Desk Pad for Office and Home Natural Large Cork Desk Mat Dual-sided Style with Premium Cork Base. We employ bark processing techniques to craft the cork surface of our desk pad, meticulously hand-cutting each piece. Using our desk mat provides a tactile experience that connects you with nature. Multifunctional Desk Blotter Pad Previous page Handcrafted We employ bark processing techniques to...', '品牌: Aothia; 制造商: Aothia'),
(4, 'Trucker Bluetooth Headsets, Wireless Headset with AI Environmental Noise Cancelling & Mute Microphone, Up to 30H Talk...', 84.99, 38, 29, 1, '/catalog/B0BK8QY1JB.webp', 'About this item 【Industry Leading AI-Powered Environmental Noise Cancellation】EKSAtelecom cutting-edge AI-Powered ENC tech can dynamically detect and nullifies up to 99.8% ambient noise with a broad sound frequency range, and cancels it out before reaching your ears to get crystal-clear communication! 【164 ft Ultra-Long Wireless Connection】This truck driver bluetooth headset with a noise-cancelling microphone...', '品牌: EKSA; 型号: H1; 制造商: EKSA'),
(4, 'MusiBaby Bluetooth Speaker, Shower Speaker,Waterproof Speaker,Portable Speaker,Dual Pairing,Bluetooth 5.2,Loud...', 25.99, 39, 32, 1, '/catalog/B09Q2R6FSP.webp', 'MUSIBABY M77 BLUETOOTH SPEAKER PORTABLE SPEAKER SOUNDVIBE Rich bass, clear mids, crystal high, and dynamic sound. IPX7 Certified Waterproof WATERPROOF BLUETOOTH SPEATER This wireless speaker is ready to go with you on any extreme water-related adventure. Bring The Music Anywhere LONG BATTERY LIFE This small outdoor speaker comes with a powerful battery, keeps Your Favorite Tune Playing 24Hrs. Made to Travel...', '品牌: MusiBady; 型号: M77 speaker; 制造商: MusiBady'),
(4, '32GB Mp3 Player with Bluetooth, Full Touch 2.4 Screen MP3 and MP4 Player Built-in HD Speaker, FM Radio, Voice...', 35.99, 40, 35, 1, '/catalog/B089K5PVBF.jpg', 'About this item 【 Full-screen touch mp3 player】The player has a 2.4-inch touch screen with a resolution of 240*320, clear vision, easy to use The sliding touch screen allows you to operate more smoothly, 【MP3 Player with Bluetooth】Bluetooth 5.0, better compatibility and stable signal. Support connection to Bluetooth wireless headphones or speakers, allowing you to enjoy your favorite music (note: the player cannot...', '品牌: JOLIKE; 型号: M4s-32GB; 制造商: JOLIKE'),
(4, 'Sony SRS-XP500 X-Series Wireless Portable-BLUETOOTH-Karaoke Party-Speaker IPX4 Splash-resistant with 20 Hour-...', 389.00, 41, 38, 1, '/catalog/B094TJ5FBF.jpg', 'Bring clear, powerful sound to the party with the SRS-XP500 Portable Wireless Speaker. Add extra juice to the hits with MEGA BASS and Sony’s unique X-Balanced Speaker units, and keep the energy going with up to 20 hours of battery life plus USB-C quick charging.', '品牌: Sony; 型号: SRSXP500; 制造商: Sony'),
(4, 'Razer Kraken Tournament Edition THX 7.1 Surround Sound Gaming Headset: Retractable Noise Cancelling Mic - USB DAC -...', 93.95, 42, 41, 1, '/catalog/B07WWN97S7.webp', 'Product Description Equipped with a USB audio controller, the Razer Kraken Tournament Edition gaming headset delivers a high-fidelity experience along with full controls for audio personalization. The built-in Digital-to-Analog Converter (DAC) delivers crisp, clear details while THX Spatial Audio immerses you in next-gen surround sound—all perfectly paired with large 50 mm drivers. Further fine-tune your audio...', '品牌: Razer; 型号: RZ04-02051000-R3M1; 制造商: Razer'),
(4, 'Coleman Waterproof, Hands-Free Speaker for Universal/Smartphones - Red', 23.23, 43, 44, 1, '/catalog/B01J4C2O4Y.webp', 'The Coleman Aktiv Sounds Waterproof Bluetooth Speaker is ideal on your desk or in the shower as it can suction or clip onto almost any surface. Small enough to carry with you anywhere, it delivers big sound wherever you go. You can also answer any phone call wirelessly for hands-free speakerphone operation. The red speaker is waterproof, rugged and portable, and can be used just about anywhere.', '品牌: Coleman; 型号: CBT11-R; 制造商: Coleman'),
(6, 'IOGEAR 2-Port USB HDMI Cabled KVM Switch - 1920 x 1200 60Hz - Hotkey or Remote Button Switch - 2.1 Stereo - USB 2.0...', 59.99, 44, 7, 1, '/catalog/B004YCUDMU.jpg', 'IOGEAR GCS62HU is a 2-Port HD cable KVM switch with audio that charts revolutionary new ground in KVM functionality by combining advanced high definition and USB 2. 0 technology. It enables effortless control over 2 HD devices or computers using a single USB keyboard, USB mouse and high definition monitor or HDTV. This advanced HD KVMP can increase efficiency and save equipment cost for applications where HD...', '品牌: IOGEAR; 型号: GCS62HU; 制造商: IOGEAR'),
(6, 'GearIT HDMI Cable (2-Pack / 6.6ft / 2m) High-Speed HDMI 2.0b, 4K 60hz, 3D, ARC, HDCP 2.2, HDR, 18Gbps - Nylon Braided...', 15.99, 45, 10, 1, '/catalog/B08B3HYC45.webp', 'Previous page GearIT is a top brand that provides innovative HiFi audio equipment for audiophiles and home audio enthusiasts. Our brand have sold all over the world from professional producers and musicians to DIYers. We''ve got all the wires you need to get the perfect home theater setup, or a killer studio with our high quality cables! More than 5 million customers later, those principles continue to drive the...', '品牌: GearIT; 型号: ‏ ‎ GI-PRO-HD2-6.6F-2PK; 制造商: ‏ ‎ GearIT'),
(6, 'Seagate Expansion 18TB External Hard Drive HDD - USB 3.0, with Rescue Data Recovery Services (STKP18000400)', 332.99, 46, 13, 1, '/catalog/B092R6HW7L.webp', 'Ideal for the home, office, or dorm, Seagate Expansion desktop drive offers enormous desktop storage for photos, movies, music, and more. Backing up and transferring content is incredibly easy—just drag and drop! To get set up, connect the USB hard drive to Windows or Mac—no software required. For Apple Time Machine, simply reformat. Included is an 18-inch USB 3.0 cable and 18W power adapter.', '品牌: Seagate; 型号: STKP18000400; 制造商: Seagate'),
(6, 'GearIT Cat 6 Ethernet Cable 6 ft (24-Pack) - Cat6 Patch Cable, Cat 6 Patch Cable, Cat6 Cable, Cat 6 Cable, Cat6...', 58.99, 30, 16, 1, '/catalog/B00XIG5IV4.jpg', 'future-proof your network for 10-Gigabit Ethernet with GearIT Cat 6 network patch cables. With GearIT Cat6 cables, your network will be ready for the future and will still be working when you get there. industry standard ANSI/TIA-568-C.2, Cat 6, etc. Verified cables with High compliance RJ45 (8P8C) snag-less connectors and molded strain relief boots. Bandwidth rating of up to 550 MHz ensures compatibility with...', '品牌: GearIT; 型号: ‏ ‎ 6CAT6E-ORANGE-24PK; 制造商: ‏ ‎ GoDirectInc.com'),
(6, 'Linksys Hydra 6 Mesh WiFi 6 Router - MR20EC-AMZ - Dual-Band WiFi Router - Mesh Routers for Wireless Internet - WiFi...', 126.49, 31, 19, 1, '/catalog/B0B7QC7HP1.jpg', 'Introducing the Linksys Hydra 6 Mesh WiFi Router - Using the power of WiFi 6 to deliver lightning-fast internet speeds, seamless connectivity, and unmatched reliability, all in a sleek and stylish package. Linksys Hydra 6 is powered by WiFi 6 technology, so you''ll experience fast internet speeds of up to 3.0 Gbps, making it perfect for streaming 4K videos, playing online games, and working from home without lag or...', '品牌: Linksys; 型号: MR20EC-AMZ; 制造商: Linksys'),
(6, 'Seagate Ultra Touch SSD 500GB External Solid State Drive Portable - Black USB-C USB 3.0 for PC MAC and Seagate Mobile...', 95.03, 32, 22, 1, '/catalog/B088CTW44F.jpg', 'Ultra Touch SSD is a palm-sized, portable solid state drive featuring an Android app so that users can bring extra protection to priceless photos, videos, and important files. Thanks to fast SSD speeds, this drive is perfect for streaming stored videos directly to a laptop and it even enables automatic, continuous backup via the included Sync Plus software. Universally compatible, it works Mac and Windows...', '品牌: Seagate; 型号: ‎STJW500401; 制造商: ‎SEAGATE'),
(6, 'MAIWO M.2 SSD Enclosure Adapter,USB 3.2 Gen 2 (10Gbps) to M.2 NVME/SATA,Support UASP Trim, 4TB Storage Expansion,M or...', 16.99, 33, 25, 1, '/catalog/B09NBLVKXJ.webp', 'Previous page Next page 1 10Gbps High Speed 2 Compatible with NVME/SATA 3 Tool-Free 4 Heat Dissipation Why the light is on,but couldn''t see the hard drive from "my computer"? If the light is on or the hard drive heats up, it means the enclosure is fine, please check the "disk-managemen", if you couldn''t find your hard drive, it means one of your hard drive,cable or interface is detective,change them can solve...', '品牌: MAIWO; 型号: M.2 SATA/NVME; 制造商: MAIWO'),
(8, 'Swingline Stapler, 747 Classic Desktop Stapler, 20 Sheet Capacity, Metal, Steel Gray (74769)', 17.45, 34, 28, 1, '/catalog/B00I28F9UI.jpg', 'The renowned Swingline 747 Classic all-metal, steel gray stapler is sturdy and dependable. A specialized inner rail delivers jam-resistant, accurate stapling. This classic stapler is intended for desktop use, but is versatile for use in tacking and pinning. It holds a strip of up to 210 staples. A handy low-staple indicator alerts you when it''s time for a refill. This stapler secures up to 20 sheets of paper and...', '品牌: Swingline; 型号: S7074769; 制造商: ACCO Brands'),
(8, 'Rotring 600 Ballpoint Pen, Medium Point, Blue Ink, Green Barrel, Refillable', 29.99, 35, 31, 1, '/catalog/B091Y7B6N2.webp', 'The rOtring 600 ballpoint pen blends the iconic form of the 600 mechanical pencil with a smooth-writing ballpoint pen. Meant for a lifetime of use, the full-metal body provides ideal weight balance for fatigue-free writing and drawing. This premium pen is refillable and comes loaded with a high-quality metal refill. It also has a hexagonal shape so it doesn’t slide off your working table or writing space. The...', '品牌: Rotring; 型号: 2114263; 制造商: Newell Brands'),
(8, 'Dunwell Binder with Plastic Sleeves 12-Pocket (2 Pack, Black) - Presentation Book, 8.5 x 11 Portfolio Folder with...', 9.95, 36, 34, 1, '/catalog/B09QM2BMPG.webp', 'About this item 8.5x11 Presentation Book: Folio document organizer with 12 bound (non-refillable) top-loading plastic sleeves. Folder binder to organize, store, present 24 pages of no hole punched projects, reports. Customizable Cover and Spine: Create your own cover page and side label to identify contents and professionally showcase your presentations. Portfolio folder for documents with clear view poly cover....', '品牌: Dunwell; 型号: DISPLTR12; 制造商: Dunwell'),
(8, '006R01220 Yellow 34000 Page Yield Toner Cartridge for DocuColor Printers 240 250 7655 7665', 126.98, 37, 37, 1, '/catalog/B016MGVQ6C.jpg', 'Toner cartridge is designed for use with Xerox Discolor 240, 250, 242, 252; WorkCentre 7655, 7665 and 7675. Toner is specially formulated and tested to provide the best image quality and reliable printing you can count on page after page. Xerox genuine supplies and Xerox equipment are made for each other. Cartridge yields approximately 34,000 pages at 5 percent coverage.', '品牌: Xerox; 型号: ‎006R01220; 制造商: ‎Xerox'),
(8, 'Safco Products Onyx Mesh 5-Tier Vertical Desktop Organizer 3257BL, Black Powder Coat Finish, Durable Steel Mesh...', 33.40, 38, 40, 1, '/catalog/B002XK2OW2.webp', 'Onyx Mesh 5-Tier Vertical Desktop Organizer keeps important files and documents visible, organized and within easy reach. The commercial-grade mesh steel provides strength and stability while the powder coat finish helps reduce nicks and scrapes for extended use. Five 2" wide tiered sections hold file folders, cookbooks, homework, journals and binders. Place it in business and home offices, small cubicles,...', '品牌: Safco; 型号: 3257BL; 制造商: Safco Products'),
(8, 'Avery 3" Economy View 3 Ring Binder, Round Ring, Holds 8.5" x 11" Paper, 1 White Binder (5741)', 10.44, 39, 43, 1, '/catalog/B00INKVS82.webp', 'From the Manufacturer General-use binder features a clear overlay on front, spine and back cover to accommodate custom cover design. Features two interior pockets for storage, 1/2" ring that is back-mounted and exposed rivets on spine. Overlay material is clear vinyl. Round rings have a nonlocking mechanism. Binder holds 8-1/2" x 11" documents.', '品牌: AVERY; 型号: ‎05741; 制造商: ‎Avery'),
(8, 'Treva 10-Inch Portable Desktop Air Circulation Battery Fan, 2 Speed, Compact Folding & Tilt Design, with AC Adapter...', 29.99, 40, 6, 1, '/catalog/B01EZ9WR34.webp', 'About this item BATTERY -OPERATED PORTABLE FAN. You can cool off when you’re at home, work or outdoors with O2COOL’s 10-Inch Battery-Operated Portable Fan. Perfectly sized, it won’t take up much space and features a convenient folding design and a built-in handle for easy storage and transport. DURABLE CONSTRUCTION. Durably crafted using sturdy plastic construction for long-lasting strength and use, this portable...', '品牌: O2COOL; 制造商: O2COOL, LLC'),
(8, 'Drawealth Touch Control Traditional Table Lamp Set of 2,3-Way Dimmable Farmhouse Bedside Table Lamps with 2 USB...', 79.99, 41, 9, 1, '/catalog/B09MDNKLM7.webp', 'Product Description .aplus-v2 { display: block; margin-left: auto; margin-right: auto; } .aplus-v2 .aplus-3p-fixed-width { width: 970px; } .aplus-v2 .aplus-3p-fixed-width.aplus-module-wrapper { margin-left: auto; margin-right: auto; } .aplus-v2 { display:block; margin-left:auto; margin-right:auto; word-wrap: break-word; overflow-wrap: break-word; word-break: break-word; } /* Undo this for tech-specs because it...', '品牌: Drawealth; 型号: ‎T0085; 制造商: ‎Lightxury'),
(8, 'Simple Designs LT3039-PRP 14.17” Contemporary Mosaic Tiled Glass Genie Standard Table Lamp with Matching Fabric Shade...', 22.24, 42, 12, 1, '/catalog/B00G7QRJKK.webp', 'Available in variety of matching shade/base options! Product Details - Uses 1 x 40W Type B10 E12 Candelabra Base bulb **BULB IS NOT INCLUDED** - Made of resin, iron, glass, grout, PVC and shade is of fabric. - Rotary switch is located on the cord of lamp. - No assembly required! Simply attach shade to base of lamp. Additional Specs Lamp weighs 2.29 lbs Shade measures Dia: 8.25" Includes 5'' white plug in cord Fit...', '品牌: Simple Designs; 型号: LT3039-PRP; 制造商: LighTunes'),
(8, 'Brother TN-620 DCP-8080 8085 HL-5340D 5350 5370 MFC-8480 8680 8690 8890 Toner -Cartridge (Black) in Retail Packaging,...', 73.98, 43, 15, 1, '/catalog/B00JPBZOJ2.webp', 'The use of Brother Genuine replacement toner cartridges like the TN-620 produces sharp, black and white pages with the quality you expect from Brother products. The Brother TN-620 replacement laser black toner cartridge is for use with 10 Brother products. This professional cartridge is intelligently engineered to work in seamless unison without compromising the quality of the print. The Brother Genuine TN-620...', '品牌: Brother; 型号: ‎TN620; 制造商: ‎BROTHER');

UPDATE product SET name = 'Koolertron 单手机械键盘 Keyboard', detail_html = '48 键可编程机械键盘，支持宏配置与办公快捷键，适合桌面效率和游戏控制。' WHERE id = 1;
UPDATE product SET name = 'Razer DeathAdder V2 电竞鼠标 Mouse', detail_html = '20K DPI 光学传感器，轻量化人体工学设计，支持 Chroma 灯效。' WHERE id = 2;
UPDATE product SET name = 'R-Go Split 人体工学分体键盘', detail_html = '分体式键盘可自然摆放，帮助减轻长时间输入造成的腕部压力。' WHERE id = 3;
UPDATE product SET name = 'Allsop 记忆棉护腕鼠标垫', detail_html = '记忆棉腕托与大面积鼠标区域，适合办公桌面长期使用。' WHERE id = 4;
UPDATE product SET name = 'YSAGi 大号皮革桌垫', detail_html = '防水耐磨桌垫，可覆盖键盘、鼠标和笔记本区域，桌面更整洁。' WHERE id = 5;
UPDATE product SET name = 'EMINTA 双面防水办公桌垫', detail_html = '双面 PU 皮革材质，边缘加固，适合办公和居家桌面。' WHERE id = 6;
UPDATE product SET name = 'Aothia 软木双面桌垫', detail_html = '软木与皮革双面设计，提供自然触感和稳定鼠标操作。' WHERE id = 7;
UPDATE product SET name = 'EKSA AI 降噪蓝牙耳麦', detail_html = 'AI 环境噪声消除，适合会议、客服和远程办公。' WHERE id = 8;
UPDATE product SET name = 'MusiBaby 防水蓝牙音箱', detail_html = '便携防水音箱，支持蓝牙连接和双机配对，适合户外与家庭使用。' WHERE id = 9;
UPDATE product SET name = 'JOLIKE 32GB 触屏音乐播放器', detail_html = '2.4 英寸触控屏，支持蓝牙、FM、录音与本地音乐播放。' WHERE id = 10;
UPDATE product SET name = 'Sony XP500 便携派对音箱', detail_html = '大功率无线音箱，支持长续航、快速充电和户外聚会使用。' WHERE id = 11;
UPDATE product SET name = 'Razer Kraken 7.1 游戏耳机', detail_html = 'THX 7.1 环绕声与可伸缩降噪麦克风，适合游戏和语音沟通。' WHERE id = 12;
UPDATE product SET name = 'Coleman 防水免提蓝牙音箱', detail_html = '小巧便携，支持防水和免提通话，适合户外与浴室场景。' WHERE id = 13;
UPDATE product SET name = 'IOGEAR 双口 HDMI KVM 切换器', detail_html = '一套键盘鼠标控制两台设备，支持 HDMI 高清输出和音频切换。' WHERE id = 14;
UPDATE product SET name = 'GearIT 高速 HDMI 连接线套装', detail_html = '支持 4K 60Hz、HDR 和高速传输，适合显示器、电视和主机连接。' WHERE id = 15;
UPDATE product SET name = 'Seagate 18TB 外置硬盘', detail_html = '大容量桌面级外置硬盘，适合备份、影音和办公资料归档。' WHERE id = 16;
UPDATE product SET name = 'GearIT 六类网线 24 条装', detail_html = '预制六类网络跳线，适合办公室、机房和家庭网络整理。' WHERE id = 17;
UPDATE product SET name = 'Linksys Hydra 6 WiFi 6 路由器', detail_html = '双频 WiFi 6 路由器，支持 Mesh 扩展和高速无线覆盖。' WHERE id = 18;
UPDATE product SET name = 'Seagate Ultra Touch 500GB 移动固态硬盘', detail_html = '轻薄便携 SSD，支持 USB-C 连接，适合文件备份和移动办公。' WHERE id = 19;
UPDATE product SET name = 'MAIWO M.2 SSD 移动硬盘盒', detail_html = '支持 NVMe/SATA M.2 固态硬盘，USB 3.2 高速传输，工具免拆。' WHERE id = 20;
UPDATE product SET name = 'Swingline 经典桌面订书机', detail_html = '全金属结构，稳定耐用，适合办公室和学习场景。' WHERE id = 21;
UPDATE product SET name = 'Rotring 600 金属圆珠笔', detail_html = '经典六角金属笔身，书写顺滑，可替换笔芯。' WHERE id = 22;
UPDATE product SET name = 'Dunwell 12 袋资料展示册', detail_html = 'A4 文件收纳与展示册，适合报告、合同和作品集整理。' WHERE id = 23;
UPDATE product SET name = 'Xerox 黄色高容量碳粉盒', detail_html = '适配 Xerox 打印设备，适合高频彩色打印场景。' WHERE id = 24;
UPDATE product SET name = 'Safco 五层网格文件架', detail_html = '钢制网格文件架，可垂直收纳文件夹、资料和办公用品。' WHERE id = 25;
UPDATE product SET name = 'Avery 经济型三孔文件夹', detail_html = '标准三孔活页夹，适合资料归档和课程文件整理。' WHERE id = 26;
UPDATE product SET name = 'Treva 10 英寸折叠桌面风扇', detail_html = '两档风速，可电池供电或接入适配器，适合桌面降温。' WHERE id = 27;
UPDATE product SET name = 'Drawealth 触控调光台灯套装', detail_html = '双台灯套装，支持触控调光和 USB 充电，适合卧室和书桌。' WHERE id = 28;
UPDATE product SET name = 'Simple Designs 马赛克玻璃台灯', detail_html = '装饰性玻璃灯身与布艺灯罩，适合桌面和床头照明。' WHERE id = 29;
UPDATE product SET name = 'Brother TN-620 黑色碳粉盒', detail_html = 'Brother 原装替换碳粉盒，打印清晰稳定，适合办公文档输出。' WHERE id = 30;

INSERT INTO product_spec (product_id, spec_name, spec_value) VALUES
(1, '品牌', 'Koolertron'),
(1, '型号', 'AE-SMKD7'),
(1, '来源', '公开商品样本'),
(2, '品牌', 'Razer'),
(2, '型号', '‎RZ01-03210300-R3M1'),
(2, '来源', '公开商品样本'),
(3, '品牌', 'R-Go Tools'),
(3, '型号', '‎RGOSP-USWIBL'),
(3, '来源', '公开商品样本'),
(4, '品牌', 'Allsop'),
(4, '型号', 'ASP30203'),
(4, '来源', '公开商品样本'),
(5, '品牌', 'YSAGi'),
(5, '型号', 'v40'),
(5, '来源', '公开商品样本'),
(6, '品牌', 'EMINTA'),
(6, '型号', 'E00BU'),
(6, '来源', '公开商品样本'),
(7, '品牌', 'Aothia'),
(7, '来源', '公开商品样本'),
(8, '品牌', 'EKSA'),
(8, '型号', 'H1'),
(8, '来源', '公开商品样本'),
(9, '品牌', 'MusiBady'),
(9, '型号', 'M77 speaker'),
(9, '来源', '公开商品样本'),
(10, '品牌', 'JOLIKE'),
(10, '型号', 'M4s-32GB'),
(10, '来源', '公开商品样本'),
(11, '品牌', 'Sony'),
(11, '型号', 'SRSXP500'),
(11, '来源', '公开商品样本'),
(12, '品牌', 'Razer'),
(12, '型号', 'RZ04-02051000-R3M1'),
(12, '来源', '公开商品样本'),
(13, '品牌', 'Coleman'),
(13, '型号', 'CBT11-R'),
(13, '来源', '公开商品样本'),
(14, '品牌', 'IOGEAR'),
(14, '型号', 'GCS62HU'),
(14, '来源', '公开商品样本'),
(15, '品牌', 'GearIT'),
(15, '型号', '‏ ‎ GI-PRO-HD2-6.6F-2PK'),
(15, '来源', '公开商品样本'),
(16, '品牌', 'Seagate'),
(16, '型号', 'STKP18000400'),
(16, '来源', '公开商品样本'),
(17, '品牌', 'GearIT'),
(17, '型号', '‏ ‎ 6CAT6E-ORANGE-24PK'),
(17, '来源', '公开商品样本'),
(18, '品牌', 'Linksys'),
(18, '型号', 'MR20EC-AMZ'),
(18, '来源', '公开商品样本'),
(19, '品牌', 'Seagate'),
(19, '型号', '‎STJW500401'),
(19, '来源', '公开商品样本'),
(20, '品牌', 'MAIWO'),
(20, '型号', 'M.2 SATA/NVME'),
(20, '来源', '公开商品样本'),
(21, '品牌', 'Swingline'),
(21, '型号', 'S7074769'),
(21, '来源', '公开商品样本'),
(22, '品牌', 'Rotring'),
(22, '型号', '2114263'),
(22, '来源', '公开商品样本'),
(23, '品牌', 'Dunwell'),
(23, '型号', 'DISPLTR12'),
(23, '来源', '公开商品样本'),
(24, '品牌', 'Xerox'),
(24, '型号', '‎006R01220'),
(24, '来源', '公开商品样本'),
(25, '品牌', 'Safco'),
(25, '型号', '3257BL'),
(25, '来源', '公开商品样本'),
(26, '品牌', 'AVERY'),
(26, '型号', '‎05741'),
(26, '来源', '公开商品样本'),
(27, '品牌', 'O2COOL'),
(27, '来源', '公开商品样本'),
(28, '品牌', 'Drawealth'),
(28, '型号', '‎T0085'),
(28, '来源', '公开商品样本'),
(29, '品牌', 'Simple Designs'),
(29, '型号', 'LT3039-PRP'),
(29, '来源', '公开商品样本'),
(30, '品牌', 'Brother'),
(30, '型号', '‎TN620'),
(30, '来源', '公开商品样本');

INSERT INTO coupon (name, threshold_amount, discount_amount, enabled) VALUES
('满300减30', 300.00, 30.00, 1),
('满500减80', 500.00, 80.00, 1);

INSERT INTO user_coupon (user_id, coupon_id, status, claimed_at) VALUES
(1, 1, 'UNUSED', NOW());

INSERT INTO promotion (product_id, title, promotion_type, promotion_price, promotion_stock, start_at, end_at, enabled) VALUES
(1, '限时优惠', 'FLASH_SALE', 75.99, 10, DATE_SUB(NOW(), INTERVAL 1 DAY), GREATEST('2026-12-31 23:59:59', DATE_ADD(NOW(), INTERVAL 180 DAY)), 1),
(2, '精选直降', 'PROMOTION', 53.20, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), GREATEST('2026-12-31 23:59:59', DATE_ADD(NOW(), INTERVAL 180 DAY)), 1);

INSERT INTO user_address (user_id, receiver_name, phone, province, city, district, detail_address, is_default) VALUES
(1, 'Alice', '13800000000', '辽宁省', '沈阳市', '和平区', '创新路 1 号', 1);

INSERT INTO product_review (user_id, product_id, rating, content, image_url, created_at) VALUES
(1, 1, 4, 'I was worried about this one. I''ve been spurned before. Overall I''m pretty happy with it, though.I''ve been PC gaming since middle school, but I broke my right arm when I was in high school. I can use a mouse in my right hand fine for day to day stuff but between the scar tissue and slight nerve pinching that causes it to be uncomfortable, really uncomfortable for gaming. For whatever reason using a keyboard in my right is fine though (I guess because my hand rests on it...', '', NOW()),
(1, 2, 5, 'Although I''ve never cared for the aesthetics or branding (it''s very tryhard and aimed at teenagers) in of Razer the overall build quality, ergonomics, and responsiveness of the mouse overcome any reservations.This is a wired mouse but unlike traditional hard rubber coated mouse cables this one is wrapped in a soft but sturdy woven material. It makes it so it doesn''t noticably resist as traditional mouse cables would which allows for smooth operation and makes for easy...', '', NOW()),
(1, 3, 3, 'Due to RSI, split keyboards are all I can use, and I''ve owned or still own well over a dozen, from Kinesis to Ergodox to homebuilt to Microsoft to you name it. I was looking for a thin split keyboard to make a portable ergonomic rig for travel.This keyboard fits the bill as it is very slim. In spite of the thinness, the key travel is significant and feels very good. There''s no wobble in the keys. If you''re familiar with Dell XPS, Macbook Pro (2015 and 2018), and modern...', '', NOW()),
(1, 4, 5, 'I tried other mouse pads, but nothing compares to this one!', '', NOW()),
(1, 5, 5, 'I don’t have a desk in my home, so I often do work on my dining room table. The worrisome part is that my dining room table is wood and I don’t want to scratch it or damage it any further than what was done during Covid. Then one day I stumbled onto this product. I fell in love with it. I can keep my water cup on top and not have to worry about water damage on the wood. I can have my laptop and mouse on it without having to worry about scratching my table. I can even have...', '', NOW()),
(1, 6, 5, 'As advertised, great price excellent buy', '', NOW()),
(1, 7, 5, 'I have a glass dining table and needed something to place my laptop on to prevent scratching of my table, and this is perfect. The size allows for my laptop and a mousepad/mouse and a drink with plenty of room. They wipe clean easily. The underside is cork. I love the color. I am very happy with the price, quality and size.', '', NOW()),
(1, 8, 4, 'The Bluetooth headset that I tested is a good product overall, but I must say it is not the best for driving. When I used it inside an office, people told me that it sounded great and had no issues. I even tested it with background noise from a TV, and it successfully canceled out the noise. However, the real problem arose when I took it on the road with the window down. The headset completely lost signal or people would say it sounded like I was underwater.I believe it...', '', NOW()),
(1, 9, 4, 'I recently bought the MusiBaby M77 Bluetooth Speaker and let me tell you, I used it at a pool party and it was a hit! The sound quality is fantastic. It fills the room (or backyard in my case) with strong, clear music. Bass? Deep and rich. Highs and mids? Crystal clear. I clipped it on my bag and had my tunes with me all the time. Battery life? Lasted all day, no sweat. One more cool thing - you can buy two and pair them for even more amazing sound. It feels like you''re...', '', NOW()),
(1, 10, 4, 'I originally gave this player 5 stars. Now I''m not sure where I stand on it. When I first got it last week, all was well. Today I plugged it into the USB port on my laptop and my laptop doesn''t even see that it''s plugged into the USB port. There''s no hope of interacting with this player to drop files into it from my laptop.I clicked on "Help" here on the Amazon page for this product and the first thing I found is "Why doesn''t my laptop see this player." -- Are you kidding...', '', NOW()),
(1, 11, 5, 'We have owned various speakers. Used lots. Listened to even more- softball games, camping, block parties, etc.These Sonys, for the price of a set are an amazing deal. No distortion. Great bass. Clean sounds. All the highs and lows you''d expect from a high end. And a great price.Do they have wheels, or a stand? No. Do they have great quality? Yes. Is it worth the trade off? Heck yes.', '', NOW()),
(1, 12, 4, 'I''m not much of a tech person so this won''t be a specs review. All I know is that I can hear a lot more going on in games than I can with some of the other Kraken models we''ve previously bought and that I can''t hear anything outside of the headphones unless I have it sitting slightly off of my ear 😅 I don''t dominately use these as they were a gift for my husband who plays games regularly. He said he really likes that it has a USB input to his computer rather than an aux...', '', NOW()),
(1, 13, 4, 'quick ship! good item quality :)', '', NOW()),
(1, 14, 4, 'THE GCS1942 ($433) IS THE ONE TO GET IF YOU HAVE DUAL 4K MONITORS!I recently upgraded my Win10 PC to a pair of LG 32" 4K monitors, which have DisplayPort inputs, and this $433 (OUCH!) IOGear GCS1942 DisplayPort KVM. My previous setup was a pair of 27" WQHD monitors with DVI-D inputs, and used an older IOGear DIV-D KVM which is also a dual-view KVM. I can then run two computers with dual monitors.So, I tried FOUR other KVM''s, and had issues all around. Issues like the...', '', NOW()),
(1, 15, 5, 'The .75 feet cord is sturdy. The way the connectors are wired is that when you stretch out the wire, one connector is "up" and one is "down." The wide part is up on one side and dow at the other end of the cable. This orientation works great if you are stacking components with both HDMI sockets oriented the same way, but my two components are side by side. A 1.5 ft. cable from another company did not survive the 180 degree twist. This shorter cable did survive.I am trying...', '', NOW()),
(1, 16, 5, 'The Seagate Portable 5TB External Hard Drive has become an indispensable companion for my digital storage needs, offering unmatched reliability, versatility, and convenience across multiple platforms. Whether I''m backing up important files, expanding storage capacity, or enhancing gaming experiences on my PC, Mac, PS4, or Xbox, this external hard drive consistently delivers exceptional performance and peace of mind.The first aspect that immediately impressed me about the...', '', NOW()),
(1, 17, 5, 'I work for an internet company. We are not huge but we are growing, 8-10 states last I checked. I found these cables and showed my boss. He agreed it was a great deal! By the time we buy cable, connectors, and pay a tech, it will cost way more than these cables. We did the math and it was almost a dollar for 2 RJ45s plus the cable (that depends on the footage) and then a tech to build and test. These also save a lot of time since they are pre-cut to the lengths we need for...', '', NOW()),
(1, 18, 4, 'Very easy to set up. Works great. Gave support 5 stars because of the help with my old router. Haven''t had to call tech support for the new one.', '', NOW()),
(1, 19, 5, 'bought this due to review comparing it to other drives by same manufacturerHowever still find it unbearably slowNow honestly, moving gigabites in volume is a lot of bits to move, so ???? .... but it clearly is usable as ''back up storrage'' only and NOT for ''live use'' of anything beyond looking at a small sized jpg.I am not unhappy, if it holds up as Back up storrage, but will need to find additional solutionsThis is great! Copies quickly, a one MB folder was completely...', '', NOW()),
(1, 20, 4, 'I used this for cloning my 256 GB stock SSD drive to a new 2 TB SSD for my Steam Deck. The enclosure got incredibly hot. Do not touch it til it cools down. I already expected it to get hot so it was not a surprise, because lots of data being transferred and no fan. It did the job excellently. Now it''s the home of my old Stock drive and I needed to reclone the stock image due to my own mistake, and it was fast. I can''t vouch for the longevity of the device and only for a...', '', NOW()),
(1, 21, 5, 'It’s a classic. Durable', '', NOW()),
(1, 22, 4, 'The pen in all it''s glory is amazing. Smooth, properly weighted for my liking. The only ting that I did not like is that I got delivered a pen with black ink, when the description clearly states blue ink. Couldn''t even find an option to return and request for refund.', '', NOW()),
(1, 23, 5, 'Very nice folder for what I need it for. I use them as substitute teacher folders for their assignments. The only issue I had was putting in the cover on the front. I used regular paper and it''s a bit difficult to slide in; maybe cardstock would be easier. Other than that, very happy with them.', '', NOW()),
(1, 24, 4, 'Excelent product.This is the second time that we received a defective item from you guys, and what really peace me of is that you guys never are willing to fix the problem at the best convenience for us (your customers) I received a defective toner with a 30% of life only, and the best option you that you guys offered me was to return the toner and after you receive it you will reimburse my money, well let me ask you this, Didn''t you think for a moment that if I do that I...', '', NOW()),
(1, 25, 5, 'These black wire mesh organizers were so helpful when it came to organizing the file folders in the filing cabinet. They kept everything nice and neat while still keeping everything easily accessible. Would definitely buy these again.', '', NOW()),
(1, 26, 5, 'Exactly as advertised.', '', NOW()),
(1, 27, 4, 'Basic small (10” across) fan for personal use.Air flow is moderate if you’re close to it, drops off a few feet away. Would be a good choice for someone sitting at a desk, but won’t blow the papers away. If you want a fan you’ll feel from across the room, this isn’t it.Seems to work as well when plugged in versus on batteries. Batteries install easily and the battery cover closes and locks securely.The fan folds against the base for storage with or without the batteries...', '', NOW()),
(1, 28, 4, 'Nice lamp, but like others stated, they are smaller than expected. One thing to note, the LED bulbs it comes with aren''t great. I replaced them with LED dimmable frosted bulbs and the lamps look better, and touch control seems to even work better', '', NOW()),
(1, 29, 4, 'Love this lamp !!', '', NOW()),
(1, 30, 5, 'We''ve been using compatibles before and noticed the quality went bad after printing a few hundreds so we bought this original toner instead. What a difference and how easy to install.I''ve been giving this toner cartridge a pretty good workout in my home office for nearly ten months. I don''t run a business here, but some of my volunteer work has been paper-intensive - including drafting revisions of lengthy documents. My Exactly what I needed!But It Works! Tried low cost...', '', NOW());

-- 补全缺少的产品规格
INSERT INTO product_spec (product_id, spec_name, spec_value) VALUES
(7, '型号', 'Aothia-Cork'),
(27, '型号', 'NPL10CPG');

-- 购物车
INSERT INTO cart_item (user_id, product_id, spec_text, quantity) VALUES
(1, 2, '品牌:Razer|型号:RZ01-03210300-R3M1', 2),
(1, 4, '品牌:Allsop|型号:ASP30203', 1);

-- 收藏
INSERT INTO product_favorite (user_id, product_id) VALUES
(1, 11),
(1, 13);

-- 反馈
INSERT INTO feedback (user_id, content, reply, status, created_at) VALUES
(1, '希望能增加更多支付方式，比如银联云闪付。', '感谢反馈，银联支付已在支付页面开放，欢迎使用。', 'REPLIED', NOW());

-- 种子订单 #1：待支付
INSERT INTO orders (order_no, user_id, address_id, total_amount, status, payment_status, payment_method, logistics_status, refund_status, created_at) VALUES
('ORD202605190001', 1, 1, 174.19, 'CREATED', 'UNPAID', 'MOCK_PAY', '待支付', 'NONE', '2026-05-19 09:30:00');
INSERT INTO order_item (order_id, product_id, product_name, spec_text, unit_price, quantity, subtotal) VALUES
(1, 1, 'Koolertron 单手机械键盘', '品牌:Koolertron|型号:AE-SMKD7', 85.99, 2, 171.98),
(1, 2, 'Razer DeathAdder V2 电竞鼠标', '品牌:Razer|型号:RZ01-03210300-R3M1', 58.20, 1, 58.20);
INSERT INTO order_logistics (order_id, content, created_at) VALUES
(1, '订单已创建', '2026-05-19 09:30:00');

-- 种子订单 #2：已支付待发货
INSERT INTO orders (order_no, user_id, address_id, total_amount, status, payment_status, payment_method, logistics_status, refund_status, created_at) VALUES
('ORD202605190002', 1, 1, 389.00, 'CREATED', 'PAID', 'ALIPAY', '待发货', 'NONE', '2026-05-19 14:20:00');
INSERT INTO order_item (order_id, product_id, product_name, spec_text, unit_price, quantity, subtotal) VALUES
(2, 11, 'Sony XP500 便携派对音箱', '品牌:Sony|型号:SRSXP500', 389.00, 1, 389.00);
INSERT INTO order_logistics (order_id, content, created_at) VALUES
(2, '订单已创建', '2026-05-19 14:20:00'),
(2, '订单已支付', '2026-05-19 14:22:00');

-- 种子订单 #3：已发货待收货
INSERT INTO orders (order_no, user_id, address_id, total_amount, status, payment_status, payment_method, logistics_status, refund_status, created_at) VALUES
('ORD202605180003', 1, 1, 95.98, 'SHIPPED', 'PAID', 'WECHAT', '运输中', 'NONE', '2026-05-18 10:05:00');
INSERT INTO order_item (order_id, product_id, product_name, spec_text, unit_price, quantity, subtotal) VALUES
(3, 8, 'EKSA AI 降噪蓝牙耳麦', '品牌:EKSA|型号:H1', 84.99, 1, 84.99),
(3, 4, 'Allsop 记忆棉护腕鼠标垫', '品牌:Allsop|型号:ASP30203', 10.99, 1, 10.99);
INSERT INTO order_logistics (order_id, content, created_at) VALUES
(3, '订单已创建', '2026-05-18 10:05:00'),
(3, '订单已支付', '2026-05-18 10:06:00'),
(3, '商家已发货，快递单号 SF1234567890', '2026-05-18 15:30:00'),
(3, '快件已到达沈阳分拣中心', '2026-05-19 08:00:00');
