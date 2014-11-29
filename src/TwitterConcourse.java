import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cinchapi.concourse.Concourse;
import org.cinchapi.concourse.Link;
import org.cinchapi.concourse.thrift.Operator;
import org.cinchapi.concourse.time.Time;

/**
 * 
 * @author Remie
 *
 */
public class TwitterConcourse implements Twitter {
	
	private Concourse concourse;
	private static long userId;

	public TwitterConcourse() {
		// TODO Auto-generated constructor stub
		concourse = Concourse.connect();
	}
	
	private void setUserId(long userId){
		this.userId = userId;
	}

	@Override
	public boolean follow(String username) {
	
		if(userExists(username)){
			long followerId = findUserId(username);
			concourse.link("follow", userId, followerId);
			if(concourse.verify("follow", userId, followerId));
				return true;
		}
		return false;
	}

	@Override
	public boolean login(String username, String password) {
		if(concourse.find("username", Operator.EQUALS, username).equals(concourse.find("password", Operator.EQUALS, password))){
		        setUserId(findUserId(username));
				return true;
			} else {
				return false;
			}
	}

	@Override
	public boolean register(String username, String password) {
		
		if(!userExists(username)){
			long id = getIncrementId("username");
			concourse.set("username", username, id);
			concourse.set("password", password, id);
			return true;
		}
		return false;
	}
	
	private boolean userExists(String username){
		if(concourse.find("username", Operator.EQUALS, username).size() > 0){
			return true;
		} else {
			return false;
		}
	}
	
	public long findUserId(String username){
	
		Set<Long> user = concourse.find("username", Operator.EQUALS, username);
		Long userId = null;
		for(Long id : user){
			userId = id;
			break;
		}
		return userId;
	}
	
	/**
	 * Returns a new increment id.
	 * @return
	 */
	private long getIncrementId(String table){

		Set<Long> ids = null;
		
		if(table.equals("tweets")){
			ids = concourse.find(table, Operator.GREATER_THAN_OR_EQUALS, 0);
		} else if(table.equals("username")){
			ids = concourse.find(table, Operator.NOT_EQUALS, "");
		}
	
		long newId = 0;
		if (ids.size() > 0) {
			for (Long id : ids) {
				newId = id;
			}
				newId++;
		}
		
		return newId;
	}
	
	@Override
	public Map<Long, String> mentions() {
		
		String username = null;
		for (Object match : concourse.fetch("username", userId)) {
			username = (String) match;
		}
		
		Map<Object, Set<Long>> tweets = concourse.browse("tweets");
		Map<Long, String> mentions = new HashMap<Long, String>();

		for(Entry<Object, Set<Long>> entry : tweets.entrySet()){
			if(((String) entry.getKey()).matches(".*" + username +".*")){
				Long tweetId = (Long)entry.getValue().iterator().next();
				mentions.put((Long) concourse.get("timestamp", tweetId), entry.getKey().toString());
			}
		}
		
		return mentions;
	}

	@Override
	public Map<Long, String> timeline() {
	
		Set<Object> matches = concourse.fetch("profile", userId);
	    Map<Long, String> timeline = new HashMap<Long, String>();
	
		for (Object match : matches) {
			 long tweetId = ((Link) match).longValue();
			 long timestamp = concourse.get("timestamp", tweetId);
			 String message = concourse.get("tweets", tweetId);
			 
			 timeline.put(timestamp, message);
		}
		
		if(matches.size() > 0){
			matches.clear();
		}
		
		return timeline;
	}
	

	@Override
	public void tweet(String message) {
			long tweetId = getIncrementId("tweets");
			concourse.set("tweets", message, tweetId);
			concourse.set("timestamp", Time.now(), tweetId);
			concourse.link("profile", userId, tweetId );
	}

	@Override
	public boolean unfollow(String username) {
		if(userExists(username)){
			if(concourse.unlink("follow", userId, findUserId(username)));
			return true;
		}

		return false;
	}
}
