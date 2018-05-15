<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<title>Status</title>
</head>
<body >

	
	<%@ include file="toolbar.jsp" %> 
	
	<div class="container">
	
		<div class="col-sm-offset-0 header">
			<h3>Status</h3>
		</div>
		
		<table class="table table-bordered">			
			<tr>
				<td>
					session id
				</td>
				<td>
					${sessionId}
				</td>
			</tr>
			<tr>
				<td>
					server url
				</td>
				<td>
					${serverUrl}
				</td>
			</tr>
		</table>

	</div>	
			
</body>
</html>