package hk.warp.vrc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Member
{
	public Member() {
		
	}
	
	long id;
	List<Event> events = new ArrayList<>();
	List<Event> events_no_show = new ArrayList<>();
	List<Event> events_late = new ArrayList<>();
	List<Date> events_no_show_d = new ArrayList<>();
	List<Date> events_late_d = new ArrayList<>();
	public String first_name;
	public String last_name;
	public boolean female;
	public int last3;
	boolean player;
	String shirtsize;
	List<String> emails;
	
	public void addEvent(Event anEvent) {
		events.add(anEvent);
	}
	@Override
	public String toString() {
		return "Member [memberid=" + id + ", first_name=" + first_name + ", last_name="
				+ last_name + ", female=" + female + "]";
	}
	public boolean isPlayer() {
		return player;
	}
	
	public List<String> getMails()
	{
		return emails;
	}
	public void addEventNoShow(Event anEvent, Date aDate) {
		events_no_show.add(anEvent);
		events_no_show_d.add(aDate);
	}
	public void addEventLateCancel(Event anEvent, Date aDate) {
		events_late.add(anEvent);
		events_late_d.add(aDate);
	}
	
	
}