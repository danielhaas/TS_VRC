package hk.warp.vrc;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Action_AttendanceSessions implements Action {

	@Override
	public String getDescr() {
		return "List Attendance per Session";
	}

	@Override
	public String getShort() {
		return "att_session";
	}

	@Override
	public void process(String accessToken, PrintWriter out, Team myTeam, BaseQueryServlet service) {
		List<Member> members = service.requestMembers(accessToken, myTeam);
		service.requestEvents(accessToken, myTeam, members, true, false);
		
		Date from = new Date(117,1,1);
		Date now = new Date();
		
		Collections.sort(members, new Comparator<Member>() {

			@Override
			public int compare(Member o1, Member o2) {
				final int x = 
						o1.first_name==null?0:
						o1.first_name.compareTo(o2.first_name);
				if (x!=0) return x;
				return o1.last_name.compareTo(o2.last_name);
			}
		});


		out.append("<table border=1>\n");
		out.append("<tr>\n");
		out.append("<td>Name</td>");
		out.append("<td>Gender</td>");
		out.append("<td>Tue AM</td>");
		out.append("<td>Tue PM</td>");
		out.append("<td>Wed AM</td>");
		out.append("<td>Thu AM</td>");
		out.append("<td>Thu PM</td>");
		out.append("<td>Sat</td>");
		out.append("<td>Total</td>");
		out.append("<td>Total(All)</td>");

		out.append("</tr>\n");
		
		for (Member member : members) {
			if (filter(member)) continue;
			int tues_am=0;
			int tues_pm=0;
			int wed_am=0;
			int thu_am=0;
			int thu_pm=0;
			int sat=0;
			int total = 0;
			int total_all = 0;
			
			for (Event event : member.events) {
					
				if (event.date.after(from)  && event.date.before(now))
				{
					if (event.name.contains("Tues"))
					{
						if (event.name.contains("AM"))
						{
							tues_am++;
						}
						else
							tues_pm++;
						total++;
					}
					else if (event.name.contains("Thurs"))
					{
						if (event.name.contains("AM"))
						{
							thu_am++;
						}
						else
							thu_pm++;
						total++;
					}
					else if (event.name.contains("Wed"))
					{
						if (event.name.contains("AM"))
						{
							wed_am++;
							total++;
						}
					}
					else if (event.name.contains("Sat"))
					{
						sat++;
						total++;
					}

					total_all++;
				}
			}
			
			print(out, member, tues_am, tues_pm, wed_am, thu_am, thu_pm, sat, total, total_all);

			out.append("</tr>\n");
			
		}
		out.append("</table>\n");

	}

	protected void print(PrintWriter out, Member member, int tues_am, int tues_pm, int wed_am, int thu_am, int thu_pm,
			int sat, int total, int total_all) {
		out.append("<tr>\n");
		out.append("<td>" + member.first_name + " " + member.last_name + "</td>");
		out.append("<td>"+(member.female ? "f" :"m")+"</td>");
		out.append("<td>"+tues_am+"</td>");
		out.append("<td>"+tues_pm+"</td>");
		out.append("<td>"+wed_am+"</td>");
		out.append("<td>"+thu_am+"</td>");
		out.append("<td>"+thu_pm+"</td>");
		out.append("<td>"+sat+"</td>");
		out.append("<td>"+total+"</td>");
		out.append("<td>"+total_all+"</td>");
	}

	public boolean filter(Member member) {
		if (!member.isPlayer()) return true;
		return false;
	}

}
