<html>
<head>
	<title>Select Apex class</title>
</head>
<body >

	<%@ include file="toolbar.jsp" %> 	
	
	<div class="container">
	
		<div class="col-sm-offset-0 header">
			<h3>List Code/Page</h3>
		</div>
		
		<form class="form-horizontal" role="form" action="apexCode.do" method="get">
		
			<div class="form-group">
				<label for="type" class="col-sm-2 control-label">Select Code/Page:</label>
				<div class="col-sm-5">
					<select class="form-control" id="type" name="type" >
						<option value="ApexClass" >ApexClass</option>
						<option value="ApexTrigger">ApexTrigger</option>
						<option value="ApexPage">ApexPage</option>
						<option value="ApexComponent">ApexComponent</option>
					</select>
				</div>
			</div>
		
			<div class="form-group">
				<label for="prefix" class="col-sm-2 control-label">Prefix</label>
				<div class="col-sm-5">
					<input type="text" class="form-control" id="prefix" placeholder="currently only support one prefix" name="prefix">
				</div>
			</div>
						
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-4">
					<button type="submit" class="btn btn-default">List Code/Page</button>
				</div>
			</div>
			
		</form>

	</div>
	
</body>
</html>