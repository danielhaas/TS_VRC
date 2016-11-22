package hk.warp.vrc;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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
		String myNextAction = (String) req.getSession().getAttribute("next_action");

		String accessToken = getToken(req, resp, code);
		Member myself = (Member) req.getSession().getAttribute("myself");
		Team myTeam =  (Team) req.getSession().getAttribute("team");
		if (myself==null)
		{
			myself = requestMyself(accessToken);
			req.getSession().setAttribute("myself", myself);
		}
		final PrintWriter out = resp.getWriter();

		if (myNextAction!=null && myNextAction.startsWith("csv"))
		{
			printCSVHeader(resp);

			if (myNextAction.equals("csv_list"))
			{
				List<Member> members = requestMembers(accessToken, myTeam);
				List<Event> events = requestEvents(accessToken, myTeam, members, true, false);
				

				Date myFrom = new Date(116,0,1);
				Date today = new Date();
				

				for (Member member : members) {
					member.last3 = countLast(member.events, myFrom, today);							
				}
				
				Collections.sort(members, new Comparator<Member>() {

					@Override
					public int compare(Member o1, Member o2) {
						return o2.last3-o1.last3;
					}
				});
				
				out.append("First Name,");
				out.append("Sir Name,");
				out.append("Attendance Count,");
				for (Event event : events) {
					final Date myDate = event.date;
					if (myDate.before(today) && myDate.after(myFrom))
					{
						out.append(event.name + " (" + new SimpleDateFormat().format(event.date) + ")");
						out.append(",");
						
					}
				}
				out.append("\r\n");
				for (Member member : members) {
//					if (member.female)
					{
						out.append(member.first_name);
						out.append(",");
						out.append(member.last_name);
						out.append(",");
						out.append(member.last3+",");
						
						for (Event event : events) {
							final Date myDate = event.date;
							if (myDate.before(today) && myDate.after(myFrom))
							{
								
								if (event.hasMemberAttended(member))
								{
									out.append("true");
								}
								out.append(",");
								
							}									
						}
						
						
						
						
						out.append("\r\n");
					}
				}
			}
		}

		else if (printHeader(req, resp))
		{
			final boolean isLoggedIn = code!=null;

			if (!isLoggedIn)
			{
				writeLogin(req, resp);
			}
			else
			{
				out.append("Hi ");
				out.append(myself.first_name);
				out.append(" ");
				out.append(myself.last_name);


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
						out.append("please choose a team<br><br>");

						for (Team team : teams) {
							out.append(team.name);
							out.append("<br>");

						}				
					}
				}
				out.append("(" );
				out.append(myTeam.name);
				out.append("),<br><br>\n" );

				writeLink(req, resp, "menu", "Show Menu");
				writeLink(req, resp, "members", "List all members");
				writeLink(req, resp, "attendance", "Show attendance");
				writeLink(req, resp, "attendance2", "Show attendance (Female, last 3 months)");
				writeLink(req, resp, "attendance3", "Show attendance (Female, last 3 months) ext");
				writeLink(req, resp, "csv_list", "CSV for year", true);

				out.append("<br><br>\n");

