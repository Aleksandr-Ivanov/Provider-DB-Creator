package com.ivanov.providerdbcreator;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class carries controller functions. It gets Map of parameters from
 * Servlet. String parameters are defined in view page and validated by
 * null and emptiness check in Servlet. This class convert parameters
 * to workable types and check transformed data. It creates instances
 * of DBManager, Users and Date type time points to work with.
 * 
 * @author Aleksandr Ivanov
 */
public class Controller {
    private static final DateFormat viewDateFormat =
            new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private static volatile Controller instance;
    private int usersQuantity;
    private Date startDate;
    private Date endDate;
    private String hostName;
    private String portName;
    private String dbName;
    private String dbUserName;
    private String dbPassword;
    
    private Controller() {}
    
    public static Controller getInstance() {
        Controller localInstance = instance;
        if (localInstance == null) {
            synchronized (Controller.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Controller();
                }
            }
        }
        return localInstance;
    }

    /**
     * Sets workable controller parameters by conversion String 
     * parameters from received Map. 
     * 
     * Map represents HttpServletRequest parameters. First element in
     * parameters values arrays is taken because it is known that view
     * page send request with only one value per parameter.
     * 
     * Contains new Users quantity check to be an Integer more than 0.
     * Forbids lenient Date parse to avoid date writing and reading 
     * misunderstanding.
     * 
     * @param parameters are null and emptiness checked request 
     * parameters
     * @throws ParseException re-thrown to Servlet to show invalid
     * Date insertion 
     * @throws NumberFormatException is thrown to Servlet to show 
     * invalid users quantity insertion 
     */
    public void setParameters(Map<String, String[]> parameters) 
            throws ParseException {
        this.hostName = parameters.get("hostName")[0];
        this.portName = parameters.get("portName")[0];
        this.dbName = parameters.get("dbName")[0];
        this.dbUserName = parameters.get("dbUserName")[0];
        this.dbPassword = parameters.get("dbPassword")[0];
        
        String usersQuantityString = parameters.get("usersQuantity")[0];
        String startDateString = parameters.get("startDate")[0];
        String endDateString = parameters.get("endDate")[0];
        
        this.usersQuantity = Integer.parseInt(usersQuantityString);
        if (this.usersQuantity < 1) {
            throw new NumberFormatException("users quantity is less than 1");
        }

        //forbid non-strict adherence to the SimpleDateFormat
        viewDateFormat.setLenient(false);       
        
        this.startDate = getDateByString(startDateString);
        this.endDate = getDateByString(endDateString);
        if (startDate.getTime() > endDate.getTime()) {
            throw new ParseException("start date goes after end date", 0);
        }
    }

    /**
     * Creates DBManager instance to store data. Divides time line
     * between start and end dates to minutes. Creates previously 
     * set users quantity instances and load users traffic to time
     * points (minutes).
     * 
     * @throws SQLException handling is implied to do in Servlet 
     * to show exception to program user.
     * @throws ClassNotFoundException re-thrown from getConnection()
     * Handling is implied to do in Servlet to show exception to 
     * program user
     */
    void storeTrafficPerMinute()
            throws ClassNotFoundException, SQLException {
        DBManager dbManager = new DBManager(hostName, portName,
                dbName, dbUserName, dbPassword);
        Set<Date> intervalByMinutes = getTimePoints();
        List<User> users = getUsersList(usersQuantity);
        
        for (User user : users) {
            user.loadTraffic(intervalByMinutes);
            dbManager.storeUserTraffic(user);
        }
    }

    /**
     * Generates list with received User instances amount.
     * 
     * @param newUsersCount to generate User instances amount.
     * @return List of generated Users
     */
    private List<User> getUsersList(int newUsersCount) {
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < newUsersCount; i++) {
            User newUser = new User();
            users.add(newUser);
        }
        return users;
    }
  
    /**
     * Divide time line between two dates on time points per minute.
     * 
     * @return natural-ordered TreeSet of minutes
     */
    private Set<Date> getTimePoints() {
        Set<Date> intervalByMinutes = new TreeSet<>();
        Date currentMinute = startDate;
        
        while (currentMinute.getTime() < endDate.getTime()) {
            intervalByMinutes.add(currentMinute);
            currentMinute = new Date(currentMinute.getTime() + 60000L);
        }
        return intervalByMinutes;
    }
    
    /**
     * Checks String matching to view date format pattern and parses
     * String to Date
     * 
     * @param dateString is view inserted String date 
     * @return validated and parsed Date
     * @throws ParseException is thrown to show error to program user
     */
    private Date getDateByString(String dateString) throws ParseException {
        if (!dateString.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}")) {
            StringBuilder messageBuilder = new StringBuilder();
            
            messageBuilder.append("\"");
            messageBuilder.append(dateString);
            messageBuilder.append("\" ");
            messageBuilder.append("date doesn't match the format");
            
            String message = messageBuilder.toString();
            
            throw new ParseException(message, 0);
        }
        return viewDateFormat.parse(dateString);
    }
}
