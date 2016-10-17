package hk.warp.vrc;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MenuServlet extends BaseQueryServlet{



	public MenuServlet(String clientId_, String client_secret_) {
		super(clientId_, client_secret_);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {


		final String code = req.getParameter("code");

		if (printHeader(req, resp))
		{
			final boolean isLoggedIn = code!=null;

			if (!isLoggedIn)
			{
				writeLogin(req, resp);
			}
			else
			{
				String accessToken = getToken(req, resp, code);
				Member myself = (Member) req.getSession().getAttribute("myself");
				Team myTeam =  (Team) req.getSession().getAttribute("team");
				if (myself==null)
				{
					myself = requestMyself(accessToken);
					req.getSession().setAttribute("myself", myself);
				}
				resp.getWriter().append("Hi ");
				resp.getWriter().append(myself.first_name);
				resp.getWriter().append(" ");
				resp.getWriter().append(myself.last_name);


				if (myTeam==null)
				{
					List<Team> teams = getTeams(accessToken, myself);
					if (teams.size()==1)
					{
						myTeam = teams.get(0);
						req.getSession().setAttribute("team", myTeam);
					}
					else
					{
						resp.getWriter().append("please choose a team<br><br>");

						for (Team team : teams) {
							resp.getWriter().append(team.name);
							resp.getWriter().append("<br>");

						}				
					}
				}
				resp.getWriter().append("(" );
				resp.getWriter().append(myTeam.name);
				resp.getWriter().append("),<br><br>\n" );

				writeLink(req, resp, "menu", "Show Menu");
				writeLink(req, resp, "members", "List all members");
				writeLink(req, resp, "attendance", "Show attendance");
				writeLink(req, resp, "attendance2", "Show attendance (Female, last 3 months)");

				resp.getWriter().append("<br><br>\n");

				String myNextAction = (String) req.getSession().getAttribute("next_action");

				if (myNextAction!=null)
				{
					if (myNextAction.equals("members"))
					{
						resp.getWriter().append("<b>Listing members of "+myTeam.name+"</b><br>\n");

						List<Member> members = requestMembers(accessToken, myTeam);

						resp.getWriter().append("<table>\n");

						for (Member member : members) {
							resp.getWriter().append("<tr><td>");
							resp.getWriter().append(member.first_name);
							resp.getWriter().append("</td>\n<td>");
							resp.getWriter().append(member.last_name);
							resp.getWriter().append("</td>\n<td>");
							resp.getWriter().append(member.female ? "female" : "male");
							resp.getWriter().append("</td>\n</tr>\n");
							
						}
						resp.getWriter().append("</table>\n");

					}
					else if (myNextAction.equals("attendance"))
					{
						List<Member> members = requestMembers(accessToken, myTeam);
						List<Event> events = requestEvents(accessToken, myTeam, members, true, false);
						
						resp.getWriter().append("<table>\n");

						for (Member member : members) {
							resp.getWriter().append("<tr><td>");
							resp.getWriter().append(member.first_name);
							resp.getWriter().append("</td>\n<td>");
							resp.getWriter().append(member.last_name);
							resp.getWriter().append("</td>\n<td>");
							resp.getWriter().append(member.female ? "female" : "male");
							resp.getWriter().append("</td>\n<td>");
							resp.getWriter().append("Events: " + member.events.size());
							
							resp.getWriter().append("</td>\n</tr>\n");
							
						}
						resp.getWriter().append("</table>\n");
					}
					else if (myNextAction.equals("attendance2"))
					{
						List<Member> members = requestMembers(accessToken, myTeam);
						List<Event> events = requestEvents(accessToken, myTeam, members, true, false);
						
						resp.getWriter().append("<table>\n");

						

						for (Member member : members) {
							member.last3 = countLast3Months(member.events);							
						}
						
						Collections.sort(members, new Comparator<Member>() {

							@Override
							public int compare(Member o1, Member o2) {
								return o2.last3-o1.last3;
							}
						});
						
						for (Member member : members) {
							if (member.female)
							{
								resp.getWriter().append("<tr><td>");
								resp.getWriter().append(member.first_name);
								resp.getWriter().append("</td>\n<td>");
								resp.getWriter().append(member.last_name);
								resp.getWriter().append("</td>\n<td>");
								resp.getWriter().append("Events: " + countLast3Months(member.events));

								resp.getWriter().append("</td>\n</tr>\n");
							}
						}
						resp.getWriter().append("</table>\n");
					}
				}
				/*String accessToken = getToken(req, resp, code);

		/*		String temp = requestMyself(accessToken);
				resp.getWriter().append(temp);

				resp.getWriter().append("<p><p><p><p><p>\n\n\n\n");
				temp = requestMembers(accessToken);
				resp.getWriter().append(temp);*/

			}
		}

		printFooter(resp);
	}
	

	private int countLast3Months(List<Event> events) {
		int counter = 0;
		Date today = new Date();
		Date my3monthsAgo = new Date(116, 8, 17);
		for (Event event : events) {
			final Date myDate = event.date;
			if (myDate.before(today) && myDate.after(my3monthsAgo))
			{
				counter++;
			}
			
		}
		return counter;
	}

	void writeLink(HttpServletRequest req, HttpServletResponse resp, String action, String descr) throws IOException
	{
		resp.getWriter().append("<a href=\"");
		resp.getWriter().append(		req.getRequestURL().toString());
		resp.getWriter().append("?action=");
		resp.getWriter().append(action);


		//				+ ""http://localhost:3000/
		resp.getWriter().append("\">");
		resp.getWriter().append(descr);
		resp.getWriter().append("</a><br>");
	}

}
