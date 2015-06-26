<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
    <link href="css/style.css" rel="stylesheet" type="text/css" />
</head>
    <body> 
    	<form action="<c:url value="/main"/>" method="post">
    		<table class="ver2">
    			<tr>
                    <th colspan = "4">Please, fill in MySQL connection form, 
                        users quantity to create and time period to load traffic. 
                </th>
                </tr>
                <tr>
                    <td>Host name:</td>
                    <td><input type="text" name="hostName" value="${hostName}" /></td>
                    <td>New unique users:</td>
                    <td><input type="text" name="usersQuantity" value="${usersQuantity}" /></td>
                </tr>
				<tr>
				    <td>Port:</td>
				    <td><input type="text" name="portName" value="${portName}" /></td>
				    <td>Date and time start:</td>
                    <td><input type="text" name="startDate" value="${startDate}" /></td>
			    </tr>
			    <tr>
			    	<td>DB/Schema name:</td>
				    <td><input type="text" name="dbName" value="${dbName}" /></td>
			    	<td>Date and time end:</td>
                    <td><input type="text" name="endDate" value="${endDate}" /></td>
			    </tr>
			    <tr>
			        <td>DB username:</td>
				    <td><input type="text" name="dbUserName" value="${dbUserName}" /></td>
			    	<td colspan = "2"><span class="bold">Date and time format is 'dd.MM.yyyy HH:mm'.</span></td>
			    </tr>
			    <tr>
			        <td>DB password:</td>
				    <td><input type="password" name="dbPassword"  value="${dbPassword}"/></td>
			        <td></td>
			        <td align = "left"><input type="submit" value="Run" name="Run"/></td>
			    </tr>
			    <tr>
			        <td colspan = "4" align = "center">${result}</td>
			    </tr>
            </table>
        </form>
    </body>
</html>
        
