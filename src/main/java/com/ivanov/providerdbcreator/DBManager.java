package com.ivanov.providerdbcreator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * This class describes SQL conversation side of current program.
 * Class is oriented to work with MySQL database. Main purpose of 
 * this class save users information and his traffic history to 
 * database storage. 
 * 
 * SQL schema consists of 2 tables: USERS and TRAFFIC. First
 * one store all user defining information: first name, last name,
 * city, address and user id as primary key. Second one is 
 * specified to work with traffic history: time points with minute 
 * precision (minutes), load in bytes, user IDs as the foreign key 
 * and traffic record ID as the primary key. 
 * 
 * It has methods to create new users table new traffic table, new 
 * user record, new traffic record and to find user ID by user 
 * registration details.
 * 
 * Tested with MySQL 5.6.25 Community version. 
 * 
 * @author Aleksandr Ivanov
 */
public class DBManager {
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
    private static final DateFormat sqlDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String dbLink;
    private final String dbUserName;
    private final String dbPassword;
    
    /**
     * Determines necessity to check USERS table existence in schema.
     */
    private boolean isNotCreatedUsersTable = true;
    
    /**
     * Determines necessity to check TRAFFIC table existence in 
     * schema 
     */
    private boolean isNotCreatedTrafficTable = true;

    /**
     * Creates new instance of DBManager and defines SQL connection
     * parameters. Constructs URL to connect to MySQL by JDBC driver.
     * 
     * @param hostName host name to connect
     * @param portName port of host to connect
     * @param dbName schema name to connect
     * @param dbUserName username to login
     * @param dbPassword password to login
     */
    public DBManager(String hostName, String portName,
                      String dbName, String dbUserName, String dbPassword) {
        this.dbLink = "jdbc:mysql://" + hostName + ":" + portName + "/"
                      + dbName;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
    }
    
    /**
     * Determines steps to do for current user traffic storing.
     * 
     * @param user defines whose traffic to store
     * @throws SQLException is re-thrown from underlying methods. 
     * Handling is implied to do in Servlet to show exception to 
     * program user.
     * @throws ClassNotFoundException re-thrown from getConnection()
     * Handling is implied to do in Servlet to show exception to 
     * program user
     */
    void storeUserTraffic(User user) 
            throws SQLException, ClassNotFoundException {
        if (isNotCreatedUsersTable()) {
            createUsersTable();
        }
        
        if (isNotCreatedTrafficTable()) {
            createTrafficTable();
        }
        
        insertUserTraffic(user);
    }

    /**
     * Store users traffic Map through iteration entries and inserting
     * by prepare statement.
     * 
     * @param user defines whose traffic to store
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     * @throws ClassNotFoundException re-thrown from getConnection()
     * Handling is implied to do in Servlet to show exception to 
     * program user
     */
    private void insertUserTraffic(User user) 
            throws SQLException, ClassNotFoundException {
        int userId = getUserId(user);
        
        String insertTrafficStmnt = "INSERT INTO TRAFFIC"
                + "(MINUTE, LOADED_BYTES, USER_ID) " 
                + "VALUES( ?, ?, ?)";
        
        try (Connection dbConnection = getDBConnection(); 
                PreparedStatement prepStmnt = 
                        dbConnection.prepareStatement(insertTrafficStmnt);) {
            
            Map<Date, Integer> userTraffic = user.getTraffic();
            
            for (Map.Entry<Date, Integer> entry : userTraffic.entrySet()) {
                Date minute = entry.getKey();
                Integer traffic = entry.getValue();
                
                String minuteString = getSQLFormatTime(minute);
                
                prepStmnt.setString(1, minuteString);
                prepStmnt.setInt(2, traffic);
                prepStmnt.setInt(3, userId);
                prepStmnt.execute();
            }
        } 
    }

