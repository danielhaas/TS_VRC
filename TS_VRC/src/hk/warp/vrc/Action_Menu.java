package hk.warp.vrc;

import java.io.PrintWriter;

public class Action_Menu implements Action {

	@Override
	public String getDescr() {
		return "Show Menu";
	}

	@Override
	public String getShort() {
		return "menu";
	}

	@Override
	public void process(String accessToken, PrintWriter out, Team myTeam, BaseQueryServlet service) {
		// TODO Auto-generated method stub

	}

}
