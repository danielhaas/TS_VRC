package hk.warp.vrc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Event
{
	long eventid;
	String name;
	
	List<Member> members = new ArrayList<>();
	List<Member> no_shows = new ArrayList<>();
	List<Member> late = new ArrayList<>();
	
	public Date date;

	public void addMember(Member member, String updated) {
		members.add(member);
	}

	@Override
	public String toString() {
		return "Event [eventid=" + eventid + ", name=" + name + ", members=" + members + ", date=" + date + "]";
	}

	public boolean hasMemberAttended(Member member_) {
		for (Member member : members) {
			if (member.id== member_.id) return true;
		}
		return false;
	}

	public void addNoShow(Member member) {
		no_shows.add(member);
	}

	public void addLateCancel(Member member) {
		late.add(member);
	}
	
}