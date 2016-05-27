var myOutGoingRequests = angular.module('myApp');

myOutGoingRequests.controller('myOutGoingRequestsCtrl', ['$scope', 'userFactory', 'modalService', function($scope, userFactory, modalService){
    
    // to get all out going requests
    var itemNextRequestId = 0;
    
    // to initialise the requests array
    $scope.requests = [];
    
    var initialPopulate = function(){
        
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
                        $scope.requests.push(response);
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
    
    $scope.deleteRequest = function(requestId){
        modalService.showModal({}, {bodyText: "Are you sure you want to delete this request?",actionButtonText: 'YES'}).then(
            function(result){
                if(requestId === '')
                    requestId = 0;
                
                var req = {
                    request_Id: requestId,
                    userId: userFactory.user
                };

                deleteRequest(req);
            },function(){});
    }
    
    var deleteRequest = function(req){
        $.ajax({
            url: '/flsv2/DeleteRequest',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.errorString == "No Error"){
                    modalService.showModal({}, {bodyText: "Request Deleted successfully", showCancel:false, actionButtonText: 'OK'}).then(
                    function(result){
                        window.location.replace('myapp.html#/myoutgoingrequests');
                    },function(){});
                }
            },
            error: function() {
            }
            });
    }
    
}]);