package com.cis5930.database;

import java.sql.*;

public class Database {
   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost/github";
   //  Database credentials
   static final String USER = "root";
   static final String PASS = "root";   
   static Connection conn = null;
//   static PreparedStatement ps=null;
   static ResultSet rs=null;
   
   public static Connection openConnection() {	
	   try{
	      //STEP 2: Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");
	
	      //STEP 3: Open a connection
	      System.out.println("Connecting to a selected database...");
	      conn = DriverManager.getConnection(DB_URL, USER, PASS);
	      System.out.println("Connected database successfully...");      
	   }catch(SQLException se){
	      se.printStackTrace();
	   }catch(Exception e){
	      e.printStackTrace();
	   }  
	   return conn;
   }
  
   public static ResultSet processQuery(PreparedStatement ps) {
	   
//	   System.out.println("Querying DB...");
	   try {
//		ps = conn.prepareStatement("SELECT COUNT(*) AS COU FROM issues WHERE ISS_REPORTER=?",ResultSet.TYPE_SCROLL_INSENSITIVE);
//		ps.setString(1,"Jukka Zitting");
		rs = ps.executeQuery();	
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	   return rs;
   }
   
   public static Integer updateQuery(PreparedStatement ps) {
	   
//	   System.out.println("Updating DB...");
	   Integer retVal=0;
	   try {
		retVal = ps.executeUpdate();	
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	   return retVal;
   }
   
   public static void houseKeeping(PreparedStatement ps) {
	   System.out.println("HouseKeeping in progress...");
	  //finally block used to close resources
	   try{
		   if (ps != null)
	           ps.close();
	   }
	   catch(SQLException se) {
		   se.printStackTrace(); 
       }
			   
      try{
         if(conn!=null)
        	 conn.close();
      }
      catch(SQLException se){
         se.printStackTrace();
      } 
      System.out.println("Goodbye!");
   }
}