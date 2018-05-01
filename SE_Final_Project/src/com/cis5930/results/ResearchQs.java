package com.cis5930.results;

import com.cis5930.database.Database;
import java.sql.*;
import java.util.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.omg.PortableServer.ServantActivator;

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
	
	public void watcherConfidence() {
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
			
			System.out.println("\n"+"Confidences:"+"\n"+commit_conf);
			System.out.println(issue_conf);
			System.out.println(pull_conf);
			System.out.println(comm_commit_conf);
			System.out.println(comm_issue_conf);
			System.out.println(comm_pull_conf);
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	
//	public void otherContributorConf() {
//		int commit_conf=0, issue_conf=0, pull_conf=0, comm_commit_conf=0, comm_issue_conf=0, comm_pull_conf=0;
//		try {
//			ps = con.prepareStatement("select count(*) as commit_conf from \r\n" + 
//					"(\r\n" + 
//					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
//					"(\r\n" + 
//					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
//					"union all\r\n" + 
//					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					") as t4\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					"having count(*)=1\r\n" + 
//					") as t6 \r\n" + 
//					"where t6.contr_type = \"commits\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			commit_conf = rs.getInt("commit_conf");
//			
//			//***************
//			ps = con.prepareStatement("select count(*) as issue_conf from \r\n" + 
//					"(\r\n" + 
//					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
//					"(\r\n" + 
//					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
//					"union all\r\n" + 
//					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					") as t4\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					"having count(*)=1\r\n" + 
//					") as t6 \r\n" + 
//					"where t6.contr_type = \"issues\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			issue_conf = rs.getInt("issue_conf");
//			
//			
//			ps = con.prepareStatement("select count(*) as pull_conf from \r\n" + 
//					"(\r\n" + 
//					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
//					"(\r\n" + 
//					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
//					"union all\r\n" + 
//					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					") as t4\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					"having count(*)=1\r\n" + 
//					") as t6 \r\n" + 
//					"where t6.contr_type = \"pull_requests\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			pull_conf = rs.getInt("pull_conf");
//			
//			ps = con.prepareStatement("select count(*) as comm_commit_conf from \r\n" + 
//					"(\r\n" + 
//					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
//					"(\r\n" + 
//					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
//					"union all\r\n" + 
//					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					") as t4\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					"having count(*)=1\r\n" + 
//					") as t6 \r\n" + 
//					"where t6.contr_type = \"commit_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_commit_conf = rs.getInt("comm_commit_conf");
//			
//			ps = con.prepareStatement("select count(*) as comm_issue_conf from \r\n" + 
//					"(\r\n" + 
//					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
//					"(\r\n" + 
//					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
//					"union all\r\n" + 
//					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					") as t4\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					"having count(*)=1\r\n" + 
//					") as t6 \r\n" + 
//					"where t6.contr_type = \"issue_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_issue_conf = rs.getInt("comm_issue_conf");
//			
//			ps = con.prepareStatement("select count(*) as comm_pull_conf from \r\n" + 
//					"(\r\n" + 
//					"select t4.project_id, t4.author_id, t4.contr_type, t4.created_at,count(*) from \r\n" + 
//					"(\r\n" + 
//					"select repo_id as project_id, author_id, created_at, contr_type from watch_contr\r\n" + 
//					"union all\r\n" + 
//					"select project_id,author_id, min(created_at) as created_at, contr_type from prj_contr_ts_typ2\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					") as t4\r\n" + 
//					"group by project_id, author_id\r\n" + 
//					"having count(*)=1\r\n" + 
//					") as t6 \r\n" + 
//					"where t6.contr_type = \"pull_request_comments\"",ResultSet.TYPE_SCROLL_INSENSITIVE);
//			rs = Database.processQuery(ps);
//			rs.next();
//			comm_pull_conf = rs.getInt("comm_pull_conf");
//			
//			System.out.println("\n"+"Other Contr Confidences:"+"\n"+commit_conf);
//			System.out.println(issue_conf);
//			System.out.println(pull_conf);
//			System.out.println(comm_commit_conf);
//			System.out.println(comm_issue_conf);
//			System.out.println(comm_pull_conf);
//			
//			
//			
//		}catch(SQLException e1) {
//			e1.printStackTrace();
//		}
//	}
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
				
		
//  RQ2 Tasks
		rq.watcherConfidence();
//		rq.otherContributorConf();
		
//  RQ3 Tasks
		
		
		
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
