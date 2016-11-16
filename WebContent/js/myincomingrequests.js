var myInComingRequests = angular.module('myApp');

myInComingRequests.controller('myInComingRequestsCtrl', ['$scope', 'userFactory', 'bannerService', 'modalService', function($scope, userFactory, bannerService, modalService){
    
    localStorage.setItem("prevPage","myapp.html#/myincomingrequests");
    
    var offset = 0;
    
    // to initialise the requests array
    $scope.requests = [];
    
    var initialPopulate = function(){
        
        $scope.requests = [];
        
        offset = 0;
        
        getInRequests(offset);
        
    }
    
    var getInRequests = function(token){
        
        if(token == '' || token == undefined)
            token = 0;
        
        var req = {
            userId: userFactory.user,
            cookie: token
        }
        
        displayInRequests(req);
    }
    
    var displayInRequests = function(req){
        $.ajax({
            url: '/GetRequestsPlus',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "JSON",
            success: function(response) {
                if(response.code == 0){
                    offset = response.offset;
                    $scope.$apply(function(){
                        $scope.requests.unshift(response);
                    });
                    
                    getInRequests(offset);
                }
            },
            error: function() {
            }
        });
    }
    
    // populate the requests list initally
    initialPopulate();
    
    $scope.grantLease = function(itemId, reqUserId, index){
        
        modalService.showModal({}, {bodyText: "Are you sure you want to lease the Item?",actionButtonText: 'Yes'}).then(
            function(result){
                var req = {
                    reqUserId: reqUserId,
                    itemId: itemId,
                    userId: userFactory.user,
                    accessToken: userFactory.userAccessToken
                }

                grantLeaseSend(req, index);
            },function(){});
    }
    
    var grantLeaseSend = function(req, index){
        $.ajax({
            url: '/GrantLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
				if(response.code==0){
					bannerService.updatebannerMessage(response.message,"");
					initialPopulate();
					$("html, body").animate({ scrollTop: 0 }, "slow");
				}else{
                modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'Ok'}).then(
                    function(result){
                        if(response.code == 400)
                            logoutService.logout();
                        else
                            initialPopulate();
                    },function(){});	
				}
            },
            error: function() {
            }
        });
    }
    
    $scope.rejectLease = function(itemId, reqUserId, index){
        
        modalService.showModal({}, {bodyText: "Are you sure you want to reject the Request?",actionButtonText: 'Yes'}).then(
            function(result){
                if(itemId === '')
                    itemId = null;

                if(reqUserId === '')
                    reqUserId = null;

                var req = {
                    itemId: itemId+"",
                    userId: reqUserId,
                };

                rejectLeaseSend(req, index);
            },function(){});
        
        
    }
    
    var rejectLeaseSend = function(req, index){
        $.ajax({
            url: '/RejectRequest',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
				if(response.Code == 0){
					bannerService.updatebannerMessage(response.Message,"");
					initialPopulate();
					$("html, body").animate({ scrollTop: 0 }, "slow");
				}else{
                modalService.showModal({}, {bodyText: response.Message, showCancel:false, actionButtonText: 'Ok'}).then(
                    function(result){
                        initialPopulate();
                    },function(){});
				}
            },
            error: function() {
            }
        });
    }
    
    $scope.showItemDetails = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
    
    $scope.sendMessage = function(ItemId, To){
        modalService.showModal({}, {messaging: true, bodyText: 'Message Item\'s Owner', actionButtonText: 'Send'}).then(function(result){
            
            var message = result;
            
            if(message == "" || message == undefined)
                message = null;
            
            var req = {
                userId: userFactory.user,
				accessToken: userFactory.userAccessToken,
				from: userFactory.user,
				to: To,
                subject: "ITEM",
                message: message,
				itemId: ItemId
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
    
}]);