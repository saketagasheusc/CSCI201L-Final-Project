

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class ClassPageUpdateServlet
 */
@WebServlet("/ClassPageUpdateServlet")
public class ClassPageUpdateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private HttpSession session;
    private User ourUser = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClassPageUpdateServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		//save the session data
		session = request.getSession();

		
		//check if the user is logged in
		if(isLoggedIn()) {
			//get all the info needed from the POST request
			
			//we have to increment
			if(session.getAttribute("diff") != null && session.getAttribute("rating") != null && session.getAttribute("yearTaken") != null
					&& session.getAttribute("grade") != null && session.getAttribute("comment") != null) {
				
				int difficulty = (int) session.getAttribute("diff");
				int rating = (int) session.getAttribute("rating");
				String yearTaken = (String) session.getAttribute("yearTaken");
				String gradeEarned = (String) session.getAttribute("grade");
				String comment = (String) session.getAttribute("comment");
				
				
				//now that we have all the data, update the page
				updatePage(difficulty, rating, yearTaken, gradeEarned, comment);
			}else {
				//throw an error saying too few parameters
			}
		}else {
			//return an error saying this action is only allowed when a user is logged in
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private boolean isLoggedIn() {
		if(session.getAttribute("userObj") != null) {
			//throws error bc User type not defined
			ourUser = session.getAttribute("userObj");
			return true;
		}
		
		ourUser = null;
		return false;
	}
	
	private void updatePage(int diffRatingVal, int ratingVal, String yearTaken, String gradeEarned, String comment) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ClassRank?user=root&password=pass&useLegacyDatetimeCode=false&serverTimezone=UTC");
			String userId = ourUser.userId;
			int intUid = Integer.parseInt(userId);
					
			//get the class data from the session variables
			int currentClassId = session.getAttribute("currentClassId"); //set this when we are on the class page
			
			//before we insert, get the professorId from the classes table
			if(currentClassId != null) {
				ps = conn.prepareStatement("SELECT professorID FROM Classes WHERE classId = " + currentClassId)
				rs = ps.executeQuery();
				
				rs.next();
				int professorId = rs.getInt("professorID");
				
				ps = conn.prepareStatement("INSERT INTO Ratings (userID, classID, professorID, difficulty, OverallQuality, YearTaken, GradeEarned, comment) "
						+ "VALUES (" + intUid + ", " + currentClassId + ", " + professorId + ", " + diffRatingVal + ", " + ratingVal + ", ?, ?, ?)");
				
				ps.setString(1, yearTaken.trim());
				ps.setString(2, gradeEarned.trim());
				ps.setString(3, comment.trim());
				rs = ps.executeQuery();
				
			}else {
				//throw some error about class not existing
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
		
	}

}
