This program emulates provider subscribers traffic storing to the database. 

It gets database connection parameters, unique users amount to create, start 
and end (date with time) of time line to generate random traffic load in bytes
on time points per minute through this time line.

SQL schema consists of 2 tables: USERS and TRAFFIC. The first one store all user
defining information: a first name, a last name, a city, an address and an 
user ID as the primary key. The second one is specified to work with a traffic 
history: a time points with minute precision (minutes), a load in bytes, an 
user IDs as the foreign key and a traffic record ID as the primary key. 

Technologies stack:
JSP, JSTL, Servlet, JDBC, SQL.

Tested technologies implementations:
Apache Maven 3, Apache Tomcat 7.0, MySQL 5.6.25 Community Server.