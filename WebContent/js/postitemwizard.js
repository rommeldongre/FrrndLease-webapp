var postItemWizardApp = angular.module('myApp');

postItemWizardApp.controller('postItemWizardCtrl', ['$scope', 'modalService', 'userFactory', 'eventsCount', function($scope, modalService, userFactory, eventsCount){
    $scope.steps = [
        {
            templateUrl: 'wizardstep1.html',
            title: 'Post your first item to earn 10 credits'
        },
        {
            templateUrl: 'wizardstep2.html',
            title: 'Share this with friends to earn xx credits'
        },
        {
            templateUrl: 'wizardstep3.html',
            title: 'Invite friends with your refferal to earn xx credits'
        }
    ];
    
    $scope.posted = false;
    $scope.shared = false;
    $scope.invited = false;
    
    var userId = userFactory.user;
    var userAccessToken = userFactory.userAccessToken;
    
    $scope.item = {};
    
    $scope.categories = [];
    
    var populateCategory = function(id){
        var req = {
            operation:"getNext",
            token: id
        }
        displayCategory(req);
    }
    
    var displayCategory = function(req){
        $.ajax({
            url: '/flsv2/GetCategoryList',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    $scope.$apply(function(){
                        $scope.categories.push(JSON.parse(response.Message).catName);
                    });
                    populateCategory(response.Id);
                }
                else{
                    //all categories are loaded
                }
            },
            error:function() {}
        });
    }
    
    // called on the page load
    populateCategory('');
    
    $scope.categorySelected = function(c){
        $scope.item.category = c;
    }
    
    //beginning of image display
    $scope.uploadImage = function(file){
        EXIF.getData(file, function(){
            exif = EXIF.getAllTags(this);
            picOrientation = exif.Orientation;
		});
        
        var reader = new FileReader();
        reader.onload = function(event) {
            loadImage(reader.result,
                function (canvas) {
                    $scope.$apply(function() {
                        $scope.item.image = canvas.toDataURL();
                    });
                },
                {
                    maxWidth: 300,
                    maxHeight: 300,
                    canvas: true,
                    orientation: picOrientation
                }
            );
        }
        reader.readAsDataURL(file);
    }		
	//end of image display
    
    $scope.postItem = function(){
        
        var item_title = $scope.item.title;
        if(item_title == '')
            item_title = null;

        var req = {
            id: 0,
            title: item_title,
            description: "",
            category: $scope.item.category,
            userId: userId,
            leaseValue: 1000,
            leaseTerm: 'Month',
            status: "InStore",
            image: $scope.item.image,
            accessToken: userAccessToken
        }
        
        modalService.showModal({}, {bodyText: 'Are you sure you want to Post this Item?'}).then(function(result){
            $.ajax({
                url: '/flsv2/PostItem',
                type: 'post',
                data: JSON.stringify(req),
                contentType: "application/x-www-form-urlencoded",
                dataType: "json",

                success: function(response) {
                    if(response.code == 0){
                        modalService.showModal({}, {bodyText: "Your Item has been added to the friend store successfully!!", showCancel:false, actionButtonText: 'OK'}).then(
                            function(result){
                                $scope.posted = true;
                                $scope.item.uid = response.uid;
                                eventsCount.updateEventsCount();
                            },function(result){});
                    }
                },

                error: function() {
                    modalService.showModal({}, {bodyText: "Something is Wrong with the network.",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                }
            });
        },function(){});
    }
    
    $scope.shareItem = function(uid){
        var link = null;
			
        if(window.location.href.indexOf("frrndlease.com") > -1){
            link = 'http://www.frrndlease.com/ItemDetails?uid='+uid;
        }else{
            link = 'http://www.frrndlease.com/ItemDetails?uid=ripstick-wave-board-156';
            console.log('http://localhost:8080/flsv2/ItemDetails?uid='+uid);
        }
        
        FB.login(function(response) {
            // Facebook checks whether user is logged in or not and asks for credentials if not.
            // Share item listing with facebook friends using share dialog
            FB.ui({
                method: 'share',
                href: link,
            },function(response){
                var m = "";
                if (response && !response.error) {
                    m = "Item sucessfully posted on Frrndlease and shared on Facebook";
                    $scope.shared = true;
                }else{
                    m = "Item sucessfully posted on Frrndlease";
                }
                console.log(m);
            });
        }, {scope: 'email,public_profile,user_friends'});
    }
    
    var friendIdArray = [];
	var friendArray = [];
    var lastFriendId = '';
	var arrEmail = [];
	var errCount = 0, len = 0,count=1;
	var reasonForAddFriend = null, googleFriendsCounter = 0, counter = 0,checkcounter = 0;
	var clientId = '349857239428-jtd6tn19skoc9ltdr6tsrbsbecv5uhh3.apps.googleusercontent.com';
	var apiKey = 'API Code';
	var scopes = 'https://www.googleapis.com/auth/contacts.readonly';
	
	var load_Gapi = function(){						//for google
		gapi.load('auth2', function() {
			gapi.auth2.init();
		});
	}
    
	load_Gapi();
    
    $scope.directImport = function(){
        modalService.showModal({}, {submitting: true, labelText: 'Invite Friends by Email,comma separated. example1@xyz.com, example2@abc.net', actionButtonText: 'Submit'}).then(function(result){
			var value = result;
			reasonForAddFriend = "importEmail";
			arrEmail = value.split(",");
			len = arrEmail.length;
			if(value != '' && value != ' '){
				if(len<=20){
					directImport_continue();
				}else{
					modalService.showModal({}, {bodyText: "Sorry, Please enter emails less than or equal to 20" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){}, function(){});
				}
			}
            eventsCount.updateEventsCount();
        }, function(){});
    }
	
	var directImport_continue = function(){
        if(count == len){
            var validEmail = len-errCount;
            modalService.showModal({}, {bodyText: "Success, Number of email(s) imported: "+validEmail+" ,Number of Invalid email(s): "+errCount ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                eventsCount.updateEventsCount();
                $scope.invited = true;
            }, function(){});
        }else{
            var isValid = checkEmailValidity(arrEmail[count]);
            if(checkEmailValidity(arrEmail[count])){
                addFriendSetValues('-', '-', arrEmail[count], userFactory.user);
            }else{
                errCount = errCount+1;
                count++;
                directImport_continue();
            }
        }	
	}
    
	$scope.importfb = function(){
        FB.login(function(response) {
            var ref_code = localStorage.getItem("userReferralCode");
            // handle the response
			// check whether user is logged in or not and ask for credentials if not.
            // send message to facebook friends using send request dialog
            FB.ui({
                method: 'send',
                link: 'http://www.frrndlease.com/index.html?ref_token='+ref_code,
            },function(response){
                if (response && !response.error) {
                    //check 'response' to see if call was successful
                    modalService.showModal({}, {bodyText: "Success, Message to Facebook Friend(s) sent" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                        eventsCount.updateEventsCount();
                        $scope.invited = true;
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
		gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: false}, handleAuthorization);
        //calls handleAuthorization()
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
				
            if(ischecked){
                name = $("#name"+counter).text();
                mobile = $("#mobile"+counter).text();
                email = $("#email"+counter).text();
                checkcounter++;
					
                addFriendSetValues(name, mobile, email, userFactory.user);
            }else{
                add_checked_friends_continued();		//basically just increment and call the same function again
            }
        }else{
            $('#myPleaseWait').modal('hide');
            modalService.showModal({}, {bodyText: "Success, Number of Friends Imported : "+checkcounter ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                eventsCount.updateEventsCount();
                $scope.invited = true;
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
        var friendName =null, friendEmail =null, friendMobile=0;
        
        friendName = name;
		friendEmail = email;
		friendMobile = mobile;
		userId = user;
		code = localStorage.getItem("userReferralCode");
		
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
			userId: user,
			referralCode: code
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
					count++;
					if(count == len){
                        var validEmail = len-errCount;
                        modalService.showModal({}, {bodyText: "Success, Number of email(s) imported: "+validEmail+" ,Number of Invalid email(s): "+errCount ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                            eventsCount.updateEventsCount();
                            $scope.invited = true;
                        }, function(){});
                    }else{
                        directImport_continue();
                    }
				}else if(reasonForAddFriend == "importGoogle"){
					add_checked_friends_continued();
				}
            },
            error: function() {
                console.log("Invalid Entry");
            }
        });
    }
    
}]);