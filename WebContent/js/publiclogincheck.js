    //This Js file is for the header of webpages which do not require user to be signed in i.e can be viewd either ways.
	
	function publicLoginCheck(){

        if(userloggedin != 'anonymous'){   // if a user is logged in show username.
			
				$('#subheader').show();			
				$("#loginoptionmenu").hide();
				$("#signupoptionmenu").hide();
				
				//show "username"
				var salutation = document.getElementById("salutation");
				var span = document.createElement("span");
				
				span.innerHTML = userloggedin;
				salutation.appendChild(span);
			}else{                               // if a user is not logged in show login & signup options.
			    $('#subheader').hide();			
				$("#loginoptionmenu").show();
				$("#signupoptionmenu").show();
			}
    }