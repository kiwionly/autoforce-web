<!DOCTYPE html>
<html>
<head>
	<title>Welcome to ToolForce</title>
	<link type="text/css" rel="stylesheet" media="screen, projection" href="css/default.css">
	<link type="text/css" rel="stylesheet" media="screen, projection" href="css/common.css">
	<link type="text/css" rel="stylesheet" media="screen, projection" href="css/bootstrap.css">
	<link type="text/css" rel="stylesheet" media="screen, projection" href="css/login.css">
	<link type="text/css" rel="stylesheet" media="screen, projection" href="css/form.css">
</head>
<body >

<div id="wrapper">
	
		<%@ include file="toolbar.jsp" %> 	
	
		<div id="task-list" class="login clearfix">
				
			<h1>your package.xml </h1>
			
			<div>
				${time} seconds
			</div>
			
			
			<div >
				${xml}
			</div>
			
		</div>
		
		<div class="clear"></div>

</div>

<script type="text/javascript" src="js/jquery-2.1.1.min.js"></script>

</body>
</html>