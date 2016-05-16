CREATE TABLE IF NOT EXISTS `tweet` (
	`tweet_id`		bigint(20) NOT NULL,
	`text`			varchar(160) NOT NULL,
	`created_at`	int(12) NOT NULL,
	`user_id`		bigint(20) NOT NULL,
	`screen_name`	varchar(255) NOT NULL,
	`name`			varchar(255) NOT NULL,
	`retweet_count`	int(10) NOT NULL,
	`fav_count`		int(10) NOT NULL,
	`categorized`	int(1) NOT NULL,
	PRIMARY KEY (`tweet_id`)
);

CREATE TABLE IF NOT EXISTS `hashtag` (
	`tweet_id`		bigint(20) NOT NULL,
	`hash_tag`		varchar(255) NOT NULL,
	FOREIGN KEY (`tweet_id`) REFERENCES tweet(`tweet_id`)
);

CREATE TABLE IF NOT EXISTS `category` (
	`tweet_id`		bigint(20) NOT NULL,
	`category`		varchar(20) NOT NULL,
	FOREIGN KEY (`tweet_id`) REFERENCES tweet(`tweet_id`)
);

CREATE TABLE IF NOT EXISTS `sentiment` (
	`tweet_id`		bigint(20) NOT NULL,
	`sentiment`		int(1) NOT NULL,
	FOREIGN KEY (`tweet_id`) REFERENCES tweet(`tweet_id`)
);
