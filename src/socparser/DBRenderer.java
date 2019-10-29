package socparser;

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
	
	DBRenderer(String dbUrl, String dbName) {
		connectToServer(dbUrl);	
		createDatabase("SOC");
		connectToServer(dbUrl + "SOC");
	}
	
	public void connectToServer(String url) {
		System.out.println("Connecting to server...");
		try {
		    conn = DriverManager.getConnection(url, "root", "8675309aA^");
		}catch (SQLException se) {
			se.printStackTrace();
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
					String sql = "CREATE TABLE " + tableName + "(";
					for (String s : columns) {
		        		sql += ("`" + s + "`");
		        		sql += " varchar(255),";
		        	}
		        	sql = sql.substring(0, sql.length() - 1); // chop trailing comma
					sql += ")";
		        	sql = sql.replace("\\", ""); // filter escape characters
		        	System.out.println(sql);
							/*+ "(Course varchar(255),"
				      		+ "GordonRule varchar(255),"
				      		+ "GenEd varchar(255),"
				      		+ "Section varchar(255),"
				      		+ "ClassNum varchar(255),"
				      		+ "MinMaxCred varchar(255),"
				      		+ "Days varchar(255),"
				      		+ "Time varchar(255),"
				      		+ "MeetingPattern varchar(255),"
				      		+ "Spec varchar(255),"
				      		+ "SOC varchar(255),"
				      		+ "CourseTitle varchar(255),"
				      		+ "Instructor varchar(255),"
				      		+ "EnrollmentCap varchar(255),"
				      		+ "ScheduleCodes varchar(255))";*/
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
	    try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
	        while (rs.next()) { 
	            String tName = rs.getString("TABLE_NAME");
	            if (tName != null && tName.equals(tableName.toLowerCase())) {
	        		System.out.println("Table " + tableName + " already exists.");
	                return true;
	            }
        		System.out.println("Table " + tableName + " does not yet exist.");
	        }
	    } catch (SQLException se) {
			se.printStackTrace();
		}
	    return false;
	}
	
	public void insertRowInDB(String tableName, Class rowItem) {
		Set<Map.Entry<String, String>> rowData = rowItem.getData();
        PreparedStatement ps = null;
        try {
	        // String sql = "Insert into SAMPLE(Course,GordonRule,GenEd,Section,ClassNum,MinMaxCred,Days,Time,MeetingPattern,Spec,SOC,CourseTitle,Instructor,EnrollmentCap,ScheduleCodes) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        	String sql = "Insert into " + tableName + "(";
        	// The keys are our column labels
        	for (Entry<String, String> entry : rowData) {
        		sql += ("`" + entry.getKey() + "`");
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
	        /*ps.setString(1, rowItem.course);
	        ps.setString(2, rowItem.gordonRule);
	        ps.setString(3, rowItem.genEd);
	        ps.setString(4, rowItem.section);
	        ps.setString(5, rowItem.classNum);
	        ps.setString(6, rowItem.minMaxCred);
	        ps.setString(7, rowItem.days);
	        ps.setString(8, rowItem.time);
	        ps.setString(9, rowItem.meetingPattern);
	        ps.setString(10, rowItem.spec);
	        ps.setString(11, rowItem.soc);
	        ps.setString(12, rowItem.courseTitle);
	        ps.setString(13, rowItem.instructor);
	        ps.setString(14, rowItem.enrCap);
	        ps.setString(15, rowItem.schedCodes);*/
	        ps.executeUpdate();
	        System.out.println("Values Inserted Successfully");
        } catch (SQLException se) {
        	se.printStackTrace();
        }
    }
	
	public ResultSet getTableEntries(String tableName) {
		String sql = "SELECT * from " + tableName;
		ResultSet rs = null;
		try {
			rs = conn.createStatement().executeQuery(sql);
		} catch (SQLException se) {
        	se.printStackTrace();
        }
    	return rs;
	}
	
	
	// TODO: This needs to be stored persistently!
	public void registerParsed(String tableName) {
		parseList.add(tableName.toLowerCase());
	}
	
	public boolean beenParsed(String tableName) {
		for (String name : parseList) {
			if (name.contentEquals(tableName.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
