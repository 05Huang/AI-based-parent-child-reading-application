-- 添加测试用户（ID: -999）到数据库
-- 修正版本：使用正确的字段名 birth_date

INSERT INTO `user` (
    `id`, 
    `username`, 
    `email`, 
    `phone`, 
    `password`, 
    `nickname`, 
    `avatar`, 
    `avatar_thumb`, 
    `sex`, 
    `user_type`, 
    `role`, 
    `birth_date`, 
    `interests`, 
    `status`, 
    `wechat_id`, 
    `created_time`, 
    `updated_time`
) VALUES (
    -999,
    'test_system_user',
    'test@example.com',
    '13800000000',
    '$2a$10$dummyPasswordHashForTestUser',
    '系统测试用户',
    'https://ui-avatars.com/api/?name=系统&background=3b82f6&color=fff&size=128',
    'https://ui-avatars.com/api/?name=系统&background=3b82f6&color=fff&size=64',
    0,
    1,
    1,
    '1990-01-01',
    '内容管理,系统测试',
    1,
    NULL,
    '2025-09-24 12:00:00',
    '2025-09-24 12:00:00'
);

-- 验证插入是否成功
SELECT id, username, nickname, status FROM `user` WHERE id = -999; 