package edu.nyu.tandon.twitter;

import java.sql.SQLException;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * 
 * Retrieves past tweets from particular users.
 * 
 * @author michal.siedlaczek@nyu.edu
 *
 */
public class HistoryScraper extends AbstractHistoryScraper {
	
	/**
	 * Get the user's past tweets.
	 * When updateMode=true, it stops at the first tweet that is already present in the database,
	 * thus only retrieving new tweets.
	 * 
	 * @param user	the screen name of the user
	 * @throws SQLException
	 */
	public void scrape(String user) throws SQLException {
		
		Twitter twitter = TwitterFactory.getSingleton();
		long minId = Long.MAX_VALUE;
		
		while (true) {
			Paging p = new Paging();
			p.setMaxId(minId - 1);
			try {
				ResponseList<Status> rl = twitter.getUserTimeline(user, p);
				if (rl.isEmpty()) break;
				minId = storeStatuses(twitter, rl);
				if (minId < 0) break;
			} catch (TwitterException e) {
				reactToTwitterException(e);
			}
		}
		
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		HistoryScraper scraper = new HistoryScraper();
		scraper.openConnection(DEFAULT_DB);
		
		String[] follow = new String[] { "CNN", "FoxNews", "MSNBC", "ABC", "CBSNews", "BBCWorld" };
		
		scraper.setUpdateMode(true);
		for (String site : follow) {
			scraper.scrape(site);
		}
		
	}
	
}
