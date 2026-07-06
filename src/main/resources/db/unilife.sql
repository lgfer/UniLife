-- ----------------------------
-- UniLife 校园生活服务平台 · 数据库初始化脚本
-- 建库建表 + 演示用种子数据（商家/用户/优惠券/种草帖均为虚构）
-- 秒杀库存需预热到 Redis，见 README「本地启动」
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `unilife_db` DEFAULT CHARACTER SET utf8mb4;
USE `unilife_db`;

DROP TABLE IF EXISTS `tb_merchant_type`;
CREATE TABLE `tb_merchant_type`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(3) UNSIGNED NULL DEFAULT NULL COMMENT '顺序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (1, '奶茶饮品', '/imgs/types/t1.png', 1, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (2, '餐饮小吃', '/imgs/types/t2.png', 2, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (3, '文印复印', '/imgs/types/t3.png', 3, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (4, '理发美容', '/imgs/types/t4.png', 4, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (5, '健身运动', '/imgs/types/t5.png', 5, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (6, '超市便利', '/imgs/types/t6.png', 6, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (7, '水果生鲜', '/imgs/types/t7.png', 7, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (8, '休闲娱乐', '/imgs/types/t8.png', 8, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (9, '书店文具', '/imgs/types/t9.png', 9, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant_type` (`id`, `name`, `icon`, `sort`, `create_time`, `update_time`) VALUES (10, '数码维修', '/imgs/types/t10.png', 10, '2026-06-01 10:00:00', '2026-06-01 10:00:00');

