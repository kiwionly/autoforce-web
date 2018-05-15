<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<title>Debug Log</title>
</head>
<body >

    <style>
    
body {
    padding-top: 70px;
}

.console {
    width: 100%;
    height: 600px;      
}

#panel {
    margin: 20px 0px;   
}

textarea.form-control {
    height: 600px;
}

    </style>

	<%@ include file="toolbar.jsp" %> 
    
    <div class="container">
    
        <div id="panel">
        
            <form class="form-inline" role="form" action="debug.do">
    
                <div class="form-group">
                    <label class="n" for="application">Enter Last N Log</label> 
                    <input class="form-control" id="n" type="text" name="n" value="5"/>
                </div>
                                
                <button class="btn btn-default" id="subscribe" type="submit">get Log</button>
                
    
            </form>
    
        </div>
        
        <textarea class="form-control console" id="log" spellcheck="false">${result}</textarea>

    </div>
        
</body>
</html>