SELECT ua.*
FROM users
CROSS APPLY (
	SELECT TOP(10) ua.user_id, ua.aisle_id, ua.affinity
	FROM user_aisles ua
	JOIN aisles a
		ON a.aisle_id = ua.aisle_id
	WHERE ua.user_id = users.user_id
	ORDER BY a.orders_support desc
) ua