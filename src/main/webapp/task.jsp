<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<title>Task List</title>
</head>
<body >

	
	<%@ include file="toolbar.jsp" %> 
	
	<div class="container">
	
		<div class="col-sm-offset-0 header">
			<h3>Select Task</h3>
		</div>
		
		<table class="table table-bordered">
			<c:forEach items="${taskList}" var="task">
			<tr>
				<td>
					<a href="<c:out value="${task.url}"/>"><c:out value="${task.name}"/></a>
				</td>
				<td><c:out value="${task.desc}"/></td>
			</tr>		
			</c:forEach>
		</table>

	</div>	
			
</body>
</html>