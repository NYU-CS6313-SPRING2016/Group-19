package edu.nyu.tandon.twitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import twitter4j.FilterQuery;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class GardenHose {
	
	protected FilterQuery filterQuery;
	
	public GardenHose() {
		filterQuery = new FilterQuery();
	}
	
	public GardenHose follow(String ... userScreenNames) throws TwitterException {
		filterQuery = filterQuery.follow(getUserIDs(userScreenNames));
		return this;
	}
	
	public GardenHose track(String ... track) {
		filterQuery = filterQuery.track(track);
		return this;
	}
	
	public GardenHose language(String ...languages) {
		filterQuery = filterQuery.language(languages);
		return this;
	}

	public void harvest(StatusListener statusListener) throws TwitterException {
		
		TwitterStream twitterStream = TwitterStreamFactory.getSingleton();
		twitterStream.addListener(statusListener);
		twitterStream.filter(filterQuery);
//		twitterStream.sample("en");
		
	}
	
	public long[] getUserIDs(String[] userScreenNames) throws TwitterException {
		Twitter t = TwitterFactory.getSingleton();
		ResponseList<User> response = t.lookupUsers(userScreenNames);
		long[] userIDs = new long[response.size()];
		int i = 0;
		for (User u : response) {
			userIDs[i++] = u.getId();
		}
		return userIDs;
	}
	
	public static void main(String[] args) throws TwitterException, IOException {
		
		String[] follow = new String[] { "CNN", "FoxNews", "MSNBC", "ABC", "CBSNews" };
		String[] track = new String[] {
				"Bernie Sanders", "BernieSanders",
				"Donald Trump", "Trump", "realDonaldTrump"
		};
		Set<String> screenNames = new HashSet<>(Arrays.asList(follow));
		
		GardenHose gardenHose = new GardenHose()
				.follow(follow)
				.track(track)
				.language("en");
		
//		FileWriter writer = new FileWriter(".tmp");
		
		gardenHose.harvest(new StatusListener() {
			
			@Override
			public void onException(Exception ex) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println(String.format("LIMIT: %d", numberOfLimitedStatuses));
			}
			
			@Override
			public void onStatus(Status status) {
				if (screenNames.contains(status.getUser().getScreenName())) {
					System.out.println(String.format("%s\t%s", status.getUser().getScreenName(), status.getText()));
					for (UserMentionEntity e :status.getUserMentionEntities()) {
						System.out.println(String.format("\t\t%s", e.getScreenName()));
					}
				}
			}
			
			@Override
			public void onStallWarning(StallWarning warning) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
}
