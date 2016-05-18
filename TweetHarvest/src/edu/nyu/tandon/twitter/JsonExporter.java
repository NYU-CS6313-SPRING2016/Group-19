package edu.nyu.tandon.twitter;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

public class JsonExporter extends SQLiteComponent implements Closeable {
	
	@FunctionalInterface
	public static interface WriterConsumer<T> {
		
	    void accept(final T t) throws IOException, SQLException;
		
	}
	
	protected BufferedWriter writer;
	
	public JsonExporter(String jsonFile) throws IOException {
		writer = new BufferedWriter(new FileWriter(jsonFile));
	}
	
	protected List<String> getCandidateNames() throws SQLException {
		Statement stmt = getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("SELECT distinct category FROM category");
		List<String> candidates = new ArrayList<String>();
		while (rs.next()) {
			candidates.add(rs.getString(1));
		}
		stmt.close();
		return candidates;
	}
	
	protected List<String> getChannelNames() throws SQLException {
		Statement stmt = getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("SELECT distinct screen_name FROM tweet");
		List<String> candidates = new ArrayList<String>();
		while (rs.next()) {
			candidates.add(rs.getString(1));
		}
		stmt.close();
		return candidates;
	}
	
	protected List<List<Long>> getTweetsForCandidate(String candidateName, String channel) throws SQLException {
		String query = String.format(
				"SELECT tweet_id "
				+ "FROM tweet "
				+ "JOIN category USING (tweet_id) "
				+ "WHERE category = \"%s\" AND screen_name = \"%s\" "
				+ "ORDER BY created_at ASC",
				candidateName, channel);
		if (candidateName.matches(".*sum$")) {
			query = String.format(
					"SELECT tweet_id "
					+ "FROM tweet "
					+ "JOIN category USING (tweet_id) "
					+ "WHERE screen_name = \"%s\" "
					+ "ORDER BY created_at ASC",
					channel);
		}
		else if (channel.matches(".*sum$")) {
			query = String.format(
					"SELECT tweet_id "
					+ "FROM tweet "
					+ "JOIN category USING (tweet_id) "
					+ "WHERE category = \"%s\" "
					+ "ORDER BY created_at ASC",
					candidateName);
		}
		
		return getTweetIds(query, query);
	}
	
	protected List<String> getCategories(Long id) throws SQLException {
		String query = String.format("SELECT category FROM category WHERE tweet_id = %d", id);
		Statement stmt = getConnection().createStatement();
		ResultSet rs = stmt.executeQuery(query);
		List<String> categories = new ArrayList<>();
		while (rs.next()) {
			categories.add(rs.getString(1));
		}
		stmt.close();
		return categories;
	}
	
	protected void openObject() throws IOException {
		writer.write("{");
	}
	
	protected void closeObject() throws IOException {
		writer.write("}");
	}
	
	protected void openArray() throws IOException {
		writer.write("[");
	}
	
	protected void closeArray() throws IOException {
		writer.write("]");
	}
	
	protected void comma() throws IOException {
		writer.write(",");
	}
	
	protected void writeField(String name, String value) throws IOException {
		fieldName(name);
		writer.write(String.format("\"%s\"", value));
	}
	
	protected void writeField(String name, int value) throws IOException {
		fieldName(name);
		writer.write(String.format("%d", value));
	}
	
	protected void writeField(String name, double value) throws IOException {
		fieldName(name);
		writer.write(String.format("%f", value));
	}
	
	protected void fieldName(String name) throws IOException {
		writer.write(String.format("\"%s\": ", name));
	}
	
//	protected void writeCandidate(String name) throws IOException {
//		openObject();
//		
//		closeObject();
//	}
	
	public <T> void writeSeparatedList(List<T> list, WriterConsumer<T> f) throws IOException, SQLException {
		Iterator<T> it = list.iterator();
		if (it.hasNext()) {
			f.accept(it.next());
			while (it.hasNext()) {
				comma();
				f.accept(it.next());
			}
		}
	}
	
