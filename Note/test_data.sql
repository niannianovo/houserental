-- ============================================
-- 测试数据脚本（精简版：只有房东 + 房源）
-- 执行前请先执行 house_rental_system.sql 建库建表
-- 密码全部是 123456 → MD5: e10adc3949ba59abbe56e057f20f883e
-- ============================================

USE houserental;

-- -------------------------------------------
-- 1. 房东账号（current_role=1 房东）
-- admin=1 由 schema 脚本插入，房东从 id=2 开始
-- -------------------------------------------
INSERT INTO `user` (`account`, `password`, `nickname`, `phone`, `email`, `current_role`, `is_admin`, `is_email_verified`, `status`) VALUES
('landlord1', 'e10adc3949ba59abbe56e057f20f883e', '张房东', '13800138001', 'zhang@test.com', 1, 0, 1, 0),
('landlord2', 'e10adc3949ba59abbe56e057f20f883e', '李房东', '13800138002', 'li@test.com',    1, 0, 1, 0),
('landlord3', 'e10adc3949ba59abbe56e057f20f883e', '王房东', '13800138003', 'wang@test.com',  1, 0, 1, 0),
('landlord4', 'e10adc3949ba59abbe56e057f20f883e', '赵房东', '13800138004', 'zhao@test.com',  1, 0, 1, 0),
('landlord5', 'e10adc3949ba59abbe56e057f20f883e', '陈房东', '13800138005', 'chen@test.com',  1, 0, 1, 0);

SET @zhang = 2;  -- 杭州房东
SET @li    = 3;  -- 深圳房东
SET @wang  = 4;  -- 上海房东
SET @zhao  = 5;  -- 北京房东
SET @chen  = 6;  -- 杭州房东（第二个）

-- -------------------------------------------
-- 2. 房源（status=1 已上架，verify_status=1 已通过）
-- view_count/favorite_count 全部 0，不预设热度，避免推荐兜底偏置
-- -------------------------------------------

-- 杭州 · 西湖区（4）
INSERT INTO `house`
(`owner_id`,`title`,`description`,`province`,`city`,`district`,`address`,`area`,`price`,`deposit`,`room_count`,`hall_count`,`floor`,`house_type`,`status`,`verify_status`,`images`,`contact_phone`,`contact_name`,`view_count`,`favorite_count`) VALUES
(@zhang,'西湖景苑 精装2室1厅','近地铁，采光好，带家电','浙江省','杭州市','西湖区','文三路100号·西湖景苑3栋502',75.00,5000.00,5000.00,2,1,'8/18',0,1,1,NULL,'13800138001','张房东',0,0),
(@zhang,'翠苑新村 宽敞3室2厅','学区房，南北通透','浙江省','杭州市','西湖区','翠苑一区22号602',110.00,8500.00,8500.00,3,2,'5/6',0,1,1,NULL,'13800138001','张房东',0,0),
(@chen,'古荡合租次卧','押一付一，可短租','浙江省','杭州市','西湖区','古荡新村15号301',15.00,2500.00,2500.00,1,0,'3/6',1,1,1,NULL,'13800138005','陈房东',0,0),
(@chen,'文二西路 精品1室','单身公寓，拎包入住','浙江省','杭州市','西湖区','文二西路88号·金都花园1203',42.00,3800.00,3800.00,1,1,'12/24',0,1,1,NULL,'13800138005','陈房东',0,0);

-- 杭州 · 拱墅区（3）
INSERT INTO `house`
(`owner_id`,`title`,`description`,`province`,`city`,`district`,`address`,`area`,`price`,`deposit`,`room_count`,`hall_count`,`floor`,`house_type`,`status`,`verify_status`,`images`,`contact_phone`,`contact_name`,`view_count`,`favorite_count`) VALUES
(@zhang,'大关苑 温馨2室1厅','地铁2号线，业态成熟','浙江省','杭州市','拱墅区','大关苑三区18号601',68.00,4500.00,4500.00,2,1,'6/12',0,1,1,NULL,'13800138001','张房东',0,0),
(@chen,'小河直街 合租主卧','文艺街区，独立卫浴','浙江省','杭州市','拱墅区','小河路66号201',18.00,2200.00,2200.00,1,0,'2/5',1,1,1,NULL,'13800138005','陈房东',0,0),
(@zhang,'运河上城 豪华3室2厅','江景房，全新家电','浙江省','杭州市','拱墅区','运河上城8幢1601',120.00,7500.00,7500.00,3,2,'16/28',0,1,1,NULL,'13800138001','张房东',0,0);

-- 杭州 · 余杭区（2）
INSERT INTO `house`
(`owner_id`,`title`,`description`,`province`,`city`,`district`,`address`,`area`,`price`,`deposit`,`room_count`,`hall_count`,`floor`,`house_type`,`status`,`verify_status`,`images`,`contact_phone`,`contact_name`,`view_count`,`favorite_count`) VALUES
(@chen,'未来科技城 2室1厅','近阿里园区，上班方便','浙江省','杭州市','余杭区','海曙路188号·未科花园A座1002',72.00,4000.00,4000.00,2,1,'10/22',0,1,1,NULL,'13800138005','陈房东',0,0),
(@chen,'良渚文化村 3室2厅','环境安静，户型方正','浙江省','杭州市','余杭区','良渚文化村玉鸟路22号702',115.00,6800.00,6800.00,3,2,'7/11',0,1,1,NULL,'13800138005','陈房东',0,0);

