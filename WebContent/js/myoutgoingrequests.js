var myOutGoingRequests = angular.module('myApp');

myOutGoingRequests.controller('myOutGoingRequestsCtrl', ['$scope', 
														'userFactory', 
														'bannerService', 
														'modalService', 
														function($scope, 
														userFactory, 
														bannerService, 
														modalService){
    
    localStorage.setItem("prevPage","myapp.html#/myoutgoingrequests");
    
    // to get all out going requests
    var itemNextRequestId = 0;
    
    // to initialise the requests array
    $scope.requests = [];
    
    var initialPopulate = function(){
        
        $scope.requests = [];
        
        itemNextRequestId = 0;
        
        getOutRequests(itemNextRequestId);
        
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
            url: '/flsv2/GetRequestsByUser',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "JSON",
            success: function(response) {
                if(response.title){
                    $scope.$apply(function(){
                        $scope.requests.unshift(response);
                    });
                    itemNextRequestId = response.request_id;
                    getOutRequests(itemNextRequestId);
                }
            },
            error: function() {
            }
        });
    }
    
    // populate the requests list initally
    initialPopulate();
    
    $scope.deleteRequest = function(requestId, index){
        modalService.showModal({}, {bodyText: "Are you sure you want to delete this request?",actionButtonText: 'YES'}).then(
            function(result){
                if(requestId === '')
                    requestId = 0;
                
                var req = {
                    request_Id: requestId,
                    userId: userFactory.user
                };

                deleteRequest(req, index);
            },function(){});
    }
    
    var deleteRequest = function(req, index){
        $.ajax({
            url: '/flsv2/DeleteRequest',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.returnCode == 0){
					bannerService.updatebannerMessage("Request Deleted successfully","");
                    $("html, body").animate({ scrollTop: 0 }, "slow");
                    initialPopulate();
                }else{
					modalService.showModal({}, {bodyText: "Some Error occured please try again", showCancel:false, actionButtonText: 'OK'}).then(
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