	protected void writeCandidate(String candidate, List<String> channels) throws IOException, SQLException {
		openObject();
		writeField("name", candidate);
		comma();
		
		writeSeparatedList(channels, channel -> {
			logger.debug(String.format("\t... for network `%s'", channel));
			fieldName(channel);
			openArray();
			
			List<List<Long>> ids = getTweetsForCandidate(candidate, channel);
			writeSeparatedList(ids, batch -> {
				List<Tweet> tweets = getTweetsWithSentiment(batch);
				writeSeparatedList(tweets, tweet -> {
					openObject();
					writeField("time", tweet.getCreatedAt());
					comma();
					writeField("sentiment", tweet.getSentiment());
					comma();
					writeField("impact", tweet.getImpact());
					closeObject();
				});
			});
			
			closeArray();
		});
		
		closeObject();
	}
	
	protected void writeChannel(String channel, List<String> candidates) throws IOException, SQLException {
		openObject();
		writeField("name", channel);
		comma();
		
		writeSeparatedList(candidates, candidate -> {
			logger.debug(String.format("\t... for candidate `%s'", candidate));
			fieldName(candidate);
			openArray();
			
			List<List<Long>> ids = getTweetsForCandidate(candidate, channel);
			writeSeparatedList(ids, batch -> {
				List<Tweet> tweets = getTweetsWithSentiment(batch);
				writeSeparatedList(tweets, tweet -> {
					openObject();
					writeField("time", tweet.getCreatedAt());
					comma();
					writeField("sentiment", tweet.getSentiment());
					comma();
					writeField("impact", tweet.getImpact());
					closeObject();
				});
			});
			
			closeArray();
		});
		
		closeObject();
	}
	
	protected void writeCandidates(List<String> candidates, List<String> channels) throws IOException, SQLException {
		
		fieldName("candidates");
		openArray();
		
		writeSeparatedList(candidates, candidate -> {
			logger.debug(String.format("Writing candidate `%s'", candidate));
			writeCandidate(candidate, channels);
		});
		
		closeArray();
		
	}
	
	protected void writeChannels(List<String> candidates, List<String> channels) throws IOException, SQLException {
		
		fieldName("channels");
		openArray();
		
		writeSeparatedList(channels, channel -> {
			logger.debug(String.format("Writing network `%s'", channel));
			writeChannel(channel, candidates);
		});
		
		closeArray();
		
	}
	
	public void writeTweets() throws IOException, SQLException {
		
		logger.info("Writing tweets...");
		
		fieldName("tweets");
		openArray();
		List<List<Long>> tweetIds = getTweetsHavingCategory();
		writeSeparatedList(tweetIds, batch -> {
			
			logger.info(String.format("Writing a batch of %d tweets.", batch.size()));
			
			List<Tweet> tweets = getTweetsWithSentiment(batch);
			writeSeparatedList(tweets, t -> {
				openObject();
				
				writeField("id", t.getId());
				comma();
				writeField("network", t.getScreenName());
				comma();
				
				fieldName("candidates");
				openArray();
				writeSeparatedList(getCategories(t.getId()), c -> {
					writer.write(String.format("\"%s\"", c));
				});
				closeArray();
				comma();
				
				writeField("sentiment", t.getSentiment());
				comma();
				writeField("retweets", t.getRetweetCount());
				comma();
				writeField("time", t.getCreatedAt());
				comma();
				writeField("text", StringEscapeUtils.escapeJson(t.getText()));
				
				closeObject();
			});
		});
		closeArray();
	}
	
	public void exportJson() throws IOException, SQLException {
		
		List<String> candidates = getCandidateNames();
		List<String> channels = getChannelNames();
				
		openObject();
		writeCandidates(candidates, channels);
		comma();
		fieldName("candidatessum");
		writeCandidate("candidatesum", channels);
		comma();
		writeChannels(candidates, channels);
		comma();
		fieldName("channelssum");
		writeChannel("channelsum", candidates);
		comma();
		writeTweets();
		closeObject();
		
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
	
	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		
		
		try (JsonExporter j = new JsonExporter("/home/elshize/Projects/infoviz/project/data.json")) {
			j.openConnection(DEFAULT_DB);
			j.exportJson();
		}
		
	}
	
}
