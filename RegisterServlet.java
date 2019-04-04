

import java.io.IOException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection conn = null;
	private ResultSet rs = null;
	private PreparedStatement ps = null;
	private HttpSession session = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		// TODO Auto-generated method stub
				response.getWriter().append("Served at: ").append(request.getContextPath());
				String username = request.getParameter("username");
				String password = request.getParameter("pass");
				String passwordConfirm = request.getParameter("confirmpass");
				String major = request.getParameter("major");
				String strGradYear = request.getParameter("gradYear");
				String firstName = request.getParameter("fName");
				String lastName = request.getParameter("lastName");
				String email = request.getParameter("email");
				
				int gradYear = Integer.parseInt(strGradYear);
				
								
				session = request.getSession();

				//check if passwords match
				if (!confPassword(password, passwordConfirm)) {
					
					//change in real version -- send an error and redirect
					request.setAttribute("passErr", "Passwords do not match");
					RequestDispatcher dispatch = getServletContext().getRequestDispatcher("/signup.jsp");
					dispatch.forward(request, response);
					
					System.out.print("Pass dont match");
					return;
				}
				
				//passwords match, lets try to create the user
				//this method will redirect the user accordingly, and close all open connections
				attemptRegister(request, response, username, email, password, major, firstName, lastName, gradYear);			
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	private boolean checkAvailability(String uName) {
		try {
			ps = conn.prepareStatement("SELECT * FROM users WHERE username=?");
			ps.setString(1, uName.trim());
			rs = ps.executeQuery();
			
			if(rs.next()) {
				return false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQL error; " + e);

			return false;
		}
		
		return true;
	}
	
	private boolean validateEmail(String email) {
		try {
			ps = conn.prepareStatement("SELECT * FROM users WHERE email=?");
			ps.setString(1, email.trim());
			rs = ps.executeQuery();
			
			if(rs.next()) {
				return false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQL error; " + e);

			return false;
		}
		
		return true;
	}
	
	private void sendConfirmation(String email) {
		//use clicksend / other API to send confirmation email
	}
	
	private boolean confPassword(String pass, String confPass) {
		if(!pass.trim().contentEquals(confPass.trim())){
			return false;
		}
		
		return true;
	}
	
	private void attemptRegister(HttpServletRequest request, HttpServletResponse response, String username, String email, String password, 
			String major, String firstName, String lastName, int gradYear) {
		String redirect = "";
		username.trim();
		password.trim();
		

		try {
			//replace all usernames with register object details
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ClassRank?user=root&password=pass&useLegacyDatetimeCode=false&serverTimezone=UTC");
			
			if(!checkAvailability(username)) {
				request.setAttribute("userErr", "Username already Exists");
				redirect = "/signup.jsp";
				System.out.println("Username unavail match");
			
			//if username free
			}else {
				if(!validateEmail(email)) {
					request.setAttribute("emailErr", "Email is invalid");
					redirect = "/signup.jsp";
					System.out.println("Username unavail match");
				}else {
					//hash the password before we put it in the table
					SecureRandom random = new SecureRandom();
					byte[] salt = new byte[16];
					random.nextBytes(salt);
					
					KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
					SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
					
					byte[] hashedPass = factory.generateSecret(spec).getEncoded();

					
					//all of this below may be 
					ps = conn.prepareStatement("INSERT INTO users (username, password, email, firstName, lastName, gradYear, major) VALUES (?,?, ?, ?, ?, ? "+ gradYear + ", ?)");
					ps.setString(1, username.trim());
					ps.setString(2, hashedPass.toString().trim()); //hashed password string
					
					//add all of the other strings from thisUser object
					
					ps.execute();

					ps = conn.prepareStatement("SELECT userId FROM users WHERE username=?");
					ps.setString(1, username.trim());
					rs = ps.executeQuery();

					rs.next();
					int userID = rs.getInt("userId");

					//create user object here
					//User newUser = new User(constructor params)
					
					redirect = "/signup.jsp";
					System.out.print("Should redirect");
					
					//log the user in
					//before this works I need to create the user object 
					session.setAttribute("userID", newUser);
					
					//if we get to this point then the user creation process was successful, send the confirmation email
					sendConfirmation(email);
				}
			}
			
			
		}catch (SQLException sqle) {
			System.out.println("sqle: "+sqle.getMessage());
		}catch(ClassNotFoundException cnfe) {
			System.out.println("cnfe: " + cnfe.getMessage());
		}finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(ps != null){
					ps.close();
				}
				if(conn != null) {
					conn.close();
				}
				
			}catch (SQLException sqle) {
				System.out.println("sqle in finally: " + sqle.getMessage());
			}
		}
		
		RequestDispatcher dispatch = getServletContext().getRequestDispatcher(redirect);
		dispatch.forward(request, response);
	}
}
