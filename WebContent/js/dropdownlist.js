$(function(){
            $.get("dynamicdropdown.html", function(response) {
                  $('#dropA').html(response);
				  
				  //applyDynamicBindings('#dropA');
            });
			
			$.get("searchbar.html", function(response) {
                  $('#navBarSearchForm').html(response);
            });
			
			var link = document.createElement('link');
			link.type = 'image/x-icon';
			link.rel = 'shortcut icon';
			link.href = "images/fls-favicon.ico";
			document.getElementsByTagName('head')[0].appendChild(link);
			
        });
		
		// Mentioned below is a test function for the above function.
		/*
		function applyDynamicBindings(container_selector){
             var $container=$(container_selector);
             $container.find("ul").click(function(){
                 //alert('triggered function bound to dynamic element');
             });
        }*/
		
		