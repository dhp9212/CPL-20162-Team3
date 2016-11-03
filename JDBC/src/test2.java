import java.sql.*;

public class test2 {

	public static void main(String[] args) {
		Connection con = null;
		try {			
			con = DriverManager.getConnection("jdbc:mysql://localhost/test?autoReconnect=true&useSSL=false","root", "Cheorhks56@@");
			Statement st = null;
			ResultSet rs = null;
			st = con.createStatement();
			rs = st.executeQuery("select Name, Artist, RunningTime, Album from music");

			System.out.println("太擠學貲 太\t\t太陛熱貲太\t 太堊 望檜太\t 太撼彰檜葷太");
			System.out.println("-------------------------------------------");
			
			while (rs.next()) {
				String Name = rs.getString("Name");
				String Artist = rs.getString("Artist");
				String RunningTime = rs.getString("RunningTime");
				String Album = rs.getString("Album");
				
				System.out.println(Name +"\t" + Artist + "\t" + RunningTime+"\t" +Album+ "\n");						
			}
			
		} catch (SQLException sqex) {	
			System.out.println("SQLException: " + sqex.getMessage());
			System.out.println("SQLState: " + sqex.getSQLState());
		}
	}
}
