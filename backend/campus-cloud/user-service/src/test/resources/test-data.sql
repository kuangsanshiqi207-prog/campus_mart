-- 清理旧数据
DELETE FROM user WHERE username IN ('testuser', 'buyer', 'seller', 'other');
DELETE FROM admin WHERE username = 'admin';

-- 用户表（明文密码统一为 123456）
INSERT INTO user (id, username, password, nickname, phone, email, role, status, certified, credit_score, create_time, update_time, deleted)
VALUES
    (1, 'testuser', '$2a$10$/Ycx3Kngf.whGVNXRrxI5OKkBLoH7AdqbIfhCcrz4IOyEoMrjH0qq', '测试用户', '13800000001', 'test@example.com', 'USER', 'normal', 0, 100, NOW(), NOW(), 0),
    (2, 'buyer',    '$2a$10$/Ycx3Kngf.whGVNXRrxI5OKkBLoH7AdqbIfhCcrz4IOyEoMrjH0qq', '买家',    '13800000002', NULL,               'USER', 'normal', 0, 100, NOW(), NOW(), 0),
    (3, 'seller',   '$2a$10$/Ycx3Kngf.whGVNXRrxI5OKkBLoH7AdqbIfhCcrz4IOyEoMrjH0qq', '卖家',    '13800000003', NULL,               'USER', 'normal', 0, 100, NOW(), NOW(), 0),
    (4, 'other',    '$2a$10$/Ycx3Kngf.whGVNXRrxI5OKkBLoH7AdqbIfhCcrz4IOyEoMrjH0qq', '路人',    '13800000004', NULL,               'USER', 'normal', 0, 100, NOW(), NOW(), 0);

-- 管理员表
INSERT INTO admin (id, username, password, nickname, role, status, create_time, update_time, deleted)
VALUES (1, 'admin', '$2a$10$/Ycx3Kngf.whGVNXRrxI5OKkBLoH7AdqbIfhCcrz4IOyEoMrjH0qq', '超级管理员', 'SUPER_ADMIN', 'normal', NOW(), NOW(), 0);