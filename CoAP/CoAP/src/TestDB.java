import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.SSLContext;


public class TestDB {
	
	private String message;
	
	private String url = "jdbc:mysql://localhost/university";
	private String id = "root";
	private String pwd = "ehdgk123";
	
	public TestDB(String message){
		this.message = message;
	}
	
	public void processMsg(String message, String tokenizer, String request){
			
		try{
	
			Connection con = doConnection(url, id, pwd);
			Statement state = con.createStatement();
			
			if(request.equals("GET")){
				ResultSet result = select(state, "SELECT * FROM STUDENT");
				/*TODO:
				 * while문 돌려서 getString으로 column마다 데이터 뽑아내고 String으로 만들든지 해서 재조합
				 */
			}
			else if(request.equals("PUT")){
				String[] strarr = message.split(tokenizer);
				modify(state, "query");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	// select
	public ResultSet select(Statement state, String query){
		
		ResultSet result = null;
		
		try {
			result = state.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	// insert, update, delete
	public void modify(Statement state, String query){
		try {
			state.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/*
	
	public static void main(String[] args){

		
		try {
			Connection con = doConnection();
			Statement state = con.createStatement();
			
			//SELECT
			//ResultSet result = state.executeQuery("SELECT * FROM STUDENT");
			
			//INSERT
			//state.executeUpdate("INSERT INTO STUDENT VALUES(700, 'TEST', '3', 'COMP')");
			
			//DELETE
			//state.executeUpdate("DELETE FROM STUDENT WHERE SNO = '700'");
			
			//UPDATE
			//state.executeUpdate("UPDATE STUDENT SET SNAME = 'CHANGED' WHERE SNO = '700'");
			
			
			
			while(result.next()){
				int id = result.getInt("SNO");
				String name = result.getString("SNAME");
				
				System.out.println(id + "\t" + name);
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		
	}
	*/
	public static Connection doConnection(String url, String id, String pwd){

		Connection con = null;
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			
			con = DriverManager.getConnection(url, id, pwd);
			System.out.println("Connection success");
		}catch(ClassNotFoundException e){
			System.err.println("Not found Driver");
		}catch(SQLException e){
			System.out.println("Connection failed");
			e.printStackTrace();
		}
		
		return con;
	}
}