-- 深圳 · 南山区（4）
INSERT INTO `house`
(`owner_id`,`title`,`description`,`province`,`city`,`district`,`address`,`area`,`price`,`deposit`,`room_count`,`hall_count`,`floor`,`house_type`,`status`,`verify_status`,`images`,`contact_phone`,`contact_name`,`view_count`,`favorite_count`) VALUES
(@li,'科技园 精装2室1厅','腾讯大厦旁，地铁直达','广东省','深圳市','南山区','科技园南区15栋2002',70.00,8500.00,8500.00,2,1,'20/32',0,1,1,NULL,'13800138002','李房东',0,0),
(@li,'后海湾 海景3室2厅','高层海景，全新装修','广东省','深圳市','南山区','后海滨路99号·海景豪庭2801',135.00,13000.00,13000.00,3,2,'28/36',0,1,1,NULL,'13800138002','李房东',0,0),
(@li,'桃源村 合租次卧','性价比高，交通便利','广东省','深圳市','南山区','桃源村二期5栋501',16.00,3800.00,3800.00,1,0,'5/11',1,1,1,NULL,'13800138002','李房东',0,0),
(@li,'海岸城 单身公寓','中心地段，楼下商超','广东省','深圳市','南山区','海岸城东座1803',45.00,6500.00,6500.00,1,1,'18/30',0,1,1,NULL,'13800138002','李房东',0,0);

-- 深圳 · 福田区（3）
INSERT INTO `house`
(`owner_id`,`title`,`description`,`province`,`city`,`district`,`address`,`area`,`price`,`deposit`,`room_count`,`hall_count`,`floor`,`house_type`,`status`,`verify_status`,`images`,`contact_phone`,`contact_name`,`view_count`,`favorite_count`) VALUES
(@li,'华强北 现代2室1厅','商圈核心，交通便利','广东省','深圳市','福田区','华强北路2066号1502',68.00,9500.00,9500.00,2,1,'15/25',0,1,1,NULL,'13800138002','李房东',0,0),
(@li,'车公庙 合租主卧','近地铁7号线，独卫','广东省','深圳市','福田区','车公庙天安数码城2栋801',20.00,4200.00,4200.00,1,0,'8/18',1,1,1,NULL,'13800138002','李房东',0,0),
(@li,'香蜜湖 豪华3室2厅','学区房，品质小区','广东省','深圳市','福田区','香蜜湖一号2201',140.00,14500.00,14500.00,3,2,'22/33',0,1,1,NULL,'13800138002','李房东',0,0);

-- 上海 · 浦东新区（3）
INSERT INTO `house`
(`owner_id`,`title`,`description`,`province`,`city`,`district`,`address`,`area`,`price`,`deposit`,`room_count`,`hall_count`,`floor`,`house_type`,`status`,`verify_status`,`images`,`contact_phone`,`contact_name`,`view_count`,`favorite_count`) VALUES
(@wang,'陆家嘴 精装2室1厅','近地铁2号线，配套齐','上海市','上海市','浦东新区','浦东南路999号1902',72.00,9000.00,9000.00,2,1,'19/28',0,1,1,NULL,'13800138003','王房东',0,0),
(@wang,'联洋社区 大3室2厅','品质小区，绿化率高','上海市','上海市','浦东新区','芳甸路200号·联洋花园1202',128.00,13500.00,13500.00,3,2,'12/22',0,1,1,NULL,'13800138003','王房东',0,0),
(@wang,'张江 合租次卧','近地铁，科技园近','上海市','上海市','浦东新区','张江高科技园区春晓路6号',18.00,4500.00,4500.00,1,0,'6/16',1,1,1,NULL,'13800138003','王房东',0,0);

-- 上海 · 静安区（2）
INSERT INTO `house`
(`owner_id`,`title`,`description`,`province`,`city`,`district`,`address`,`area`,`price`,`deposit`,`room_count`,`hall_count`,`floor`,`house_type`,`status`,`verify_status`,`images`,`contact_phone`,`contact_name`,`view_count`,`favorite_count`) VALUES
(@wang,'南京西路 时尚2室1厅','市中心，地铁口','上海市','上海市','静安区','南京西路1788号1402',70.00,10500.00,10500.00,2,1,'14/21',0,1,1,NULL,'13800138003','王房东',0,0),
(@wang,'曹家渡 精品1室','紧凑户型，独立卫浴','上海市','上海市','静安区','万航渡路23号1002',48.00,7800.00,7800.00,1,1,'10/18',0,1,1,NULL,'13800138003','王房东',0,0);

-- 北京 · 海淀区（3）
INSERT INTO `house`
(`owner_id`,`title`,`description`,`province`,`city`,`district`,`address`,`area`,`price`,`deposit`,`room_count`,`hall_count`,`floor`,`house_type`,`status`,`verify_status`,`images`,`contact_phone`,`contact_name`,`view_count`,`favorite_count`) VALUES
(@zhao,'中关村 2室1厅','地铁4号线，互联网区','北京市','北京市','海淀区','中关村大街27号1101',74.00,8800.00,8800.00,2,1,'11/20',0,1,1,NULL,'13800138004','赵房东',0,0),
(@zhao,'五道口 宽敞3室2厅','学区房，清华北大近','北京市','北京市','海淀区','五道口华清嘉园8号楼901',130.00,13200.00,13200.00,3,2,'9/16',0,1,1,NULL,'13800138004','赵房东',0,0),
(@zhao,'西二旗 合租主卧','近百度腾讯，通勤短','北京市','北京市','海淀区','西二旗软件园2栋705',22.00,4100.00,4100.00,1,0,'7/15',1,1,1,NULL,'13800138004','赵房东',0,0);
