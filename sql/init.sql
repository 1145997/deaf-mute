-- ============================================
-- 项目名称：基于微信小程序的校园失物招领系统
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- ============================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS campus_lost_found;
CREATE DATABASE campus_lost_found
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE campus_lost_found;

-- 关闭外键检查，便于删除旧表
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 2. 删除旧表（按依赖顺序）
-- ============================================
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS claim_record;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS lost_found;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 3. 用户表
-- ============================================
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    openid VARCHAR(64) NOT NULL COMMENT '微信openid',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '用户昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    student_no VARCHAR(30) DEFAULT NULL COMMENT '学号',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_openid (openid),
    KEY idx_user_phone (phone),
    KEY idx_user_student_no (student_no),
    KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 4. 物品分类表
-- ============================================
CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name),
    KEY idx_category_sort (sort),
    KEY idx_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品分类表';

-- ============================================
-- 5. 管理员表
-- 说明：password 建议后续存加密后的密码（如 BCrypt）
-- 这里先给一个演示值：admin123
-- ============================================
CREATE TABLE admin (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL COMMENT '管理员账号',
    password VARCHAR(255) NOT NULL COMMENT '管理员密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '管理员昵称',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    role VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色 super_admin/admin',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username),
    KEY idx_admin_status (status),
    KEY idx_admin_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ============================================
