<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${name}</title>

    <link href="css/normalize.css" rel="stylesheet">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    
     <link href="css/autoforce.css" rel="stylesheet">

</head>
<body>    
   
	<%@ include file="toolbar.jsp" %> 

	<div class="container-fluid">
	
		<div class="col-sm-offset-0">
			<h4>${name}</h4>
		</div>
		
		<span class="checkbox" style="float: right;"> 
	        <input type="checkbox" id="check" checked="checked"></input>Check Modified
		</span>
			
		<div class="">
			press Ctrl + S and MAGIC !! <br />			
			<span id="result"></span> time : <span id="time">${time}</span>
			<input type="hidden" id="lastModified" value="${last}" />
		</div>
		
		<div id="editor" class="col-sm-12"></div>
		

	</div>
	
<script src="js/jquery-2.1.1.min.js"></script>
<script src="js/bootstrap.min.js"></script>

<script type="text/javascript" src="js/jquery.base64.js"></script> 
<script src="src-min/ace.js" type="text/javascript" charset="utf-8"></script>
<script src="src-min/ext-language_tools.js" type="text/javascript" charset="utf-8"></script>
<script>
	$.base64.utf8encode = true;

	var langTools = ace.require("ace/ext/language_tools");
	
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/textmate");
    document.getElementById('editor').style.fontSize='14px';
    
    editor.setOptions({
    	enableBasicAutocompletion: true,
    	enableLiveAutocompletion: false,
    	enableSnippets: false
    });
           
    editor.getSession().setMode("ace/mode/java");
    
    if("${type}" == "ApexPage")
   		editor.getSession().setMode("ace/mode/xml");
    else  if("${type}" == "ApexComponent")
   		editor.getSession().setMode("ace/mode/xml");    
    else if("${type}" == "ApexTrigger")
   		editor.getSession().setMode("ace/mode/mysql");
    
    var code = $.base64.atob("${code}", true);    
    editor.setValue(code, 1);
    
    editor.commands.addCommand({
        name: 'save',
        bindKey: {win: 'Ctrl-S',  mac: 'Command-S'},
        exec: function(editor) {
            
        	//send to server :)
        	var code = editor.getValue();
    		var type = "${type}";
    		var id = "${id}";

        	$.ajax({
        		  url: "compile.do",
        		  type : "POST",
        		  dataType : "json",
        		  data: "code=" + encodeURIComponent(code) + "&type="+type + "&id="+id + "&lastModified=" + $('#lastModified').val() + "&checked=" + $('#check').is(':checked'),
        		  beforeSend: function( xhr ) {
        			  $('#result').text("send code to server...");
        		}
        	}).done(function(data) {
        		
        		var line = data.line;
        		var msg = data.message;
        		var time = data.time;
				var lastModified = data.lastModified;
        		
        		if(line >= 0)
        		{
        			editor.gotoLine(line);
        			editor.setHighlightActiveLine(true);
        		}
        		        		
        		 $('#time').text(time);
        		 $('#result').text(msg);
        		 
        		 if(lastModified != null)
        		 	$('#lastModified').val(lastModified);
        	});
        	
        	
        },
        readOnly: true // false if this command should not apply in readOnly mode
    });
    
    editor.commands.addCommand({
        name: 'autocomplete',
        bindKey: {win: 'Ctrl-Space',  mac: 'Command-Space'},
        exec: function(editor) {
                    
        	$.ajax({
      		  url: "checkAutoComplete.do",
      		  type : "POST",
      		  dataType : "json",
      		  data: "code="+encodeURIComponent(editor.getValue())+"&cursor="+editor.getCursorPosition().row,
      		  
      		}).done(function(data) {
      		      		
      			var apexCompleter = {
      			        getCompletions: function(editor, session, pos, prefix, callback) {
      			           
      			        	 callback(null, data.map(function(ea) {
			                        return {name: ea.word, value: ea.word, meta: ea.type}
			                    }));
      			        }
      			    }
      			langTools.addCompleter(apexCompleter);     			 
      				
      		});
        	
        },
        readOnly: true // false if this command should not apply in readOnly mode
    });
</script>
</body>
</html>