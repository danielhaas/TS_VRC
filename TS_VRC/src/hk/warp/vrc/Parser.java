package hk.warp.vrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class Parser {

	
	
	
	public Parser() {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public void runIt() throws IOException
	{
		System.out.println("Loading Members");
		final String members =requestMembers();
		FileWriter myWriter = new FileWriter(new File("members.json"));
		myWriter.write(members);
		myWriter.close();
		parseMembers(members);
		
/*		Event myEvent = new Event();
		myEvent.eventid = 74712965;*/
		
		System.out.println("Loading Events");
		final String events = requestEvents();
		myWriter = new FileWriter(new File("events.json"));
		myWriter.write(events);
		myWriter.close();
		
		System.out.println("Parsing Events");
		parseEvents(events);
		System.out.println("Writing CSV");
		String csv = writeCSV();
		writeCSV(csv);
	}
	
	
	abstract void writeCSV(String csv);

	abstract String requestEventAvailability(Event myEvent);
	abstract String requestMembers();
	abstract String requestEvents();


	
	protected String writeCSV() {
		
		List<Event> myEvents = new ArrayList<>(events.values());
		
		Collections.sort(myEvents, new Comparator<Event>() {

			@Override
			public int compare(Event o1, Event o2) {
				
				final long x = (o1.date.getTime() - o2.date.getTime());
				return (int) Math.signum(x);
			}
		});
		
		List<Member> myMembers = new ArrayList<>(members.values());
		
		Collections.sort(myMembers, new Comparator<Member>() {

			@Override
			public int compare(Member o1, Member o2) {

				final int x = o1.last_name.compareTo(o2.last_name);
				if (x!=0) return x;
				return o1.first_name.compareTo(o2.first_name);
			}
		});
		
		
		StringBuffer myBuffer = new StringBuffer();
		
		writeHeader(myEvents, myBuffer);
		
		for (Member member : myMembers) {
			myBuffer.append('"');			
			myBuffer.append(member.last_name);
			myBuffer.append('"');			
			myBuffer.append(',');			
			
			myBuffer.append('"');			
			myBuffer.append(member.first_name);
			myBuffer.append('"');			
			myBuffer.append(',');			
			
			myBuffer.append(member.female? "Female" : "Male");
			myBuffer.append(',');
			
			
			for (Event event : myEvents) {
				
				boolean hasGone = (member.events.contains(event));
				
				
				myBuffer.append(hasGone ? "X" : "");
				myBuffer.append(',');			
			}
			
			myBuffer.append('\r');
			myBuffer.append('\n');

		}
		
		return myBuffer.toString();
	}

	private void writeHeader(List<Event> myEvents, StringBuffer myBuffer) {
		final int member_cols = 3;
		for (int i = 0; i < member_cols; i++) {
			myBuffer.append(',');			
		}
		
		for (Event event : myEvents) {
			myBuffer.append('"');			
			myBuffer.append(event.name.replace(',', ';'));
			myBuffer.append('"');			
			myBuffer.append(',');			
		}
		myBuffer.append('\r');
		myBuffer.append('\n');
		
		myBuffer.append("LastName");
		myBuffer.append(',');			
		
		myBuffer.append("FirstName");
		myBuffer.append(',');			
		
		myBuffer.append("Female");
		myBuffer.append(',');			
		
		for (Event event : myEvents) {
			myBuffer.append('"');			
			myBuffer.append(event.date.toString());
			myBuffer.append('"');			
			myBuffer.append(',');			
		}
		myBuffer.append('\r');
		myBuffer.append('\n');
	}

	static String readJson(String file) throws IOException
	{
		BufferedReader in = new BufferedReader( new FileReader(file));

		StringBuilder builder = new StringBuilder();
		String str;

		while ((str = in.readLine())!=null)
		{
			builder.append(str);
		}

		in.close();
		return builder.toString();
	}

	void parseMembers(String json)
	{
		try {
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
			JSONObject collection = (JSONObject) jsonObject.get("collection");
			JSONArray items = (JSONArray) collection.get("items");
			Iterator<JSONObject> iterator = items.iterator();
			while (iterator.hasNext()) {
				
				JSONObject data = iterator.next();
				Member myMember = parseMember((JSONArray) data.get("data"));
				members.put(myMember.memberid, myMember);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private Member parseMember(JSONArray memberArray) {
		Member member = new Member();
		Iterator<JSONObject> iterator = memberArray.iterator();
		while (iterator.hasNext()) {
			
			final JSONObject data = iterator.next();
			final String name = (String) data.get("name");
			if (name.equals("id"))
			{
				member.memberid = (long) data.get("value");
			}
			else if (name.equals("first_name"))
			{
				member.first_name = (String) data.get("value");
			}
			else if (name.equals("last_name"))
			{
				member.last_name = (String) data.get("value");
			}
			else if (name.equals("gender"))
			{
				String x = (String) data.get("value");
				member.female = x==null || x.equals("Female");
			}
			
			
		}
		return member;
	}

	void parseEvents(String json) throws IOException
	{
		try {
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
			JSONObject collection = (JSONObject) jsonObject.get("collection");
			JSONArray items = (JSONArray) collection.get("items");
			Iterator<JSONObject> iterator = items.iterator();
			while (iterator.hasNext()) {
				JSONObject data = iterator.next();
				Event myEvent = parseEvent((JSONArray) data.get("data"));
				events.put(myEvent.eventid, myEvent);
				getAvailability(myEvent);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	
	void getAvailability(Event myEvent) throws IOException {
		
		System.out.println("Getting Availabiltiy for Event " + myEvent);
		final String json = 
				requestEventAvailability(myEvent);
		
		parseEventAvailability(json, myEvent);
	}



	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private Event parseEvent(JSONArray eventArray) {
		Event event = new Event();
		Iterator<JSONObject> iterator = eventArray.iterator();
		while (iterator.hasNext()) {
			
			final JSONObject data = iterator.next();
			final String name = (String) data.get("name");
			if (name.equals("id"))
			{
				event.eventid = (long) data.get("value");
			}
			else if (name.equals("name"))
			{
				event.name = (String) data.get("value");
			}
			
			else if (name.equals("arrival_date"))
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
		return event;
	}

	boolean once = true;

	void parseEventAvailability(String json, Event anEvent)
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
					Member member = getMember(memberid);
					
					anEvent.addMember(member, updated);
					member.addEvent(anEvent);
				}
				
				if (once)
				{
					once = false;
				}
			}
			System.out.println();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	
	Member getMember(long aMemberID)
	{
		Member member = members.get(aMemberID);
		if (member!=null) return member;
		
		
		
		member = createMember(aMemberID);
		
		members.put(aMemberID, member);
		
		return member;
	}
	
	private Member createMember(long aMemberID) {
		// TODO Auto-generated method stub
		return new Member();
	}

	Map<Long, Member> members = new HashMap<>();
	Map<Long, Event> events = new HashMap<>();
	
	void parseJson(String json)
	{
		try {
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
			System.out.println();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println();
	}
}
