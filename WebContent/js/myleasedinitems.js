var myLeasedInItemsApp = angular.module('myApp');

myLeasedInItemsApp.controller('myLeasedInItemsCtrl', ['$scope', 'userFactory', function($scope, userFactory){

    localStorage.setItem("prevPage","myapp.html#/myleasedinitems");
    
    $scope.leases = [];
    
    var initialPopulate = function(){
        getLeasedInItems(0);
    }
    
    var getLeasedInItems = function(cookie){
        
        req = {
            cookie: cookie,
            leaseUserId: "",
            leaseReqUserId: userFactory.user
        }
        
        getLeasedInItemsSend(req);
        
    }
    
    var getLeasedInItemsSend = function(req){
        $.ajax({
			url: '/flsv2/GetLeasesByX',
			type: 'post',
			data: JSON.stringify(req),
			contentType:"application/json",
			dataType:"json",
			
			success: function(response){
                if(response.code == 0){
                    $scope.$apply(function(){
                        $scope.leases.unshift(response);
                    });
                    getLeasedInItems(response.cookie);
                }
            },
		
			error: function() {}
		});
    }
    
    initialPopulate();
    
    $scope.showItemDetails = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
}]);