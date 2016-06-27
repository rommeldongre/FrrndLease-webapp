var myFriendsListApp = angular.module('myApp');

myFriendsListApp.controller('myFriendsListCtrl', ['$scope', 'userFactory', 'modalService', function($scope, userFactory, modalService){
    
    localStorage.setItem("prevPage","myapp.html#/myfriendslist");
    
    var friendIdArray = [];
    var lastFriendId = '';
    $scope.friends = [];
	var reasonForAddFriend = null, googleFriendsCounter = 0, counter = 0,checkcounter = 0;
	var clientId = '349857239428-jtd6tn19skoc9ltdr6tsrbsbecv5uhh3.apps.googleusercontent.com';
	var apiKey = 'API Code';
	var scopes = 'https://www.googleapis.com/auth/contacts.readonly';
	$("#openBtn").hide();
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    var getFriendsList = function(){
        
        var req = {
            operation: "getNext",
            id: userFactory.user,
            token: lastFriendId
        }
        
        displayFriendsList(req);
    }
	
	var load_Gapi = function(){						//for google
		gapi.load('auth2', function() {
			gapi.auth2.init();
		});
	}
    
    var displayFriendsList = function(req){
        $.ajax({
                url: '/flsv2/GetFriends',
                type:'get',
                data: {req: JSON.stringify(req)},
                contentType:"application/json",
                dataType: "json",

                success: function(response) {
                    if(response.Code == "FLS_SUCCESS") {
                        var obj = JSON.parse(response.Message);
                        
                        // to check if the friend id is email or not
                        var strIndex = obj.friendId.indexOf('@fb');
                        if(strIndex != -1)
                            obj.friendId = '-';
                        
                        if(lastFriendId == '')
                            $scope.$apply(function(){
                                $scope.friends = [obj];
                            });
                        else
                            $scope.$apply(function(){
                                $scope.friends.unshift(obj);
                            });
                             
                        lastFriendId = response.Id;
                        friendIdArray.unshift(response.Id);
                        getFriendsList();
                    }
                },

                error: function() {
                }
            });
    }
    
    getFriendsList();
	load_Gapi();
	
    $scope.directImport = function(){
        modalService.showModal({}, {submitting: true, labelText: 'Invite Friends by Email', actionButtonText: 'Submit'}).then(function(result){
			var value = result;
			reasonForAddFriend = "importEmail";
			var arrEmail = [];
			var errCount = 0, len = 0;
			arrEmail = value.split(",");
			len = arrEmail.length;
			if(value != '' && value != ' '){
				if(len<=20){
					for(i=0;i<len;i++){
						var isValid = checkEmailValidity(arrEmail[i]);
						if(checkEmailValidity(arrEmail[i])){
								addFriendSetValues('-', '-', arrEmail[i], userFactory.user);
							}else{
								errCount = errCount+1;
							}
						
					}
					if(errCount!=0){
						var validEmail = len-errCount;
						modalService.showModal({}, {bodyText: "Success, Number of email(s) imported: "+validEmail+" ,Number of Invalid email(s): "+errCount ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						}, function(){});
					}
				}else{
					modalService.showModal({}, {bodyText: "Sorry, Please enter emails less than or equal to 20" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						}, function(){});
				}
			}
        }, function(){});
    }
	
	$scope.importfb = function(){
		
		FB.login(function(response) {
				// handle the response
				
				// check whether user is logged in or not and ask for credentials if not.

				// send message to facebook friends using send request dialog
				FB.ui({
					method: 'send',
					link: 'http://www.frrndlease.com/',
				},function(response){
					if (response && !response.error) {
						//check 'response' to see if call was successful
						modalService.showModal({}, {bodyText: "Success, Message to Facebook Friend(s) sent" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						}, function(){});
						}
				});
            }, {scope: 'email,public_profile,user_friends'});
	}
	
	$scope.importgoogle = function(){
		window.setTimeout(authorize);		//calls authorize()
		$("#openBtn").click();	
	}
	
	var authorize = function(){
		gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: false}, handleAuthorization);		//calls handleAuthorization()
	}
	
	var handleAuthorization = function(authorizationResult){
		if (authorizationResult && !authorizationResult.error) {
				$.get("https://www.google.com/m8/feeds/contacts/default/thin?alt=json&access_token=" + authorizationResult.access_token + "&max-results=500&v=3.0",
				function(response){
					//function for length of entry array  (for number of contacts)
					var getLength = function(obj) {
						var i = 0, key;
						for (key in obj) {
							if (obj.hasOwnProperty(key)){
								i++;
							}
						}
						return i;
					};
					var n = getLength(response.feed.entry);
					var tp, user_email='',number='',email = '', name='';
					
					googleFriendsCounter = n;
					
					for(i=0;i<n;i++){
					    if(response.feed.entry[i].gd$email){
						tp = i+1;
						
						user_email = JSON.stringify(response.feed.author[0].email.$t);
						user_email = user_email.substring(1,user_email.length-1);
						
						try{
							number = JSON.stringify(response.feed.entry[i].gd$phoneNumber[0].$t);
							number = number.substring(1,number.length-1);
						}catch(Exception){
							//number = "Number do not Exist for Friend " + tp;
							number = "-";
						}
						try{
							name = JSON.stringify(response.feed.entry[i].gd$name.gd$fullName.$t);
							name =  name.substring(1,name.length-1);
						}catch (Exception){ 
							//name = "Name do not Exist for Friend " +  tp;	 	
							name = "-";
						}
						try{
							email =  JSON.stringify(response.feed.entry[i].gd$email[0].address);
							email = email.substring(1,email.length-1);
						}catch(Exception){
							//email = "Email do not Exist for Friend " +  tp;	
							email = "-";
						}
						addFriendOptionToPage(email,name,number,user_email,i); 
						}
					}	
				});
			}	
	}
	
	var addFriendOptionToPage = function(email,name,number,user_email,i){
		var table = document.getElementById("friendsoptionstable");
		var row = table.insertRow(1);
		var cell0 = row.insertCell(0);
		var cell1 = row.insertCell(1);
		var cell2 = row.insertCell(2);
		var cell3 = row.insertCell(3);
		
		var inputgp = document.createElement("div");
		var checkbox = document.createElement("input");
		inputgp.className = "input-group";
		checkbox.setAttribute("type","checkbox");
		inputgp.appendChild(checkbox);
		checkbox.id = i;
		
		cell0.innerHTML = email;
		cell1.innerHTML = name;
		cell2.innerHTML = number;
		cell3.appendChild(inputgp);
		
		cell0.id = "email"+i;
		cell1.id = "name"+i;
		cell2.id = "mobile"+i;
		
		cell0.className = "tablecellName";
		cell1.className = "tablecellMobile";
		cell2.className = "tablecellEmail";
		cell3.className = "checkboxes";
	}
	
	$scope.add_checked_friends = function(){
			if(counter==0){
			process_dialog("Adding Gmail friends Please Wait");
			$('#myModalTable').modal('toggle');
			}
			reasonForAddFriend = "importGoogle";
			if(counter<googleFriendsCounter){
				var ischecked = $("#"+counter).is(":checked");
				console.log(counter+": "+ischecked);
				
				if(ischecked){
					name = $("#name"+counter).text();
					mobile = $("#mobile"+counter).text();
					email = $("#email"+counter).text();
					console.log("name: "+name+" mobile: "+mobile+" email: "+email);
					checkcounter++;
					
					addFriendSetValues(name, mobile, email, userFactory.user);
				}else{
					add_checked_friends_continued();		//basically just increment and call the same function again
				}
			}else{
				$('#myPleaseWait').modal('hide');
				modalService.showModal({}, {bodyText: "Success, Number of Friends Imported : "+checkcounter ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
				}, function(){});
			} 
	}
	
	var add_checked_friends_continued = function(email){
		counter++;
		$scope.add_checked_friends();
	}
	var checkEmailValidity = function(email){
		var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
			return re.test(email);
	}
	
	var addFriendSetValues = function(name, mobile, email, user){
	
	var friendName =null,friendEmail =null,friendMobile=0,
	
		friendName = name;
		friendEmail = email;
		friendMobile = mobile;
		userId = user;
		
		if(friendName == '')
			friendName = null;
		if(friendEmail == '')
			friendEmail = null;
		if(friendMobile == '')
			friendMobile = 0;
		
		var req = {
			id: friendEmail,
			fullName: friendName,
			mobile: friendMobile,
			userId: user
		};
		
		addFriendSend(req);	
	}
	
	var addFriendSend = function(req){
		$.ajax({
            url: '/flsv2/AddFriend',
            type:'get',
            data: {req : JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
				if(reasonForAddFriend == "importEmail"){
					modalService.showModal({}, {bodyText: response.Message ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
					}, function(){});
				}else if(reasonForAddFriend == "importGoogle"){
					add_checked_friends_continued();
				}
            },
            error: function() {
                console.log("Invalid Entry");
            }
        });
		
	}
    $scope.addFriend = function(){
        localStorage.setItem("prevFunc", "addFriend");
        window.location.replace("myfrienddetails.html");
    }
    
    $scope.importFriend = function(){
        window.location.replace("myimportfriend.html");
    }
    
    $scope.showFriendDetails = function(index){
        localStorage.setItem("prevFunc", "viewFriend");
        localStorage.setItem("itemToShow", friendIdArray[index]);
        window.location.replace('myfrienddetails.html');
    }
    
}]);