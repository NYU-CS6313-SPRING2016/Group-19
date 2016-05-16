package edu.nyu.tandon.twitter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * Retrieves tweets from the database and assigns categories to them (and stores them in the database).
 * It either processes all records (when updateMode=false) or only those having categorized=false (when updateMode=true).
 * 
 * @author michal.siedlaczek@nyu.edu
 * 
 */
public class Categorizer extends SQLiteComponent {
	
	public static final Logger logger = Logger.getLogger(Categorizer.class);
	
	private List<CategoryFilter> categoryFilters = new ArrayList<>();
	
	public void addFilter(CategoryFilter f) {
		categoryFilters.add(f);
		logger.debug(String.format("Added filter `%s'.", f.getCategoryName()));
	}
	
	/**
	 * Retrieves IDs of the tweets that are going to be categorized, based on updateMode:
	 * if it's true, then only those having categorized=false are retrieved, otherwise all of them.
	 * @return tweets IDs
	 * @throws SQLException
	 */
	protected List<List<Long>> getTweetsToCategorize() throws SQLException {
		return getTweetIds(
				"SELECT tweet_id FROM tweet",
				"SELECT tweet_id FROM tweet WHERE categorized = 0");
	}
	
	/**
	 * Assigns categories (which candidate they refer to) to tweets.
	 * @throws SQLException
	 */
	public void categorize() throws SQLException {
		
		List<List<Long>> ids = getTweetsToCategorize();
		
		try (PreparedStatement insertStmt = getConnection().prepareStatement("INSERT INTO category VALUES (?, ?)")) {
		
			for (List<Long> batch : ids) {
				
				String idList = inList(batch);
				logger.trace(String.format("Categorizing batch of tweets %s.", idList));
				
				/*
				 * Deleting old categories if not updateMode.
				 */
				if (!getUpdateMode()) {
					logger.trace(String.format("Update mode is OFF, deleting old categories for tweets: %s.", idList));
					deleteCategories(batch);
				}
				
				List<Tweet> tweets = getTweets(batch);
				
				/*
				 * Insert category records.
				 */
				for (Tweet tweet : tweets) {
					insertStmt.setLong(1, tweet.getId());
					for (CategoryFilter filter : categoryFilters) {
						if (filter.test(tweet)) {
							logger.debug(String.format("Tweet %d is categorized as `%s'.",
									tweet.getId(),
									filter.getCategoryName()));
							insertStmt.setString(2, filter.getCategoryName());
							insertStmt.execute();
						}
					}
				}
				
				/*
				 * Update categorized=1.
				 */
				Statement updateStmt = getConnection().createStatement();
				int rowCount = updateStmt.executeUpdate(
						String.format("UPDATE tweet SET categorized=1 WHERE tweet_id IN (%s)", idList));
				logger.trace(String.format("Updated %d rows.", rowCount));
				
				commit();
			}
			
		}
		
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		// TODO: generalize
		
		Categorizer c = new Categorizer();
		c.openConnection("/home/elshize/tweets.db");
		
		c.addFilter(MatchingFilter.create("Donald Trump")
				.matchWord("Trump")
				.matchWord("Donald")
				.matchSubstring("Donald Trump")
				.matchSubstring("@realDonaldTrump"));
		
		c.addFilter(MatchingFilter.create("Bernie Sanders")
				.matchWord("Bernie")
				.matchWord("Sanders")
				.matchSubstring("Bernie Sanders")
				.matchSubstring("@BernieSanders"));
		
		c.addFilter(MatchingFilter.create("Ted Cruz")
//				.matchWord("Ted")
				.matchWord("Cruz")
				.matchSubstring("Ted Cruz")
				.matchSubstring("@tedcruz"));
		
		c.addFilter(MatchingFilter.create("John Kasich")
//				.matchWord("John")
				.matchWord("Kasich")
				.matchSubstring("John Kasich")
				.matchSubstring("@JohnKasich"));
		
		c.addFilter(MatchingFilter.create("Hillary Clinton")
				.matchWord("Hillary")
//				.matchWord("Clinton")
				.matchSubstring("Hillary Clinton")
				.matchSubstring("@HillaryClinton"));
		
		c.addFilter(MatchingFilter.create("Marco Rubio")
//				.matchWord("Marco")
				.matchWord("Rubio")
				.matchSubstring("Marco Rubio")
				.matchSubstring("@marcorubio"));
		
		c.addFilter(MatchingFilter.create("Jeb Bush")
//				.matchWord("Jeb")
//				.matchWord("Bush")
				.matchSubstring("Jeb Bush")
				.matchSubstring("@JebBush"));
		
		c.addFilter(MatchingFilter.create("Ben Carson")
//				.matchWord("Ben")
				.matchWord("Carson")
				.matchSubstring("Ben Carson")
				.matchSubstring("@RealBenCarson"));
		
		c.addFilter(MatchingFilter.create("Rand Paul")
				.matchWord("Rand")
//				.matchWord("Paul")
				.matchSubstring("Rand Paul")
				.matchSubstring("@RandPaul"));
		
		c.addFilter(MatchingFilter.create("Martin O'Maley")
//				.matchWord("Martin")
				.matchWord("O'Maley")
				.matchSubstring("Martin O'Maley")
				.matchSubstring("@MartinOMaley"));
		
		c.setUpdateMode(true);
		c.categorize();
		
	}
	
}
