package hk.warp.vrc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Event
{
	long eventid;
	String name;
	
	List<Member> members = new ArrayList<>();
	public Date date;

	public void addMember(Member member, String updated) {
		members.add(member);
	}

	@Override
	public String toString() {
		return "Event [eventid=" + eventid + ", name=" + name + ", members=" + members + ", date=" + date + "]";
	}
	
}