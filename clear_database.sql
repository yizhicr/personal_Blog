-- 清空博客系统数据库中的所有测试数据
-- 注意：这将删除所有表中的数据，但保留表结构

-- 关闭外键检查以避免删除顺序问题
SET FOREIGN_KEY_CHECKS = 0;

-- 按照依赖关系的逆序删除数据
DELETE FROM comments;
DELETE FROM article_tag;  -- 关联表
DELETE FROM article_tags; -- 另一个可能的关联表
DELETE FROM articles;
DELETE FROM tags;
DELETE FROM categories;
DELETE FROM files;
DELETE FROM users;
DELETE FROM test_table; -- 如果有测试表也删除

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 显示确认信息
SELECT 'Database cleared successfully' AS status;