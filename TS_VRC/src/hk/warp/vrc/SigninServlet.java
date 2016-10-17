package hk.warp.vrc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class SigninServlet extends HttpServlet {
		/**
		 * 
		 */
		private final ParserNet parserNet;

		/**
		 * @param parserNet
		 */
		SigninServlet(ParserNet parserNet) {
			this.parserNet = parserNet;
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {

			// redirect to google for authorization
			StringBuilder oauthUrl = new StringBuilder().append("https://auth.teamsnap.com/oauth/authorize")
					.append("?client_id=").append(this.parserNet.clientId) // the client id from the api console registration
//					.append("&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fcallback") // the servlet that google redirects to after authorization
					.append("&redirect_uri=http://localhost:3000/callback") // the servlet that google redirects to after authorization
					.append("&response_type=code")
//					.append("&scope=openid%20email") // scope is the api permissions we are requesting
//					
					
//					.append("&redirect_uri=http://localhost:3000/callback") // the servlet that google redirects to after authorization
//					.append("&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id")
//					.append("&access_type=offline") // here we are asking to access to user's data while they are not signed in
//					.append("&approval_prompt=force"); // this requires them to verify which account to use, if they are already signed in
;
			resp.sendRedirect(oauthUrl.toString());
		} 
	}