package hk.warp.vrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ParserNet extends Parser{
	private Server server;

	final String clientId;
	final String clientSecret;

	String accessToken;
	private String teamId = "37803";
	HttpServletResponse theResp;
	
	public ParserNet() throws IOException {
		BufferedReader myReader = new BufferedReader(new FileReader("ts/login.txt"));
		clientId = myReader.readLine();
		clientSecret = myReader.readLine();
		myReader.close();
	}
	
	public static void main(String[] args) throws Exception {
		new ParserNet().startJetty();
	}

	public void startJetty() throws Exception {
		server = new Server(3000);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);

		final MenuServlet ms = new MenuServlet(clientId, clientSecret);
		
		context.addServlet(new ServletHolder(ms),"/");        
		context.addServlet(new ServletHolder(ms),"/callback");        
		// map servlets to endpoints
		context.addServlet(new ServletHolder(new SigninServlet(this)),"/signin");        
//		context.addServlet(new ServletHolder(new CallbackServlet(this)),"/callback");        

		server.start();
		server.join();
	}

	// makes a GET request to url and returns body as a string
	public String get(String url) throws ClientProtocolException, IOException {
		return execute(new HttpGet(url));
	}

	public String post(String url, Map<String,String> formParameters) throws ClientProtocolException, IOException {
		StringBuilder st = new StringBuilder(url);
		boolean first = true;
		for (String key : formParameters.keySet()) {
			st.append(first ? '?':'&');
			st.append(key);
			st.append('=');
			st.append(formParameters.get(key));
			first = false;
		}
		System.out.println(st.toString());
		return execute(new HttpPost(st.toString()));		
	}
	
	
	// makes a POST request to url with form parameters and returns body as a string
	public String post2(String url, Map<String,String> formParameters) throws ClientProtocolException, IOException { 
		HttpPost request = new HttpPost(url);
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();

		for (String key : formParameters.keySet()) {
			nvps.add(new BasicNameValuePair(key, formParameters.get(key))); 
		}

		request.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

		return execute(request);
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
	@Override
	void writeCSV(String csv) {
		try {
			theResp.setContentType("text/csv");
			theResp.getWriter().println(csv);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	String requestEventAvailability(Event myEvent) {
		final File myCacheFile;
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
		}
		return null;
	}

	@Override
	String requestMembers() {
		try {
			HttpGet g = new HttpGet("https://api.teamsnap.com/v3/members/search?team_id="+teamId); // members
			g.addHeader("Content-Type", "application/json");
			g.addHeader("Authorization", "Bearer " + accessToken);
			return execute(g);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	String requestEvents() {
		try {
			HttpGet g = new HttpGet("https://api.teamsnap.com/v3/events/search?team_id="+teamId); // events
			g.addHeader("Content-Type", "application/json");
			g.addHeader("Authorization", "Bearer " + accessToken);
			return execute(g);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
