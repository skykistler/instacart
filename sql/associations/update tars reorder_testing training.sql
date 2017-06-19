UPDATE reorder_testing
	SET tars_dpt_support = b.tars_dpt_support,
		tars_user_support = b.tars_user_support,
		tars_occurrences_left = b.tars_occurrences_left

FROM (
	SELECT 
		user_id, y, 
		tars_dpt_support = support / (select cast(max(support) as real) from user_potential_products upp2 where upp2.department_cluster = upp.department_cluster), 
		tars_user_support = support / (select cast(max(support) as real) from user_potential_products upp2 where upp2.user_id = upp.user_id), 
		tars_occurrences_left = occurrences_left
	FROM user_potential_products upp
) as b

WHERE b.user_id = reorder_testing.user_id
AND   b.y = reorder_testing.product_id;

UPDATE reorder_training
	SET tars_dpt_support = b.tars_dpt_support,
		tars_user_support = b.tars_user_support,
		tars_occurrences_left = b.tars_occurrences_left

FROM (
	SELECT 
		user_id, y, 
		tars_dpt_support = support / (select cast(max(support) as real) from user_potential_products upp2 where upp2.department_cluster = upp.department_cluster), 
		tars_user_support = support / (select cast(max(support) as real) from user_potential_products upp2 where upp2.user_id = upp.user_id), 
		tars_occurrences_left = occurrences_left
	FROM user_potential_products upp
) as b

WHERE b.user_id = reorder_training.user_id
AND   b.y = reorder_training.product_id;

-------------------------------------------

UPDATE reorder_testing
	SET tars_user_support = -1,
		tars_occurrences_left = -1000,
		tars_dpt_support = -1
WHERE tars_user_support is null or tars_dpt_support is null;

UPDATE reorder_training
	SET tars_user_support = -1,
		tars_occurrences_left = -1000,
		tars_dpt_support = -1
WHERE tars_user_support is null or tars_dpt_support is null;