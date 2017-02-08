var itemDetailsApp = angular.module('itemDetailsApp', ['headerApp', 'footerApp']);

itemDetailsApp.controller('itemDetailsCtrl', ['$scope',
											'$window', 
											'$http', 
											'userFactory', 
											'bannerService', 
											'modalService',
                                            'logoutService',
                                            'eventsCount',
											function($scope, 
											$window, 
											$http, 
											userFactory, 
											bannerService, 
											modalService,
                                            logoutService,
                                            eventsCount){
    
    var user = localStorage.getItem("userloggedin");
    
    $scope.item = {};
    
    $scope.item.primaryImageLink = $window.primaryImageLink;
    $scope.item.title = $window.title;
	$scope.item.category = $window.category;
	$scope.item.leaseValue = $window.leaseValue;
	$scope.item.leaseTerm = $window.leaseTerm;
                                                
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
                                bannerService.updatebannerMessage(response.Message,"/index.html");
                                $("html, body").animate({ scrollTop: 0 }, "slow");
                            }else{
                                modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                                    window.location.replace("index.html");
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
        
        modalService.showModal({}, {bodyText: 'Are you sure you want to add this Item to your Wishlist?'}).then(
            function(result){
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
            },
            function(){

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
							bannerService.updatebannerMessage(response.Message,"/index.html");
							$("html, body").animate({ scrollTop: 0 }, "slow");
						}else{
							modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
								window.location.replace("index.html");
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
                    $("html, body").animate({ scrollTop: 0 }, "slow");
					
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
			modalService.showModal({}, {bodyText: "Functionality not supported on Localhost" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						}, function(){});
        }
    }
    
}]);
