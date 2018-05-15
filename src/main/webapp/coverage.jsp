<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<title>list of coverage</title>
</head>
<body >

	<%@ include file="toolbar.jsp" %> 
		
	<div class="container">
	
		<div class="col-sm-offset-0">
			<h3>Code Coverage</h3>
		</div>
		
		<div class="col-sm-offset-0">
			<h5>Time : ${time}</h5>
		</div>
		
		<div class="">
			<table class="table table-bordered">
			
			<c:forEach items="${result}" var="test">
			
				<c:set var="status" value="success"/> 
			
				<c:if test="${test.percent < 75}" >
					<c:set var="status" value="danger"/> 
				</c:if> 		
						
				<tr class="<c:out value="${status}"/>">
					<td>
						<a target="_blank" href="codecoverage.do?type=${type}&id=<c:out value="${test.id}"/>&name=<c:out value="${test.name}"/>&covered=<c:out value="${test.coverage.coverageString}"/>&uncovered=<c:out value="${test.coverage.uncoverageString}"/>">
							<c:out value="${test.name}"/>
						</a>
					</td>
					<td><c:out value="${test.percent}"/>%</td>
				</tr>
				
			</c:forEach>	
			
			</table>
		</div>

	</div>
	
</body>
</html>