function logInCheck() {
	
	        if(userloggedin == "" || userloggedin == null){					//this will be required when database is empty
				userloggedin = "anonymous";
				localStorage.setItem("userloggedin", userloggedin);
			}
			
			if(userloggedin == "anonymous"){	//if logged in as anonymous then don't display subheader
				//$('#subheader').hide();		 
				window.location.replace("mylogin.html");
			}else{							//else show the subheader, and hide login and signup buttons	
				$('#subheader').show();			
				$("#loginoptionmenu").hide();
				$("#signupoptionmenu").hide();
				
				//show "username"
				var salutation = document.getElementById("salutation");
				var span = document.createElement("span");
				
				span.innerHTML = userloggedin;
				salutation.appendChild(span);
				
			}
			
			//end of checking if user is logged in or not
}