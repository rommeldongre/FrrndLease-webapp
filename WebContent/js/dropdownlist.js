$(function(){
            $.get("dynamicdropdown.html", function(response) {
                  $('#dropA').html(response);
				  //applyDynamicBindings('#dropA');
            });
        });
		
		// Mentioned below is a test function for the above function.
		/*
		function applyDynamicBindings(container_selector){
             var $container=$(container_selector);
             $container.find("ul").click(function(){
                 //alert('triggered function bound to dynamic element');
             });
        }*/
		