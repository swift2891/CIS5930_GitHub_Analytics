package com.cis5930.results;

import com.cis5930.database.Database;
import com.mysql.jdbc.StreamingNotifiable;

import java.sql.*;
import java.util.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.omg.PortableServer.ServantActivator;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

public class ResearchQs {
	private static ResultSet rs;
	static Connection con = null;
	static PreparedStatement ps=null;
	static private Hashtable<Integer,Set<String>> projectWatchers = new Hashtable<Integer,Set<String>>();
	static private Hashtable<Integer,Set<String>> projectContributors = new Hashtable<Integer,Set<String>>();
	static Set<Integer> keys;
	static Set<String> allContrib, allWatch;
	
	
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
	
	public static void totContributors(){
		//Computer Total Contributors
		keys = projectContributors.keySet();
		allContrib = new HashSet<String>(projectContributors.get(1));
		int k=0;
        for(Integer key: keys){
            if(k==0)
            	allContrib = new HashSet<String>(projectContributors.get(key)); 
            else
            	allContrib.addAll(projectContributors.get(key));
            k+=1;
        }
        System.out.println("Total Contributors = "+allContrib.size());	
	}
	
	public static void totWatchers() {
		//Computer Total Watchers
        keys = projectWatchers.keySet();
        allWatch = new HashSet<String>(projectWatchers.get(1));
		int k=0;
        for(Integer key: keys){
//            System.out.println("Value of "+key+" is: "+projectWatchers.get(key));
            if(k==0)
            	allWatch = new HashSet<String>(projectWatchers.get(key)); 
            else
            	allWatch.addAll(projectWatchers.get(key));
            k+=1;
        }
        System.out.println("Total Watchers = "+allWatch.size());
	}
	
	
	//Get each Project's Watchers and Contributors:
	void getAllProjects() {
		int p_id=0,i=0;
		String contribs="";
		List contributorList;
		Set<String> contributorSet,temp;
		//Get Watchers
		try {
			ps = con.prepareStatement("SET SESSION group_concat_max_len = 1000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			ps = con.prepareStatement("select watchers.repo_id,group_concat(watchers.user_id) as user_id\r\n" + 
					"from watchers\r\n" + 
					"group by watchers.repo_id \r\n" + 
					"order by watchers.repo_id asc",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {			
			while(rs.next()){
		      p_id = rs.getInt("repo_id");
		      contribs = rs.getString("user_id");
		      contributorList = Arrays.asList(contribs.split("\\s*,\\s*"));
		      contributorSet = new HashSet<String>(contributorList); 		      
		      projectWatchers.put(p_id, contributorSet);
//		      if(i==0)
//	    	  	System.out.println("ID: "+p_id+"  Val: "+contributorSet);
		      i+=1;
  		    }
			System.out.println("#Projects_Watchers = "+projectWatchers.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Get Contributors
		//Type1 - Commits
		try {	
			ps = con.prepareStatement("SET SESSION group_concat_max_len = 1000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			ps = con.prepareStatement("SELECT project_id,group_concat(distinct(author_id)) as author \r\n" + 
					"FROM github.commits inner join \r\n" + 
					"(\r\n" + 
					"SELECT projects.id FROM projects inner join  \r\n" + 
					"(SELECT distinct(repo_id) FROM watchers) as t4 \r\n" + 
					"on projects.forked_from = t4.repo_id or projects.id = t4.repo_id\r\n" + 
					") as t1\r\n" + 
					"on commits.project_id = t1.id\r\n" + 
					"group by project_id limit 10000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			i=0;
			while(rs.next()){
		      p_id = rs.getInt("project_id");
		      contribs = rs.getString("author");
		      contributorList = Arrays.asList(contribs.split("\\s*,\\s*"));
		      contributorSet = new HashSet<String>(contributorList); 		      
		      projectContributors.put(p_id, contributorSet);
//		      if(i==0)
//		    	  System.out.println("ID: "+p_id+"  Val: "+projectContributors.get(p_id));
		      i+=1;
  		    }
			System.out.println("#Projects_Contributors_1 = "+projectContributors.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		//Type2 - Pull Requests
		try {
			ps = con.prepareStatement("SET SESSION group_concat_max_len = 1000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			ps = con.prepareStatement("SELECT head_repo_id as project_id,group_concat(distinct(user_id)) as author \r\n" + 
					"FROM pull_requests inner join projects_needed\r\n" + 
					"on head_repo_id = projects_needed.id\r\n" + 
					"group by head_repo_id limit 10000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			i=0;
			while(rs.next()){
		      p_id = rs.getInt("project_id");
		      contribs = rs.getString("author");
		      contributorList = Arrays.asList(contribs.split("\\s*,\\s*"));
		      contributorSet = new HashSet<String>(contributorList); 	
		      if(projectContributors.containsKey(p_id))
		      {
		    	  temp = projectContributors.get(p_id);
		    	  temp.addAll(contributorSet);
		    	  projectContributors.put(p_id, temp);
		      }
		      else {
		    	  projectContributors.put(p_id, contributorSet);
		      }
//		      if(i==0)
//		    	  System.out.println("ID: "+p_id+"  Val: "+projectContributors.get(p_id));
		      i+=1;
  		    }
			System.out.println("#Projects_Contributors_2 = "+projectContributors.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Type3 - Issues
		try {
			ps = con.prepareStatement("SET SESSION group_concat_max_len = 1000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			ps = con.prepareStatement("SELECT repo_id as project_id, group_concat(distinct(reporter_id)) as user_id \r\n" + 
					"FROM issues inner join projects_needed\r\n" + 
					"on repo_id = projects_needed.id\r\n" + 
					"where isnull(reporter_id)=0\r\n" + 
					"group by repo_id limit 10000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			i=0;
			while(rs.next()){
		      p_id = rs.getInt("project_id");
		      contribs = rs.getString("user_id");
		      contributorList = Arrays.asList(contribs.split("\\s*,\\s*"));
		      contributorSet = new HashSet<String>(contributorList); 		      
		      if(projectContributors.containsKey(p_id))
		      {
		    	  temp = projectContributors.get(p_id);
		    	  temp.addAll(contributorSet);
		    	  projectContributors.put(p_id, temp);
		      }
		      else {
		    	  projectContributors.put(p_id, contributorSet);
		      }
//		      if(i==0)
//		    	  System.out.println("ID: "+p_id+"  Val: "+projectContributors.get(p_id));
		      i+=1;
  		    }
			System.out.println("#Projects_Contributors_3 = "+projectContributors.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//Type4 - Commit Comments
		try {
			ps = con.prepareStatement("SET SESSION group_concat_max_len = 1000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			ps = con.prepareStatement("SELECT t2.project_id, group_concat(distinct(t2.user_id)) as user_id \r\n" + 
					"FROM  (\r\n" + 
					"select commit_comments.user_id, commits.project_id \r\n" + 
					"from commit_comments inner join commits \r\n" + 
					"on commit_comments.commit_id = commits.id\r\n" + 
					") as t2\r\n" + 
					"inner join projects_needed\r\n" + 
					"on t2.project_id = projects_needed.id\r\n" + 
					"where isnull(t2.user_id)=0\r\n" + 
					"group by t2.project_id limit 10000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			i=0;
			while(rs.next()){
		      p_id = rs.getInt("project_id");
		      contribs = rs.getString("user_id");
		      contributorList = Arrays.asList(contribs.split("\\s*,\\s*"));
		      contributorSet = new HashSet<String>(contributorList);      
		      if(projectContributors.containsKey(p_id))
		      {
		    	  temp = projectContributors.get(p_id);
		    	  temp.addAll(contributorSet);
		    	  projectContributors.put(p_id, temp);
		      }
		      else {
		    	  projectContributors.put(p_id, contributorSet);
		      }
//		      if(i==0)
//		    	  System.out.println("ID: "+p_id+"  Val: "+projectContributors.get(p_id));
		      i+=1;
  		    }
			System.out.println("#Projects_Contributors_4 = "+projectContributors.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Type5 - Issue Comments
		try {
			ps = con.prepareStatement("SET SESSION group_concat_max_len = 1000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			ps = con.prepareStatement("SELECT t2.repo_id as project_id, group_concat(distinct(t2.user_id)) as user_id \r\n" + 
					"FROM  (\r\n" + 
					"select issue_comments.user_id, issues.repo_id\r\n" + 
					"from issue_comments inner join issues \r\n" + 
					"on issue_comments.issue_id = issues.id\r\n" + 
					") as t2\r\n" + 
					"inner join projects_needed\r\n" + 
					"on t2.repo_id = projects_needed.id\r\n" + 
					"where isnull(t2.user_id)=0\r\n" + 
					"group by t2.repo_id limit 10000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			i=0;
			while(rs.next()){
		      p_id = rs.getInt("project_id");
		      contribs = rs.getString("user_id");
		      contributorList = Arrays.asList(contribs.split("\\s*,\\s*"));
		      contributorSet = new HashSet<String>(contributorList); 		      
		      if(projectContributors.containsKey(p_id))
		      {
		    	  temp = projectContributors.get(p_id);
		    	  temp.addAll(contributorSet);
		    	  projectContributors.put(p_id, temp);
		      }
		      else {
		    	  projectContributors.put(p_id, contributorSet);
		      }
//		      if(i==0)
//		    	  System.out.println("ID: "+p_id+"  Val: "+projectContributors.get(p_id));
		      i+=1;
  		    }
			System.out.println("#Projects_Contributors_5 = "+projectContributors.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Type6 - Pull_Request Comments
		try {
			ps = con.prepareStatement("SET SESSION group_concat_max_len = 1000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			ps = con.prepareStatement("SELECT t2.head_repo_id as project_id, group_concat(distinct(t2.user_id)) as user_id \r\n" + 
					"FROM(\r\n" + 
					"select pull_request_comments.user_id, pull_requests.head_repo_id\r\n" + 
					"from pull_request_comments inner join pull_requests \r\n" + 
					"on pull_request_comments.pull_request_id\r\n" + 
					" = pull_requests.id\r\n" + 
					") as t2\r\n" + 
					"inner join projects_needed\r\n" + 
					"on t2.head_repo_id = projects_needed.id\r\n" + 
					"where isnull(t2.user_id)=0\r\n" + 
					"group by t2.head_repo_id limit 10000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			i=0;
			while(rs.next()){
		      p_id = rs.getInt("project_id");
		      contribs = rs.getString("user_id");
		      contributorList = Arrays.asList(contribs.split("\\s*,\\s*"));
		      contributorSet = new HashSet<String>(contributorList); 		      
		      if(projectContributors.containsKey(p_id))
		      {
		    	  temp = projectContributors.get(p_id);
		    	  temp.addAll(contributorSet);
		    	  projectContributors.put(p_id, temp);
		      }
		      else {
		    	  projectContributors.put(p_id, contributorSet);
		      }
//		      if(i==0)
//		    	  System.out.println("ID: "+p_id+"  Val: "+projectContributors.get(p_id));
		      i+=1;
  		    }
			System.out.println("#Projects_Contributors_6 = "+projectContributors.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Type7 - Assigned Issues
		try {
			ps = con.prepareStatement("SET SESSION group_concat_max_len = 1000000",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			ps = con.prepareStatement("SELECT repo_id as project_id, group_concat(distinct(assignee_id)) as user_id \r\n" + 
					"FROM issues inner join projects_needed\r\n" + 
					"on issues.repo_id = projects_needed.id\r\n" + 
					"where isnull(assignee_id)=0\r\n" + 
					"group by repo_id\r\n" + 
					"limit 99999999",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		try {
			i=0;
			while(rs.next()){
		      p_id = rs.getInt("project_id");
		      contribs = rs.getString("user_id");
		      contributorList = Arrays.asList(contribs.split("\\s*,\\s*"));
		      contributorSet = new HashSet<String>(contributorList); 		      
		      if(projectContributors.containsKey(p_id))
		      {
		    	  temp = projectContributors.get(p_id);
		    	  temp.addAll(contributorSet);
		    	  projectContributors.put(p_id, temp);
		      }
		      else {
		    	  projectContributors.put(p_id, contributorSet);
		      }
//		      if(i==0)
//		    	  System.out.println("ID: "+p_id+"  Val: "+projectContributors.get(p_id));
		      i+=1;
  		    }
			System.out.println("#Projects_Contributors_7 = "+projectContributors.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		totContributors();
		totWatchers();
        
        Set<String> intersection = new HashSet<String>(allWatch);
        intersection.retainAll(allContrib);
        System.out.println("#Watchers who  become Contributors = "+intersection.size());
	}
//	

	//Get Project - Author - (least)Date - Contribution Type
	public void getAllContributions()
	{
		Integer qryOutcome=0; 
		
		//SQL Queries
		try {
			//Truncate prj_contr_ts_typ
			ps = con.prepareStatement("truncate prj_contr_ts_typ");	
			qryOutcome = Database.updateQuery(ps);
	
			//Truncate prj_contr_ts_typ2
			ps = con.prepareStatement("truncate prj_contr_ts_typ2");	
			qryOutcome = Database.updateQuery(ps);
			
			//Commits
			ps = con.prepareStatement("insert into prj_contr_ts_typ\r\n" + 
					"\r\n" + 
					"select project_id, author_id, min(created_at), \"commits\" as contr_type from commits\r\n" + 
					"inner join projects_needed\r\n" + 
					"on commits.project_id = projects_needed.id\r\n" + 
					"group by project_id, author_id");	
			qryOutcome = Database.updateQuery(ps);
			System.out.println("#Rows Inserted = "+ qryOutcome);
			
			//Pull Requests
			ps = con.prepareStatement("insert into prj_contr_ts_typ\r\n" + 
					"\r\n" + 
					"select t4.project_id, actor_id, min(created_at), \"pull_requests\" from pull_request_history \r\n" + 
					"inner join \r\n" + 
					"\r\n" + 
					"(select pull_requests.id, projects_needed.id as project_id from pull_requests inner join projects_needed\r\n" + 
					"on pull_requests.base_repo_id = projects_needed.id) as t4\r\n" + 
					"\r\n" + 
					"on pull_request_history.pull_request_id = t4.id \r\n" + 
					"where action=\"opened\"\r\n" + 
					"group by t4.project_id, actor_id");	
			qryOutcome = Database.updateQuery(ps);
			System.out.println("#Rows Inserted = "+ qryOutcome);
			
			//Issues
			ps = con.prepareStatement("insert into prj_contr_ts_typ\r\n" + 
					"\r\n" + 
					"SELECT repo_id, reporter_id, min(created_at), \"issues\" FROM issues inner join projects_needed\r\n" + 
					"on issues.repo_id = projects_needed.id\r\n" + 
					"where isnull(reporter_id)=0\r\n" + 
					"group by repo_id, reporter_id\r\n" + 
					"");	
			qryOutcome = Database.updateQuery(ps);
			System.out.println("#Rows Inserted = "+ qryOutcome);
			
			//commit comments
			ps = con.prepareStatement("insert into prj_contr_ts_typ\r\n" + 
					"\r\n" + 
					"select t4.project_id, user_id, min(created_at), \"commit_comments\" from commit_comments \r\n" + 
					"inner join \r\n" + 
					"\r\n" + 
					"(select commits.id, commits.project_id from commits inner join projects_needed\r\n" + 
					"on commits.project_id = projects_needed.id) as t4\r\n" + 
					"\r\n" + 
					"on commit_comments.commit_id = t4.id \r\n" + 
					"group by t4.project_id, user_id");	
			qryOutcome = Database.updateQuery(ps);
			System.out.println("#Rows Inserted = "+ qryOutcome);
			
			//issue comments
			ps = con.prepareStatement("insert into prj_contr_ts_typ\r\n" + 
					"\r\n" + 
					"select t4.repo_id,user_id, min(created_at), \"issue_comments\" from issue_comments \r\n" + 
					"inner join \r\n" + 
					"\r\n" + 
					"(select issues.id, issues.repo_id from issues inner join projects_needed\r\n" + 
					"on issues.repo_id = projects_needed.id) as t4\r\n" + 
					"\r\n" + 
					"on issue_comments.issue_id = t4.id \r\n" + 
					"group by t4.repo_id,user_id");	
			qryOutcome = Database.updateQuery(ps);
			System.out.println("#Rows Inserted = "+ qryOutcome);
			
			//pull request  comments
			ps = con.prepareStatement("insert into prj_contr_ts_typ\r\n" + 
					"\r\n" + 
					"select t4.base_repo_id,user_id, min(created_at),\"pull_request_comments\" from pull_request_comments \r\n" + 
					"inner join \r\n" + 
					"\r\n" + 
					"(select pull_requests.id, pull_requests.base_repo_id from pull_requests inner join projects_needed\r\n" + 
					"on pull_requests.base_repo_id = projects_needed.id) as t4\r\n" + 
					"\r\n" + 
					"on pull_request_comments.pull_request_id = t4.id \r\n" + 
					"group by t4.base_repo_id,user_id");	
			qryOutcome = Database.updateQuery(ps);
			System.out.println("#Rows Inserted = "+ qryOutcome);
			
			//assignee
			ps = con.prepareStatement("insert into prj_contr_ts_typ\r\n" + 
					"\r\n" + 
					"SELECT issues.repo_id, assignee_id, min(created_at), \"assignee\" FROM issues inner join projects_needed\r\n" + 
					"on issues.repo_id = projects_needed.id\r\n" + 
					"where isnull(assignee_id)=0\r\n" + 
					"group by issues.repo_id, assignee_id");	
			qryOutcome = Database.updateQuery(ps);
			System.out.println("#Rows Inserted = "+ qryOutcome);
			
			//prj_contr_ts_typ2
			ps = con.prepareStatement("insert into prj_contr_ts_typ2\r\n" + 
					"\r\n" + 
					"select prj_contr_ts_typ.project_id, prj_contr_ts_typ.author_id, prj_contr_ts_typ.created_at, prj_contr_ts_typ.contr_type, \r\n" + 
					"projects.forked_from from prj_contr_ts_typ\r\n" + 
					"inner join projects\r\n" + 
					"on prj_contr_ts_typ.project_id = projects.id;");	
			qryOutcome = Database.updateQuery(ps);
			System.out.println("Final #Rows Inserted = "+ qryOutcome);
			
			
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		
	}
		
// RQ1 Stats	
	void rq1Stats() {
		int contr_count=0, watch_contr=0, watchers_count=0;
		try {
			//Get Contributors Count
			ps = con.prepareStatement("SELECT count(distinct(author_id)) as contr_count FROM github.prj_contr_ts_typ2;",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			contr_count = rs.getInt("contr_count");
		    System.out.println("\n"+"Total #Contributors = "+contr_count);
		    
		    //Watchers turned Contributors
		    ps = con.prepareStatement("select count(*) as watch_contr from watchers inner join\r\n" + 
		    		"(\r\n" + 
		    		"SELECT project_id, author_id, min(created_at) as created_at FROM github.prj_contr_ts_typ2\r\n" + 
		    		"group by project_id, author_id\r\n" + 
		    		") as t4\r\n" + 
		    		"on watchers.user_id = t4.author_id and\r\n" + 
		    		"watchers.created_at < t4.created_at and \r\n" + 
		    		"watchers.repo_id = t4.project_id",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			watch_contr = rs.getInt("watch_contr");
		    System.out.println("\n"+"Total No. Of Watchers turned to Contributors = "+watch_contr);
			
		    //Get Watchers Count
			ps = con.prepareStatement("SELECT count(distinct(user_id)) as watch_count FROM watchers",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			watchers_count = rs.getInt("watch_count");
		    System.out.println("\n"+"Total #Contributors = "+watchers_count);
		    
		    //Get percentage
		    System.out.println("% of Watchers turned to Contributors = "+ (float)watch_contr*100/(float)watchers_count+"%");
		    System.out.println("% of Contributors Who are Watchers = "+ (float)watch_contr*100/(float)contr_count+"%");
		    
		} catch (SQLException e1) {
			e1.printStackTrace();
		}		
		
	}
	
	public double[] watcherConfidence() {
		double[] watcherConfArray = new double[6];
		int commit_conf=0, issue_conf=0, pull_conf=0, comm_commit_conf=0, comm_issue_conf=0, comm_pull_conf=0;
		try {
			ps = con.prepareStatement("select count(*) as commit_conf from watchers inner join\r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, min(created_at) as created_at, contr_type  FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"on watchers.user_id = t4.author_id and\r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"watchers.repo_id = t4.project_id  and\r\n" + 
					"t4.contr_type=\"commits\";",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			commit_conf = rs.getInt("commit_conf");
			
			ps = con.prepareStatement("select count(*) as issue_conf from watchers inner join\r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, min(created_at) as created_at, contr_type  FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"on watchers.user_id = t4.author_id and\r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"watchers.repo_id = t4.project_id  and\r\n" + 
					"t4.contr_type=\"issues\";",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			issue_conf = rs.getInt("issue_conf");
			
			
			ps = con.prepareStatement("select count(*) as pull_conf from watchers inner join\r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, min(created_at) as created_at, contr_type  FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"on watchers.user_id = t4.author_id and\r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"watchers.repo_id = t4.project_id  and\r\n" + 
					"t4.contr_type=\"pull_requests\";",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			pull_conf = rs.getInt("pull_conf");
			
			ps = con.prepareStatement("select count(*) as comm_commit_conf from watchers inner join\r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, min(created_at) as created_at, contr_type  FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"on watchers.user_id = t4.author_id and\r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"watchers.repo_id = t4.project_id  and\r\n" + 
					"t4.contr_type=\"commit_comments\";",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			comm_commit_conf = rs.getInt("comm_commit_conf");
			
			ps = con.prepareStatement("select count(*) as comm_issue_conf from watchers inner join\r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, min(created_at) as created_at, contr_type  FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"on watchers.user_id = t4.author_id and\r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"watchers.repo_id = t4.project_id  and\r\n" + 
					"t4.contr_type=\"issue_comments\";",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			comm_issue_conf = rs.getInt("comm_issue_conf");
			
			ps = con.prepareStatement("select count(*) as comm_pull_conf from watchers inner join\r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, min(created_at) as created_at, contr_type  FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"on watchers.user_id = t4.author_id and\r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"watchers.repo_id = t4.project_id  and\r\n" + 
					"t4.contr_type=\"pull_request_comments\";",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			comm_pull_conf = rs.getInt("comm_pull_conf");
			
//			System.out.println("\n"+"Confidences:"+"\n"+commit_conf);
//			System.out.println(issue_conf);
//			System.out.println(pull_conf);
//			System.out.println(comm_commit_conf);
//			System.out.println(comm_issue_conf);
//			System.out.println(comm_pull_conf);
			
			System.out.println("Watcher Confidence = "+(float)(commit_conf+issue_conf+pull_conf)*100/17244+"%");
			System.out.println("Watchers: Commit Confidence = "+(float)(commit_conf)*100/17244+"%");
			System.out.println("Watchers: Issues Confidence = "+(float)(issue_conf)*100/17244+"%");
			System.out.println("Watchers: Pull_Requests Confidence = "+(float)(pull_conf)*100/17244+"%");
			System.out.println("Watchers: Commit Comments Confidence = "+(float)(comm_commit_conf)*100/17244+"%");
			System.out.println("Watchers: Issue Comments Confidence = "+(float)(comm_issue_conf)*100/17244+"%");
			System.out.println("Watchers: Pull_Request Comments Confidence = "+(float)(comm_pull_conf)*100/17244+"%");
			
			watcherConfArray[0] = commit_conf;
			watcherConfArray[1] = issue_conf;
			watcherConfArray[2] = pull_conf;
			watcherConfArray[3] = comm_commit_conf;
			watcherConfArray[4] = comm_issue_conf;
			watcherConfArray[5] = comm_pull_conf;
			
			return watcherConfArray;
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		return new double[0];
	}
	
	
	public long[] otherContributorConf() {
		int commit_conf=0, issue_conf=0, pull_conf=0, comm_commit_conf=0, comm_issue_conf=0, comm_pull_conf=0;
		long[] othersConfArray = new long[6];
		try {
			ps = con.prepareStatement("select count(*) as commit_conf from \r\n" + 
					"(\r\n" + 
					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
					"(\r\n" + 
					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
					"union all\r\n" + 
					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"group by project_id, author_id\r\n" + 
					"having count(*)=1\r\n" + 
					") as t6 \r\n" + 
					"where t6.contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			commit_conf = rs.getInt("commit_conf");
			
			//***************
			ps = con.prepareStatement("select count(*) as issue_conf from \r\n" + 
					"(\r\n" + 
					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
					"(\r\n" + 
					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
					"union all\r\n" + 
					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"group by project_id, author_id\r\n" + 
					"having count(*)=1\r\n" + 
					") as t6 \r\n" + 
					"where t6.contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			issue_conf = rs.getInt("issue_conf");
			
			
			ps = con.prepareStatement("select count(*) as pull_conf from \r\n" + 
					"(\r\n" + 
					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
					"(\r\n" + 
					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
					"union all\r\n" + 
					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"group by project_id, author_id\r\n" + 
					"having count(*)=1\r\n" + 
					") as t6 \r\n" + 
					"where t6.contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			pull_conf = rs.getInt("pull_conf");
			
			ps = con.prepareStatement("select count(*) as comm_commit_conf from \r\n" + 
					"(\r\n" + 
					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
					"(\r\n" + 
					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
					"union all\r\n" + 
					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"group by project_id, author_id\r\n" + 
					"having count(*)=1\r\n" + 
					") as t6 \r\n" + 
					"where t6.contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			comm_commit_conf = rs.getInt("comm_commit_conf");
			
			ps = con.prepareStatement("select count(*) as comm_issue_conf from \r\n" + 
					"(\r\n" + 
					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
					"(\r\n" + 
					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
					"union all\r\n" + 
					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"group by project_id, author_id\r\n" + 
					"having count(*)=1\r\n" + 
					") as t6 \r\n" + 
					"where t6.contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			comm_issue_conf = rs.getInt("comm_issue_conf");
			
			ps = con.prepareStatement("select count(*) as comm_pull_conf from \r\n" + 
					"(\r\n" + 
					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
					"(\r\n" + 
					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
					"union all\r\n" + 
					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id\r\n" + 
					") as t4\r\n" + 
					"group by project_id, author_id\r\n" + 
					"having count(*)=1\r\n" + 
					") as t6 \r\n" + 
					"where t6.contr_type = \"pull_request_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			comm_pull_conf = rs.getInt("comm_pull_conf");
			
//			System.out.println("\n"+"Other Contr Confidences:"+"\n"+commit_conf);
//			System.out.println(issue_conf);
//			System.out.println(pull_conf);
//			System.out.println(comm_commit_conf);
//			System.out.println(comm_issue_conf);
//			System.out.println(comm_pull_conf);
			
			System.out.println("\n"+"Other Contributor Confidence = "+(float)(commit_conf+issue_conf+pull_conf)*100/62885+"%");
			System.out.println("Others: Commit Confidence = "+(float)(commit_conf)*100/62885+"%");
			System.out.println("Others: Issues Confidence = "+(float)(issue_conf)*100/62885+"%");
			System.out.println("Others: Pull_Requests Confidence = "+(float)(pull_conf)*100/62885+"%");
			System.out.println("Others: Commit Comments Confidence = "+(float)(comm_commit_conf)*100/62885+"%");
			System.out.println("Others: Issue Comments Confidence = "+(float)(comm_issue_conf)*100/62885+"%");
			System.out.println("Others: Pull_Request Comments Confidence = "+(float)(comm_pull_conf)*100/62885+"%"+"\n");
			
			othersConfArray[0] = commit_conf;
			othersConfArray[1] = issue_conf;
			othersConfArray[2] = pull_conf;
			othersConfArray[3] = comm_commit_conf;
			othersConfArray[4] = comm_issue_conf;
			othersConfArray[5] = comm_pull_conf;
			
			return othersConfArray;
			
		}catch(SQLException e1) {
			e1.printStackTrace();
		}
		return new long[0];
	}
	
	
	public void confidenceCalc() {
		
		double[] series1; 
		long[] series2;
		double[] res;
		
 		series1=this.watcherConfidence();
		series2=this.otherContributorConf();
//		ChiSquareTest chi = new ChiSquareTest();

	}
//	
//	public void languageContribution(){
//		try {
//			int c_count=0, c_sharp_count=0, cpp_count=0, css_count=0, go_count=0, java_count=0, javascript_count=0, php_count=0, r_count=0, python_count=0, 
//					scala_count=0, ruby_count=0, typescript_count=0; 
//			
//			//Get All Counts
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"C\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			c_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"C#\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			c_sharp_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"C++\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			cpp_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"CSS\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			css_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"Go\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			go_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"Java\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			java_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"JavaScript\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			javascript_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"PHP\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			php_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"Python\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			python_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"R\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			r_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"Ruby\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			ruby_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"Scala\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			scala_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("select count(*) as row_count from prj_contr_ts_typ_lan\r\n" + 
//					"where lang = \"TypeScript\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			typescript_count = rs.getInt("row_count");
//			
//			int issue_count=0, commit_count=0, pull_request_count=0, comm_commit_count=0, comm_issue_count=0;
//			//Get 5 Types RI / SC / PR / CC / CI
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("C: "+(float)issue_count*100/c_count+" "+(float)commit_count*100/c_count+" "+
//			(float)pull_request_count*100/c_count+" "+(float)comm_commit_count*100/c_count+" "+(float)comm_issue_count*100/c_count );
//			
//			//C# -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C#\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C#\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C#\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C#\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C#\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("C#: "+(float)issue_count*100/c_sharp_count+" "+(float)commit_count*100/c_sharp_count+" "+
//			(float)pull_request_count*100/c_sharp_count+" "+(float)comm_commit_count*100/c_sharp_count+" "+(float)comm_issue_count*100/c_sharp_count );
//			
//			
//			//C++ -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C++\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C++\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C++\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C++\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"C++\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("C++: "+(float)issue_count*100/cpp_count+" "+(float)commit_count*100/cpp_count+" "+
//			(float)pull_request_count*100/cpp_count+" "+(float)comm_commit_count*100/cpp_count+" "+(float)comm_issue_count*100/cpp_count );
//			
//			
//			//CSS -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"CSS\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"CSS\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"CSS\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"CSS\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"CSS\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("CSS: "+(float)issue_count*100/css_count+" "+(float)commit_count*100/css_count+" "+
//			(float)pull_request_count*100/css_count+" "+(float)comm_commit_count*100/css_count+" "+(float)comm_issue_count*100/css_count );
//	
//			
//			//Go -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Go\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Go\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Go\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Go\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Go\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("Go: "+(float)issue_count*100/go_count+" "+(float)commit_count*100/go_count+" "+
//			(float)pull_request_count*100/go_count+" "+(float)comm_commit_count*100/go_count+" "+(float)comm_issue_count*100/go_count );
//			
//			//Java -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Java\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Java\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Java\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Java\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Java\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("Java: "+(float)issue_count*100/java_count+" "+(float)commit_count*100/java_count+" "+
//			(float)pull_request_count*100/java_count+" "+(float)comm_commit_count*100/java_count+" "+(float)comm_issue_count*100/java_count );
//			
//			
//			//JavaScript -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"JavaScript\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"JavaScript\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"JavaScript\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"JavaScript\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"JavaScript\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("JavaScript: "+(float)issue_count*100/javascript_count+" "+(float)commit_count*100/javascript_count+" "+
//			(float)pull_request_count*100/javascript_count+" "+(float)comm_commit_count*100/javascript_count+" "+(float)comm_issue_count*100/javascript_count );
//			
//			
//			//PHP -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"PHP\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"PHP\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"PHP\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"PHP\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"PHP\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("PHP: "+(float)issue_count*100/php_count+" "+(float)commit_count*100/php_count+" "+
//			(float)pull_request_count*100/php_count+" "+(float)comm_commit_count*100/php_count+" "+(float)comm_issue_count*100/php_count );
//
//			
//			//Python -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Python\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Python\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Python\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Python\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Python\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("Python: "+(float)issue_count*100/python_count+" "+(float)commit_count*100/python_count+" "+
//			(float)pull_request_count*100/python_count+" "+(float)comm_commit_count*100/python_count+" "+(float)comm_issue_count*100/python_count);
//			
//			
//			//R -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"R\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"R\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"R\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"R\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"R\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("R: "+(float)issue_count*100/r_count+" "+(float)commit_count*100/r_count+" "+
//			(float)pull_request_count*100/r_count+" "+(float)comm_commit_count*100/r_count+" "+(float)comm_issue_count*100/r_count);
//			
//			
//			//Ruby -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Ruby\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Ruby\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Ruby\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Ruby\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Ruby\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("Ruby: "+(float)issue_count*100/ruby_count+" "+(float)commit_count*100/ruby_count+" "+
//			(float)pull_request_count*100/ruby_count+" "+(float)comm_commit_count*100/ruby_count+" "+(float)comm_issue_count*100/ruby_count);
//			
//			
//			//Scala -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Scala\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Scala\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Scala\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Scala\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"Scala\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("Scala: "+(float)issue_count*100/scala_count+" "+(float)commit_count*100/scala_count+" "+
//			(float)pull_request_count*100/scala_count+" "+(float)comm_commit_count*100/scala_count+" "+(float)comm_issue_count*100/scala_count);
//			
//			
//			//TypeScript -----------------------
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"TypeScript\" and \r\n" + 
//					"contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"TypeScript\" and \r\n" + 
//					"contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"TypeScript\" and \r\n" + 
//					"contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_request_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"TypeScript\" and \r\n" + 
//					"contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_count = rs.getInt("row_count");
//			
//			ps = con.prepareStatement("SELECT count(*) as row_count FROM prj_contr_ts_typ_lan \r\n" + 
//					"where lang = \"TypeScript\" and \r\n" + 
//					"contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_count = rs.getInt("row_count");
//				
//			System.out.println("TypeScript: "+(float)issue_count*100/typescript_count+" "+(float)commit_count*100/typescript_count+" "+
//			(float)pull_request_count*100/typescript_count+" "+(float)comm_commit_count*100/typescript_count+" "+(float)comm_issue_count*100/typescript_count+"\n");
//			
//		}catch(SQLException e1) {
//			e1.printStackTrace();
//		}
//		
//	}
//	
	
	public void languageMTBF() {
		try {
			
			int avg_time=0; 
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"C\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("C: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"C#\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("C#: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"C++\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("C++: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"CSS\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("CSS: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"Go\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("Go: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"Java\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("Java: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"JavaScript\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("JavaScript: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"PHP\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("PHP: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"Python\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("Python: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"R\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("R: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"Ruby\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("Ruby: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"Scala\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("Scala: "+avg_time);
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_time from\r\n" + 
					"(\r\n" + 
					"SELECT t4.author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type \r\n" + 
					"FROM \r\n" + 
					"(\r\n" + 
					"SELECT project_id, author_id, created_at, contr_type FROM github.prj_contr_ts_typ_lan\r\n" + 
					"where lang = \"TypeScript\"\r\n" + 
					") as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			avg_time = rs.getInt("avg_time");
			System.out.println("TypeScript: "+avg_time);
			
			
		}catch(SQLException e1) {
			e1.printStackTrace();
		}
		
		
	}
	
	public void contributorsMTBF() {
		try {
			
			float tot_mtbf=0;
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_mtbf from(\r\n" + 
					"SELECT author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type FROM \r\n" + 
					"(SELECT project_id, author_id, min(created_at) as created_at, contr_type FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id) as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at \r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			tot_mtbf = rs.getFloat("avg_mtbf");
			System.out.println("Author- Mean Time Before First Contribution (All Types) = "+tot_mtbf+" weeks");
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_mtbf from(\r\n" + 
					"SELECT author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type FROM \r\n" + 
					"(SELECT project_id, author_id, min(created_at) as created_at, contr_type FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id) as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"t4.contr_type = \"commits\"\r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			tot_mtbf = rs.getFloat("avg_mtbf");
			System.out.println("Author- MTBF (commits) = "+tot_mtbf+" weeks");
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_mtbf from(\r\n" + 
					"SELECT author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type FROM \r\n" + 
					"(SELECT project_id, author_id, min(created_at) as created_at, contr_type FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id) as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"t4.contr_type = \"issues\"\r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			tot_mtbf = rs.getFloat("avg_mtbf");
			System.out.println("Author- MTBF (issues) = "+tot_mtbf+" weeks");
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_mtbf from(\r\n" + 
					"SELECT author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type FROM \r\n" + 
					"(SELECT project_id, author_id, min(created_at) as created_at, contr_type FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id) as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"t4.contr_type = \"pull_requests\"\r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			tot_mtbf = rs.getFloat("avg_mtbf");
			System.out.println("Author- MTBF (pull_requests) = "+tot_mtbf+" weeks");
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_mtbf from(\r\n" + 
					"SELECT author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type FROM \r\n" + 
					"(SELECT project_id, author_id, min(created_at) as created_at, contr_type FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id) as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"t4.contr_type = \"commit_comments\"\r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			tot_mtbf = rs.getFloat("avg_mtbf");
			System.out.println("Author- MTBF (commit_comments) = "+tot_mtbf+" weeks");
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_mtbf from(\r\n" + 
					"SELECT author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type FROM \r\n" + 
					"(SELECT project_id, author_id, min(created_at) as created_at, contr_type FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id) as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"t4.contr_type = \"issue_comments\"\r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			tot_mtbf = rs.getFloat("avg_mtbf");
			System.out.println("Author- MTBF (issue_comments) = "+tot_mtbf+" weeks");
			
			ps = con.prepareStatement("select avg(time_diff)/86400 as avg_mtbf from(\r\n" + 
					"SELECT author_id, time_to_sec(timediff(t4.created_at,watchers.created_at)) as time_diff, t4.contr_type FROM \r\n" + 
					"(SELECT project_id, author_id, min(created_at) as created_at, contr_type FROM github.prj_contr_ts_typ2\r\n" + 
					"group by project_id, author_id) as t4\r\n" + 
					"inner join watchers\r\n" + 
					"on watchers.repo_id = t4.project_id and \r\n" + 
					"watchers.user_id = t4.author_id and \r\n" + 
					"watchers.created_at < t4.created_at and \r\n" + 
					"t4.contr_type = \"pull_request_comments\"\r\n" + 
					") as t7",ResultSet.TYPE_SCROLL_INSENSITIVE);
			rs = Database.processQuery(ps);
			rs.next();
			tot_mtbf = rs.getFloat("avg_mtbf");
			System.out.println("Author- MTBF (pull_request_comments) = "+tot_mtbf+" weeks");
			
			
			
		}catch(SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		con=Database.openConnection();
		ResearchQs rq = new ResearchQs();
		
//  Task 4 - Pearson Correlation
//		rq1.pearsonCorrelation();

//  RQ1 tasks
		
//  Old Method (without considering TimeStamp of Contributors)
//		rq1.getAllProjects();
		
/* Construct a Table with Project/Author/TS/Contr_Type - prj_contr_ts_typ2, such that
   It will have the least time stamp for an author for  a specific project.
   If the author contributed to multiple projects, the least timestamp of author contribution 
   timestamp will be recorded for each project. 
*/
//		rq.getAllContributions();
		
// Get RQ1 Stats
//		rq.rq1Stats();
		rq.contributorsMTBF();				
		
//  RQ2 Tasks
//		rq.confidenceCalc();
		
		
//  RQ3 Tasks
//		rq.languageContribution();
//		rq.languageMTBF();
		
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
