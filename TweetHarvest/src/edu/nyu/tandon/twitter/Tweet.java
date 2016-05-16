package edu.nyu.tandon.twitter;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * 
 * @author michal.siedlaczek@nyu.edu
 * 
 * A container for tweet data.
 *
 */
public class Tweet {
	
	private long id;
	private String text;
	private long createdAt;
	private long userId;
	private String screenName;
	private String name;
	private int retweetCount;
	private int favoriteCount;
	private boolean categorized;
	private Integer sentiment;
	
	public Tweet(long id, String text, long createdAt, long userId, String screenName, String name, int retweetCount,
			int favoriteCount, boolean categorized) {
		super();
		this.id = id;
		this.text = text;
		this.createdAt = createdAt;
		this.userId = userId;
		this.screenName = screenName;
		this.name = name;
		this.retweetCount = retweetCount;
		this.favoriteCount = favoriteCount;
		this.categorized = categorized;
		this.sentiment = null;
	}

	public long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public long getCreatedAt() {
		return createdAt;
	}
	
	public Date getCreatedAtDateTime() {
		return new Date(createdAt);
	}

	public long getUserId() {
		return userId;
	}

	public String getScreenName() {
		return screenName;
	}

	public String getName() {
		return name;
	}

	public int getRetweetCount() {
		return retweetCount;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public boolean isCategorized() {
		return categorized;
	}
	
	public void setSentiment(int sentiment) {
		this.sentiment = sentiment;
	}
	
	public int getSentiment() {
		if (sentiment == null) throw new IllegalStateException("Sentiment was null on access.");
		return sentiment;
	}
	
	public double getImpact() {
		return getRetweetCount() * getSentiment();
	}
	
	public static void appendJson(Writer writer, Tweet tweet, double impact) throws IOException {
		writer.write("{");
		writer.write(String.format(
				"'text': %s, 'userScreenName': %s, 'createdAt': %d, '"
				));
		writer.write("}");
	}

}
