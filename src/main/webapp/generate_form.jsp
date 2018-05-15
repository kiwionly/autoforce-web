<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>Generate package.xml</title>
</head>
<body >

	<%@ include file="toolbar.jsp" %> 	

	<div class="container">
	
		<div class="col-sm-offset-0 header">
			<h3>Generate Package xml</h3>
		</div>
		
		<form class="form-horizontal" role="form" action="package.do" method="get">
		
			<div class="col-sm-offset-0 header">
				<h5>Click View source to copy package.xml after generated</h5>
			</div>			
		
			<div class="form-group">
				<label for="prefix" class="col-sm-2 control-label">Prefix</label>
				<div class="col-sm-5">
					<input type="text" class="form-control" id="prefix" placeholder="you can put multiple prefix by using comma or space" name="prefix">
				</div>
			</div>
				
			<c:forEach items="${type}" var="type">	
			
				<div class="form-group">
				    <div class="col-sm-offset-2 col-sm-5">
				      <div class="checkbox">
				        <label>
				        
				        	<c:if test="${type.checked == true }" >
					  			<c:set var="checked" value="checked=\"checked\""/> 
							</c:if>		
		
				          	<input type="checkbox" name="metaTypes" value="<c:out value="${type.name}"/>" ${checked} ><c:out value="${type.name}"/>
				          
				          	<c:set var="checked" value=""/> 
				          
				        </label>
				      </div>
				    </div>
				</div>						
				
			</c:forEach>
			
			
			<div class="form-group">
				<label for="exclude" class="col-sm-2 control-label">Exclude Prefix Types:</label>
				<div class="col-sm-5">
					<input type="text" class="form-control" id=exclude placeholder="specific the type you want excluded from prefix, e.g. ApexClass" name="exclude">
				</div>
			</div>
			
			
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-4">
					<button type="submit" class="btn btn-default">Generate Package Xml</button>
				</div>
			</div>
			
		</form>

	</div>
	
</body>
</html>