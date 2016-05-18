package edu.nyu.tandon.twitter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.Status;

/**
 * 
 * @author michal.siedlaczek@nyu.edu
 * 
 * A base class for all tasks related to SQLite database.
 *
 */
public class SQLiteComponent {
	
	public static final Logger logger = Logger.getLogger(SQLiteComponent.class);

	public static final String DEFAULT_DB = "/home/elshize/tweets.db";
	protected static final int BATCH_SIZE = 200;
	protected static final String SELECT_TWEET = "SELECT * FROM tweet WHERE tweet_id = ?";
//	protected static final String SELECT_MANY_TWEETS = "SELECT * FROM tweet WHERE tweet_id IN (?)";
	
	private Connection connection;
	private boolean updateMode = true;
	
	protected PreparedStatement insertTweetStmt;
	
	public void createSchemaNotExists() {
		// TODO
	}
	
	public void openConnection(String dbName) throws SQLException, ClassNotFoundException {
		String sDriverName = "org.sqlite.JDBC";
        Class.forName(sDriverName);
		connection = DriverManager.getConnection("jdbc:p6spy:sqlite:" + dbName);
		connection.setAutoCommit(false);
	}
	
	public void setUpdateMode(boolean updateMode) {
		this.updateMode = updateMode;
	}
	
	public boolean getUpdateMode() {
		return updateMode;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	protected List<List<Long>> getTweetIds(String unconditionedQuery, String conditionedQuery) throws SQLException {
		
		Statement stmt = getConnection().createStatement();
		ResultSet rs = stmt.executeQuery(getUpdateMode() ? conditionedQuery : unconditionedQuery);
		List<List<Long>> ids = new ArrayList<>();
		List<Long> batch = new ArrayList<>();
		long count = 0;
		while (rs.next()) {
			count++;
			if (batch.size() < BATCH_SIZE) {
				batch.add(rs.getLong("tweet_id"));
			}
			else {
				ids.add(batch);
				batch = new ArrayList<>();
			}
		}
		if (batch.size() > 0) {
			ids.add(batch);
		}
		
		logger.debug(String.format("Selected %d tweets to process.", count));
		
		stmt.close();
		return ids;
	}
	
	protected List<List<Long>> getAllTweetIds() throws SQLException {
		String allTweetsQuery = "SELECT tweet_id FROM tweet";
		return getTweetIds(allTweetsQuery, allTweetsQuery);
	}
	
	protected List<List<Long>> getTweetsHavingCategory() throws SQLException {
		String query = "SELECT tweet_id FROM tweet t WHERE (SELECT COUNT(*) FROM category c WHERE c.tweet_id = t.tweet_id) > 0";
		return getTweetIds(query, query);
	}
	
	public boolean tweetExists(long id) throws SQLException {
		PreparedStatement stmt = getConnection().prepareStatement("SELECT COUNT(*) AS count FROM tweet WHERE tweet_id = ?");
		stmt.setLong(1, id);
		ResultSet resultSet = stmt.executeQuery();
		resultSet.next();
		return resultSet.getLong("count") > 0;
	}
	
	public Tweet getTweet(ResultSet rs) throws SQLException {
		rs.next();
		return new Tweet(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getLong(4),
				rs.getString(5), rs.getString(6), rs.getInt(7), rs.getInt(8), rs.getInt(9) != 0);
	}
	
	public List<Tweet> getTweets(List<Long> ids) throws SQLException {
		return getTweets(ids, "SELECT * FROM tweet WHERE tweet_id IN (%s)");
	}
	
	public List<Tweet> getTweetsWithSentiment(List<Long> ids) throws SQLException {
		return getTweets(ids, "SELECT t.*, s.sentiment FROM tweet t LEFT JOIN sentiment s USING (tweet_id) WHERE tweet_id IN (%s)");
	}
	
	public List<Tweet> getTweetsWithSentimentHavingCagetory(List<Long> ids) throws SQLException {
		return getTweets(ids, "SELECT t.*, s.sentiment FROM tweet t LEFT JOIN sentiment s USING (tweet_id) WHERE tweet_id IN (%s)");
	}
	
	public List<Tweet> getTweets(List<Long> ids, String q) throws SQLException {
		List<Tweet> tweets = new ArrayList<>();
		Statement stmt = getConnection().createStatement();
		ResultSet rs = stmt.executeQuery(String.format(q, inList(ids)));
		while (rs.next()) {
			Tweet t = new Tweet(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getLong(4),
					rs.getString(5), rs.getString(6), rs.getInt(7), rs.getInt(8), rs.getInt(9) != 0);
			if (rs.getMetaData().getColumnCount() == 10) {
				t.setSentiment(rs.getInt(10));
			}
			tweets.add(t);
		}
		return tweets;
	}
	
	public void insertTweet(Status s) throws SQLException {
//		if (insertTweetStmt == null || insertTweetStmt.isClosed()) {
			insertTweetStmt = getConnection().prepareStatement("INSERT INTO tweet VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)");
//		}
		insertTweetStmt.setLong(1, s.getId());
		insertTweetStmt.setString(2, s.getText());
		insertTweetStmt.setLong(3, s.getCreatedAt().getTime());
		insertTweetStmt.setLong(4, s.getUser().getId());
		insertTweetStmt.setString(5, s.getUser().getScreenName());
		insertTweetStmt.setString(6, s.getUser().getName());
		insertTweetStmt.setInt(7, s.getRetweetCount());
		insertTweetStmt.setInt(8, s.getFavoriteCount());
		insertTweetStmt.execute();
		insertTweetStmt.close();
	}
	
	public void deleteCategories(List<Long> ids) throws SQLException {
		Statement stmt = getConnection().createStatement();
		stmt.executeUpdate(String.format("DELETE FROM category WHERE tweet_id IN (%s)", inList(ids)));
		stmt.close();
	}
	
	public void deleteSentiments(List<Long> ids) throws SQLException {
		Statement stmt = getConnection().createStatement();
		stmt.executeUpdate(String.format("DELETE FROM sentiment WHERE tweet_id IN (%s)", inList(ids)));
		stmt.close();
	}
	
	public void insertHashTag(long id, String hashTag) throws SQLException {
		PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO hashtag VALUES (?, ?)");
		stmt.setLong(1, id);
		stmt.setString(2, hashTag);
		stmt.execute();
		stmt.close();
	}
	
	public String inList(List<Long> ids) {
		StringBuilder b = new StringBuilder();
		Iterator<Long> i = ids.iterator();
		if (i.hasNext()) {
			b.append(String.valueOf(i.next()));
			while (i.hasNext()) {
				b.append(',');
				b.append(String.valueOf(i.next()));
			}
		}
		return b.toString();
	}
	
	public void commit() throws SQLException {
		getConnection().commit();
		logger.debug("COMMIT");
	}
	
}
