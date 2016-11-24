var myOutGoingRequests = angular.module('myApp');

myOutGoingRequests.controller('myOutGoingRequestsCtrl', ['$scope', 
														'userFactory', 
														'bannerService', 
														'modalService',
                                                        'logoutService',
														function($scope, 
														userFactory, 
														bannerService, 
														modalService,
                                                        logoutService){
    
    localStorage.setItem("prevPage","myapp.html#/myoutgoingrequests");
    
    var offset = 0;
    
    // to initialise the requests array
    $scope.requests = [];
    
    var initialPopulate = function(){
        
        $scope.requests = [];
        
        offset = 0;
        
        getOutRequests(offset);
        
    }
    
    var getOutRequests = function(token){
        
        if(token == '' || token == undefined)
            token = 0;
        
        var req = {
            userId: userFactory.user,
            cookie: token
        }
        
        displayOutRequests(req);
    }
    
    var displayOutRequests = function(req){
        $.ajax({
            url: '/GetRequestsByUser',
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
                    
                    getOutRequests(offset);
                }
            },
            error: function() {
            }
        });
    }
    
    // populate the requests list initally
    initialPopulate();
    
    $scope.deleteRequest = function(requestId, index){
        modalService.showModal({}, {bodyText: "Are you sure you want to delete this request?",actionButtonText: 'Yes'}).then(
            function(result){
                if(requestId === '')
                    requestId = 0;
                
                var req = {
                    request_Id: requestId,
                    userId: userFactory.user,
                    accessToken: userFactory.userAccessToken
                };

                deleteRequest(req, index);
            },function(){});
    }
    
    var deleteRequest = function(req, index){
        $.ajax({
            url: '/DeleteRequest',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.code == 0){
					bannerService.updatebannerMessage(response.message,"");
                    $("html, body").animate({ scrollTop: 0 }, "slow");
                    initialPopulate();
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