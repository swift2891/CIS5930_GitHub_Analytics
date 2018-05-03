# Understanding Watchers on GitHub.

## Description:
A Course project to analyse the GitHub data to "Understand the Role of Watchers in Git Hub". 
We are basically implementing the research paper published in this link => https://dl.acm.org/citation.cfm?id=2597114

## Installing:
1. Load the database dump(github_dump.rar) into mysql (using MySQL Workbench).
2. Load the project folder(SE_Final_Project) into eclipse IDE. 
3. Add mysql-connector in Build Path. 
3. Add apache commons math library to Build Path.

## Running the software:
1. Now open ResearchQs.java in Eclipse IDE whose directory path is as follows CIS5930_GitHub_Analytics\SE_Final_Project\src\com\cis5930\results\ResearchQs.java. 
2. Run ResearchQs.java and observe the result in console for each RQ's.

## Description of Project directory: 
Our project contains code in two .java files which can be found in the following directory paths.

1. CIS5930_GitHub_Analytics\SE_Final_Project\src\com\cis5930\database\Database.java - This file contains a class that is used to achieve java - database connection.

2. CIS5930_GitHub_Analytics\SE_Final_Project\src\com\cis5930\results\ResearchQs.java - This file contains the class which uses the class in previous file to set up database connection and interact with database and compute the Research questions.

3. Project_Report.pdf - The Project Report document.

4. github_dump.rar - The mysql database dump, which contains the original github dataset and inter-mediary Tables created for the ease of computation.  

## Authors:
* Harish Mandava
* Vigneshwar Padmanaban