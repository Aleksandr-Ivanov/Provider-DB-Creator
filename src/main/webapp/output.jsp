<%@ page contentType="text/html; charset=utf-8" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<link href="css/style.css" rel="stylesheet" type="text/css"/>
    </head>
    <body> 
    	<form action="<c:url value="/main"/>" method="post">
    		<table class="ver2">
                <tr>
					<td><textarea name="expression">${lastExpression}</textarea></td>
					<td><input type="submit" value="Calculate" name="Calculate"/></td>
                </tr>
               <tr>
               		<td>${lastResult}</td>
               </tr>
            </table>
        </form>
    </body>
</html>