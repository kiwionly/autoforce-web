<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
	<title>list of coverage</title>
	<link href="css/normalize.css" rel="stylesheet">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    
     <link href="css/autoforce.css" rel="stylesheet">
	
</head>
<body >

	<%@ include file="toolbar.jsp" %> 
		
	<div class="container-fluid">
	
		<div class="col-sm-offset-0">
			<h3>Code Coverage for <span class="type">${type}</span> <span class="name">${name}</span> </h3> 
			<a class="open" href="compile.do?id=${id}&type=${type}&name=${name}" target="_blank">open source code</a>
		</div>
		
		<div class="col-sm-offset-0">
			<h5>Time : ${time}</h5>
		</div>
		
		<div>
		
			<div class="textarea" contenteditable="false"></div>
		
		</div>

	</div>
	
<script src="js/jquery-2.1.1.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script type="text/javascript" src="js/jquery.base64.js"></script> 
<script>

var covered = [${covered}];
var uncovered = [${uncovered}];

$( document ).ready(function() {
	
	$.base64.utf8encode = true;
    
    var code = $.base64.atob("${code}", true); 
	
	var arr = code.split('\n');
	
	var dom = '';
	
	$.each(arr, function( index, value ) {
		
		index +=1;
		
		var style = '';
		
		if($.inArray(index, covered) > -1) {			
			style = 'covered';
		}
		else if($.inArray(index, uncovered) > -1) {		
			style = 'uncovered';
		}
		
		if(value.trim() == '')
			value = ' ';
		
			
		dom+= '<span class="line">' + index + '</span>';
		dom+= '<pre class="' + style + '">' + htmlEntities(value) + '</pre>';
				
	});
	
	var codeElement = $.parseHTML(dom);
	
	$(codeElement).appendTo($('.textarea'));
	
	
});

function htmlEntities(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
</script>
</body>
</html>