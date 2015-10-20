INSERT INTO assetstore 
SELECT 1, 'main', 'LOCAL', '{"root":"CHANGE_ME/webapps/shepherd_data_dir","webroot":"http://CHANGE_ME/shepherd_data_dir"}', true
WHERE NOT EXISTS (
   SELECT *
   FROM assetstore
   WHERE id = 1
   );
