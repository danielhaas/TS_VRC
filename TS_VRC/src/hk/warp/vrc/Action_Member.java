package hk.warp.vrc;

import java.io.PrintWriter;
import java.util.List;

public class Action_Member implements Action{

	@Override
	public String getDescr() {
		return "List all members";
	}

	@Override
	public String getShort() {
		return "members";
	}

	@Override
	public void process(String accessToken, PrintWriter out, Team myTeam, BaseQueryServlet myService) {
		
		out.append("<b>Listing members of "+myTeam.name+"</b><br>\n");

		List<Member> members = myService.requestMembers(accessToken, myTeam);

		out.append("<table>\n");

		for (Member member : members) {
			out.append("<tr><td>");
			out.append(member.first_name);
			out.append("</td>\n<td>");
			out.append(member.last_name);
			out.append("</td>\n<td>");
			out.append(member.female ? "female" : "male");
			out.append("</td>\n</tr>\n");
			
		}
		out.append("</table>\n");
	}

}
