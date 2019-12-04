package socparser;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DBRenderer {
	
	//private final String url = "jdbc:mysql://localhost:3306/";
	private Connection conn = null;
	private ArrayList<String> parseList = new ArrayList<String>(); // used ArrayList for its auto resizing
	
	DBRenderer(String dbUrl, String driver) {
		connectToServer(dbUrl, driver);	
		//createDatabase(dbName);
		//connectToServer(dbUrl + dbName);
	}
	
	public Connection getConnection() {
		return this.conn;
	}
	
	public void connectToServer(String url, String driver) {
		System.out.println("Connecting to server...");
		try {
			Class.forName(driver);
		    conn = DriverManager.getConnection(url);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createDatabase(String dbName) {
		Statement stmt = null;
		//connectToServer(url);
		try {  
			if (!databaseExists(dbName)) {
				System.out.println("Creating database...");
				stmt = conn.createStatement();	      
				String sql = "CREATE DATABASE " + dbName;
				stmt.executeUpdate(sql);
				System.out.println("Database created successfully...");
			}
		}catch (SQLException se) {
	    	//Handle errors for JDBC
	    	se.printStackTrace();
	    }finally {
	    	//finally block used to close resources
	    	try {
	    		if (stmt != null)
	    			stmt.close();
	    	}catch (SQLException se2) {} // nothing we can do
	   }
	   System.out.println("Goodbye!");
	}
	
	public void createTable(String tableName, String[] columns) {
		Statement stmt = null;
		try {	
			if (!tableExists(tableName)) {
				stmt = conn.createStatement();
				if (columns != null) {
					String sql = "CREATE TABLE \"" + tableName + "\"(";
					for (String s : columns) {
		        		sql += ("\"" + s + "\"");
		        		sql += " varchar(255),";
		        	}
		        	sql = sql.substring(0, sql.length() - 1); // chop trailing comma
					sql += ")";
		        	sql = sql.replace("\\", ""); // filter escape characters
		        	System.out.println(sql);
					stmt.executeUpdate(sql);
					System.out.println("Table " + tableName + " created successfully...");
				}
				else {
					System.out.println("Failed to create table: " + tableName + ".");
				}
			}
		}catch (SQLException se) {
			   //Handle errors for JDBC
			   se.printStackTrace();
		}finally {
			   //finally block used to close resources
			try {
				if(stmt != null)
					stmt.close();
			}catch (SQLException se2) {
			}// nothing we can do
		}
		System.out.println("Goodbye!");
	}
	
	public boolean databaseExists(String dbName) {
		System.out.println("Checking if database exists...");
		try (ResultSet resultSet = conn.getMetaData().getCatalogs()) {	
			//iterate each catalog in the ResultSet
			while (resultSet.next()) {
			  // Get the database name, which is at position 1
				if (dbName.toLowerCase().equals(resultSet.getString(1))) {
					System.out.println("Database already exists.");
					return true;
				}
			}
			System.out.println("Database does not yet exist.");
			resultSet.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return false;
	}
	
	public boolean tableExists(String tableName) {
		System.out.println("Checking if table " + tableName + " exists...");
	    try (ResultSet rs = conn.getMetaData().getTables(null, null, null, null)) {
	        while (rs.next()) { 
	            String tName = rs.getString("TABLE_NAME");
	            if (tName != null && tName.toLowerCase().equals(tableName.toLowerCase())) {
	        		System.out.println("Table " + tableName + " already exists.");
	                return true;
	            }
	        }
    		System.out.println("Table " + tableName + " does not yet exist.");
	    } catch (SQLException se) {
			se.printStackTrace();
		}
	    return false;
	}
	
	public void insertRowInDB(String tableName, RowObject rowItem) {
		Set<Map.Entry<String, String>> rowData = rowItem.getData();
        PreparedStatement ps = null;
        try {
	        // String sql = "Insert into SAMPLE(Course,GordonRule,GenEd,Section,ClassNum,MinMaxCred,Days,Time,MeetingPattern,Spec,SOC,CourseTitle,Instructor,EnrollmentCap,ScheduleCodes) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        	String sql = "Insert into \"" + tableName + "\"(";
        	// The keys are our column labels
        	for (Entry<String, String> entry : rowData) {
        		sql += ("\"" + entry.getKey() + "\"");
        		sql += ",";
        	}
        	sql = sql.substring(0, sql.length() - 1); // chop trailing comma
        	sql += ") values(";
        	for (Entry<String, String> entry : rowData) {
        		sql += "?,";
        	}
        	sql = sql.substring(0, sql.length() - 1); // chop trailing comma
        	sql += ")";
        	sql = sql.replace("\\", ""); // filter escape characters
        	System.out.println(sql);
	        ps = conn.prepareStatement(sql);
	        int index = 1;
	        for (Entry<String, String> entry : rowData) {
        		ps.setString(index++, entry.getValue().toString());
        	}
	        ps.executeUpdate();
	        System.out.println("Values Inserted Successfully");
        } catch (SQLException se) {
        	se.printStackTrace();
        }
    }
	
	public ArrayList<String> getTableNames() {
		ArrayList<String> result = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = conn.getMetaData().getTables("SOCPARSER", "PUBLIC", null, null);
			while (rs.next()) {              
			   result.add(rs.getString(3));
			}
		}
		catch(SQLException se) {
			se.printStackTrace();
		}
		return result;
	}
	
	public ResultSet getTableEntries(String tableName) {
		String sql = "SELECT * from \"" + tableName + "\"";
		ResultSet rs = null;
		try {
			rs = conn.createStatement().executeQuery(sql);
		} catch (SQLException se) {
        	se.printStackTrace();
        }
    	return rs;
	}
	
	public String[] getColumnNames(String tableName) {
		String[] result = null;
		try {
			ResultSet rs = getTableEntries(tableName);		
			result = new String[rs.getMetaData().getColumnCount()];
			for(int i = 0 ; i < result.length; i++) {
				result[i] = rs.getMetaData().getColumnLabel(i+1);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return result;
	}
	
	public void update(String tableName, String id, String[] columns, String[] values) throws SQLException {
		if (columns.length != values.length) {
			throw new SQLException();
		}
		else {
	        String sql = "UPDATE \"" + tableName + "\" SET ";
	        for (int i = 0; i < columns.length; i++) {
	        	sql += ("\"" + columns[i] + "\" = ");
	        	sql += ("\'" + values[i] + "\' ");
	        }
	        sql += "WHERE \"Id\" = " + id;
	        System.out.println(sql);
	        conn.prepareStatement(sql).execute();
		}
	}
}
