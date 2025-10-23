-- 修复已生成的测试数据的creator_id问题
-- 将creator_id从1改为-999（测试管理员）

-- 查看需要修复的记录
SELECT id, title, creator_id, type, created_time 
FROM content 
WHERE creator_id = 1 
ORDER BY id;

-- 修复：更新creator_id
UPDATE content 
SET creator_id = -999 
WHERE creator_id = 1;

-- 验证修复结果
SELECT id, title, creator_id, type, created_time 
FROM content 
WHERE id >= 73 
ORDER BY id;