DROP TABLE IF EXISTS `tb_merchant`;
CREATE TABLE `tb_merchant`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商家名称',
  `type_id` bigint(20) UNSIGNED NOT NULL COMMENT '商家类型的id',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商家图片，多个图片以\',\'隔开',
  `area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商圈，例如陆家嘴',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址',
  `x` double UNSIGNED NOT NULL COMMENT '经度',
  `y` double UNSIGNED NOT NULL COMMENT '维度',
  `avg_price` bigint(10) UNSIGNED NULL DEFAULT NULL COMMENT '均价，取整数',
  `sold` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '销量',
  `comments` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '评论数量',
  `score` int(2) UNSIGNED ZEROFILL NOT NULL COMMENT '评分，1~5分，乘10保存，避免小数',
  `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间，例如 10:00-22:00',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `foreign_key_type`(`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (1, '椰语时光奶茶(大学城店)', 1, '/imgs/merchants/m1.jpg', '大学城', '学苑路12号', 120.3521, 30.3129, 12, 3421, 286, 47, '09:00-22:30', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (2, '半杯春鲜果茶', 1, '/imgs/merchants/m2.jpg', '大学城', '文汇路88号', 120.3534, 30.314, 14, 2107, 153, 45, '09:30-22:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (3, '知食堂麻辣烫', 2, '/imgs/merchants/m3.jpg', '大学城', '学林街5号', 120.3547, 30.3151, 16, 4210, 342, 46, '10:00-23:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (4, '老灶头盖浇饭', 2, '/imgs/merchants/m4.jpg', '大学城', '学苑路45号', 120.356, 30.3162, 15, 3860, 271, 44, '10:30-21:30', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (5, '深夜食光炸串', 2, '/imgs/merchants/m5.jpg', '大学城', '求是巷3号', 120.3573, 30.3173, 20, 1980, 167, 45, '17:00-01:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (6, '极印文印社', 3, '/imgs/merchants/m6.jpg', '大学城', '文汇路21号', 120.3586, 30.3129, 8, 5230, 412, 48, '08:00-22:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (7, '快人一步打印店', 3, '/imgs/merchants/m7.jpg', '大学城', '学林街17号', 120.3599, 30.314, 6, 4415, 198, 43, '08:00-21:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (8, '出发造型Studio', 4, '/imgs/merchants/m8.jpg', '大学城', '学苑路66号', 120.3612, 30.3151, 38, 1250, 240, 46, '10:00-21:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (9, '燃力健身工作室', 5, '/imgs/merchants/m9.jpg', '大学城', '文汇路102号', 120.3625, 30.3162, 45, 860, 178, 47, '07:00-23:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (10, '惠邻超市(学苑路店)', 6, '/imgs/merchants/m10.jpg', '大学城', '学苑路30号', 120.3638, 30.3173, 18, 6120, 95, 44, '07:30-23:30', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (11, '鲜果日记', 7, '/imgs/merchants/m11.jpg', '大学城', '学林街9号', 120.3651, 30.3129, 13, 2760, 188, 46, '08:30-22:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (12, '巷口桌游社', 8, '/imgs/merchants/m12.jpg', '大学城', '求是巷11号', 120.3664, 30.314, 25, 930, 142, 47, '13:00-24:00', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (13, '页边书店', 9, '/imgs/merchants/m13.jpg', '大学城', '文汇路56号', 120.3677, 30.3151, 32, 780, 164, 48, '09:00-21:30', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_merchant` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`, `create_time`, `update_time`) VALUES (14, '修机侠数码维修', 10, '/imgs/merchants/m14.jpg', '大学城', '学林街2号', 120.369, 30.3162, 60, 540, 89, 45, '09:30-20:30', '2026-06-01 10:00:00', '2026-06-01 10:00:00');

DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码，加密存储',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '昵称，默认是用户id',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '人物头像',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniqe_key_phone`(`phone`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (1, '13800001001', '', '图书馆钉子户', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (2, '13800001002', '', '青柠不加冰', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (3, '13800001003', '', '晚八人', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (4, '13800001004', '', '自习室常驻', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (5, '13800001005', '', '操场夜跑侠', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (6, '13800001006', '', '奶茶戒断失败', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (7, '13800001007', '', '六点早八战士', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (8, '13800001008', '', '校门口等外卖', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (9, '13800001009', '', '靠窗座位爱好者', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (10, '13800001010', '', '期末周幸存者', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (11, '13800001011', '', '天台看晚霞', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`, `create_time`, `update_time`) VALUES (12, '13800001012', '', '水房吹风机', '', '2026-06-01 10:00:00', '2026-06-01 10:00:00');

DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '主键，用户id',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '城市名称',
  `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人介绍，不要超过128个字符',
  `fans` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '粉丝数量',
  `followee` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '关注的人的数量',
  `gender` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '性别，0：男，1：女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `credits` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '积分',
  `level` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '会员级别，0~9级,0代表未开通会员',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

DROP TABLE IF EXISTS `tb_coupon`;
CREATE TABLE `tb_coupon`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '商家id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '优惠券标题',
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '副标题',
  `rules` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用规则',
  `pay_value` bigint(10) UNSIGNED NOT NULL COMMENT '支付金额，单位是分。例如200代表2元',
  `actual_value` bigint(10) NOT NULL COMMENT '抵扣金额，单位是分。例如200代表2元',
  `type` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0,普通券；1,秒杀券',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1,上架; 2,下架; 3,过期',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

INSERT INTO `tb_coupon` (`id`, `merchant_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `status`, `create_time`, `update_time`) VALUES (1, 1, '新生专享奶茶券', '全场通用', '周一至周日全天可用\n每单限用一张\n不可与其他优惠同享', 1000, 1500, 0, 1, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_coupon` (`id`, `merchant_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `status`, `create_time`, `update_time`) VALUES (2, 6, '期末周打印半价券', 'A4黑白单面', '仅限工作日使用\n单次最多200页', 500, 1000, 0, 1, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_coupon` (`id`, `merchant_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `status`, `create_time`, `update_time`) VALUES (3, 1, '招牌杨枝甘露秒杀券', '限量100张', '仅限到店自取\n每人限购一张\n有效期至学期末', 100, 1600, 1, 1, '2026-06-01 10:00:00', '2026-06-01 10:00:00');
INSERT INTO `tb_coupon` (`id`, `merchant_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `status`, `create_time`, `update_time`) VALUES (4, 9, '健身体验月卡秒杀', '限量200张', '新会员专享\n每人限购一张\n需提前一天预约', 990, 9900, 1, 1, '2026-06-01 10:00:00', '2026-06-01 10:00:00');

DROP TABLE IF EXISTS `tb_seckill_coupon`;
CREATE TABLE `tb_seckill_coupon`  (
  `coupon_id` bigint(20) UNSIGNED NOT NULL COMMENT '关联的优惠券的id',
  `stock` int(8) NOT NULL COMMENT '库存',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `begin_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '生效时间',
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '失效时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`coupon_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '秒杀优惠券表，与优惠券是一对一关系' ROW_FORMAT = Compact;

INSERT INTO `tb_seckill_coupon` (`coupon_id`, `stock`, `create_time`, `begin_time`, `end_time`, `update_time`) VALUES (3, 100, '2026-06-01 10:00:00', '2026-06-01 10:00:00', '2026-12-31 23:59:59', '2026-06-01 10:00:00');
INSERT INTO `tb_seckill_coupon` (`coupon_id`, `stock`, `create_time`, `begin_time`, `end_time`, `update_time`) VALUES (4, 200, '2026-06-01 10:00:00', '2026-06-01 10:00:00', '2026-12-31 23:59:59', '2026-06-01 10:00:00');

DROP TABLE IF EXISTS `tb_coupon_order`;
CREATE TABLE `tb_coupon_order`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '下单的用户id',
  `coupon_id` bigint(20) UNSIGNED NOT NULL COMMENT '购买的优惠券id',
  `pay_type` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '核销时间',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

DROP TABLE IF EXISTS `tb_post`;
CREATE TABLE `tb_post`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(20) NOT NULL COMMENT '商户id',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '打卡的照片，最多9张，多张以\",\"隔开',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '打卡的文字描述',
  `liked` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '点赞数量',
  `comments` int(8) UNSIGNED NULL DEFAULT NULL COMMENT '评论数量',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

INSERT INTO `tb_post` (`id`, `merchant_id`, `user_id`, `title`, `images`, `content`, `liked`, `comments`, `create_time`, `update_time`) VALUES (1, 1, 2, '自习到闭馆全靠这杯续命', '/imgs/posts/p1.jpg', '三楼靠窗位配大杯四季春，下午的高数作业居然写完了。出示学生证还能再减两块，大学城这家真的可以冲。', 36, 3, '2026-06-02 15:20:00', '2026-06-02 15:20:00');
INSERT INTO `tb_post` (`id`, `merchant_id`, `user_id`, `title`, `images`, `content`, `liked`, `comments`, `create_time`, `update_time`) VALUES (2, 3, 8, '晚课后的麻辣烫救了我', '/imgs/posts/p2.jpg', '九点下课冲过去还有位置，锅底不齁，素菜给得实在。人均十五吃到扶墙，比等外卖快多了。', 52, 6, '2026-06-03 21:40:00', '2026-06-03 21:40:00');
INSERT INTO `tb_post` (`id`, `merchant_id`, `user_id`, `title`, `images`, `content`, `liked`, `comments`, `create_time`, `update_time`) VALUES (3, 6, 10, '期末周打印省钱攻略', '/imgs/posts/p3.jpg', '复习资料两百多页，半价券叠学生价一共花了不到十块。老板还帮忙双面排版，装订免费，考研人快收藏。', 87, 12, '2026-06-05 10:05:00', '2026-06-05 10:05:00');
INSERT INTO `tb_post` (`id`, `merchant_id`, `user_id`, `title`, `images`, `content`, `liked`, `comments`, `create_time`, `update_time`) VALUES (4, 9, 5, '第一次上团课没有社死', '/imgs/posts/p4.jpg', '体验月卡真香，教练会照顾新手，动作不标准也不会点名。器械区晚上人少，适合下晚自习来出出汗。', 29, 4, '2026-06-06 20:10:00', '2026-06-06 20:10:00');
INSERT INTO `tb_post` (`id`, `merchant_id`, `user_id`, `title`, `images`, `content`, `liked`, `comments`, `create_time`, `update_time`) VALUES (5, 11, 1, '水果拼盘性价比之王', '/imgs/posts/p5.jpg', '切好的当季拼盘十块一大盒，宿舍四个人分着吃刚好。老板看是学生还会多塞两块西瓜。', 41, 5, '2026-06-08 13:45:00', '2026-06-08 13:45:00');
INSERT INTO `tb_post` (`id`, `merchant_id`, `user_id`, `title`, `images`, `content`, `liked`, `comments`, `create_time`, `update_time`) VALUES (6, 12, 7, '周五晚的桌游局yyds', '/imgs/posts/p6.jpg', '六个人开了两局狼人杀加一局卡坦，人均二十还包热水和小食。老板会带新手，社恐也能玩得开。', 33, 2, '2026-06-09 22:30:00', '2026-06-09 22:30:00');

DROP TABLE IF EXISTS `tb_post_comments`;
CREATE TABLE `tb_post_comments`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `post_id` bigint(20) UNSIGNED NOT NULL COMMENT '打卡id',
  `parent_id` bigint(20) UNSIGNED NOT NULL COMMENT '关联的1级评论id，如果是一级评论，则值为0',
  `answer_id` bigint(20) UNSIGNED NOT NULL COMMENT '回复的评论id',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复的内容',
  `liked` int(8) UNSIGNED NULL DEFAULT NULL COMMENT '点赞数',
  `status` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '状态，0：正常，1：被举报，2：禁止查看',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `follow_user_id` bigint(20) UNSIGNED NOT NULL COMMENT '关联的用户id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

DROP TABLE IF EXISTS `tb_sign`;
CREATE TABLE `tb_sign`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `year` year NOT NULL COMMENT '签到的年',
  `month` tinyint(2) NOT NULL COMMENT '签到的月',
  `date` date NOT NULL COMMENT '签到的日期',
  `is_backup` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '是否补签',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;