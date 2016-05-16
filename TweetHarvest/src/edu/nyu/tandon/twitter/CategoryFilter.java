package edu.nyu.tandon.twitter;

/**
 * 
 * @author michal.siedlaczek@nyu.edu
 *
 */
public interface CategoryFilter {
	
	/**
	 * @return the name of the category (as stored in the database)
	 */
	String getCategoryName();
	
	/**
	 * Tests whether the tweet belongs to the category.
	 * @param tweet
	 * @return true if the tweet belongs to the category, otherwise false
	 */
	boolean test(Tweet tweet);
	
}