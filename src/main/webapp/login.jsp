<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<title>Welcome to AutoForce</title>
</head>
<body >
	
	<%@ include file="toolbar.jsp" %> 

	<div class="container">
	
		<div class="col-sm-offset-2 header">
			<h3>Login to Instance</h3>
		</div>
		
		<c:if test="${error != null }" >
  			<div class="col-sm-offset-2 bg-danger error">${error}</div>
		</c:if>		

		<form class="form-horizontal" role="form" action="login.do" method="post">
		
			<div class="form-group">
				<label for="email" class="col-sm-2 control-label">Email</label>
				<div class="col-sm-5">
					<input type="email" class="form-control" id="email" placeholder="Email" name="email">
				</div>
			</div>
			
			<div class="form-group">
				<label for="password" class="col-sm-2 control-label">Password</label>
				<div class="col-sm-5">
					<input type="password" class="form-control" id="password"	placeholder="Password" name="password">
				</div>
			</div>
			
			<div class="form-group">
				<label for="instance" class="col-sm-2 control-label">Instance</label>
				<div class="col-sm-5">
					<select class="form-control" id="instance" name="instance" >
						<option value="https://test.salesforce.com/services/Soap/c/">Sandbox</option>
						<option value="https://login.salesforce.com/services/Soap/c/">Developer Edition</option>
					</select>
				</div>
			</div>
			
			<div class="form-group">
				<label for="version" class="col-sm-2 control-label">Instance</label>
				<div class="col-sm-2">
					<select  class="form-control" id="version" name="version">
						<option value="33.0">33</option>
						<option value="32.0">32</option>
						<option value="31.0">31</option>						
					</select>
				</div>
			</div>			
			
			
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-4">
					<button type="submit" class="btn btn-default">Sign in</button>
				</div>
			</div>
			
		</form>
	</div>
	
</body>
</html>