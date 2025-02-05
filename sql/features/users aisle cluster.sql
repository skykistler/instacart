UPDATE users
	SET users.aisle_cluster = b.aisle_cluster

FROM (
SELECT a.*
FROM users us
CROSS APPLY (
	SELECT TOP(1) 
		 u.user_id
		,aisle_cluster = CONCAT(fa.aisle_id_1, ' ', fa.aisle_id_2)-- + ' ' + fa.aisle_id_3
		,cluster_affinity = (ua1.affinity + ua2.affinity)-- + ua3.affinity)
	FROM users u
	LEFT JOIN frequent_aisles fa
		ON fa.aisle_id_1 IN (
				SELECT aisle_id
				FROM user_aisles ua
				WHERE ua.user_id = u.user_id
			) AND
			fa.aisle_id_2 IN (
				SELECT aisle_id
				FROM user_aisles ua
				WHERE ua.user_id = u.user_id
			) 
			--AND fa.aisle_id_3 IN (
			--	SELECT aisle_id
			--	FROM user_aisles ua
			--	WHERE ua.user_id = u.user_id
			--)

	LEFT JOIN user_aisles ua1
		ON ua1.user_id = u.user_id
		AND ua1.aisle_id = fa.aisle_id_1

	LEFT JOIN user_aisles ua2
		ON ua2.user_id = u.user_id
		AND ua2.aisle_id = fa.aisle_id_2
	
	--LEFT JOIN user_aisles ua3
	--	ON ua3.user_id = u.user_id
	--	AND ua3.aisle_id = fa.aisle_id_3

	WHERE u.user_id = us.user_id

	ORDER BY cluster_affinity desc, support desc
) as a
) as b

WHERE b.user_id = users.user_id;

UPDATE users
	SET aisle_cluster = b.aisle_id
FROM (
	SELECT ua.*
	FROM user_aisles ua
	WHERE ua.aisle_id = (
		SELECT TOP 1 ua2.aisle_id
		FROM user_aisles ua2
		WHERE ua2.user_id = ua.user_id
		ORDER BY ua2.affinity desc
	)
) as b
WHERE b.user_id = users.user_id 
AND users.aisle_cluster = '';