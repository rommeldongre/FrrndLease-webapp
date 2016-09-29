var myInComingRequests = angular.module('myApp');

myInComingRequests.controller('myInComingRequestsCtrl', ['$scope', 
														'userFactory', 
														'bannerService', 
														'modalService', 
														function($scope, 
														userFactory, 
														bannerService,
														modalService){
  
    localStorage.setItem("prevPage","myapp.html#/myincomingrequests");
    
    var itemNextId = "0";
    
    // to get all the incoming requests
    var itemNextRequestId = "0";
    
    // to initialise the requests array
    $scope.requests = [];
    
    var initialPopulate = function(){
        
        $scope.requests = [];
        
        itemNextRequestId = 0;
        
        getInRequests(itemNextRequestId);
        
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
            url: '/flsv2/GetRequestsPlus',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "JSON",
            success: function(response) {
                if(response.title){
                    itemNextRequestId = response.requestId;
                    itemNextId = response.requestItemId;
                    $scope.$apply(function(){
                        $scope.requests.unshift(response);
                    });
                    
                    getInRequests(itemNextRequestId);
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
            url: '/flsv2/GrantLease',
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
            url: '/flsv2/RejectRequest',
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
    
}]);