function logInCheck() {
	
	if(userloggedin == "" || userloggedin == null){					//this will be required when database is empty
				userloggedin = "anonymous";
				localStorage.setItem("userloggedin", userloggedin);
			}
			
			// Code to shown Admin button. To be added later.(Copied from index.html)
			/*
			if(userloggedin == "frrndlease@greylabs.org"){		//if admin is logged in(i.e. frrndlease@greylabs.org)
				$("#admin").show();
				$('#subheader').show();			
				$("#loginoptionmenu").hide();
				$("#signupoptionmenu").hide();
			}else if(userloggedin == "anonymous"){	//if logged in as anonymous then don't display subheader
				$('#subheader').hide();
				$("#admin").hide();	
			}else{							//else show the subheader, and hide login and signup buttons	
				$("#admin").hide();
				$('#subheader').show();			
				$("#loginoptionmenu").hide();
				$("#signupoptionmenu").hide();
				
				//show "username"
				var salutation = document.getElementById("salutation");
				var span = document.createElement("span");
				span.style.color = "white";
				span.innerHTML = userloggedin;
				salutation.appendChild(span);
			}
			*/
			// Code to add Admin button ends here.
			
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