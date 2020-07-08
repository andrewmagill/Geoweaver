package gw.tools;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import gw.database.DataBaseOperation;
import gw.utils.BaseTool;
import gw.utils.RandomString;

public class HostTool {

	static Logger logger = Logger.getLogger(HostTool.class);
	

	/**
	 * Judge if the host is localhost
	 * @param hid
	 * @return
	 */
	public static boolean islocal(String hid) {
		
		boolean is = false;
		
		JSONObject obj = HostTool.detailJSONObj(hid);
		
		if("127.0.0.1".equals(obj.get("ip"))) {
			
			is = true;
			
		}
		
		return is;
		
	}
	
	
	/**
	 * Detail JSON object
	 * @param id
	 * @return
	 */
	public static JSONObject detailJSONObj(String id) {
		
		String sql = "select * from hosts where id = '" + id + "';";
		
		ResultSet rsmd = DataBaseOperation.query(sql);

	    JSONObject obj = new JSONObject();
	      
		try {
			
			if(rsmd.next()) {
				
			      int numColumns = rsmd.getMetaData().getColumnCount();
			      
			      for (int i=1; i<numColumns+1; i++) {
			      
			    	String column_name = rsmd.getMetaData().getColumnName(i).toLowerCase();

			        if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.ARRAY){
			        	obj.put(column_name, rsmd.getArray(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BIGINT){
			        	obj.put(column_name, rsmd.getInt(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BOOLEAN){
			        	obj.put(column_name, rsmd.getBoolean(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BLOB){
			        	obj.put(column_name, rsmd.getBlob(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.DOUBLE){
			        	obj.put(column_name, rsmd.getDouble(column_name)); 
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.FLOAT){
			        	obj.put(column_name, rsmd.getFloat(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.INTEGER){
			        	obj.put(column_name, rsmd.getInt(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.NVARCHAR){
			        	obj.put(column_name, rsmd.getNString(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.VARCHAR){
			        	obj.put(column_name, rsmd.getString(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.TINYINT){
			        	obj.put(column_name, rsmd.getInt(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.SMALLINT){
			        	obj.put(column_name, rsmd.getInt(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.DATE){
			        	obj.put(column_name, rsmd.getDate(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.TIMESTAMP){
			        	obj.put(column_name, rsmd.getTimestamp(column_name));   
			        }
			        else{
			        	obj.put(column_name, rsmd.getObject(column_name));
			        }
			      }

			}
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}

		
		return obj;
		
	}
	
	public static String detail(String id) {
		
		String detail = detailJSONObj(id).toJSONString();
		
		return detail;
		
	}
	
	public static String[] getHostDetailsById(String id) {
		
		StringBuffer sql = new StringBuffer("select * from hosts where id = '").append(id).append("'; ");
		
		String[] hostdetails = new String[4] ;
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
			if(rs.next()) {
				
				hostdetails[0] = rs.getString("name");
				
				hostdetails[1] = rs.getString("ip");
				
				hostdetails[2] = rs.getString("port");
				
				hostdetails[3] = rs.getString("username");
				
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
		
		return hostdetails;
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public static String list(String owner) {
		
		StringBuffer json = new StringBuffer("[");
		try {
			
			String sql = "select id, ip, name from hosts;";
			
			ResultSet rs = DataBaseOperation.query(sql);
			
			int num = 0;
			
			while(rs.next()) {
				
				String hostid = rs.getString("id");
				
				String hostname = rs.getString("name");
				
				String ip = rs.getString("ip");
				
				if( num++ != 0) {
					
					json.append(",");
					
				}
				
				json.append("{\"id\":\"").append(hostid)
					.append("\", \"name\": \"").append(hostname)
					.append("\", \"ip\": \"").append(ip)
					.append("\"}");
				
			}
			
			json.append("]");
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}
		
		return json.toString();
		
	}
	
	
	/**
	 * Add a new host
	 * @param hostname
	 * @param hostip
	 * @param hostport
	 * @param username
	 * @param owner
	 */
	public static String add(String hostname, String hostip, String hostport, String username, String owner) {
		
		String newhostid = new RandomString(6).nextString();
		
		StringBuffer sql = new StringBuffer("insert into hosts (id, name, ip, port, username, owner) values ('")
				
				.append(newhostid).append("', '")
				
				.append(hostname).append("', '")
				
				.append(hostip).append("', '")
				
				.append(hostport).append("', '")
				
				.append(username).append("', '")
				
				.append(owner).append("'); ");
		
//		logger.info(sql);
		
		DataBaseOperation.execute(sql.toString());
		
		return newhostid;
		
	}
	
	/**
	 * Remove a host from database
	 * @param hostid
	 */
	public static String del(String hostid) {
		
		StringBuffer sql = new StringBuffer("delete from hosts where id = '").append(hostid).append("';");
		
		DataBaseOperation.execute(sql.toString());
		
		return "done";
		
	}

	/**
	 * Add environment to database
	 * @param historyid
	 * @param bin
	 * @param env
	 * @param basedir
	 * @return
	 */
	public static String addEnv(String historyid, String hostid, String type, String bin, String env, String basedir, String settings) {
		
		String resp = null;
		
		try {
			
			String enviroment = getEnvironmentByBEB(hostid, bin, env, basedir);
			
			logger.info("existing environment " + enviroment);
			
			if(enviroment.equals("[]")) {
				
				StringBuffer sql = new StringBuffer("insert into environment (id, name, type, bin, pyenv, host, basedir, settings) values ('");
				
				sql.append(historyid).append("', '");
				
				sql.append(bin).append("-").append(env).append("-").append(basedir).append("', '");
				
				sql.append(type).append("', '");
				
				sql.append(bin).append("', '");
				
				sql.append(env).append("', '");

				sql.append(hostid).append("', '");
				
				sql.append(basedir).append("', '");
				
				sql.append(settings).append("' ); ");
				
				logger.info(sql);
				
				DataBaseOperation.execute(sql.toString());
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp;
		
	}
	
	public static void showAllEnvironment() {
		

		String resp = null;
		
		try {
			
			StringBuffer sql = new StringBuffer("select * from environment ;");
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			StringBuffer envstr = new StringBuffer();
			
			envstr.append("[");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				envstr.append("{ \"id\": \"").append(rs.getString("id"));
				
				envstr.append("\", \"name\": \"").append(rs.getString("name"));
				
				envstr.append("\", \"type\": \"").append(rs.getString("type"));
				
				envstr.append("\", \"bin\": \"").append(rs.getString("bin"));
				
				envstr.append("\", \"pyenv\": \"").append(rs.getString("pyenv")).append("\" }");
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
			logger.info(resp);
			
		} catch (SQLException e) {

			e.printStackTrace();
			
		}
	}
	
	public static String getEnvironmentByBEB(String hostid, String bin, String env, String basedir) {
		
		String resp = null;
		
		try {
			
			StringBuffer sql = new StringBuffer("select * from environment where host = '").append(hostid)
					.append("' and bin = '").append(bin).append("' and pyenv = '")
					.append(env).append("' and basedir = '").append(basedir).append("';");
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			StringBuffer envstr = new StringBuffer();
			
			envstr.append("[");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				envstr.append("{ \"id\": \"").append(rs.getString("id"));
				
				envstr.append("\", \"name\": \"").append(rs.getString("name"));
				
				envstr.append("\", \"type\": \"").append(rs.getString("type"));
				
				envstr.append("\", \"bin\": \"").append(rs.getString("bin"));
				
				envstr.append("\", \"basedir\": \"").append(rs.getString("basedir"));
				
				envstr.append("\", \"pyenv\": \"").append(rs.getString("pyenv")).append("\" }");
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
		} catch (SQLException e) {

			e.printStackTrace();
			
		}
		
		return resp;
		
	}
	
	/**
	 * Get environments by host
	 * @param hid
	 * @return
	 */
	public static String getEnvironments(String hid) {
		
		String resp = null;
		
		try {
			
			StringBuffer sql = new StringBuffer("select * from environment where host = '").append(hid).append("';");
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			StringBuffer envstr = new StringBuffer();
			
			envstr.append("[");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				envstr.append("{ \"id\": \"").append(rs.getString("id"));
				
				envstr.append("\", \"name\": \"").append(rs.getString("name"));
				
				envstr.append("\", \"type\": \"").append(rs.getString("type"));
				
				envstr.append("\", \"bin\": \"").append(rs.getString("bin"));
				
				envstr.append("\", \"basedir\": \"").append(rs.getString("basedir"));
				
				envstr.append("\", \"pyenv\": \"").append(rs.getString("pyenv")).append("\" }");
				
				num++;
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
			logger.info("the python environment for host: " + hid + " " + resp);
			
		} catch (SQLException e) {

			e.printStackTrace();
			
		}
		
		return resp;
	}
	
	
	

}
