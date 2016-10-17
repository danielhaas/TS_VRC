package hk.warp.vrc;

public interface ParserI {
	public void writeCSV(String csv);
	public String requestEventAvailability(Event myEvent);
	public String requestMembers(int aTeamID);
	public String requestEvents(int aTeamID);
}
