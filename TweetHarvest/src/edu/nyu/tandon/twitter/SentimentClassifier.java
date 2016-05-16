package edu.nyu.tandon.twitter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class SentimentClassifier extends SQLiteComponent {
	
	private StanfordCoreNLP tokenizer;
	private StanfordCoreNLP pipeline;
	
	public SentimentClassifier() {
		Properties tokenizerProps = new Properties();
		tokenizerProps.setProperty("annotators", "tokenize, ssplit");
		tokenizerProps.setProperty("ssplit.eolonly", "true");
		tokenizer = new StanfordCoreNLP(tokenizerProps);
		
		Properties pipelineProps =  new Properties();
		pipelineProps.setProperty("annotators", "parse, sentiment");
		pipelineProps.setProperty("enforceRequirements", "false");
		pipeline = new StanfordCoreNLP(pipelineProps);
	}
	
	protected String getSentiment(String text) {
		Annotation annotation = tokenizer.process(text);
		pipeline.annotate(annotation);
		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			return sentence.get(SentimentCoreAnnotations.SentimentClass.class);
		}
		throw new RuntimeException("There were no sentences on the output of the pipline.");
	}
	
	protected List<List<Long>> getTweetsToClassify() throws SQLException {
		
		return getTweetIds(
				"SELECT tweet_id FROM tweet ",
				"SELECT tweet_id "
				+ "FROM tweet "
				+ "INNER JOIN sentiment USING (tweet_id)");
		
	}
	
	protected int getSentimentValue(String sentiment) {
		if ("Negative".equals(sentiment)) {
			return -1;
		}
		else if ("Positive".equals(sentiment)) {
			return 1;
		}
		else if ("Very negative".equals(sentiment)) {
			return -1;
		}
		else if ("Very positive".equals(sentiment)) {
			return -1;
		}
		else if ("Neutral".equals(sentiment)) {
			return 0;
		}
		throw new IllegalStateException(String.format("Unknown sentiment `%s'.", sentiment));
	}
	
	public void classify() throws SQLException {
		
		List<List<Long>> ids = getTweetsToClassify();
		
		try (PreparedStatement insertStmt = getConnection().prepareStatement("INSERT INTO sentiment VALUES (?, ?)")) {
		
			for (List<Long> batch : ids) {
				
				String idList = inList(batch);
				logger.trace(String.format("Classifying batch of tweets %s.", idList));
				
				/*
				 * Deleting old sentiments if not updateMode.
				 */
				if (!getUpdateMode()) {
					logger.trace(String.format("Update mode is OFF, deleting old sentiments for tweets: %s.", idList));
					deleteSentiments(batch);
				}
				
				List<Tweet> tweets = getTweets(batch);
				
				/*
				 * Insert sentiment records.
				 */
				for (Tweet tweet : tweets) {
					insertStmt.setLong(1, tweet.getId());
					String sentiment = getSentiment(tweet.getText());
					logger.debug(String.format("Tweet %d is %s.",
									tweet.getId(),
									sentiment));
					insertStmt.setInt(2, getSentimentValue(sentiment));
					insertStmt.execute();
				}
				
				commit();
				
			}
			
		}
		
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		SentimentClassifier s = new SentimentClassifier();
		s.openConnection("/home/elshize/tweets.db");
		
		s.setUpdateMode(true);
		s.classify();
		
	}
	
}
