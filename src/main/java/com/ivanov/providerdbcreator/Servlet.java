package com.ivanov.providerdbcreator;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class receives parameters from view page. It does simple null and
 * emptiness String validation. If String parameters are valid as 
 * String they will be send to controller to be set. 
 * 
 * If some parameter is not valid Servlet returns error message for
 * wrong parameter as result. Servlet handles all program exceptions 
 * and shows it to program user on main view page. 
 * 
 * All received parameters will be converted to attributes to be shown on view 
 * form as form autofill.
 * 
 * @author Aleksandr Ivanov
 */
@WebServlet("/main") 
public class Servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /** 
     * Main functions of method is to get parameters from view page,
     * to validate parameters as String (null and emptiness check),
     * to forward results back to view. 
     * 
     * If parameters are valid it gets Controller instance to handle
     * parameters and to process users traffic generation and storing.
     * 
     * It handles all program execution exceptions to send it to view
     * page.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) 
            throws ServletException, IOException {
        
        /* Provides String validation and parameters setting */
        String requestParamsValidation = checkAndSetParameters(request);
        
        String error = "Error: ";
        String result = null;
        
        /*
         * if check returns "successful." then start controller else show 
         * validation error as result
         * 
         * exceptions are handling to show on view as result
         */
        if ("succesful.".equals(requestParamsValidation)) {
            try {
                Controller controller = Controller.getInstance();
                Map<String, String[]> parameters = request.getParameterMap();
                
                controller.setParameters(parameters);
                controller.storeTrafficPerMinute();
                result = "Last operation passed successfully!";
            } catch (ClassNotFoundException | SQLException e) {
                result = error + e;
            } catch (ParseException e) {
                result = error + e + ". Can't parse date. "
                         + "Format example: 13.01.2012 01:57";
            } catch (NumberFormatException e) {
                result = error + e + ". Can't parse users quantity. "
                         + "Users quantity should be an integer and > 0.";
            }
        } else {
            result = error + requestParamsValidation;
        }
        
        /* 
         * received parameters and result is being set as request attributes to
         * be shown on view 
         */
        request = setRequestAttributes(request, result);
        forwardIt(request, response);
    }
    
    /**
     * parameters Map is filling by Map which represents 
     * HttpServletRequest parameters. First element in parameters 
     * values arrays is taken because it is known that view page send
     * request with only one value per parameter.
     * 
     * @param request provides parameters Map
     * @return String of validation result
     */
    private String checkAndSetParameters(HttpServletRequest request) {
        String usersQuantityStr = request.getParameter("usersQuantity");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String hostName = request.getParameter("hostName");
        String portName = request.getParameter("portName");
        String dbName = request.getParameter("dbName");
        String dbUserName = request.getParameter("dbUserName");
        String dbPassword = request.getParameter("dbPassword");
        
        if (usersQuantityStr == null || usersQuantityStr.isEmpty()){
            return "users quantity is empty.";
        } else if (startDate == null || startDate.isEmpty()) {
            return "start date is empty.";
        } else if (endDate == null || endDate.isEmpty()) {
            return "end date is empty.";
        } else if (hostName == null || hostName.isEmpty()) {
            return "host name is empty.";
        } else if (portName == null || portName.isEmpty()) {
            return "port name is empty.";
        } else if (dbName == null || dbName.isEmpty()) {
            return "DB/Schema name is empty.";
        } else if (dbUserName == null || dbUserName.isEmpty()) {
            return "DB username is empty.";
        } else if (dbPassword == null || dbPassword.isEmpty()) {
            return "DB password is empty.";
        } else {
            return "succesful.";
        }
    }
    
    /**
     * Sets parameters and result String as attributes into prepared 
     * request to be shown on view page
     * 
     * @param request which will be forwarded
     * @param result to be shown user
     * @return request prepared to forward
     */
    private HttpServletRequest setRequestAttributes(
            HttpServletRequest request, String result) {
        Map<String, String[]> parameters = request.getParameterMap();
        
        /*Loop to get parameter and set it as attribute*/
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String attributeName = entry.getKey();
            
            /* 
             * Get the first element from parameter values in string
             * array. The first one cause it is known that view page
             * sends only one value per parameter
             */
            String attributeValue = entry.getValue()[0]; 
            
            request.setAttribute(attributeName, attributeValue);
        }
        request.setAttribute("result", result);
        return request;
    }
    
    /**
     * Forwards prepared request to view page 
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void forwardIt(
            HttpServletRequest request,
            HttpServletResponse response) 
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        
        RequestDispatcher dispatcher = 
                context.getRequestDispatcher("/index.jsp");
        
        dispatcher.forward(request, response);
    }
}