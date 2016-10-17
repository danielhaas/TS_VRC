package hk.warp.vrc;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class ParserI_Net implements ParserI {

	@Override
	public void writeCSV(String csv) {
	/*	try {
			theResp.setContentType("text/csv");
			theResp.getWriter().println(csv);
		} catch (IOException e) {
			e.printStackTrace();
		}		*/
	}

	@Override
	public String requestEventAvailability(Event myEvent) {
	/*	final File myCacheFile;
		if (myEvent.date.before(new Date()))
		{
			myCacheFile = new File("cache/available_"+myEvent.eventid);
			if (myCacheFile.exists())
			{
				try {
					BufferedReader myReader = new BufferedReader(new FileReader(myCacheFile));
					String myReturn = myReader.readLine();
					myReader.close();
					return myReturn;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else
			myCacheFile=null;
		
		try {
			HttpGet g = new HttpGet("https://api.teamsnap.com/v3/availabilities/search?event_id="+myEvent.eventid);
			g.addHeader("Content-Type", "application/json");
			g.addHeader("Authorization", "Bearer " + accessToken);
			final String myReturn =  execute(g);
			if (myCacheFile!=null)
			{
				FileWriter myWriter = new FileWriter(myCacheFile);
				myWriter.write(myReturn+"\n");
				myWriter.close();
			}
			return myReturn;
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		return null;
	}

	@Override
	public String requestMembers(int aTeamID) {
/*		try {
			HttpGet g = new HttpGet("https://api.teamsnap.com/v3/members/search?team_id="+aTeamID); // members
			g.addHeader("Content-Type", "application/json");
			g.addHeader("Authorization", "Bearer " + accessToken);
			return execute(g);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		return null;
	}

	@Override
	public String requestEvents(int aTeamID) {
/*		try {
			HttpGet g = new HttpGet("https://api.teamsnap.com/v3/events/search?team_id="+aTeamID); // events
			g.addHeader("Content-Type", "application/json");
			g.addHeader("Authorization", "Bearer " + accessToken);
			return execute(g);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		return null;
	}
	
	// makes request and checks response code for 200
	private String execute(HttpRequestBase request) throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String body = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
		}

		return body;
	}

}
