package edu.nyu.tandon.twitter;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public abstract class AbstractHistoryScraper extends SQLiteComponent {
	
	public static final Logger logger = Logger.getLogger(AbstractHistoryScraper.class);

	/**
	 * Stores the statuses into the database.
	 * @param twitter
	 * @param statuses
	 * @return	the last seen (thus, the lowest) tweet ID,
	 * 			or -1 if in updateMode and found already existing tweet in db.
	 * @throws SQLException
	 */
	protected long storeStatuses(Twitter twitter, List<Status> statuses) throws SQLException {
		long minId = Long.MAX_VALUE;
		for (Status s : statuses) {
			
			minId = Math.min(s.getId(), minId);
			
			if (tweetExists(s.getId())) {
				if (getUpdateMode()) {
					minId = -1;
					break;
				}
				else continue;
			}
			
			insertTweet(s);
			for (HashtagEntity he : s.getHashtagEntities()) {
				insertHashTag(s.getId(), he.getText());
			}
			
			logger.trace(String.format("%s --- %s", s.getUser().getScreenName(), s.getText()));
		}
		commit();
		return minId;
	}
	
	protected void reactToTwitterException(TwitterException e) {
		if (e.getErrorCode() == 88) {
			long waitInSeconds = e.getRateLimitStatus().getSecondsUntilReset();
			logger.info(String.format("Rate limit exceeded. Waiting for: %d seconds.", waitInSeconds));
			try {
				while (waitInSeconds > 0) {
					Thread.sleep(10000);
					waitInSeconds = e.getRateLimitStatus().getSecondsUntilReset();
					logger.debug(String.format("Remaining time to wait: %d seconds.", waitInSeconds));
				}
			} catch (InterruptedException e1) {
				logger.warn("The tread was interrupted. No biggie.");
			}
		}
		else {
			logger.error(String.format("Unable to get user timeline: %s", e.getMessage()));
		}
	}
	
}
