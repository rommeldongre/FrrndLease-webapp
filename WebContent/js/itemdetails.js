var itemDetailsApp = angular.module('itemDetailsApp', ['headerApp', 'footerApp']);

itemDetailsApp.controller('itemDetailsCtrl', ['$scope',
											'$window', 
											'$http',											
											'userFactory', 
											'scrollService',
											'bannerService', 
											'modalService',
                                            'logoutService',
                                            'eventsCount',
                                            'searchService',
											'$filter',
											function($scope, 
											$window, 
											$http,
											userFactory, 
											scrollService,
											bannerService, 
											modalService,
                                            logoutService,
                                            eventsCount,
                                            searchService,
											$filter){
    
    var user = localStorage.getItem("userloggedin");
    
    $scope.item = {};
    
    $scope.item.primaryImageLink = $window.primaryImageLink;
    $scope.item.title = $window.title;
	$scope.item.category = $window.category;
	$scope.item.leaseValue = $window.leaseValue;
	$scope.item.leaseTerm = $window.leaseTerm;
    $scope.item.userName = $window.userName;
                                                
    if($window.imageLinks != '' && $window.imageLinks != null)
        $scope.item.imageLinks = $window.imageLinks.split(",");
    else
        $scope.item.imageLinks = [];
                                   
    if($scope.item.primaryImageLink != null){
        $scope.item.imageLinks.unshift($scope.item.primaryImageLink);
    }
                                                
    $scope.selectedImage = function(index){
        $scope.item.primaryImageLink = $scope.item.imageLinks[index];
    }
                                                
    $scope.item_id = $window.item_id;
    $scope.user_id = $window.userId;
	$scope.uid     = $window.uid;
	        
    // checking if the response code is 0 or not to show error div of itemdetails div
    if($window.code != 0){
        $scope.showError = true;
    }
    else{
        $scope.showError = false;
    }
    
    // checking if the loggedIn user matches with the items userId
    if(userFactory.user == $window.userId){
        $scope.userMatch = true;
    }else{
        $scope.userMatch = false;
    }
    
	$scope.googleNumbers = false;
	$scope.addFriend= true;
	
    var getItemsRating = function(){
        
        var req = {
            itemId:$scope.item_id,
            fromDate: null
        }
        
        $.ajax({
            url: '/GetItemRating',
            type: 'post',
            data: JSON.stringify(req),
			contentType:"application/json",
			dataType:"json",
            success: function(response){
				if(response.code == 0){
                    $scope.$apply(function(){
                        $scope.raters = response.totalRaters;
                        $scope.rating = Math.round(response.totalRating/$scope.raters);
                    });
				}
            },
            error: function(){
            }
	
        });
    }
	    
    getItemsRating();
    
    $scope.requestItem = function(){
        modalService.showModal({}, {messaging: true, bodyText: 'Create A Request. Write a message to Item\'s Owner', actionButtonText: 'Send'}).then(
            function(result){
                if (userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous"){
					$('#loginModal').modal('show');
                }else{
                    var message = result;
            
                    if(message == "" || message == undefined)
                        message = null;
                    
                    $.ajax({ 
                        url: '/RequestItem',
                        type: 'post',
                        data: {req : JSON.stringify({itemId:$scope.item_id,userId:userFactory.user,message:message})},
                        contentType: "application/x-www-form-urlencoded",
                        dataType: "json",
                        success:function(response){
                            if(response.Code== 0){
                                bannerService.updatebannerMessage(response.Message,"/myapp.html");
								scrollService.scrollToDiv("navbar");
                            }else{
                                modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                                    if(response.code == 236)
                                        window.location.replace("myapp.html#/myprofile");
                                    else
                                        window.location.replace("myapp.html");
                                },function(){});
                           }
                        },
                        error: function(){
                            modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                        }
                    });
                }
            }, 
            function(){

            });
    }
	
	$scope.wishItem = function(){
        
        var item_title = $scope.item.title;
        if(item_title == '')
            item_title = null;
        
        var item_id = $scope.item_id;
        if(item_id == '')
            item_id = 0;
        
        var item_description = $scope.item.description;
        if (item_description == '') 
		  item_description = null;
        
        var item_category = $scope.item.category;
        if (item_category == '' || item_category == 'Category') 
		  item_category = null;
        
        var item_user_id = userFactory.user;
        if (item_user_id == '') 
		  item_user_id = "anonymous";
        
        var item_lease_value = $scope.item.leaseValue;
        if (item_lease_value == '') 
		  item_lease_value = 0;
        
        var item_lease_term = $scope.item.leaseTerm;
        if (item_lease_term == '' || item_lease_term == 'Lease Term') 
		  item_lease_term = null;
	
		var item_status = "Wished";
        
        req = {id:item_id,
                        title:item_title,
                        description:item_description,
                        category: item_category,
                        userId: item_user_id,
                        leaseValue: item_lease_value,
                        leaseTerm: item_lease_term,
						status: item_status,
                        image: $scope.item.primaryImageLink};
        
				if (userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
					$('#loginModal').modal('show');
				else
					$.ajax({ url: '/WishItem',
							type: 'post',
							data: {req : JSON.stringify(req)},
							contentType: "application/x-www-form-urlencoded",
							dataType: "json",
							success:function(response){
								modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
									 window.location.replace("myapp.html#/mywishlists");
								},function(){});
							},
							error: function(){
								modalService.showModal({}, {bodyText: "Not Working",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
							}
						});
    }
    
    $scope.editItem = function(){
        window.location.replace("myapp.html#/edititem/"+$scope.item_id);
    }
    
    $scope.deleteItem = function(){
        modalService.showModal({}, {bodyText: 'Are you sure you want to delete the Item?'}).then(
            function(result){
                $.ajax({
                    url: '/DeletePosting',
                    type: 'get',
                    data: {
                        req: JSON.stringify({id:$scope.item_id,userId:userFactory.user})
                    },
                    contentType: "application/json",
                    dataType: "json",
                    success: function (response) {
                        if(response.Code == 0){
							bannerService.updatebannerMessage(response.Message,"/myapp.html");
							scrollService.scrollToDiv("navbar");
						}else{
							modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
								window.location.replace("myapp.html");
							},function(){});
						}
                    },
                    error: function () {
                        console.log("Error response");
                    }
                });
            }, 
            function(){

            });
    }
    
    var lastOffset = 0;
    
    $scope.showItemTimeline = function(){
		$("#openBtn_item").click();
		$scope.showNext = true;
		getItemTimeline(lastOffset);
	}
    
	var getItemTimeline = function(Offset){
		var req = {
			itemId : $scope.item_id,
			cookie: Offset,
			limit: 3
		}
		
		getItemTimelineSend(req);
	}
	
	var getItemTimelineSend = function(req){
		$.ajax({
            url: '/GetItemTimeline',
            type: 'post',
            data: JSON.stringify(req),
			contentType:"application/json",
			dataType:"json",
            success: function(response){
				if(response.code == 0){
                if(lastOffset == 0){
					$scope.$apply(function(){
						$scope.timelineArray = [response.resList];
					});
                    getItemTimeline(response.cookie);
                    }else{
						$scope.$apply(function(){
						  $scope.timelineArray.push(response.resList);
						});
                    }
                    lastOffset = response.cookie;
				}else{
					$scope.showNext = false;
                }
            },
            error: function(){
            }
	
        });
	}
	
	// called when Show More Items Timeline button is clicked
    $scope.loadNextItemTimeline = function(){
        getItemTimeline(lastOffset);
    }
    
    $scope.sendItemMessage = function(){
		modalService.showModal({}, {messaging: true, bodyText: 'Message Item\'s Owner', actionButtonText: 'Send'}).then(function(result){
            
            var message = result;
            
            if(message == "" || message == undefined)
                message = null;
            
            var req = {
                userId: userFactory.user,
				accessToken: userFactory.userAccessToken,
				from: userFactory.user,
				to: $scope.user_id,
                subject: "ITEM",
                message: message,
				itemId: parseInt($scope.item_id)
            }
            
            sendMessage(req);
            
        }, function(){});
    }
	
	var sendMessage = function(req){
		
		$.ajax({
			url: '/SendMessage',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code==0){
					bannerService.updatebannerMessage("Message Sent!!");
					scrollService.scrollToDiv("navbar");
					
				}else{
					modalService.showModal({}, {bodyText: "Error while sending message, please try again later" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						}, function(){});
				}
			},
		
			error: function() {
				console.log("Not able to send message");
			}
		});
	}
    
	$scope.checkflsState = function(){
		if($scope.flsState){ //If it is checked
			$scope.flsStatus= true;
			
		}else{
			$scope.flsStatus= false;
		}
		if($scope.googleState){ //If it is checked
			$scope.googleStatus= true;
			
		}else{
			$scope.googleStatus= false;
		}
		if($scope.addFriendState){ //If it is checked
			$scope.addFriend= true;
			
		}else{
			$scope.addFriend= false;
		}		
	}
    $scope.shareItem = function(){
        var link = null;

        if(window.location.href.indexOf("frrndlease.com") > -1){
            link = 'https://www.frrndlease.com/ItemDetails?uid='+$scope.uid;
			
			FB.login(function(response) {
				// Facebook checks whether user is logged in or not and asks for credentials if not.
				// Share item listing with facebook friends using share dialog
				FB.ui({
					method: 'share',
					href: link,
				},function(response){
					var m = "";
					if (response && !response.error_code) {
                        $scope.shared = true;
                        userFactory.buyCredits("shared@10", 0, null).then(
                            function(response){
                                if(response.data.code == 400)
                                    logoutService.logout();
                                if(response.data.code == 0){
                                    modalService.showModal({}, {bodyText: "You have successfully shared this item on facebook!!" ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
                                        eventsCount.updateEventsCount();
                                    }, function(){});
                                }
                            }, 
                            function(error){
                                console.log("Not able to buy credits internally.");
                            }
                        );
					}
				});
			}, {scope: 'email,public_profile,user_friends'});
        }else{
			modalService.showModal({}, {bodyText: "Functionality not supported on Localhost" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){}, function(){});
        }
    }

    $scope.friendsList = function () {
        localStorage.setItem("searchText", $scope.item.userName);
        window.location.replace("myapp.html#/");
    }
	
	$scope.shareItem = function(){
		if (userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous"){
			$('#loginModal').modal('show');
        }else{
			$("#openBtn_itemShare").click();
		}	
	}
	
	$scope.shareWithfriends = function(){
		
		var reqListArray = [];
		if($scope.shareMessage === undefined || $scope.shareMessage== null || $scope.shareMessage==""){
			$scope.shareMessage = "";
		}
		
		var selectedContacts = $filter('filter')($scope.contacts, {selected:true});
        for(var i in selectedContacts){
			var reqList = {};
			reqList["name"] = selectedContacts[i].name;
			reqList["number"] = selectedContacts[i].number;
			reqList["email"] = selectedContacts[i].email;
			reqListArray.push(reqList);
        }
		
		$("#openBtn_itemShare").click();
		
		var req = {
				userId: userFactory.user,
				userName:userFactory.userName,
				accessToken: userFactory.userAccessToken,
				itemId: parseInt($scope.item_id),
				itemTitle: $scope.item.title,
				itemUid: $scope.uid,
				itemOwnerId: $scope.user_id,
				shareMessage: $scope.shareMessage,
				friendsStatus: $scope.friendsCount,
				flsStatus: $scope.flsStatus,
				googleStatus: $scope.googleStatus,
				addFriendStatus: $scope.addFriend,
				friendNumbersLength: reqListArray.length,
				friendNumbers: reqListArray
			}
		sendshareWithfriends(req);
	}
	
	var sendshareWithfriends = function(req){
		
		$.ajax({
			url: '/ShareItem',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code==0){
					scrollService.scrollToDiv("navbar");
					bannerService.updatebannerMessage(response.message,"");
					cancel_share();
				}else if(response.code==201){
					modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						}, function(){})
				}else{
					modalService.showModal({}, {bodyText: "Error while Sharing Item, please try again later" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						}, function(){});
				}
			},
		
			error: function() {
				console.log("Not able to send message");
			}
		});
	}
	
	var clientId = '1074096639539-cect2rfj254j3q1i5fo7lmbfhm93jg34.apps.googleusercontent.com';
	var scopes = 'https://www.googleapis.com/auth/contacts.readonly';
	
	var load_Gapi = function(){						//for google
		gapi.load('auth2', function() {
			gapi.auth2.init();
		});
	}
    
	load_Gapi();
	
	$scope.contacts = [];
	
	$scope.importgoogle = function(){
		$scope.googleNumbers = true;
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
                        if(arr[i].gd$phoneNumber){
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
							
							$scope.$apply(function(){
								if(contact.number!='-'){
									$scope.contacts.push(contact);
									
								}
							});
                        }
                        
                    }
					
                }
            );
        }	
	}
	
	$scope.cancel_share = function () {
		cancel_share();
        }
		
	var cancel_share = function(){
			$scope.contacts = [];
			$scope.flsStatus= false;
			$scope.googleState = false;
			$scope.googleStatus = false;
			$scope.googleNumbers = false;
			$scope.shareMessage =''; 
	}
    
}]);
