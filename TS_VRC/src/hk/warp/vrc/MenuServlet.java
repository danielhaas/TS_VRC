package hk.warp.vrc;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
				writeLink(req, resp, "shirts", "List all members with Shirts");
				writeLink(req, resp, "members_mails", "List all active members Email");
				writeLink(req, resp, "attendance", "Show attendance");
				writeLink(req, resp, "attendance2", "Show attendance (Female, last 3 months)");
				writeLink(req, resp, "attendance3", "Show attendance (Female, last 3 months) ext");
				writeLink(req, resp, "night", "Night Sessions");
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
					else if (myNextAction.equals("shirts"))
					{
						out.append("<b>Listing members of "+myTeam.name+"</b><br>\n");

						List<Member> members = requestMembers(accessToken, myTeam);
						getCustomFields(accessToken, members);

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
							requestMemberEmails(accessToken, member);
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
										
					else if (myNextAction.equals("members_mails"))
					{
						int counter =0;
						out.append("<b>Listing members of "+myTeam.name+"</b><br>\n");

						List<Member> members = requestMembers(accessToken, myTeam);

						out.append("<table>\n");
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

						for (Member member : members) {
							requestMemberEmails(accessToken, member);

							if (member.isPlayer())
							{
								counter++;
								out.append("<tr><td valign=top>");
								out.append(member.first_name);
								out.append(" ");
								out.append(member.last_name);
								out.append("</td>\n<td valign=top>");
								boolean oneline = true;
								if (member.emails!=null)
								{
									for (String email : member.emails) {
										if (!oneline)
											out.append(",<br>");
										out.append("<a href=\"mailto:" + email + "\">" + email +"</a>");
										oneline=false;
									}
								}
								else
								{
									out.append("No email");
								}
								
								out.append("</td>\n</tr>\n");
							}
							
						}
						out.append("</table>\n");
						
						out.append("<br>Active Players:" + counter);

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
					else if (myNextAction.equals("night"))
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
						out.append("<th>Event</th>");
						out.append("<th>Male</th>");
						out.append("<th>Female</th>");
						out.append("<th>Average</th>");
						out.append("</tr>\n");
						
						int tue_male=0;
						int tue_female=0;
						int tue_count=0;
						int thu_male=0;
						int thu_female=0;
						int thu_count=0;
						
						for (Event event : events) {
							final Date myDate = event.date;
							if (myDate.before(today) && myDate.after(myFrom))
							{
								if (event.name.toLowerCase().startsWith("tue pm"))
								{
									tue_male+=countMembers(event.members, false);
									tue_female+=countMembers(event.members, true);
									tue_count++;
								}
								else if (event.name.toLowerCase().startsWith("thu pm"))
								{
									thu_male+=countMembers(event.members, false);
									thu_female+=countMembers(event.members, true);
									thu_count++;
								}
							}
						}
						
						{
							out.append("<tr>");
							out.append("<td>");
							out.append("Tuesday Evening");
							out.append("</td>");
							
							out.append("<td>");
							out.append(""+tue_male);
							out.append("</td>");
							out.append("<td>");
							out.append(""+tue_female);
							out.append("</td>");
							
							out.append("<td>");
							
							double avg =(tue_male+tue_female)/(tue_count*1.0);
							avg = Math.round(avg*100)/100;
							out.append(avg+"");
							out.append("</td>");
							
							out.append("</tr>\n");								
							
						}			{
							out.append("<tr>");
							out.append("<td>");
							out.append("Thursday Evening");
							out.append("</td>");
							
							out.append("<td>");
							out.append(""+thu_male);
							out.append("</td>");
							out.append("<td>");
							out.append(""+thu_female);
							out.append("</td>");
							
							out.append("<td>");
							double avg =(tue_male+tue_female)/(thu_count*1.0);
							avg = Math.round(avg*100)/100;
							out.append(avg+"");
							out.append("</td>");
							

							out.append("</tr>\n");								
							
						}
						out.append("</table>\n");
						out.append("<p>\n");

						out.append("<table>\n");
						out.append("<tr>");
						out.append("<th>Name</th>");
						out.append("<th>Tuesday</th>");
						out.append("<th>Thursday</th>");
						out.append("<th>Total</th>");
						out.append("</tr>\n");

						List<Temp> temps = new ArrayList<MenuServlet.Temp>();
						for (Member member : members) {
							
							Temp myTemp = new Temp();
							myTemp.member = member;
							temps.add(myTemp);
							for (Event event : events) {
								final Date myDate = event.date;
								if (myDate.before(today) && myDate.after(myFrom))
								{
									if (event.hasMemberAttended(member))
									{
										if (event.name.toLowerCase().startsWith("tue pm"))
										{
											myTemp.tue++;
										}
										else if (event.name.toLowerCase().startsWith("thu pm"))
										{
											myTemp.thu++;
										}

									}

								}									
							}							
						}
						Collections.sort(temps);
						
						for (Temp myTemp : temps) {
							out.append("<tr><td>");
							out.append(myTemp.member.first_name);
							out.append(" ");
							out.append(myTemp.member.last_name);
							out.append("</td>\n");

							out.append("<td>");
							out.append(""+myTemp.tue);
							out.append("</td>");

							out.append("<td>");
							out.append(""+myTemp.thu);
							out.append("</td>");


							out.append("<td>");
							out.append(""+(myTemp.tue+myTemp.thu));
							out.append("</td>");


							out.append("</tr>\n");
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
	
	

	class Temp implements Comparable<Temp>
	{
		Member member;
		int tue;
		int thu;
		@Override
		public int compareTo(Temp o) {
			int x =  (o.tue+o.thu)-(tue+thu) ;
			if (x!=0)
			return x;
			return member.last_name.compareTo(member.last_name);
		}		
	}

	private int countMembers(List<Member> members, boolean female) {
		
		int out =0;
		for (Member member : members) {
			if (member.female == female) out++;
		}
		return out;
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
