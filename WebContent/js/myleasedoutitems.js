var myLeasedOutItemsApp = angular.module('myApp');

myLeasedOutItemsApp.controller('myLeasedOutItemsCtrl', ['$scope', 'userFactory', 'modalService', function($scope, userFactory, modalService){
    
    localStorage.setItem("prevPage","myapp.html#/myleasedoutitems");
    
    $scope.leases = [];
    
    var initialPopulate = function(){
        getLeasedOutItems(0);
    }
    
    var getLeasedOutItems = function(cookie){
        
        req = {
            cookie: cookie,
            leaseUserId: userFactory.user,
            leaseReqUserId: ""
        }
        
        getLeasedOutItemsSend(req);
        
    }
    
    var getLeasedOutItemsSend = function(req){
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
                    getLeasedOutItems(response.cookie);
                }
            },
		
			error: function() {}
		});
    }
    
    $scope.closeLease = function(ItemId, RequestorUserId, index){
        modalService.showModal({}, {bodyText: "Are you sure you want to close the lease on the Item?",actionButtonText: 'YES'}).then(
            function(result){
                req = {
                    itemId: ItemId,
                    userId: userFactory.user,
                    reqUserId: RequestorUserId,
                    flag: "close"
                };
                closeLeaseSend(req, index);
            },function(){});
    }
    
    var closeLeaseSend = function(req, index){
        $.ajax({
            url: '/flsv2/RenewLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                
                modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'OK'}).then(
                function(result){
                    $scope.leases.splice(index, 1);
                },function(){});
                
            },
            error: function() {
            }
        });
    }
    
    $scope.renewLease = function(ItemId, RequestorUserId){
        modalService.showModal({}, {bodyText: "Are you sure you want to renew the lease on the Item?",actionButtonText: 'YES'}).then(
            function(result){
                req = {
                    itemId: ItemId,
                    userId: userFactory.user,
                    reqUserId: RequestorUserId,
                    flag: "renew"
                }
                renewLeaseSend(req);
            },function(){});
    }
    
    var renewLeaseSend = function(req){
        $.ajax({
            url: '/flsv2/RenewLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'OK'}).then(
                function(result){
                    $scope.leases = [];
                    initialPopulate();
                },function(){});
            },
            error: function() {
            }
        });	
    }
    
    initialPopulate();
    
    $scope.showItemDetails = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
    
}]);