<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>List Code/Page</title>	
</head>
<body >

	<%@ include file="toolbar.jsp" %> 
		
	<div class="container">
	
		<div class="form-group form-horizontal" >
            <div class="col-sm-2" style="float: right; margin-top: 30px;">
                <select class="form-control" id="type" name="type" >
                    <option value="ApexClass">ApexClass</option>
                    <option value="ApexTrigger">ApexTrigger</option>
                    <option value="ApexPage">ApexPage</option>
					<option value="ApexComponent">ApexComponent</option>
                </select>
            </div>
            <label for="type" class="col-sm-2 control-label" style="float: right; margin-top: 30px;">Change Type</label>
        </div>
	
		<div class="col-sm-offset-0">
			<h3>List Code/Page</h3>
		</div>
		
		<div class="col-sm-offset-0">
			<h5>Time : ${time}</h5>
		</div>
		
		<div class="">
			<table class="table table-bordered">
			<c:forEach items="${result}" var="clazz">				
				<tr>
					<td>
						 <a href="compile.do?id=<c:out value="${clazz.id}"/>&name=<c:out value="${clazz.name}"/>&type=<c:out value="${type}"/>" target="_blank">				    
					   	 	<c:out value="${clazz.name}"/> <br/>				   
					    </a> 
					</td>
					<td>
						 <a href="${server}/${clazz.id}" target="_blank">				    
					   	 	open in salesforce
					    </a> 
					</td>
					
					<c:if test="${type == 'ApexPage' }" >					
						<td>
							 <a href="${server}/apex/${clazz.name}" target="_blank">				    
						   	 	view page
						    </a> 
						</td>					
					</c:if>		
				</tr>
			</c:forEach>
			</table>
		</div>

	</div>
	
	<script>
            $(document).ready( function() {
            	
            	var preselect = getUrlVars()["type"];
                $("#type option:contains(" + preselect + ")").attr('selected', 'selected');
                
                $('#type').on('change', function() {
                
                	var type = $('#type').val();
                	
                	var prefix = getUrlVars()["prefix"];
                	
                	var params = {};
                	params.prefix = prefix;
                	params.type = type;
                	
                	var path = $.param(params);
                	
                	var href = $(location).attr('href');
                	var search = $(location).attr('search');
                	
                	var index = href.indexOf(search);
                	
                	var url = href.substring(0, index);
                	
                    location.replace( url + '?' + path);
                });
            });
            
            function getUrlVars()
            {
                var vars = [], hash;
                var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
                for(var i = 0; i < hashes.length; i++)
                {
                    hash = hashes[i].split('=');
                    vars.push(hash[0]);
                    vars[hash[0]] = hash[1];
                }
                return vars;
            }
        </script>

</body>
</html>