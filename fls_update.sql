ALTER TABLE `requests` DROP PRIMARY KEY, ADD PRIMARY KEY( `request_id`, `request_requser_id`, `request_item_id`);

ALTER TABLE items MODIFY item_image MEDIUMTEXT;

ALTER TABLE items MODIFY item_desc VARCHAR(255) null