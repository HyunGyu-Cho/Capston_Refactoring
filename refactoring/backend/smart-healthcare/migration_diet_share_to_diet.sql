-- PostCategory 정리: DIET_SHARE를 DIET로 마이그레이션
-- 실행 전 백업 권장: CREATE TABLE community_post_backup AS SELECT * FROM community_post;

-- 1. DIET_SHARE 카테고리를 DIET로 변경
UPDATE community_post 
SET category = 'DIET' 
WHERE category = 'DIET_SHARE';

-- 2. 변경된 레코드 수 확인
SELECT 
    category,
    COUNT(*) as count
FROM community_post 
GROUP BY category
ORDER BY category;

-- 3. 마이그레이션 완료 후 DIET_SHARE 카테고리가 남아있는지 확인
SELECT COUNT(*) as remaining_diet_share
FROM community_post 
WHERE category = 'DIET_SHARE';
