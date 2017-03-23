package hk.warp.vrc;

import java.io.PrintWriter;

public class Action_AttendanceSessionsWeekGuys extends Action_AttendanceSessions{

	
	@Override
	public String getDescr() {
		return "List Attendance per Session (only Guys @ Week)";
	}

	@Override
	public String getShort() {
		return "att_session2";
	}
	
	@Override
	protected void print(PrintWriter out, Member member, int tues_am, int tues_pm, int wed_am, int thu_am, int thu_pm,
			int sat, int total, int total_all) {		
		if (sat == total) return;
		super.print(out, member, tues_am, tues_pm, wed_am, thu_am, thu_pm, sat, total, total_all);
	}

	@Override
	public boolean filter(Member member) {
		if (super.filter(member)) return true;
		if (member.female) return true;
		return false;
	}

}
