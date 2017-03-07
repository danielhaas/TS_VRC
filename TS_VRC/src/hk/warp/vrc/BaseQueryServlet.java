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
	public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


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
	
	public void requestMemberEmails(String accessToken, Member member) {
		String json = request(accessToken, "member_email_addresses", "member_id=" + member.id);
		MemberEmailFunction f = new MemberEmailFunction(false, member);
		parse(json, f);
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
				boolean noShow = false;
				boolean late = false;
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
					else if (x.equals("notes"))
					{
						String v = (String) data2.get("value");
						if (v!=null){
							if (v.equalsIgnoreCase("no show"))
							{
								noShow = true;
							}
							else if (v.toLowerCase().startsWith("late can"))
							{
								late = true;							
							}
						}
					}

				}
				Date myDate=null;
				if (updated!=null)
				{
					try {
						myDate = sdf.parse(updated);
					} catch (java.text.ParseException e) {
						e.printStackTrace();
					}
				}

				if (noShow)
				{
					Member member = getMember(members, memberid);
					anEvent.addNoShow(member);
					member.addEventNoShow(anEvent, myDate);

				}
				else if (late)
				{
					Member member = getMember(members, memberid);
					anEvent.addLateCancel(member);					
					member.addEventLateCancel(anEvent, myDate);
				}
				else if (myWasGoing)
				{
					Member member = getMember(members, memberid);

					anEvent.addMember(member, updated);
					member.addEvent(anEvent);
				}
				else
				{
					if (updated!=null)
					{
						long time = anEvent.date.getTime() - myDate.getTime();

						if (time<0)
						{
							Member member = getMember(members, memberid);
							anEvent.addNoShow(member);
							member.addEventNoShow(anEvent, myDate);
						}
						else if (time < 10 *60*60*1000)
						{
							Member member = getMember(members, memberid);
							anEvent.addLateCancel(member);					
							member.addEventLateCancel(anEvent, myDate);								
						}

					}
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
			if (items == null) return;
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
				else if (key.equals("T Shirt Size"))
				{
					member.shirtsize = (String) data.get("value");
				}
				else if (key.equals("is_non_player"))
				{
					Boolean x =  (Boolean) data.get("value");
					member.player = x==null || !x.booleanValue();
				}
				
				
			}
			members.add(member);
			return onlyOne;
		}	
	}
	
	class MemberEmailFunction implements Function
	{
		final boolean onlyOne;
		final Member member;
		MemberEmailFunction(boolean onlyOne_, Member aMember)
		{
			onlyOne = onlyOne_;
			member = aMember;
		}
		
		
		@Override
		public boolean handle(JSONArray jsonArray) {
			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				
				final JSONObject data = iterator.next();
				final String key = (String) data.get("name");
				
				if (key.equals("email"))
				{
					final String email = (String) data.get("value");
					if (member.emails==null) member.emails = new  ArrayList<String>();
					if (!member.emails.contains(email))
					
					member.emails.add(email);
				}
				
			}
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
	
	void getCustomFields(String accessToken, List<Member> members){
		final String json= request(accessToken, "custom_data", "custom_field_id=431124");
//		final String json= request(accessToken, "custom_fields/431124", "member_id="+1871589);
		
		CustomFunction f = new CustomFunction(members);
		parse(json, f);
/*		TeamFunction f = new TeamFunction();
		parse(json, f);*/
	}

	
	
	class CustomFunction implements Function
	{
		List<Member> members;
		CustomFunction(List<Member> aMembers)
		{
			members = aMembers;
		}
		
		
		@Override
		public boolean handle(JSONArray jsonArray) {
			Member member=null;
			String shirtsize=null;
			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				
				final JSONObject data = iterator.next();
				final String key = (String) data.get("name");
				
				if (key.equals("member_id"))
				{
					long mid = (long) data.get("value");
					for (Member member_ : members) {
						if (member_.id== mid)
						{
							member = member_;
						}
					}
					if (member!=null && shirtsize!=null)
						member.shirtsize = shirtsize;
					
				}
				else if (key.equals("value"))
				{
					shirtsize =(String) data.get("value");
					if (member!=null)
						member.shirtsize = shirtsize;
				}
			}
			return false;
		}	
	}
	

}
