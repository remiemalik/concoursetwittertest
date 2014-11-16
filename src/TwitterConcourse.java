import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cinchapi.concourse.Concourse;
import org.cinchapi.concourse.thrift.Operator;
import org.cinchapi.concourse.time.Time;

/**
 * 
 * @author Remie
 *
 */
public class TwitterConcourse implements Twitter {
	
	private Concourse concourse;

	public TwitterConcourse() {
		// TODO Auto-generated constructor stub
		concourse = Concourse.connect();
	}

	@Override
	public boolean follow(String username) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean login(String username, String password) {
		if(concourse.find("username", Operator.EQUALS, username).size() > 0
				&& concourse.find("password", Operator.EQUALS, password).size() > 0){
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

		Set<Long> ids = concourse.find(table, Operator.NOT_EQUALS, "");
		long newId = 0;
		
		for (Long id : ids) {
			newId = id;
		}
		return newId;
	}
	
	@Override
	public Map<Long, String> mentions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Long, String> timeline() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void tweet(long userId, String message) {

		if(concourse.fetch("tweets", userId).size()  == 0){
			Map<Long, String> tweets = new HashMap<Long, String>();
			tweets.put(Time.now(), message);
			concourse.set("tweets", tweets, userId);
		} else {
			Set<Object> tweetObject= concourse.fetch("tweets", userId);
			for (Object tweet : tweetObject) {
				Map<Long, String> tweets = (Map<Long, String>) tweet;
				tweets.put(Time.now(), message);
				concourse.set("tweets", tweets, userId);
			}
			
		}
	}

	@Override
	public boolean unfollow(String username) {
		// TODO Auto-generated method stub
		return false;
	}

}
