SELECT ua.*
FROM users
CROSS APPLY (
	SELECT TOP(10) ua.user_id, ua.department_id, ua.affinity
	FROM user_departments ua
	JOIN departments d
		ON d.department_id = ua.department_id
	WHERE ua.user_id = users.user_id
	ORDER BY d.orders_support desc
) ua