var postItemWizardApp = angular.module('myApp');

postItemWizardApp.controller('postItemWizardCtrl', ['$scope', 'modalService', 'userFactory', 'eventsCount', '$filter', function($scope, modalService, userFactory, eventsCount, $filter){
    
    $scope.steps = [
        {
            templateUrl: 'wizardstep1.html',
            title: 'Post an item to earn 10 credits'
        },
        {
            templateUrl: 'wizardstep2.html',
            title: 'Share this with friends (Earn 10 credits for the first time you share)'
        },
        {
            templateUrl: 'wizardstep3.html',
            title: 'Invite friends & Earn credits'
        }
    ];
    
    $scope.posted = false;
    $scope.shared = false;
    $scope.invited = false;
    
    var userId = userFactory.user;
    var userAccessToken = userFactory.userAccessToken;
    
    $scope.refferalCode = localStorage.getItem("userReferralCode");
    
    $scope.item = {};
    
    $scope.categories = [];
    
    $scope.selectedCategory = 0;
    
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
                    $scope.item.category = $scope.categories[$scope.selectedCategory];
                    populateCategory(response.Id);
                }
            },
            error:function() {}
        });
    }
    
    // called on the page load
    populateCategory('');
    
    $scope.categorySelected = function(i){
        $scope.selectedCategory = i;
        $scope.item.category = $scope.categories[i];
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
                        modalService.showModal({}, {bodyText: "Your account has been credited with 10 credits!!", showCancel:false, actionButtonText: 'OK'}).then(
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
                    userFactory.userCredits("shared@10");
                    $scope.shared = true;
                }
            });
        }, {scope: 'email,public_profile,user_friends'});
    }
    
    var testEmail = /^\w+([-+.']\ w+)*@\w+([-.]\ w+)*\.\w+([-.]\ w+)*$/;
	var count=0;
	var counter = 0;
	var clientId = '349857239428-jtd6tn19skoc9ltdr6tsrbsbecv5uhh3.apps.googleusercontent.com';
	var scopes = 'https://www.googleapis.com/auth/contacts.readonly';
	
	var load_Gapi = function(){						//for google
		gapi.load('auth2', function() {
			gapi.auth2.init();
		});
	}
    
	load_Gapi();
	
	var addFriendSetValues = function(name, mobile, email){
		
		if(email == '')
			email = null;
		if(name == '')
			name = null;
		if(mobile == '')
			mobile = 0;
		
		var req = {
			id: email,
			fullName: name,
			mobile: mobile,
			userId: userFactory.user,
			referralCode: $scope.refferalCode
		}
		
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
                if(response.Code == 'FLS_SUCCESS'){
                    count++;
                    eventsCount.updateEventsCount();
                }
            },
            error: function() {
                console.log("Invalid Entry");
            }
        });
    }
    
    $scope.directImport = function(){
        modalService.showModal({}, {submitting: true, labelText: 'Invite Friends by Email,comma separated. example1@xyz.com, example2@abc.net', actionButtonText: 'Submit'}).then(function(result){
            
			var arrEmail = result.split(",");
			var len = arrEmail.length;
            
			if(result != '' && result != ' '){
				if(len<=20){
					for(var i in arrEmail){
                        if(testEmail.test(arrEmail[i])){
                            addFriendSetValues('-', '-', arrEmail[i]);
                        }
                    }
                    if(count > 0){
                        modalService.showModal({}, {bodyText: "Successfully imported "+count+" friends.", showCancel:false, actionButtonText: 'OK'}).then(function(result){
                            eventsCount.updateEventsCount();
                            $scope.invited = true;
                            count = 0;
                        }, function(){});
                    }
				}else{
					modalService.showModal({}, {bodyText: "Sorry, Please enter emails less than or equal to 20" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){}, function(){});
				}
			}
            
            eventsCount.updateEventsCount();
            
        }, function(){});
    }
    
	$scope.importfb = function(){
        FB.login(function(response) {
            // handle the response
			// check whether user is logged in or not and ask for credentials if not.
            // send message to facebook friends using send request dialog
            FB.ui({
                method: 'send',
                link: 'http://www.frrndlease.com/index.html?ref_token='+$scope.refferalCode,
            },function(response){
                if (response && !response.error) {
                    //check 'response' to see if call was successful
                    modalService.showModal({}, {bodyText: "Invitation successfully sent to Facebook Friend(s)" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                        userFactory.userCredits("invited@10");
                        eventsCount.updateEventsCount();
                        $scope.invited = true;
                    }, function(){});
                }
            });
        }, {scope: 'email,public_profile,user_friends'});
	}
	
    $scope.contacts = [];
    
	$scope.importgoogle = function(){
		window.setTimeout(authorize);
	}
	
	var authorize = function(){
		gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: false}, handleAuthorization);
        //calls handleAuthorization()
	}
	
	var handleAuthorization = function(authorizationResult){
		if (authorizationResult && !authorizationResult.error) {
            $.get("https://www.google.com/m8/feeds/contacts/default/thin?alt=json&access_token=" + authorizationResult.access_token + "&max-results=500&v=3.0",
                function(response){
                    var arr = response.feed.entry;
                    for(var i in arr){
                        var contact = {};
                        if(arr[i].gd$email){
                            contact.user = response.feed.author[0].email.$t;

                            try{
                                contact.number = arr[i].gd$phoneNumber[0].$t;
                            }catch(Exception){
                                contact.number = "-";
                            }
                            try{
                                contact.name = arr[i].gd$name.gd$fullName.$t;
                            }catch (Exception){	
                                contact.name = "-";
                            }
                            try{
                                contact.email =  arr[i].gd$email[0].address;
                            }catch(Exception){	
                                contact.email = "-";
                            }

                            contact.selected = false;
                        }
                        $scope.$apply(function(){
                            $scope.contacts.push(contact);
                        });
                    }
                }
            );
        }	
	}
	
	$scope.inviteSelected = function(){
        var selectedContacts = $filter('filter')($scope.contacts, {selected:true});
        for(var i in selectedContacts){
            addFriendSetValues(selectedContacts[i].name, selectedContacts[i].mobile, selectedContacts[i].email);
        }
	}
    
}]);