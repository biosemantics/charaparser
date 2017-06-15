CREATE TABLE IF NOT EXISTS `datasetprefixes` (
	prefix varchar(100) NOT NULL, 
	glossary_version varchar(100), 
	oto_uploadid int(11) NOT NULL DEFAULT '-1', 
	oto_secret varchar(100) NOT NULL DEFAULT '',
	created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (prefix)
) CHARACTER SET utf8 engine=innodb;