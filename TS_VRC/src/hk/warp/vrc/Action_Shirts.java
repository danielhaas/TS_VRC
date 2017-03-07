package hk.warp.vrc;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Action_Shirts implements Action {

	@Override
	public String getDescr() {
		return "List all members with Shirts";
	}

	@Override
	public String getShort() {
		return "shirts";
	}

	@Override
	public void process(String accessToken, PrintWriter out, Team myTeam, BaseQueryServlet service) {
		out.append("<b>Listing members of "+myTeam.name+"</b><br>\n");

		List<Member> members = service.requestMembers(accessToken, myTeam);
		service.getCustomFields(accessToken, members);

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

		out.append("<table>\n");

		for (Member member : members) {
			service.requestMemberEmails(accessToken, member);
			out.append("<tr><td>");
			out.append(member.first_name);
			out.append("</td>\n<td>");
			out.append(member.last_name);
			out.append("</td>\n<td>");
			out.append(member.female ? "female" : "male");
			out.append("</td>\n<td>");
			out.append(member.isPlayer() ? "Player" : "Non Player");
			out.append("</td>\n<td>");
			if (member!=null && member.getMails()!=null)
			{
				for (String email : member.getMails()) {

					out.append("<a href=\"mailto:"+email+"\">"+email + "</a><br>");
				}
			}

			out.append("</td>\n<td>");
			out.append(member.shirtsize);
			out.append("</td>\n</tr>\n");
			
		}
		out.append("</table>\n");
	}

}
