package hk.warp.vrc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.ImmutableMap;

class CallbackServlet extends HttpServlet {
		/**
		 * 
		 */
		private final ParserNet parserNet;

		/**
		 * @param parserNet
		 */
		CallbackServlet(ParserNet parserNet) {
			this.parserNet = parserNet;
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
			// google redirects with
			//http://localhost:8089/callback?state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id&code=4/ygE-kCdJ_pgwb1mKZq3uaTEWLUBd.slJWq1jM9mcUEnp6UAPFm0F2NQjrgwI&authuser=0&prompt=consent&session_state=a3d1eb134189705e9acf2f573325e6f30dd30ee4..d62c

			// if the user denied access, we get back an error, ex
			// error=access_denied&state=session%3Dpotatoes

			if (req.getParameter("error") != null) {
				resp.getWriter().println(req.getParameter("error"));
				return;
			}

			// google returns a code that can be exchanged for a access token
			String code = req.getParameter("code");

			// get the access token by post to Google
			String body = this.parserNet.post("https://auth.teamsnap.com/oauth/token", ImmutableMap.<String,String>builder()
					.put("client_id", this.parserNet.clientId)
					.put("client_secret", this.parserNet.clientSecret)
					.put("redirect_uri", "http%3A%2F%2Flocalhost%3A3000%2Fcallback")
					
//					.put("redirect_uri", "http://localhost:3000/callback")
					.put("code", code)
					.put("grant_type", "authorization_code").build());

			// ex. returns
			//   {
			//	       "access_token": "ya29.AHES6ZQS-BsKiPxdU_iKChTsaGCYZGcuqhm_A5bef8ksNoU",
			//	       "token_type": "Bearer",
			//	       "expires_in": 3600,
			//	       "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjA5ZmE5NmFjZWNkOGQyZWRjZmFiMjk0NDRhOTgyN2UwZmFiODlhYTYifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiZW1haWwiOiJhbmRyZXcucmFwcEBnbWFpbC5jb20iLCJhdWQiOiI1MDgxNzA4MjE1MDIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdF9oYXNoIjoieUpVTFp3UjVDX2ZmWmozWkNublJvZyIsInN1YiI6IjExODM4NTYyMDEzNDczMjQzMTYzOSIsImF6cCI6IjUwODE3MDgyMTUwMi5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImlhdCI6MTM4Mjc0MjAzNSwiZXhwIjoxMzgyNzQ1OTM1fQ.Va3kePMh1FlhT1QBdLGgjuaiI3pM9xv9zWGMA9cbbzdr6Tkdy9E-8kHqrFg7cRiQkKt4OKp3M9H60Acw_H15sV6MiOah4vhJcxt0l4-08-A84inI4rsnFn5hp8b-dJKVyxw1Dj1tocgwnYI03czUV3cVqt9wptG34vTEcV3dsU8",
			//	       "refresh_token": "1/Hc1oTSLuw7NMc3qSQMTNqN6MlmgVafc78IZaGhwYS-o"
			//   }

			JSONObject jsonObject = null;

			// get the access token from json and request info from Google
			try {
				jsonObject = (JSONObject) new JSONParser().parse(body);
			} catch (ParseException e) {
				throw new RuntimeException("Unable to parse json " + body);
			}

			// google tokens expire after an hour, but since we requested offline access we can get a new token without user involvement via the refresh token
			this.parserNet.accessToken = (String) jsonObject.get("access_token");

			// you may want to store the access token in session
			req.getSession().setAttribute("access_token", this.parserNet.accessToken);

			this.parserNet.theResp = resp;

			this.parserNet.runIt();
			
		} 
	}