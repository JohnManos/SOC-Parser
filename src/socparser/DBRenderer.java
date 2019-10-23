package socparser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBRenderer {
	
	private final String url = "jdbc:mysql://localhost:3306/COURSES";
	private Connection conn = null;
	
	public boolean databaseExists(String dbName) {
		System.out.println("Checking if database COURSES exists...");
		try (ResultSet resultSet = conn.getMetaData().getCatalogs()) {	
			//iterate each catalog in the ResultSet
			while (resultSet.next()) {
			  // Get the database name, which is at position 1
				if (dbName.toLowerCase().equals(resultSet.getString(1))) {
					System.out.println("Database COURSES already exists.");
					return true;
				}
			}
			resultSet.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return false;
	}
	
	public boolean tableExists(String tableName) {
		System.out.println("Checking if table SAMPLE exists...");
	    try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
	        while (rs.next()) { 
	            String tName = rs.getString("TABLE_NAME");
	            if (tName != null && tName.equals(tableName.toLowerCase())) {
	        		System.out.println("Table SAMPLE already exists.");
	                return true;
	            }
	        }
	    } catch (SQLException se) {
			se.printStackTrace();
		}
	    return false;
	}
	
	public void connectToServer(String url) {
		System.out.println("Connecting to server...");
		try {
		    conn = DriverManager.getConnection(url, "root", "8675309aA^");
		}catch (SQLException se) {
			se.printStackTrace();
		}
	}
	
	public void createDatabase() {
		Statement stmt = null;
		connectToServer(url);
		try {  
			if (!databaseExists("COURSES")) {
				System.out.println("Creating database...");
				stmt = conn.createStatement();	      
				String sql = "CREATE DATABASE COURSES";
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
	
	public void createTable() {
		Statement stmt = null;
		try {	
			if (!tableExists("SAMPLE")) {
				stmt = conn.createStatement();		      
				String sql = "CREATE TABLE SAMPLE(Course varchar(255),"
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
			      		+ "ScheduleCodes varchar(255))";
				stmt.executeUpdate(sql);
				System.out.println("Table created successfully...");
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
	
	public void insertRowInDB(Class rowItem) {
        PreparedStatement ps = null;
        try {
	        String sql = "Insert into SAMPLE(Course,GordonRule,GenEd,Section,ClassNum,MinMaxCred,Days,Time,MeetingPattern,Spec,SOC,CourseTitle,Instructor,EnrollmentCap,ScheduleCodes) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	        ps = conn.prepareStatement(sql);
	        ps.setString(1, rowItem.course);
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
	        ps.setString(15, rowItem.schedCodes);
	        ps.executeUpdate();
	        System.out.println("Values Inserted Successfully");
        } catch (SQLException se) {
        	se.printStackTrace();
        }
    }
}
