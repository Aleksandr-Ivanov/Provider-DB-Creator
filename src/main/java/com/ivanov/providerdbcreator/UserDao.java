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
public class UserDao {
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
     * Creates new instance of UserDao and defines SQL connection
     * parameters. Constructs URL to connect to MySQL by JDBC driver.
     * 
     * @param hostName host name to connect
     * @param portName port of host to connect
     * @param dbName schema name to connect
     * @param dbUserName username to login
     * @param dbPassword password to login
     */
    public UserDao(String hostName, String portName,
                   String dbName, String dbUserName, String dbPassword) {
        StringBuilder linkBuilder = new StringBuilder();
        
        linkBuilder.append("jdbc:mysql://");
        linkBuilder.append(hostName);
        linkBuilder.append(":");
        linkBuilder.append(portName);
        linkBuilder.append("/");
        linkBuilder.append(dbName);
        
        this.dbLink = linkBuilder.toString();
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
        Connection connection = getConnection();
        try {
            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);

            if (isNotCreatedUsersTable()) {
                createUsersTable(connection);
            }

            if (isNotCreatedTrafficTable()) {
                createTrafficTable(connection);
            }

            insertUserTraffic(connection, user);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }
    }

    /**
     * Store users traffic Map through iteration entries and inserting
     * by prepare statement.
     * 
     * @param connection for SQL manipulations usage
     * @param user defines whose traffic to store
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     */
    private void insertUserTraffic(Connection connection, User user) 
            throws SQLException {
        int userId = getUserId(connection, user);
        StringBuilder queryBuilder = new StringBuilder();
        
        queryBuilder.append("INSERT INTO TRAFFIC(MINUTE, ");
        queryBuilder.append("LOADED_BYTES, USER_ID) ");
        queryBuilder.append("VALUES( ?, ?, ?);");
        
        String query = queryBuilder.toString();
        
        try (PreparedStatement prepStmnt = connection.prepareStatement(query)) {
            
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
     * @param connection for SQL manipulations usage
     * @param user defines whose ID to get
     * @return user id in SQL schema USERS table
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     */
    private int getUserId(Connection connection, User user) 
            throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        
        queryBuilder.append("SELECT USER_ID FROM USERS ");
        queryBuilder.append("WHERE FIRSTNAME = '");
        queryBuilder.append(user.getFirstName());
        queryBuilder.append("' AND LASTNAME = '");
        queryBuilder.append(user.getLastName());
        queryBuilder.append("' AND CITY = '");
        queryBuilder.append(user.getCity());
        queryBuilder.append("' AND ADDRESS = '");
        queryBuilder.append(user.getAddress());
        queryBuilder.append("';");
        
        String query = queryBuilder.toString();
         
        try (Statement statement = connection.createStatement()) {
     
            ResultSet rs = statement.executeQuery(query);
                if (rs.next()) {
                    return rs.getInt("USER_ID");
                } else {
                    insertUser(connection, user);
                    return getUserId(connection, user);
                }
        }
    }

    /**
     * Execute statement to insert new user into USERS table with 
     * registration details of received User.
     * 
     * @param connection for SQL manipulations usage
     * @param user defines whose credentials to store.
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     */
    private void insertUser(Connection connection, User user) 
            throws SQLException {
        String userCreationTime = getSQLFormatTime(new Date());
        StringBuilder queryBuilder = new StringBuilder();
        
        queryBuilder.append("INSERT INTO USERS(");
        queryBuilder.append("FIRSTNAME, LASTNAME, CITY, ");
        queryBuilder.append("ADDRESS, CREATED_TIME) ");
        queryBuilder.append("VALUES('");
        queryBuilder.append(user.getFirstName());
        queryBuilder.append("', '");
        queryBuilder.append(user.getLastName());
        queryBuilder.append("', '");
        queryBuilder.append(user.getCity());
        queryBuilder.append("', '");
        queryBuilder.append(user.getAddress());
        queryBuilder.append("', '");
        queryBuilder.append(userCreationTime);
        queryBuilder.append("');");
        
        String query = queryBuilder.toString();
        
        try (Statement statement = connection.createStatement()) {
            
            statement.executeUpdate(query);
        }
    }

    /**
     * Creates new USERS table in SQL schema with check 
     * 'if not exists'
     * 
     * @param connection for SQL manipulations usage
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     */
    private void createUsersTable(Connection connection) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        
        queryBuilder.append("CREATE TABLE IF NOT EXISTS USERS(");
        queryBuilder.append("USER_ID INT UNSIGNED NOT NULL AUTO_INCREMENT, ");
        queryBuilder.append("FIRSTNAME VARCHAR(20) NOT NULL, ");
        queryBuilder.append("LASTNAME VARCHAR(20) NOT NULL, ");
        queryBuilder.append("CITY VARCHAR(20) NOT NULL, ");
        queryBuilder.append("ADDRESS VARCHAR(40) NOT NULL, ");
        queryBuilder.append("CREATED_TIME DATETIME NOT NULL, ");
        queryBuilder.append("PRIMARY KEY (USER_ID)" + ");");
        
        String query = queryBuilder.toString();
     
        try (Statement statement = connection.createStatement()) {
     
            statement.execute(query);
            isNotCreatedUsersTable = false;
        }
    }

    /**
     * Creates new TRAFFIC table in SQL schema with check 
     * 'if not exists'
     * 
     * @param connection for SQL manipulations usage
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     */
    private void createTrafficTable(Connection connection) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        
        queryBuilder.append("CREATE TABLE IF NOT EXISTS TRAFFIC(");
        queryBuilder.append("RECORD_ID INT UNSIGNED NOT NULL AUTO_INCREMENT, ");
        queryBuilder.append("MINUTE DATETIME NOT NULL, ");
        queryBuilder.append("LOADED_BYTES INT UNSIGNED NOT NULL, ");
        queryBuilder.append("USER_ID INT UNSIGNED NOT NULL, ");
        queryBuilder.append("PRIMARY KEY (RECORD_ID), ");
        queryBuilder.append("FOREIGN KEY (USER_ID) ");
        queryBuilder.append("REFERENCES USERS (USER_ID));");
        
        String query = queryBuilder.toString();
     
        try (Statement statement = connection.createStatement()) {
     
            statement.execute(query);
            isNotCreatedTrafficTable = false;
        }
    }

    /**
     * Creates new connection to schema with current instance 
     * UserDao credentials
     * 
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     * @throws ClassNotFoundException handling is implied to do in 
     * Servlet to show exception to program user
     */
    private Connection getConnection() 
            throws SQLException, ClassNotFoundException {
        Class.forName(DB_DRIVER);
        return DriverManager.getConnection(dbLink, dbUserName, dbPassword);
    }
    
    /**
     * Returns String with database readable date
     * 
     * @param date is given Date object
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
