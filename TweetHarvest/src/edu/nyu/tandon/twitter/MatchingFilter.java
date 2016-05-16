package edu.nyu.tandon.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

/**
 * 
 * An implementation of CategoryFilter that filters tweets based on key phrases.
 * A phrase matches a tweet if it contains <b>every</b> phrase word (although in <b>any order</b>).
 * A tweet passes the filter if <b>any</b> of the phrases matches the tweet.
 *
 * @author michal.siedlaczek@nyu.edu
 *
 */
public class MatchingFilter implements CategoryFilter {

	private String name;
	private List<String[]> matchPhrases = new ArrayList<>();
	private List<String> matchSubstrings = new ArrayList<>();
	private Splitter splitter = Splitter
			.on(Pattern.compile("\\s+|[`~!@#$%^&*()-_=+\\[\\]{}\\\\\\|;:'\",<.>/?]"))
			.trimResults().omitEmptyStrings();;
	
	public static MatchingFilter create(String name) {
		return new MatchingFilter(name);
	}
	
	private MatchingFilter(String name) {
		this.name = name;
	}
	
	@Override
	public String getCategoryName() {
		return name;
	}
	
	public boolean matchesPhrase(String text, String[] phrase) {
		List<String> textWords = splitter.splitToList(text);
		return textWords.containsAll(Arrays.asList(phrase));
	}

	@Override
	public boolean test(Tweet tweet) {
		for (String[] phrase : matchPhrases) {
			if (matchesPhrase(tweet.getText().toLowerCase(), phrase)) {
				return true;
			}
		}
		for (String s : matchSubstrings) {
			if (tweet.getText().toLowerCase().contains(s)) {
				return true;
			}
		}
		return false;
	}
	
	public MatchingFilter matchWord(String phrase) {
		matchPhrases.add(phrase.toLowerCase().split("\\s+"));
		return this;
	}
	
	public MatchingFilter matchSubstring(String substring) {
		matchSubstrings.add(substring.toLowerCase());
		return this;
	}

}
