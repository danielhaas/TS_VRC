package hk.warp.vrc;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Action_No_show implements Action {

	
	@Override
	public String getDescr() {
		return "Show No Shows";
	}

	@Override
	public String getShort() {
		return "no shows";
	}

	@Override
	public void process(String accessToken, PrintWriter out, Team myTeam, BaseQueryServlet service) {
		List<Member> members = service.requestMembers(accessToken, myTeam);
		service.requestEvents(accessToken, myTeam, members, true, false);

		for (Member member : members) {
			if (member.first_name.equals("Julia"))
			{
				out.append(member.first_name);
				out.append(" ");
				out.append(member.last_name);
				out.append("<br>");
				
				
				out.append("<table><tr>");
				out.append("<th colspan=3>Late Cancels:</th></tr>\n");
				out.append("<tr><td>Event");				
				out.append("</td><td>Event Time");
				out.append("</td><td>Cancelled at");
				out.append("</td></tr>\n");

				for (int i = 0; i < member.events_late.size(); i++) {
					Event event = member.events_late.get(i);
					Date myDate = member.events_late_d.get(i);
					out.append("<tr><td>");
					
					out.append(event.name);
					out.append("</td><td>");
					out.append(toDate(event.date));
					out.append("</td><td>");
					out.append(toDate(myDate));
					out.append("</td></tr>\n");
				}
				
				out.append("<th colspan=3>No Shows:</th></tr>\n");
				for (int i = 0; i < member.events_no_show.size(); i++) {
					Event event = member.events_no_show.get(i);
					Date myDate = member.events_no_show_d.get(i);
					out.append("<tr><td>");
					
					out.append(event.name);
					out.append("</td><td>");
					out.append(toDate(event.date));
					out.append("</td><td>");
					out.append(toDate(myDate));
					out.append("</td></tr>\n");
				}
				out.append("</tr></table>");
				
			}
		}

	}
	
	
	String toDate(Date aDate)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(new Date(aDate.getTime()+8 *60*60*1000));
	}

}
