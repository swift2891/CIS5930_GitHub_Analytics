package com.cis5930.results;

import com.cis5930.database.Database;
import java.sql.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import org.omg.PortableServer.ServantActivator;

public class ResearchQs {
	private static ResultSet rs;
	static Connection con = null;
	static PreparedStatement ps=null;
	
	//Pearson Corr [watchers Vs Forks/Issues/Commits]
	void pearsonCorrelation() {
		double[] watchers = null, forks = null, issues = null, commits = null;
		//Get Watchers Array
		try {
			ps = con.prepareStatement("select count(repo_id) as watchers_count\r\n" + 
					"from (SELECT id FROM github.projects where ISNULL(forked_from) = 1) as t1 inner join watchers on watchers.repo_id=t1.id \r\n" + 
					"group by watchers.repo_id",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			int i=0;int siz =0;
			if(rs.last()) {
				siz = rs.getRow();
//				System.out.println("Size: "+siz);
			}
			watchers = new double[siz];
			rs.beforeFirst();
			while(rs.next()){
		      watchers[i] = rs.getInt("watchers_count");
//		      System.out.println(watchers[i]);
		      i+=1;
  		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Get Forks Array
		try {
			ps = con.prepareStatement("SELECT t1.id, count(*) as forks_count from \r\n" + 
					"(\r\n" + 
					"select t2.id as id,count(repo_id) as watchers_count\r\n" + 
					"from (SELECT id FROM github.projects where ISNULL(forked_from) = 1) as t2 inner join watchers on watchers.repo_id=t2.id \r\n" + 
					"group by watchers.repo_id\r\n" + 
					") as t1 inner join projects on t1.id = projects.forked_from\r\n" + 
					"group by projects.forked_from",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			int i=0;int siz =0;
			if(rs.last()) {
				siz = rs.getRow();
//				System.out.println("Size: "+siz);
			}
			forks = new double[siz];
			rs.beforeFirst();
			while(rs.next()){
		      forks[i] = rs.getInt("forks_count");
//		      System.out.println(forks[i]);
		      i+=1;
  		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Get Issues Array
		try {
			ps = con.prepareStatement("SELECT count(*) as issues_count from \r\n" + 
					"(\r\n" + 
					"select t2.id as id,count(repo_id) as watchers_count\r\n" + 
					"from (SELECT id FROM github.projects where ISNULL(forked_from) = 1) as t2 inner join watchers on watchers.repo_id=t2.id \r\n" + 
					"group by watchers.repo_id\r\n" + 
					") as t1 inner join issues on t1.id = issues.repo_id\r\n" + 
					"group by issues.repo_id",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			int i=0;int siz =0;
			if(rs.last()) {
				siz = rs.getRow();
//				System.out.println("Size: "+siz);
			}
			issues = new double[siz];
			rs.beforeFirst();
			while(rs.next()){
		      issues[i] = rs.getInt("issues_count");
//		      System.out.println(issues[i]);
		      i+=1;
  		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Get Commits Array
		try {
			ps = con.prepareStatement("SELECT count(*) as commits_count from \r\n" + 
					"(\r\n" + 
					"select t2.id as id,count(repo_id) as watchers_count\r\n" + 
					"from (SELECT id FROM github.projects where ISNULL(forked_from) = 1) as t2 inner join watchers on watchers.repo_id=t2.id \r\n" + 
					"group by watchers.repo_id\r\n" + 
					") as t1 inner join commits on t1.id = commits.project_id\r\n" + 
					"group by commits.project_id",ResultSet.TYPE_SCROLL_INSENSITIVE);
			
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			int i=0;int siz =0;
			if(rs.last()) {
				siz = rs.getRow();
//				System.out.println("Size: "+siz);
			}
			commits = new double[siz];
			rs.beforeFirst();
			while(rs.next()){
		      commits[i] = rs.getInt("commits_count");
//		      System.out.println(commits[i]);
		      i+=1;
  		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Compute Pearson Corr:
		
		PearsonsCorrelation p = new PearsonsCorrelation();
		double res = p.correlation(watchers,forks);
		System.out.print("\n"+"Pearson Correlation: "+res);
		res = p.correlation(watchers,issues);
		System.out.print("\n"+"Pearson Correlation: "+res);
		res = p.correlation(watchers,commits);
		System.out.print("\n"+"Pearson Correlation: "+res);
	}
	
//	
	void watchToContr() {
		
	}
	
//	
	void watchPop() {
		
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		con=Database.openConnection();
		ResearchQs rq1 = new ResearchQs();
		
		//Task 4 - Pearson Correlation
		rq1.pearsonCorrelation();

		//RQ1 tasks

		//Ratio-Mean-Median of Watchers becoming Contributors:
		rq1.watchToContr();
		
		//Ratio-Mean-Median of Contributors who are Watchers:
		rq1.watchPop();
		
		//Harish - RQ1 c) Time duration between watcher to become contributor
		
		
		//RQ2 Tasks
			
		
		//RQ3 Tasks
		
		
		
		System.out.println("\n"+"----------End of Summary----------:");
	    try {
	    	if (rs != null)
	    		rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Database.houseKeeping(ps);
	}

}
