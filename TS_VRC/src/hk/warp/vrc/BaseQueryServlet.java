package hk.warp.vrc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BaseQueryServlet extends BaseServlet{
	@Deprecated
	private boolean own_caching = true;

	final Caching caching;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


	public BaseQueryServlet(String clientId_, String client_secret_) {
		super(clientId_, client_secret_);
		caching = new Caching();
	}

	List<Member> requestMembers(String accessToken, Team aTeam) {
		String json = request(accessToken, "members", "team_id=" + aTeam.id);
		MemberFunction f = new MemberFunction(false);
		parse(json, f);
		return f.members;
	}
	
	List<Event> requestEvents(String accessToken, Team aTeam, List<Member> members, boolean past, boolean future) {
		String json = request(accessToken, "events", "team_id=" + aTeam.id);
		EventFunction f = new EventFunction(false);
		parse(json, f);
		
		for (Event event : f.events) {
			final String json2 = request(accessToken, "availabilities", "event_id=" + event.eventid);
			parseEventAvailability(json2, event, members);
		}
		
		return f.events;
	}
	
	void parseEventAvailability(String json, Event anEvent, List<Member> members)
	{
		try {
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
			
			JSONObject collection = (JSONObject) jsonObject.get("collection");

			JSONArray items = (JSONArray) collection.get("items");
			
			Iterator<JSONObject> iterator = items.iterator();
			while (iterator.hasNext()) {
				
				JSONObject data = iterator.next();
				JSONArray memberdata = (JSONArray) data.get("data");

				boolean myWasGoing = false;
				long memberid =0;
				String updated="";
				
				Iterator<JSONObject> member_iterator = memberdata.iterator();
				while (member_iterator.hasNext()) {
					
					JSONObject data2 = member_iterator.next();
				
					String x = (String) data2.get("name");
					if (x.equals("status"))
					{
						myWasGoing = ((String)data2.get("value")).startsWith("Yes");
					}
					else if (x.equals("member_id"))
					{
						memberid =  (long) data2.get("value");
					}
					else if (x.equals("updated_at"))
					{
						updated = (String) data2.get("value");
					}
				}
				
				if (myWasGoing)
				{
					Member member = getMember(members, memberid);
					
					anEvent.addMember(member, updated);
					member.addEvent(anEvent);
				}				
			}
			System.out.println();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	
	
	private Member getMember(List<Member> members, long memberid) {
		for (Member member : members) {
			if (member.id == memberid) return member;
		}
		return null;
	}

	String request(String accessToken, String collection, String search) {
		return request(accessToken, collection, search, true);
	}
	String request(String accessToken, String collection, String search, boolean useCache) {
		try {
			if (useCache)
			{
				final String myReturn = caching.checkCache(collection, search);
				if (myReturn!=null) return myReturn;
			}

			StringBuffer buffer = new StringBuffer();
			buffer.append("https://api.teamsnap.com/v3/");
			buffer.append(collection);
			if (search!=null && search.length()>0)
			{
				buffer.append("/search?");
				buffer.append(search);
			}
			
			HttpGet g = new HttpGet(buffer.toString()); // members
			g.addHeader("Content-Type", "application/json");
			g.addHeader("Authorization", "Bearer " + accessToken);
			String myReturn = execute(g);
			if (useCache)
				caching.writeCache(collection, search, myReturn);
			return myReturn;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	Member requestMyself(String accessToken) {
		final String json= request(accessToken, "me", null, own_caching);
		MemberFunction f = new MemberFunction(true);
		parse(json, f);
		return f.members.get(0);

	}

	
	void parse(String json, Function f)
	{
		try {
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
			JSONObject collection = (JSONObject) jsonObject.get("collection");
			JSONArray items = (JSONArray) collection.get("items");
			Iterator<JSONObject> iterator = items.iterator();
			while (iterator.hasNext()) {
				
				JSONObject data = iterator.next();
				if (f.handle((JSONArray) data.get("data")))
				{
					return;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	class OneValueFunction implements Function
	{

		public OneValueFunction(String key_) {
			key = key_;
		}
		
		final String key;
		String value;
		
		@Override
		public boolean handle(JSONArray jsonArray) {
			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				
				final JSONObject data = iterator.next();
				final String name = (String) data.get("name");
				if (name.equals(key))
				{
					value =(String) data.get("value");
					return true;
				}
			}
			return false;
		}
		
	}

	class MemberFunction implements Function
	{
		List<Member> members = new ArrayList<>();
		final boolean onlyOne;
		MemberFunction(boolean onlyOne_)
		{
			onlyOne = onlyOne_;
		}
		
		
		@Override
		public boolean handle(JSONArray jsonArray) {
			Member member = new Member();
			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				
				final JSONObject data = iterator.next();
				final String key = (String) data.get("name");
				if (key.equals("id"))
				{
					member.id = (long) data.get("value");
				}
				else if (key.equals("first_name"))
				{
					member.first_name = (String) data.get("value");
				}
				else if (key.equals("last_name"))
				{
					member.last_name = (String) data.get("value");
				}
				else if (key.equals("gender"))
				{
					String x = (String) data.get("value");
					member.female = x==null || x.equals("Female");
				}
			}
			members.add(member);
			return onlyOne;
		}	
	}
	
	class EventFunction implements Function
	{
		final List<Event> events = new ArrayList<>();
		final boolean onlyOne;
		EventFunction(boolean onlyOne_)
		{
			onlyOne = onlyOne_;
		}
		
		
		@Override
		public boolean handle(JSONArray jsonArray) {
			Event  event = new Event();
			
			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				
				final JSONObject data = iterator.next();
				final String key = (String) data.get("name");
				if (key.equals("id"))
				{
					event.eventid = (long) data.get("value");
				}
				else if (key.equals("name"))
				{
					event.name = (String) data.get("value");
				}
				
				else if (key.equals("arrival_date"))
				{
					String x = (String) data.get("value");
					try {
						Date myDate = sdf.parse(x);
						event.date = myDate;
					} catch (java.text.ParseException e) {
						e.printStackTrace();
					}
				}
		
			}
			events.add(event);
			return onlyOne;
		}	
	}
	
	
	class TeamFunction implements Function
	{
		List<Team> teams = new ArrayList<>();
		
		@Override
		public boolean handle(JSONArray jsonArray) {
			Team team = new Team();
			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				
				final JSONObject data = iterator.next();
				final String key = (String) data.get("name");
				if (key.equals("id"))
				{
					team.id = (long) data.get("value");
				}
				else if (key.equals("name"))
				{
					team.name = (String) data.get("value");
				}
			}
			teams.add(team);
			return true;
		}	
	}
	
	List<Team> getTeams(String accessToken, Member aMember){
		final String json= request(accessToken, "teams", "user_id="+ aMember.id, own_caching);
		TeamFunction f = new TeamFunction();
		parse(json, f);
		return f.teams;
	}
	

}