    /**
     * Get user ID from USERS table. If user not exists in table 
     * it gives the command to create.
     * 
     * @param user defines whose ID to get
     * @return user id in SQL schema USERS table
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     * @throws ClassNotFoundException re-thrown from getConnection()
     * Handling is implied to do in Servlet to show exception to 
     * program user
     */
    private int getUserId(User user) 
            throws SQLException, ClassNotFoundException {
        String getUserIdQuery = "SELECT USER_ID "
                + "FROM USERS "
                + "WHERE FIRSTNAME = '" + user.getFirstName() + "' "
                + "AND LASTNAME = '" + user.getLastName() + "' "
                + "AND CITY = '" + user.getCity() + "' "
                + "AND ADDRESS = '" + user.getAddress() + "';";
         
        try (Connection dbConnection = getDBConnection(); 
                Statement statement = dbConnection.createStatement();) {
     
            ResultSet rs = statement.executeQuery(getUserIdQuery);
                if (rs.next()) {
                    return rs.getInt("USER_ID");
                } else {
                    insertUser(user);
                    return getUserId(user);
                }
        }
    }

    /**
     * Execute statement to insert new user into USERS table with 
     * registration details of received User.
     * 
     * @param user defines whose credentials to store.
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     * @throws ClassNotFoundException re-thrown from getConnection()
     * Handling is implied to do in Servlet to show exception to 
     * program user
     */
    private void insertUser(User user) 
            throws SQLException, ClassNotFoundException {
        String userCreationTime = getSQLFormatTime(new Date());
        
        String insertUserStmnt = "INSERT INTO USERS"
                + "(FIRSTNAME, LASTNAME, CITY, ADDRESS, CREATED_TIME) "
                + "VALUES('" + user.getFirstName() + "', " 
                + "'" + user.getLastName() + "', " 
                + "'" + user.getCity() + "', "  
                + "'" + user.getAddress() + "', " 
                + "'" + userCreationTime + "')";
        
        try (Connection dbConnection = getDBConnection(); 
                Statement statement = dbConnection.createStatement();) {
            
            statement.executeUpdate(insertUserStmnt);
        }
    }

    /**
     * Creates new USERS table in SQL schema with check if not exists
     * 
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     * @throws ClassNotFoundException re-thrown from getConnection()
     * Handling is implied to do in Servlet to show exception to 
     * program user
     */
    private void createUsersTable() 
            throws SQLException, ClassNotFoundException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS USERS("
                + "USER_ID INT NOT NULL AUTO_INCREMENT, "
                + "FIRSTNAME VARCHAR(20) NOT NULL, "
                + "LASTNAME VARCHAR(20) NOT NULL, "
                + "CITY VARCHAR(20) NOT NULL, "
                + "ADDRESS VARCHAR(40) NOT NULL, "
                + "CREATED_TIME DATETIME NOT NULL, " 
                + "PRIMARY KEY (USER_ID)" + ")";
     
        try (Connection dbConnection = getDBConnection(); 
                Statement statement = dbConnection.createStatement();) {
     
            statement.execute(createTableSQL);
            isNotCreatedUsersTable = false;
        }
    }

    /**
     * Creates new TRAFFIC table in SQL schema with check if not 
     * exists
     * 
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     * @throws ClassNotFoundException re-thrown from getConnection()
     * Handling is implied to do in Servlet to show exception to 
     * program user
     */
    private void createTrafficTable() 
            throws SQLException, ClassNotFoundException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS TRAFFIC("
                + "RECORD_ID INT NOT NULL AUTO_INCREMENT, "
                + "MINUTE DATETIME NOT NULL, "
                + "LOADED_BYTES INT NOT NULL, "
                + "USER_ID INT NOT NULL REFERENCES USERS (USER_ID), "
                + "PRIMARY KEY (RECORD_ID)"
                + ")";
     
        try (Connection dbConnection = getDBConnection(); 
                Statement statement = dbConnection.createStatement();) {
     
            statement.execute(createTableSQL);
            isNotCreatedTrafficTable = false;
        }
    }

    /**
     * Creates new connection to schema with current instance 
     * DBManager credentials
     * 
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     * @throws ClassNotFoundException handling is implied to do in 
     * Servlet to show exception to program user
     */
    private Connection getDBConnection() 
            throws SQLException, ClassNotFoundException {
        Class.forName(DB_DRIVER);
        return DriverManager.getConnection(dbLink, dbUserName, dbPassword);
    }
    
    /**
     * Returns String with database readable date
     * 
     * @param date
     * @return String with time in database accepted format
     */
    private String getSQLFormatTime(Date date) {
        return sqlDateFormat.format(date);
    }
    
    private boolean isNotCreatedUsersTable() {
        return isNotCreatedUsersTable;
    }

    private boolean isNotCreatedTrafficTable() {
        return isNotCreatedTrafficTable;
    }
}