-- 6. 失物招领信息表
-- type: 1寻物 2招领
-- status: 0待审核 1审核通过/已发布 2已完成 3已驳回 4已下架
-- ============================================
CREATE TABLE lost_found (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '信息ID',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    type TINYINT NOT NULL COMMENT '类型 1寻物 2招领',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    brand VARCHAR(100) DEFAULT NULL COMMENT '品牌',
    color VARCHAR(50) DEFAULT NULL COMMENT '颜色',
    description TEXT COMMENT '详细描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '主图片地址',
    event_time DATETIME DEFAULT NULL COMMENT '丢失/拾取时间',
    event_place VARCHAR(255) DEFAULT NULL COMMENT '丢失/拾取地点',
    contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    contact_wechat VARCHAR(50) DEFAULT NULL COMMENT '微信号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待审核 1已发布 2已完成 3已驳回 4已下架',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    audit_reason VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    audit_admin_id BIGINT DEFAULT NULL COMMENT '审核管理员ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_lost_found_user_id (user_id),
    KEY idx_lost_found_category_id (category_id),
    KEY idx_lost_found_type (type),
    KEY idx_lost_found_status (status),
    KEY idx_lost_found_event_time (event_time),
    KEY idx_lost_found_create_time (create_time),
    KEY idx_lost_found_item_name (item_name),
    KEY idx_lost_found_audit_admin_id (audit_admin_id),
    CONSTRAINT fk_lost_found_user
        FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_lost_found_category
        FOREIGN KEY (category_id) REFERENCES category(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_lost_found_admin
        FOREIGN KEY (audit_admin_id) REFERENCES admin(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失物招领信息表';

-- ============================================
-- 7. 评论表
-- parent_id 为空表示一级评论
-- status: 1正常 0隐藏
-- ============================================
CREATE TABLE comment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    info_id BIGINT NOT NULL COMMENT '失物信息ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0隐藏',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_comment_info_id (info_id),
    KEY idx_comment_user_id (user_id),
    KEY idx_comment_parent_id (parent_id),
    KEY idx_comment_status (status),
    KEY idx_comment_create_time (create_time),
    CONSTRAINT fk_comment_info
        FOREIGN KEY (info_id) REFERENCES lost_found(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_user
        FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_id) REFERENCES comment(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ============================================
-- 8. 认领记录表（可选但推荐）
-- status: 0申请中 1已确认 2已拒绝 3已取消
-- ============================================
CREATE TABLE claim_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '认领记录ID',
    info_id BIGINT NOT NULL COMMENT '失物信息ID',
    claimant_id BIGINT NOT NULL COMMENT '认领人ID',
    message VARCHAR(255) DEFAULT NULL COMMENT '认领说明',
    proof_image VARCHAR(255) DEFAULT NULL COMMENT '证明图片',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0申请中 1已确认 2已拒绝 3已取消',
    handle_admin_id BIGINT DEFAULT NULL COMMENT '处理管理员ID',
    handle_reason VARCHAR(255) DEFAULT NULL COMMENT '处理说明',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_claim_info_id (info_id),
    KEY idx_claim_claimant_id (claimant_id),
    KEY idx_claim_status (status),
    KEY idx_claim_handle_admin_id (handle_admin_id),
    KEY idx_claim_create_time (create_time),
    CONSTRAINT fk_claim_info
        FOREIGN KEY (info_id) REFERENCES lost_found(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_claim_user
        FOREIGN KEY (claimant_id) REFERENCES user(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_claim_admin
        FOREIGN KEY (handle_admin_id) REFERENCES admin(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认领记录表';

-- ============================================
-- 9. 公告表
-- status: 1发布 0隐藏
-- ============================================
CREATE TABLE notice (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(100) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶 0否 1是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1发布 0隐藏',
    publish_admin_id BIGINT DEFAULT NULL COMMENT '发布管理员ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_notice_is_top (is_top),
    KEY idx_notice_status (status),
    KEY idx_notice_create_time (create_time),
    KEY idx_notice_publish_admin_id (publish_admin_id),
    CONSTRAINT fk_notice_admin
        FOREIGN KEY (publish_admin_id) REFERENCES admin(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================
-- 10. 管理操作日志表（推荐）
-- ============================================
CREATE TABLE operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    admin_id BIGINT DEFAULT NULL COMMENT '管理员ID',
    module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    request_method VARCHAR(20) DEFAULT NULL COMMENT '请求方式',
    request_url VARCHAR(255) DEFAULT NULL COMMENT '请求地址',
    ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_operation_log_admin_id (admin_id),
    KEY idx_operation_log_module (module),
    KEY idx_operation_log_operation_type (operation_type),
    KEY idx_operation_log_create_time (create_time),
    CONSTRAINT fk_operation_log_admin
        FOREIGN KEY (admin_id) REFERENCES admin(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理操作日志表';

-- ============================================
-- 11. 初始化分类数据
-- ============================================
INSERT INTO category (name, sort, status) VALUES
('证件', 1, 1),
('电子产品', 2, 1),
('书籍资料', 3, 1),
('衣物饰品', 4, 1),
('钥匙', 5, 1),
('校园卡', 6, 1),
('钱包', 7, 1),
('雨伞', 8, 1),
('水杯', 9, 1),
('其他', 10, 1);

-- ============================================
-- 12. 初始化管理员数据
-- 注意：正式项目中密码请务必加密存储
-- 这里 admin123 仅用于课程设计演示
-- ============================================
INSERT INTO admin (username, password, nickname, phone, role, status)
VALUES ('admin', 'admin123', '系统管理员', '13800000000', 'super_admin', 1);

-- ============================================
-- 13. 初始化公告数据
-- ============================================
INSERT INTO notice (title, content, is_top, status, publish_admin_id) VALUES
(
    '校园失物招领平台上线通知',
    '欢迎使用校园失物招领系统。请同学们在发布寻物或招领信息时，尽量填写完整的物品名称、时间、地点和联系方式，以便提高找回效率。',
    1,
    1,
    1
),
(
    '信息发布须知',
    '请勿发布虚假信息、广告信息或与失物招领无关的内容。管理员会对所有发布内容进行审核，违规信息将被驳回或下架。',
    0,
    1,
    1
);

-- ============================================
-- 14. 初始化测试用户（可选）
-- 真正接微信登录后可不需要这些数据
-- ============================================
INSERT INTO user (openid, nickname, avatar, real_name, student_no, phone, email, gender, status)
VALUES
('test_openid_001', '张三', 'https://example.com/avatar1.png', '张三', '20230001', '13900000001', 'zhangsan@example.com', 1, 1),
('test_openid_002', '李四', 'https://example.com/avatar2.png', '李四', '20230002', '13900000002', 'lisi@example.com', 2, 1);

-- ============================================
-- 15. 初始化测试失物招领信息（可选）
-- ============================================
INSERT INTO lost_found (
    user_id, type, title, item_name, category_id, brand, color, description,
    image, event_time, event_place, contact_name, contact_phone, contact_wechat,
    status, view_count, audit_admin_id, audit_time
) VALUES
(
    1, 1, '寻找丢失的校园卡', '校园卡', 6, NULL, '蓝色',
    '本人于图书馆三楼自习区附近丢失校园卡一张，如有拾到请联系我，非常感谢。',
    'https://example.com/lost1.jpg', '2026-03-28 15:30:00', '图书馆三楼',
    '张三', '13900000001', 'wechat_zhangsan',
    1, 12, 1, NOW()
),
(
    2, 2, '招领一把宿舍钥匙', '钥匙', 5, NULL, '银色',
    '在第二教学楼门口捡到一把钥匙，请失主凭特征联系认领。',
    'https://example.com/found1.jpg', '2026-03-29 08:20:00', '第二教学楼门口',
    '李四', '13900000002', 'wechat_lisi',
    1, 8, 1, NOW()
);

-- ============================================
-- 16. 初始化测试评论（可选）
-- ============================================
INSERT INTO comment (info_id, user_id, parent_id, content, status)
VALUES
(1, 2, NULL, '我好像在一楼服务台附近看到过，可以去问问。', 1),
(1, 1, 1, '好的，谢谢你，我去看看。', 1);

-- ============================================
-- 17. 初始化测试认领记录（可选）
-- ============================================
INSERT INTO claim_record (
    info_id, claimant_id, message, proof_image, status, handle_admin_id, handle_reason, handle_time
) VALUES
(
    2, 1, '这把钥匙可能是我的，钥匙扣上有一个黑色挂件。', NULL,
    0, NULL, NULL, NULL
);

-- ============================================
-- 18. 初始化操作日志（可选）
-- ============================================
INSERT INTO operation_log (
    admin_id, module, operation_type, operation_desc, request_method, request_url, ip
) VALUES
(1, '管理员模块', '登录', '管理员登录系统', 'POST', '/admin/login', '127.0.0.1'),
(1, '公告模块', '新增', '新增系统公告', 'POST', '/admin/notice/add', '127.0.0.1');

-- ============================================
-- 19. 视图（可选，方便统计）
-- ============================================

-- 已发布信息统计视图
CREATE OR REPLACE VIEW v_published_info AS
SELECT
    lf.id,
    lf.user_id,
    u.nickname AS user_nickname,
    lf.type,
    lf.title,
    lf.item_name,
    c.name AS category_name,
    lf.event_place,
    lf.event_time,
    lf.status,
    lf.view_count,
    lf.create_time
FROM lost_found lf
LEFT JOIN user u ON lf.user_id = u.id
LEFT JOIN category c ON lf.category_id = c.id
WHERE lf.status = 1;

-- 分类统计视图
CREATE OR REPLACE VIEW v_category_stat AS
SELECT
    c.id AS category_id,
    c.name AS category_name,
    COUNT(lf.id) AS total_count
FROM category c
LEFT JOIN lost_found lf ON c.id = lf.category_id
GROUP BY c.id, c.name;

-- ============================================
-- 20. 存储过程（可选，方便统计）
-- ============================================

DELIMITER $$

CREATE PROCEDURE sp_dashboard_stat()
BEGIN
    SELECT
        (SELECT COUNT(*) FROM user WHERE status = 1) AS total_users,
        (SELECT COUNT(*) FROM lost_found) AS total_infos,
        (SELECT COUNT(*) FROM lost_found WHERE type = 1) AS total_lost,
        (SELECT COUNT(*) FROM lost_found WHERE type = 2) AS total_found,
        (SELECT COUNT(*) FROM lost_found WHERE status = 0) AS pending_infos,
        (SELECT COUNT(*) FROM lost_found WHERE status = 1) AS published_infos,
        (SELECT COUNT(*) FROM lost_found WHERE status = 2) AS finished_infos,
        (SELECT COUNT(*) FROM notice WHERE status = 1) AS total_notices;
END$$

DELIMITER ;

-- ============================================
-- 21. 常用查询示例（注释保留，后续开发方便）
-- ============================================

-- 查询所有已发布的寻物信息
-- SELECT * FROM lost_found WHERE type = 1 AND status = 1 ORDER BY create_time DESC;

-- 查询所有已发布的招领信息
-- SELECT * FROM lost_found WHERE type = 2 AND status = 1 ORDER BY create_time DESC;

-- 查询某用户发布的信息
-- SELECT * FROM lost_found WHERE user_id = 1 ORDER BY create_time DESC;

-- 查询某条信息的评论
-- SELECT * FROM comment WHERE info_id = 1 ORDER BY create_time ASC;

-- 调用首页统计存储过程
-- CALL sp_dashboard_stat();