//				String myNextAction = (String) req.getSession().getAttribute("next_action");

				if (myNextAction!=null)
				{
					if (myNextAction.equals("members"))
					{
						out.append("<b>Listing members of "+myTeam.name+"</b><br>\n");

						List<Member> members = requestMembers(accessToken, myTeam);

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
					else if (myNextAction.equals("attendance"))
					{
						List<Member> members = requestMembers(accessToken, myTeam);
						List<Event> events = requestEvents(accessToken, myTeam, members, true, false);
						
						out.append("<table>\n");

						for (Member member : members) {
							out.append("<tr><td>");
							out.append(member.first_name);
							out.append("</td>\n<td>");
							out.append(member.last_name);
							out.append("</td>\n<td>");
							out.append(member.female ? "female" : "male");
							out.append("</td>\n<td>");
							out.append("Events: " + member.events.size());
							
							
							out.append("<br>");
							
							Date today = new Date();
//							Date today = new Date(116,9,1);

							Date my3monthsAgo = new Date(116, 0, 1);
							for (Event event : member.events) {
								final Date myDate = event.date;
								if (myDate.before(today) && myDate.after(my3monthsAgo))
								{
									out.append(new SimpleDateFormat().format(event.date) + " " + event.name + "<br>");
								}
								
							}


							out.append("</td>\n</tr>\n");
							
						}
						out.append("</table>\n");
					}
					else if (myNextAction.equals("attendance2"))
					{
						List<Member> members = requestMembers(accessToken, myTeam);
						List<Event> events = requestEvents(accessToken, myTeam, members, true, false);
						
						out.append("<table>\n");

						

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
								out.append("<tr><td>");
								out.append(member.first_name);
								out.append("</td>\n<td>");
								out.append(member.last_name);
								out.append("</td>\n<td>");
								out.append("Events: " + countLast3Months(member.events) + "<br>");
								out.append("</td>\n</tr>\n");
							}
						}
						out.append("</table>\n");
					}
					else if (myNextAction.equals("attendance3"))
					{
						List<Member> members = requestMembers(accessToken, myTeam);
						List<Event> events = requestEvents(accessToken, myTeam, members, true, false);
						
						out.append("<table>\n");

						

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
//							if (member.female)
							{
								out.append("<tr><td>");
								out.append(member.first_name);
								out.append("</td>\n<td>");
								out.append(member.last_name);
								out.append("</td>\n<td>");
								out.append("Events: " + countLast3Months(member.events) + "<br>");
								
								Date today = new Date();
//								Date today = new Date(116,9,1);

								Date my3monthsAgo = new Date(116, 5, 1);
								for (Event event : member.events) {
									final Date myDate = event.date;
									if (myDate.before(today) && myDate.after(my3monthsAgo))
									{
										out.append(new SimpleDateFormat().format(event.date) + " " + event.name + "<br>");
									}
									
								}

								out.append("</td>\n</tr>\n");
							}
						}
						out.append("</table>\n");
					}
					else if (myNextAction.equals("csv"))
					{
						List<Member> members = requestMembers(accessToken, myTeam);
						List<Event> events = requestEvents(accessToken, myTeam, members, true, false);
						
						out.append("<table>\n");

						Date myFrom = new Date(116,0,1);
						Date today = new Date();
						

						for (Member member : members) {
							member.last3 = countLast(member.events, myFrom, today);							
						}
						
						Collections.sort(members, new Comparator<Member>() {

							@Override
							public int compare(Member o1, Member o2) {
								return o2.last3-o1.last3;
							}
						});
						
						out.append("<tr>");
						out.append("<td>First Name</td>");
						out.append("<td>Sir Name</td>");
						out.append("<td>Attendance Count</td>");
						for (Event event : events) {
							final Date myDate = event.date;
							if (myDate.before(today) && myDate.after(myFrom))
							{
								out.append("<td>");
								out.append(event.name + " (" + new SimpleDateFormat().format(event.date) + ")");
								out.append("</td>");
								
							}
						}
						out.append("</tr>\n");
						for (Member member : members) {
//							if (member.female)
							{
								out.append("<tr><td>");
								out.append(member.first_name);
								out.append("</td>\n<td>");
								out.append(member.last_name);
								out.append("</td>\n<td>");
								out.append(member.last3 + "</td>");
								
								for (Event event : events) {
									final Date myDate = event.date;
									if (myDate.before(today) && myDate.after(myFrom))
									{
										
										out.append("<td>");
										if (event.hasMemberAttended(member))
										{
											out.append("true");
										}
										out.append("</td>");
										
									}									
								}
								
								
								
								
								out.append("</tr>\n");
							}
						}
						out.append("</table>\n");
					}
				}
				/*String accessToken = getToken(req, resp, code);

		/*		String temp = requestMyself(accessToken);
				resp.getWriter().append(temp);

				resp.getWriter().append("<p><p><p><p><p>\n\n\n\n");
				temp = requestMembers(accessToken);
				resp.getWriter().append(temp);*/

			}
			printFooter(resp);
		}

	}
	

	private int countLast3Months(List<Event> events) {
		return countLast(events, new Date(116, 9, 1), null);
	}

	private int countLast(List<Event> events, Date aFromDate, Date aToDate) {
		int counter = 0;
//		Date today = new Date(116,9,1);
//		Date my3monthsAgo = new Date(116, 4, 1);
		Date today = aToDate!=null ? aToDate : new Date();
		Date my3monthsAgo = aFromDate;
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
		writeLink(req, resp, action, descr, false);
	}
	
	void writeLink(HttpServletRequest req, HttpServletResponse resp, String action, String descr, boolean newWindow) throws IOException
	{
		resp.getWriter().append("<a href=\"");
		resp.getWriter().append(		req.getRequestURL().toString());
		resp.getWriter().append("?action=");
		resp.getWriter().append(action);

		//				+ ""http://localhost:3000/
		resp.getWriter().append("\"");
		if (newWindow)
		{
			resp.getWriter().append(" target=\"_blank\" ");
		}
		
		resp.getWriter().append(">");
		resp.getWriter().append(descr);
		resp.getWriter().append("</a><br>");
	}

}
