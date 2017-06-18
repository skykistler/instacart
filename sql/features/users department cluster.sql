UPDATE users
	SET users.department_cluster = b.department_cluster

FROM (
SELECT a.*
FROM users us
CROSS APPLY (
	SELECT TOP(1) 
		 u.user_id
		,department_cluster = CONCAT(fd.department_id_1, ' ', fd.department_id_2, ' ', fd.department_id_3)
		,cluster_affinity = (ud1.affinity + ud2.affinity + ud3.affinity)
	FROM users u
	LEFT JOIN frequent_departments fd
		ON fd.department_id_1 IN (
				SELECT department_id
				FROM user_departments ud
				WHERE ud.user_id = u.user_id
			) AND
			fd.department_id_2 IN (
				SELECT department_id
				FROM user_departments ud
				WHERE ud.user_id = u.user_id
			) AND 
			fd.department_id_3 IN (
				SELECT department_id
				FROM user_departments ud
				WHERE ud.user_id = u.user_id
			)

	LEFT JOIN user_departments ud1
		ON ud1.user_id = u.user_id
		AND ud1.department_id = fd.department_id_1

	LEFT JOIN user_departments ud2
		ON ud2.user_id = u.user_id
		AND ud2.department_id = fd.department_id_2
	
	LEFT JOIN user_departments ud3
		ON ud3.user_id = u.user_id
		AND ud3.department_id = fd.department_id_3

	WHERE u.user_id = us.user_id

	ORDER BY cluster_affinity desc, support desc
) as a
) as b

WHERE b.user_id = users.user_id;

UPDATE users
	SET department_cluster = b.department_id
FROM (
	SELECT ud.*
	FROM user_departments ud
	WHERE ud.department_id = (
		SELECT TOP 1 ud2.department_id
		FROM user_departments ud2
		WHERE ud2.user_id = ud.user_id
		ORDER BY ud2.affinity desc
	)
) as b
WHERE b.user_id = users.user_id 
AND users.department_cluster = '';