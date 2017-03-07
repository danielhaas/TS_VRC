package hk.warp.vrc;

import java.io.PrintWriter;

public interface Action {

	String getDescr();

	String getShort();

	void process(String accessToken, PrintWriter out, Team myTeam, BaseQueryServlet service);

}
