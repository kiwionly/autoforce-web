<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <title></title>

    <link href="css/normalize.css" rel="stylesheet">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    
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
    			
</head>
<body>

	<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
	    <div class="container">
	        <div class="navbar-header">
	            <a class="navbar-brand" href="login.do">Salesforce Logging</a>
	        </div>          
	    </div>
	</nav>
    
    <div class="container">
    
        <div id="panel">
        
            <form class="form-inline" role="form">
    
                <div class="form-group">
                    <label class="sr-only" for="application">Application Name</label> 
                    <input class="form-control" id="application" placeholder="Enter Application Name" type="text" />
                </div>
                
                <div class="form-group">
                    <label class="sr-only" for="tag">Tag</label> 
                    <input class="form-control" id="tag" placeholder="Enter Tag" type="text" />
                </div>
                
                <button class="btn btn-default" id="subscribe" type="submit">Subscribe</button>
                
                <button class="btn btn-default" id="clear" type="button">Clear</button>
    
            </form>
    
        </div>
        
        <textarea class="form-control console" id="log" spellcheck="false"></textarea>

    </div>
    
    
    <script src="js/jquery-2.1.1.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    
    <script>
		
   	var WSConsole = (function(){
   		
   		var appValue = '';
   	    var tagValue = '';
   	       		
   		var initialize = function()
   		{
   			$("#subscribe").click(function() {
   	            
   	            appValue = $("#application").val();
   	            tagValue = $("#tag").val();
   	            
   	            $("#application").prop("disabled", true);
   	            $("#tag").prop("disabled", true);
   	            
   	            subscribe();
   	            
   	            return false;
   	        });
   			
   			
			$("#clear").click(function() {
				$("#log").empty();
		             
				return false;
			});
   			
   		}
   		
   		var isArray = function(what) 
   		{
   		    return Object.prototype.toString.call(what) === '[object Array]';
   		}
   			
   		var subscribe = function() 
   		{  
			var websocket = new WebSocket("ws://localhost:8080/tool/log"); 
   			
   			websocket.onopen = function(event) {    				
   				$("#log").append("connection open\n");
                
   			}; 
   			
   			websocket.onclose = function(event) { 
   				$("#log").append("connection close. If want to view the log file, please refresh this page.\n");
   			};
   			
   			websocket.onmessage = function(message) {
   				   				
   				var json = $.parseJSON(message.data);
   				
   				if(isArray(json)) {
   					
   					console.log(json);
   					   					
   					$.each(json, function(i, item) {
   						
   						if(filter(item))
   		                {
   		                    var me = format(item);
   		                    $("#log").append(me + "\n");
   		                }
   					});
   					
   					return;
   				}
   				else 
   				{	
   					if(filter(json))
   	                {
   	                    var me = format(json);
   	                    $("#log").append(me + "\n");
   	                }
   				}  				

   			}; 
   			
   			websocket.onerror = function(event) { 
   				$("#log").append("something happen !" + event + "\n");
   			};
			
   		}  
   		
   		var filter = function(obj)
   	    {   	         
   	        var app = obj.Name;
   	        var tag = obj.Tag__c;

   	        if(tag == null)
   	            tag = "";

   	        if(appValue == app && tagValue == tag)
   	            return true;
   	        
   	        return false;
   	    }
   		
   		var format = function(obj)
   	    {   	        
   	        var clazz = obj.Class__c;
   	        var LogLevel = obj.LogLevel;
   	        var app = obj.Name;
   	        var message = obj.message__c;
   	        var date = obj.CreatedAt__c;
   	        var tag = obj.Tag__c;
   	        
   	        var msg = date + " : " + clazz + " : " + tag + " : " + message;
   	        
   	        return msg;
   	    }
   		
   	
   		return {
   			
   			init: function()
   			{
   				initialize();
   			}
   		}
   		
   	})();
   	
   	$( document ).ready(function() {
   		WSConsole.init();
   	});
   
    </script>
</body>
</html>