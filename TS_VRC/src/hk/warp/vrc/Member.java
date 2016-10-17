package hk.warp.vrc;

import java.util.ArrayList;
import java.util.List;

class Member
{
	public Member() {
		
	}
	
	long id;
	List<Event> events = new ArrayList<>();
	public String first_name;
	public String last_name;
	public boolean female;
	public int last3;
	public void addEvent(Event anEvent) {
		events.add(anEvent);
	}
	@Override
	public String toString() {
		return "Member [memberid=" + id + ", first_name=" + first_name + ", last_name="
				+ last_name + ", female=" + female + "]";
	}
	
	
}