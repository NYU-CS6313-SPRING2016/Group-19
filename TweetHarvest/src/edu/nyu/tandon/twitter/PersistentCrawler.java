package edu.nyu.tandon.twitter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * 
 * TODO
 * 
 * @author michal.siedlaczek@nyu.edu
 *
 */
public class PersistentCrawler extends AbstractHistoryScraper {
	
	public static final Logger logger = Logger.getLogger(PersistentCrawler.class);
	
	public static final int LOOKUP_LIMIT = 100;
	
	public PersistentCrawler() {
		setUpdateMode(false);
	}
	
	protected long getMinId() throws SQLException {
		Statement stmt = getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("SELECT tweet_id FROM tweet ORDER BY tweet_id LIMIT 1");
		if (rs.next()) {
			return rs.getLong(1);
		}
		else {
			throw new RuntimeException("PersistentCrawler needs some tweet records in the database.");
		}
	}
	
	public long[] generateIds(long startId) {
		if (startId <= 0) return new long[0];
		int numbersToGenerate = LOOKUP_LIMIT;
		if (startId < LOOKUP_LIMIT) numbersToGenerate = (int) startId;
		
		long[] ids = new long[numbersToGenerate];
		for (int i = 0; i < numbersToGenerate; i++) {
			ids[i] = startId--;
		}
		return ids;
	}
	
	public void crawl(List<String> follow) throws SQLException {
		crawl(follow, getMinId());
	}
	
	public void crawl(List<String> follow, long minId) throws SQLException {
		
		Twitter twitter = TwitterFactory.getSingleton();
		logger.debug(String.format("Starting crawling from id %d.", minId));
		long[] ids = generateIds(minId - 1);
		
		while (true) {
			try {
				ResponseList<Status> rl = twitter.lookup(ids);
				logger.debug(String.format("Lookup performed for IDs between %d and %d.",
						ids[0], ids[ids.length - 1]));
				
				List<Status> statuses = new ArrayList<>();
				for (Status s : rl) {
					if (follow.contains(s.getUser().getScreenName())) {
						statuses.add(s);
					}
				}
				
				if (!statuses.isEmpty()) {
					storeStatuses(twitter, statuses);
				}
				ids = generateIds(ids[ids.length - 1] - 1);
				if (ids.length == 0) break;
			} catch (TwitterException e) {
				reactToTwitterException(e);
			}
		}
		
	}
	
	@Override
	public void setUpdateMode(boolean updateMode) {
		if (updateMode) throw new IllegalStateException("PersistentCrawler does not support updateMode.");
	}
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		
		PersistentCrawler c = new PersistentCrawler();
		c.openConnection("tweets.db");
		
		List<String> follow = Arrays.asList("CNN", "FoxNews", "MSNBC", "ABC", "CBSNews", "BBCWorld");
		c.crawl(follow, 697699119209544112L);
		
	}
	
}
