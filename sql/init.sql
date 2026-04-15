-- ============================================
-- Project: Smart Sign Language Translation Tool
-- Scope: phase1 platform + phase2 first batch
-- Database: MySQL 8.0+
-- ============================================

DROP DATABASE IF EXISTS deafmute;
CREATE DATABASE deafmute
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE deafmute;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS recognition_config;
DROP TABLE IF EXISTS gesture_flow_output;
DROP TABLE IF EXISTS gesture_flow_node;
DROP TABLE IF EXISTS gesture_flow;
DROP TABLE IF EXISTS gesture_library;
DROP TABLE IF EXISTS phrase_template;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS post_image;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    openid VARCHAR(64) NOT NULL COMMENT '微信openid',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
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

CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    type VARCHAR(20) NOT NULL COMMENT '分类类型 POST/LEARNING',
    icon VARCHAR(255) DEFAULT NULL COMMENT '图标',
    description VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name_type (name, type),
    KEY idx_category_type (type),
    KEY idx_category_sort (sort),
    KEY idx_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用分类表';

CREATE TABLE post (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    cover_image VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待审核 1已发布 2已驳回 3已下架',
    audit_reason VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    audit_admin_id BIGINT DEFAULT NULL COMMENT '审核管理员ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    source_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '来源 MANUAL/RECOGNITION',
    source_record_id BIGINT DEFAULT NULL COMMENT '来源识别记录ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_post_user_id (user_id),
    KEY idx_post_category_id (category_id),
    KEY idx_post_status (status),
    KEY idx_post_audit_admin_id (audit_admin_id),
    KEY idx_post_source_type (source_type),
    KEY idx_post_create_time (create_time),
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_post_admin FOREIGN KEY (audit_admin_id) REFERENCES admin(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

CREATE TABLE post_image (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    image_url VARCHAR(255) NOT NULL COMMENT '图片地址',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_post_image_post_id (post_id),
    CONSTRAINT fk_post_image_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子图片表';

CREATE TABLE comment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0隐藏',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_comment_post_id (post_id),
    KEY idx_comment_user_id (user_id),
    KEY idx_comment_parent_id (parent_id),
    KEY idx_comment_status (status),
    KEY idx_comment_create_time (create_time),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE phrase_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '短语模板ID',
    phrase_code VARCHAR(50) NOT NULL COMMENT '短语编码',
    phrase_text VARCHAR(255) NOT NULL COMMENT '短语文本',
    tts_text VARCHAR(255) DEFAULT NULL COMMENT '播报文本',
    scene_type VARCHAR(50) DEFAULT NULL COMMENT '场景类型',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_phrase_template_code (phrase_code),
    KEY idx_phrase_template_status (status),
    KEY idx_phrase_template_scene_type (scene_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短语模板表';

CREATE TABLE gesture_library (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '基础手势ID',
    gesture_code VARCHAR(50) NOT NULL COMMENT '手势编码',
    gesture_name VARCHAR(50) NOT NULL COMMENT '手势名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    preview_image VARCHAR(255) DEFAULT NULL COMMENT '预览图',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    is_builtin TINYINT NOT NULL DEFAULT 1 COMMENT '是否系统内置 1是 0否',
    detection_key VARCHAR(50) NOT NULL COMMENT '基础识别器key',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gesture_library_code (gesture_code),
    UNIQUE KEY uk_gesture_library_detection_key (detection_key),
    KEY idx_gesture_library_status (status),
    KEY idx_gesture_library_builtin (is_builtin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基础手势库';

CREATE TABLE recognition_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '识别配置ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    confidence_min DECIMAL(5,4) NOT NULL DEFAULT 0.5000 COMMENT '最小置信度',
    hold_ms INT NOT NULL DEFAULT 300 COMMENT '持续命中时长',
    debounce_ms INT NOT NULL DEFAULT 500 COMMENT '防抖时间',
    cooldown_ms INT NOT NULL DEFAULT 1000 COMMENT '冷却时间',
    required_hits INT NOT NULL DEFAULT 3 COMMENT '连续命中次数',
    max_interval_ms INT NOT NULL DEFAULT 1500 COMMENT '动作节点最大间隔',
    lock_timeout_ms INT NOT NULL DEFAULT 3000 COMMENT '锁定超时时间',
    reset_on_fail TINYINT NOT NULL DEFAULT 1 COMMENT '失败后是否重置 1是 0否',
    allow_repeat TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许重复触发 1是 0否',
    gesture_order_json JSON DEFAULT NULL COMMENT '基础手势优先级顺序',
    active_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否生效 1是 0否',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recognition_config_name (config_name),
    KEY idx_recognition_config_active_flag (active_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局识别配置';

CREATE TABLE gesture_flow (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '动作流ID',
    flow_code VARCHAR(50) NOT NULL COMMENT '动作流编码',
    flow_name VARCHAR(100) NOT NULL COMMENT '动作流名称',
    flow_type VARCHAR(20) NOT NULL COMMENT '动作流类型 SINGLE/SEQUENCE/CONTROL',
    trigger_mode VARCHAR(20) NOT NULL DEFAULT 'STATE_MACHINE' COMMENT '触发模式 DIRECT/STATE_MACHINE',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级',
    version_no INT NOT NULL DEFAULT 1 COMMENT '版本号',
    start_node_id BIGINT DEFAULT NULL COMMENT '起始节点ID',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    is_builtin TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置 1是 0否',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gesture_flow_code (flow_code),
    KEY idx_gesture_flow_type (flow_type),
    KEY idx_gesture_flow_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动作流定义表';

CREATE TABLE gesture_flow_node (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    flow_id BIGINT NOT NULL COMMENT '动作流ID',
    node_code VARCHAR(50) NOT NULL COMMENT '节点编码',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    parent_node_id BIGINT DEFAULT NULL COMMENT '父节点ID',
    gesture_library_id BIGINT NOT NULL COMMENT '基础手势ID',
    is_start TINYINT NOT NULL DEFAULT 0 COMMENT '是否起始节点 1是 0否',
    is_end TINYINT NOT NULL DEFAULT 0 COMMENT '是否结束节点 1是 0否',
    node_order INT NOT NULL DEFAULT 0 COMMENT '同级排序',
    confidence_min DECIMAL(5,4) DEFAULT NULL COMMENT '最小置信度',
    hold_ms INT DEFAULT NULL COMMENT '持续命中时长',
    debounce_ms INT DEFAULT NULL COMMENT '防抖时间',
    cooldown_ms INT DEFAULT NULL COMMENT '冷却时间',
    required_hits INT DEFAULT NULL COMMENT '连续命中次数',
    max_interval_ms INT DEFAULT NULL COMMENT '最大间隔',
    reset_on_fail TINYINT DEFAULT NULL COMMENT '失败后是否重置',
    allow_repeat TINYINT DEFAULT NULL COMMENT '是否允许重复触发',
    success_next_strategy VARCHAR(20) NOT NULL DEFAULT 'CHILDREN' COMMENT '成功后流转策略',
    fail_strategy VARCHAR(20) NOT NULL DEFAULT 'RESET' COMMENT '失败策略',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_gesture_flow_node_code (flow_id, node_code),
    KEY idx_gesture_flow_node_flow_id (flow_id),
    KEY idx_gesture_flow_node_parent_node_id (parent_node_id),
    CONSTRAINT fk_gesture_flow_node_flow FOREIGN KEY (flow_id) REFERENCES gesture_flow(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_gesture_flow_node_parent FOREIGN KEY (parent_node_id) REFERENCES gesture_flow_node(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_gesture_flow_node_library FOREIGN KEY (gesture_library_id) REFERENCES gesture_library(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动作流节点表';

CREATE TABLE gesture_flow_output (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '输出ID',
    flow_id BIGINT NOT NULL COMMENT '动作流ID',
    end_node_id BIGINT DEFAULT NULL COMMENT '结束节点ID',
    output_type VARCHAR(20) NOT NULL COMMENT '输出类型 TEXT/PHRASE/CONTROL/NONE',
    output_text VARCHAR(255) DEFAULT NULL COMMENT '直接输出文本',
    phrase_template_id BIGINT DEFAULT NULL COMMENT '短语模板ID',
    control_action VARCHAR(30) DEFAULT NULL COMMENT '控制动作 DELETE_LAST/CLEAR_ALL/FINISH_INPUT/CONFIRM/CANCEL',
    tts_text VARCHAR(255) DEFAULT NULL COMMENT '播报文本',
    display_text VARCHAR(255) DEFAULT NULL COMMENT '展示文本',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_gesture_flow_output_flow_id (flow_id),
    CONSTRAINT fk_gesture_flow_output_flow FOREIGN KEY (flow_id) REFERENCES gesture_flow(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_gesture_flow_output_end_node FOREIGN KEY (end_node_id) REFERENCES gesture_flow_node(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_gesture_flow_output_phrase FOREIGN KEY (phrase_template_id) REFERENCES phrase_template(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动作流输出表';

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
    CONSTRAINT fk_notice_admin FOREIGN KEY (publish_admin_id) REFERENCES admin(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

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
    CONSTRAINT fk_operation_log_admin FOREIGN KEY (admin_id) REFERENCES admin(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

INSERT INTO admin (username, password, nickname, phone, role, status)
VALUES ('admin', 'admin123', '系统管理员', '13800000000', 'super_admin', 1);

INSERT INTO user (openid, nickname, avatar, real_name, student_no, phone, email, gender, status)
VALUES
('test_openid_001', '张三', 'https://example.com/avatar1.png', '张三', '20230001', '13900000001', 'zhangsan@example.com', 1, 1),
('test_openid_002', '李四', 'https://example.com/avatar2.png', '李四', '20230002', '13900000002', 'lisi@example.com', 2, 1);

INSERT INTO category (name, type, icon, description, sort, status)
VALUES
('交流分享', 'POST', NULL, '社区交流帖子', 1, 1),
('经验求助', 'POST', NULL, '经验交流与求助', 2, 1),
('入门学习', 'LEARNING', NULL, '初级手语学习', 1, 1),
('进阶练习', 'LEARNING', NULL, '中高级练习内容', 2, 1);

INSERT INTO post (
    user_id, category_id, title, content, cover_image,
    status, audit_reason, audit_admin_id, audit_time,
    view_count, comment_count, like_count, source_type, source_record_id
) VALUES
(
    1, 1, '手语学习打卡第一天',
    '今天开始做手语学习记录，欢迎大家一起交流基础词汇和练习方法。',
    'https://example.com/post1.jpg',
    1, NULL, 1, NOW(),
    18, 2, 0, 'MANUAL', NULL
),
(
    2, 2, '想请教一个问候动作怎么练',
    '最近在练习日常问候相关手语，想请教一下动作切换时有没有更顺手的方法。',
    'https://example.com/post2.jpg',
    0, NULL, NULL, NULL,
    3, 0, 0, 'MANUAL', NULL
);

INSERT INTO post_image (post_id, image_url, sort)
VALUES
(1, 'https://example.com/post1.jpg', 1),
(2, 'https://example.com/post2.jpg', 1);

INSERT INTO comment (post_id, user_id, parent_id, content, status)
VALUES
(1, 2, NULL, '可以先从固定问候词开始练，动作节奏稳定最重要。', 1),
(1, 1, 1, '收到，谢谢建议，我继续练。', 1);

INSERT INTO phrase_template (phrase_code, phrase_text, tts_text, scene_type, status, sort)
VALUES
('hello', '你好', '你好', 'daily', 1, 1),
('thanks', '谢谢', '谢谢', 'daily', 1, 2),
('confirm', '确认', '确认', 'control', 1, 3),
('cancel', '取消', '取消', 'control', 1, 4);

INSERT INTO gesture_library (gesture_code, gesture_name, description, preview_image, status, sort, is_builtin, detection_key)
VALUES
('is_thumbs_up', '点赞手势', '基础点赞手势', NULL, 1, 1, 1, 'is_thumbs_up'),
('is_v_sign', 'V手势', '剪刀手', NULL, 1, 2, 1, 'is_v_sign'),
('is_four_sign', '四手势', '四指展开', NULL, 1, 3, 1, 'is_four_sign'),
('is_fist', '握拳手势', '四指收拢', NULL, 1, 4, 1, 'is_fist'),
('is_ok_sign', 'OK手势', '拇指食指闭合', NULL, 1, 5, 1, 'is_ok_sign');

INSERT INTO recognition_config (
    config_name, confidence_min, hold_ms, debounce_ms, cooldown_ms,
    required_hits, max_interval_ms, lock_timeout_ms,
    reset_on_fail, allow_repeat, gesture_order_json, active_flag, remark
) VALUES (
    '默认识别配置', 0.5000, 300, 500, 1000,
    3, 1500, 3000, 1, 0,
    JSON_ARRAY('is_thumbs_up', 'is_v_sign', 'is_four_sign', 'is_fist', 'is_ok_sign'),
    1, '从 blind 原型默认参数迁移'
);

INSERT INTO gesture_flow (
    flow_code, flow_name, flow_type, trigger_mode, status, priority, version_no, start_node_id, description, is_builtin
) VALUES
('single_hello', '单动作-你好', 'SINGLE', 'DIRECT', 1, 100, 1, NULL, 'V手势直接输出你好', 1),
('flow_thanks', '动作流-谢谢', 'SEQUENCE', 'STATE_MACHINE', 1, 90, 1, NULL, '四手势后接V手势输出谢谢', 1);

INSERT INTO gesture_flow_node (
    flow_id, node_code, node_name, parent_node_id, gesture_library_id,
    is_start, is_end, node_order, required_hits, remark
)
SELECT id, 'start', '起始节点', NULL, (
    SELECT gl.id FROM gesture_library gl WHERE gl.gesture_code = 'is_v_sign'
), 1, 1, 1, 3, '单动作节点'
FROM gesture_flow
WHERE flow_code = 'single_hello';

INSERT INTO gesture_flow_node (
    flow_id, node_code, node_name, parent_node_id, gesture_library_id,
    is_start, is_end, node_order, required_hits, remark
)
SELECT id, 'n1', '第一节点', NULL, (
    SELECT gl.id FROM gesture_library gl WHERE gl.gesture_code = 'is_four_sign'
), 1, 0, 1, 3, '动作流起始节点'
FROM gesture_flow
WHERE flow_code = 'flow_thanks';

INSERT INTO gesture_flow_node (
    flow_id, node_code, node_name, parent_node_id, gesture_library_id,
    is_start, is_end, node_order, required_hits, max_interval_ms, remark
)
SELECT gf.id, 'n2', '第二节点', parent_node.id, child_gesture.id,
    0, 1, 2, 3, 1500, '动作流结束节点'
FROM gesture_flow gf
JOIN gesture_flow_node parent_node ON parent_node.flow_id = gf.id AND parent_node.node_code = 'n1'
JOIN gesture_library child_gesture ON child_gesture.gesture_code = 'is_v_sign'
WHERE gf.flow_code = 'flow_thanks';

UPDATE gesture_flow gf
SET start_node_id = (
    SELECT gfn.id FROM gesture_flow_node gfn
    WHERE gfn.flow_id = gf.id
      AND gfn.is_start = 1
    ORDER BY gfn.id
    LIMIT 1
);

INSERT INTO gesture_flow_output (
    flow_id, end_node_id, output_type, output_text, display_text, tts_text
)
SELECT gf.id, gfn.id, 'TEXT', '你好', '你好', '你好'
FROM gesture_flow gf
JOIN gesture_flow_node gfn ON gfn.flow_id = gf.id AND gfn.node_code = 'start'
WHERE gf.flow_code = 'single_hello';

INSERT INTO gesture_flow_output (
    flow_id, end_node_id, output_type, output_text, display_text, tts_text
)
SELECT gf.id, gfn.id, 'PHRASE', '谢谢', '谢谢', '谢谢'
FROM gesture_flow gf
JOIN gesture_flow_node gfn ON gfn.flow_id = gf.id AND gfn.node_code = 'n2'
WHERE gf.flow_code = 'flow_thanks';

INSERT INTO notice (title, content, is_top, status, publish_admin_id)
VALUES
(
    '智能手语翻译工具平台上线通知',
    '欢迎使用智能手语翻译工具一期版本。当前已开放社区发帖、评论互动、公告查看和后台审核能力。',
    1,
    1,
    1
),
(
    '社区内容发布说明',
    '请勿发布与手语学习、交流无关的内容。平台会对帖子进行审核，违规内容会被驳回或下架。',
    0,
    1,
    1
);

INSERT INTO operation_log (
    admin_id, module, operation_type, operation_desc, request_method, request_url, ip
) VALUES
(1, 'admin', 'login', '管理员登录系统', 'POST', '/api/admin/login', '127.0.0.1'),
(1, 'notice', 'add', '新增系统公告', 'POST', '/api/admin/notice', '127.0.0.1');
