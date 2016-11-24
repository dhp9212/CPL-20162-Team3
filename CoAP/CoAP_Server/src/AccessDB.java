import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.SSLContext;


public class AccessDB {
	
	static final String REQUEST_CURRENT_TEMP = "2";
	static final String RQUEST_TODAY_TEMP ="3";
	
	static final String ENDPOINT_BLUETOOTH = "BT";
	static final String ENDPOINT_BLUETOOTH_CLOSE = "BTCL";
	static final String ENDPOINT_SENSOR_TEMPERATURE = "TMPR";

	
	private String url = "jdbc:mysql://localhost/VLC_Infra";
	private String id = "root";
	private String pwd = "ehdgk123";
	
	
	public String processMsg(String message, String request){

		String ret = "";
		String seperater = "#"; // for device
		String tokenizer = "/"; // for data
		
		try{
	
			Connection con = getConnection(url, id, pwd);
			Statement state = con.createStatement();
			
			if(request.equals("GET")){
				
				String query = "";

				
				
				if(message.equals(REQUEST_CURRENT_TEMP)){
					query = "SELECT SVALUE FROM SENSOR_DATA "
							+ "ORDER BY STIME DESC LIMIT 1";
					
					
					ResultSet result = select(state, query);
					
					
					while(result.next()){
						
						float value = result.getFloat("SVALUE");
						
						/*
						value = Math.round(value*10);
						value = value / 10;
						*/
						ret += value;
						
						if(!result.isLast()){
							ret += "/";
						}
					}
				}
				
				else if(message.equals(RQUEST_TODAY_TEMP)){
					query = "SELECT SVALUE FROM SENSOR_DATA "
							+ "WHERE EXTRACT(YEAR FROM STIME) = EXTRACT(YEAR FROM NOW()) "
							+ "AND EXTRACT(MONTH FROM STIME) = EXTRACT(MONTH FROM NOW()) "
							+ "AND EXTRACT(DAY FROM STIME) = EXTRACT(DAY FROM NOW()) "
							+ "AND EXTRACT(HOUR FROM STIME) MOD 3 = 0 "
							+ "AND EXTRACT(MINUTE FROM STIME) = 0 "
							+ "AND EXTRACT(SECOND FROM STIME) = 0";
							
					ResultSet result = select(state, query);
					
					while(result.next()){
						
						float value = result.getFloat("SVALUE");
						/*
						value = Math.round(value*10);
						value = value / 10;
						*/
						ret += value;
						
						if(!result.isLast()){
							ret += "/";
						}
					}
				}
				System.out.println(ret);
			}
			else if(request.equals("PUT")){
				String[] sep = message.split(seperater);
				
				String[] token = sep[1].split(tokenizer);
				
				
				String query = "";
				

				if(sep[0].equals(ENDPOINT_BLUETOOTH)){
					
					boolean vlc_con = true;
					boolean ep_con = true;
					
					
					if(vlc_con = chkInsert(state, "VLC_INFO", "MAC", token[0])){
						query = "INSERT INTO VLC_INFO VALUES('" + token[0] + "', '" + token[1] + "')";
						modify(state, query);
					}
					if(ep_con = chkInsert(state, "EP_INFO", "MAC", token[2])){
						query = "INSERT INTO EP_INFO VALUES('" + token[2] + "', '" + token[3] + "')";
						modify(state, query);
					}
					
					
					// handover ????????
					if(chkHandover(state, token[0], token[2])){
						query = "UPDATE REGISTRATION SET VLC_MAC = '" + token[0] + "' ";
						query += "WHERE EP_MAC = '" + token[2] + "'";
						modify(state, query);
					}
					
					// new pairing registration
					else{
						query = "INSERT INTO REGISTRATION VALUES('" + token[0] + "', '" + token[2] + "')";
						modify(state, query);
					}
					
					
					
					
				}
				else if(sep[0].equals(ENDPOINT_SENSOR_TEMPERATURE)){
					query = "INSERT INTO SENSOR_DATA VALUES('" + sep[0] + "', " + Float.parseFloat(token[0]) + ", now())";
					modify(state, query);
				}
				else if(sep[0].equals(ENDPOINT_BLUETOOTH_CLOSE)){
					query = "DELETE FROM REGISTRATION WHERE VLC_MAC = '" + token[0] + "' AND EP_MAC = '" + token[1] + "'";
					modify(state, query);
				}
				ret = "Data Accepted";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
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
	
	
	public static Connection getConnection(String url, String id, String pwd){

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
	
	public boolean chkInsert(Statement state, String table, String col, String condition) throws SQLException{
		
		boolean ret = true;
		String query = "";
		
		query = "SELECT " + col + " FROM " + table + " WHERE " + col + " = '" + condition + "'";
		
		
		ResultSet result = select(state, query);
		
		while(result.next()){
			if(result.getString(col).equals(condition)){
				ret = false;
				break;
			}
		}
		return ret;
	}
	
	
	// different VLC_MAC at same EP_MAC return true
	public boolean chkHandover(Statement state, String vlc_mac, String ep_mac) throws SQLException{
		
		boolean ret = true;
		
		boolean vlc_con = false;
		boolean ep_con = false;
		
		ResultSet result;
		String query = "";
		
		query = "SELECT VLC_MAC FROM REGISTRATION WHERE VLC_MAC = '" + vlc_mac + "'";
		result = select(state, query);
		
		while(result.next()){
			if(result.getString("VLC_MAC").equals(vlc_mac)){
				vlc_con = false;
				break;
			}
		}
		
		query = "SELECT EP_MAC FROM REGISTRATION WHERE EP_MAC = '" + ep_mac + "'";
		result = select(state, query);
		
		while(result.next()){
			if(result.getString("EP_MAC").equals(vlc_mac)){
				vlc_con = true;
				break;
			}
		}
				
		return (vlc_con && ep_con);
		
	}

}
