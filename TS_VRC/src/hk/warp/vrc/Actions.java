package hk.warp.vrc;

import java.util.ArrayList;
import java.util.List;

public class Actions {

	
	public static List<Action> getActions()
	{
		List<Action> actions = new ArrayList<Action>();
		actions.add(new Action_Menu());
		actions.add(new Action_Member());
		actions.add(new Action_Shirts());
		actions.add(new Action_No_show());
		actions.add(new Action_AttendanceSessions());
		actions.add(new Action_AttendanceSessionsWeekGuys());
		return actions;
	}
}
