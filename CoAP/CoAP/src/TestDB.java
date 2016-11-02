import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.SSLContext;


public class TestDB {
	public static void main(String[] args){

		
		try {
			Connection con = makeConnection();
			Statement state = con.createStatement();
			
			//SELECT
			//ResultSet result = state.executeQuery("SELECT * FROM STUDENT");
			
			//INSERT
			//state.executeUpdate("INSERT INTO STUDENT VALUES(700, 'TEST', '3', 'COMP')");
			
			//DELETE
			//state.executeUpdate("DELETE FROM STUDENT WHERE SNO = '700'");
			
			//UPDATE
			//state.executeUpdate("UPDATE STUDENT SET SNAME = 'CHANGED' WHERE SNO = '700'");
			
			
			/*
			while(result.next()){
				int id = result.getInt("SNO");
				String name = result.getString("SNAME");
				
				System.out.println(id + "\t" + name);
			}
			*/
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		
	}
	
	public static Connection makeConnection(){
		String url = "jdbc:mysql://localhost/university";
		String id = "root";
		String pwd = "ehdgk123";
		
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
