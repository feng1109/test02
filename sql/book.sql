/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.7.33 : Database - book
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`book` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `book`;

/*Table structure for table `oauth_client_details` */

DROP TABLE IF EXISTS `oauth_client_details`;

CREATE TABLE `oauth_client_details` (
  `client_id` varchar(128) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `client_decoding` varchar(50) DEFAULT NULL COMMENT 'client_secret解码',
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  `tenant_code` varchar(255) DEFAULT NULL COMMENT '租户code',
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Data for the table `oauth_client_details` */

insert  into `oauth_client_details`(`client_id`,`resource_ids`,`client_secret`,`client_decoding`,`scope`,`authorized_grant_types`,`web_server_redirect_uri`,`authorities`,`access_token_validity`,`refresh_token_validity`,`additional_information`,`autoapprove`,`tenant_code`) values ('book_whu','book','e10adc3949ba59abbe56e057f20f883e','123456','all','password,authorization_code,refresh_token,sms_code,miniProgram,wx',NULL,NULL,360000,200000,NULL,NULL,'whu');

/*Table structure for table `sys_dict` */

DROP TABLE IF EXISTS `sys_dict`;

CREATE TABLE `sys_dict` (
  `id` int(64) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `dict_name` varchar(100) NOT NULL COMMENT '名称',
  `dict_code` varchar(50) DEFAULT NULL COMMENT '字典编码',
  `description` varchar(100) DEFAULT NULL COMMENT '描述',
  `sort` int(4) DEFAULT NULL COMMENT '排序（升序）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注信息',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `sys_dict_del_flag` (`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

/*Data for the table `sys_dict` */

insert  into `sys_dict`(`id`,`dict_name`,`dict_code`,`description`,`sort`,`create_time`,`update_time`,`remark`,`del_flag`) values (1,'order_rule_type','orderruletype','预约规则类型',NULL,'2021-04-01 09:47:55','2021-04-09 10:09:50',NULL,'0'),(2,'order_list_state','orderliststate','预约订单状态',NULL,'2021-04-09 09:26:01','2021-04-09 09:26:13',NULL,'0'),(3,'blacklist_rule_type','blacklistruletype','黑名单规则类型',NULL,'2021-04-09 10:09:17','2021-04-09 13:54:18',NULL,'0');

/*Table structure for table `sys_dict_item` */

DROP TABLE IF EXISTS `sys_dict_item`;

CREATE TABLE `sys_dict_item` (
  `id` int(50) NOT NULL,
  `dict_id` varchar(20) CHARACTER SET utf8 DEFAULT NULL COMMENT '字典id',
  `item_text` varchar(100) CHARACTER SET utf8 DEFAULT NULL COMMENT '字典项文本',
  `item_value` varchar(100) CHARACTER SET utf8 DEFAULT NULL COMMENT '字典项值',
  `description` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '描述',
  `status` int(11) DEFAULT NULL COMMENT '状态（1启用 0不启用）',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_table_dict_id` (`dict_id`) USING BTREE,
  KEY `index_table_dict_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典详情表';

/*Data for the table `sys_dict_item` */

insert  into `sys_dict_item`(`id`,`dict_id`,`item_text`,`item_value`,`description`,`status`,`create_time`,`update_time`) values (1,'1','预约规则','1','预约规则',NULL,'2021-04-01 09:48:56','2021-04-09 14:05:58'),(2,'1','签到规则','2','签到规则',NULL,'2021-04-01 09:49:18','2021-04-09 14:06:02'),(3,'1','黑名单规则','3','黑名单规则',NULL,'2021-04-01 09:49:27','2021-04-09 14:06:04'),(4,'2','待签到','1','待签到',NULL,'2021-04-09 09:27:07','2021-04-09 14:06:07'),(5,'2','使用中','2','使用中',NULL,'2021-04-09 09:31:48','2021-04-09 14:06:11'),(6,'2','暂离（使用中）','3','暂离（使用中）',NULL,'2021-04-09 09:34:20','2021-04-09 14:06:14'),(7,'2','已完成','4','已完成',NULL,'2021-04-09 09:35:59','2021-04-09 14:06:16'),(9,'2','未签到（违约）','5','未签到（违约）',NULL,'2021-04-09 09:41:58','2021-04-09 14:06:19'),(10,'2','未签退（违约）','6','未签退（违约）',NULL,'2021-04-09 09:42:55','2021-04-09 14:06:21'),(11,'2','暂离未返回（违约）','7','暂离未返回（违约）',NULL,'2021-04-09 09:44:01','2021-04-09 14:06:23'),(12,'3','连续多次未签到','1','连续多次未签到',NULL,'2021-04-09 10:11:14','2021-04-09 14:06:25'),(13,'3','一段时间内多次未签到','2','一段时间内多次未签到',NULL,'2021-04-09 10:42:23','2021-04-09 14:06:27'),(14,'3','一段时间内多次暂离未返回','3','一段时间内多次暂离未返回',NULL,'2021-04-09 10:42:36','2021-04-09 14:06:32'),(15,'3','一段时间内多次未签退','4','一段时间内多次未签退',NULL,'2021-04-09 13:55:12','2021-04-09 14:06:33'),(16,'3','一段时间内多次迟到','5','一段时间内多次迟到',NULL,'2021-04-09 13:56:27','2021-04-09 14:06:35');

/*Table structure for table `sys_resource` */

DROP TABLE IF EXISTS `sys_resource`;

CREATE TABLE `sys_resource` (
  `id` varchar(32) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '资源名称',
  `url` varchar(100) DEFAULT NULL COMMENT '资源地址',
  `pid` varchar(32) DEFAULT NULL COMMENT '父级菜单',
  `permission` varchar(32) DEFAULT NULL COMMENT '权限',
  `resource_type` char(1) DEFAULT NULL COMMENT '资源类型(1:目录 2:菜单 3:按钮)',
  `icon` varchar(100) DEFAULT NULL COMMENT '资源图标',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0:未删除 1:删除 )',
  `create_user` varchar(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_user` varchar(32) DEFAULT NULL COMMENT '更新用户',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单资源表';

/*Data for the table `sys_resource` */

insert  into `sys_resource`(`id`,`name`,`url`,`pid`,`permission`,`resource_type`,`icon`,`sort`,`del_flag`,`create_user`,`create_time`,`update_user`,`update_time`) values ('1','问卷管理','/qusmanage','-1',NULL,NULL,'iconfont icon-wenjuan',1,'0',NULL,NULL,NULL,NULL),('2','问卷列表','/qusmanage/quslist','1',NULL,NULL,NULL,1,'0',NULL,NULL,NULL,NULL),('3','分类管理','/qusmanage/classify','1',NULL,NULL,NULL,2,'0',NULL,NULL,NULL,NULL),('4','回收站','/qusmanage/recycle','1',NULL,NULL,NULL,3,'0',NULL,NULL,NULL,NULL);

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` varchar(32) NOT NULL,
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名称',
  `role_desc` varchar(50) DEFAULT NULL COMMENT '角色描述',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0:未删除 1:删除 )',
  `create_user` varchar(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_user` varchar(32) DEFAULT NULL COMMENT '更新用户',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

/*Data for the table `sys_role` */

/*Table structure for table `sys_role_resource` */

DROP TABLE IF EXISTS `sys_role_resource`;

CREATE TABLE `sys_role_resource` (
  `id` varchar(32) NOT NULL,
  `role_id` varchar(32) DEFAULT NULL COMMENT '角色id',
  `resource_id` varchar(32) DEFAULT NULL COMMENT '资源id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Data for the table `sys_role_resource` */

insert  into `sys_role_resource`(`id`,`role_id`,`resource_id`) values ('1','1','1'),('2','1','2'),('3','1','3'),('4','1','4'),('5','2','2'),('6','2','1');

/*Table structure for table `sys_tenant` */

DROP TABLE IF EXISTS `sys_tenant`;

CREATE TABLE `sys_tenant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '租户id',
  `tenant_name` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '租户名称',
  `datasource_url` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '数据源url',
  `datasource_username` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '数据源用户名',
  `datasource_password` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '数据源密码',
  `datasource_driver` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '数据源驱动',
  `system_account` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '系统账号',
  `system_password` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '账号密码',
  `system_project` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '系统project',
  `status` tinyint(1) DEFAULT NULL COMMENT '是否启用（1是0否）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

/*Data for the table `sys_tenant` */

insert  into `sys_tenant`(`id`,`tenant_code`,`tenant_name`,`datasource_url`,`datasource_username`,`datasource_password`,`datasource_driver`,`system_account`,`system_password`,`system_project`,`status`,`create_time`,`update_time`) values (1,'whu','武汉大学','jdbc:mysql://139.9.90.142:3306/book_whu?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai','esurvey','131415','com.mysql.jdbc.Driver','admin','123456','',1,NULL,NULL);

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `org_id` varchar(32) DEFAULT NULL COMMENT '机构id',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `education` varchar(100) DEFAULT NULL COMMENT '学历',
  `classes` varchar(30) DEFAULT NULL COMMENT '班级',
  `professional` varchar(30) DEFAULT NULL COMMENT '专业',
  `user_type` char(1) DEFAULT NULL COMMENT '用户类型(1:学生 2:教职工)',
  `user_no` varchar(30) DEFAULT NULL COMMENT '用户编号',
  `phone` varchar(15) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱地址',
  `profile` varchar(200) DEFAULT NULL COMMENT '个人简介',
  `status` char(1) DEFAULT '0' COMMENT '用户状态(0:正常 1:注销)',
  `del_flag` char(1) DEFAULT '0' COMMENT '逻辑删除(0:未删除 1:删除 )',
  `create_user` varchar(32) DEFAULT NULL COMMENT '创建用户',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_user` varchar(32) DEFAULT NULL COMMENT '更新用户',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

/*Data for the table `sys_user` */

insert  into `sys_user`(`id`,`org_id`,`username`,`password`,`education`,`classes`,`professional`,`user_type`,`user_no`,`phone`,`email`,`profile`,`status`,`del_flag`,`create_user`,`create_time`,`update_user`,`update_time`) values ('1','2','admin','e10adc3949ba59abbe56e057f20f883e','1',NULL,NULL,'1','110','18817926883','5536235111@qq.com',NULL,'0','0',NULL,NULL,'1','2021-03-23 17:36:50'),('2','1','test','e10adc3949ba59abbe56e057f20f883e','1',NULL,NULL,'2','111',NULL,NULL,NULL,'0','0',NULL,NULL,NULL,NULL),('4','2','boshi','e10adc3949ba59abbe56e057f20f883e','4',NULL,NULL,'1','119','18755555555','18755555555@139.com',NULL,'0','0',NULL,NULL,NULL,NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '用户id',
  `role_id` varchar(32) NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色中间表';

/*Data for the table `sys_user_role` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
