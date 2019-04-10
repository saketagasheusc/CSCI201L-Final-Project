

import java.io.IOException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		HttpSession session = request.getSession();
		
		//get login data
		String username = request.getParameter("username");
		String password = request.getParameter("pass");
		
		//set up connection data
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		User thisUser = null;
		
		try {
			//replace all usernames with register object details
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ClassRank?user=root&password=pass&useLegacyDatetimeCode=false&serverTimezone=UTC");

			//check if the user is valid
			ps = conn.prepareStatement("SELECT password, salt FROM users WHERE username=?");
			ps.setString(1, username.trim());
			rs = ps.executeQuery();
			
			rs.next();
			String passHash = rs.getString("password");
			String salt = rs.getString("salt");
			
			KeySpec spec = new PBEKeySpec(passHash.toCharArray(), salt.getBytes(), 65536, 128);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			
			byte[] hashedPass = factory.generateSecret(spec).getEncoded();
			
			if(hashedPass == passHash.getBytes()) {
				//fill in constructor
				thisUser = new User();
			}
			
			//if not the thisUser will not equal anything
			
		}catch (SQLException sqle) {
			System.out.println("sqle: "+sqle.getMessage());
		}catch(ClassNotFoundException cnfe) {
			System.out.println("cnfe: " + cnfe.getMessage());
			
		}finally{
			//no matter what set the userID variable in the session
			session.setAttribute("userID", thisUser);
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
