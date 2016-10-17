package hk.warp.vrc;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.ImmutableMap;

public class BaseServlet extends HttpServlet{
	
	
	protected final String clientId;
	protected final String client_secret;
	
	public BaseServlet(String clientId_, String client_secret_) {
		clientId = clientId_;
		client_secret = client_secret_;
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

	
	// makes request and checks response code for 200
	protected String execute(HttpRequestBase request) throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String body = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
		}

		return body;
	}

	
	void writeParameters(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		final PrintWriter out = resp.getWriter();
		final Map<String,String[]> values = req.getParameterMap();
		for (String key : values.keySet()) {
			out.append(key +": ");
			for (String value : values.get(key)) {
				out.append(value +", ");				
			}
			out.append("<br>\n");

		}
	}

	void writeLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.getWriter().append("Redirecting to Authorization");
		// redirect to google for authorization
		StringBuilder oauthUrl = new StringBuilder().append("https://auth.teamsnap.com/oauth/authorize")
				.append("?client_id=").append(clientId) // the client id from the api console registration
				.append("&redirect_uri=http://localhost:3000/callback") // the servlet that google redirects to after authorization
				.append("&response_type=code");
		resp.sendRedirect(oauthUrl.toString());

	}

	boolean printHeader(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html;charset=utf-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		
		resp.getWriter().append("<html><head><title>Teamsnap Master</title><head><body>");

		if (req.getParameter("error") != null) {
			resp.getWriter().println(req.getParameter("error"));
			return false;
		}
		final String myParamAction = req.getParameter("action");
		
		if (myParamAction!=null)
			req.getSession().setAttribute("next_action", myParamAction);


		return true;
	}
	
	void printFooter(HttpServletResponse resp) throws IOException {
		resp.getWriter().append("<p><a href=\"http://localhost:3000/\">load menu</a> " + new SimpleDateFormat().format(new Date()));
		resp.getWriter().append("</body></html>");
	}


	String getToken(HttpServletRequest req, HttpServletResponse resp, String code) throws ClientProtocolException, IOException {
		
		String accessToken = (String) req.getSession().getAttribute("access_token");

		if (accessToken!=null && accessToken.length()>0) return accessToken;
		
		
		String body = post("https://auth.teamsnap.com/oauth/token", ImmutableMap.<String,String>builder()
				.put("client_id", clientId)
				.put("client_secret", client_secret)
				.put("redirect_uri", "http%3A%2F%2Flocalhost%3A3000%2Fcallback")
				.put("code", code)
				.put("grant_type", "authorization_code").build());

		JSONObject jsonObject = null;

		// get the access token from json and request info from Google
		try {
			jsonObject = (JSONObject) new JSONParser().parse(body);
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse json " + body);
		}

		// google tokens expire after an hour, but since we requested offline access we can get a new token without user involvement via the refresh token
		accessToken = (String) jsonObject.get("access_token");

		// you may want to store the access token in session
		req.getSession().setAttribute("access_token", accessToken);
		return accessToken;
	}

}
