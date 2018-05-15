<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<title>Execute Apex Code</title>
</head>
<body >

<style>
    
body {
    padding-top: 70px;
}

.console {
    width: 100%;
    margin-bottom: 20px;
}

.result {
	margin-top: 20px;
}

#panel {
    margin: 20px 0px;   
}

textarea.form-control {
    height: 200px;
}

.error {
	margin-top: 20px;
}

</style>

	<%@ include file="toolbar.jsp" %> 
    
    <div class="container">
    
        <div id="panel">
        
            <form class="form-horizontal" role="form" action="execute.do" method="post">
            
            	<div class="form-group">
					<label for="level" class="col-sm-2 control-label">Log Level</label>
					<div class="col-sm-2">
						<select class="form-control" id="level" name="level" >
							<option value="DEBUG">DEBUG</option>
							<option value="INFO">INFO</option>
							<option value="ERROR">ERROR</option>	
						</select>
					</div>
				</div>
            
            	<textarea class="form-control console" id="log" rows="3" spellcheck="false" name="code" id="code">${code}</textarea>
    
                <button class="btn btn-default" id="run" type="submit">Run</button>
                    
            </form>
            
            <c:if test="${error != null }" >
	  			<div class="bg-danger error">${error}</div>
			</c:if>	
            
            <div class="result">${result}</div>
    
        </div>
        
    </div>   
      
</body>
</html>