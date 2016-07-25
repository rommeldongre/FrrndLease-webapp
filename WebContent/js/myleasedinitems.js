var myLeasedInItemsApp = angular.module('myApp');

myLeasedInItemsApp.controller('myLeasedInItemsCtrl', ['$scope', 'userFactory', 'modalService', function($scope, userFactory, modalService){

    localStorage.setItem("prevPage","myapp.html#/myleasedinitems");
    
    var leaseStatusDesc = {'LeaseReady':'This item will be picked up soon.','PickedUpOut':'This item has been picked up and will be delivered to the requestor.', 'LeaseStarted':'This item has been delivered to the requestor','LeaseEnded':'This item is ready to be picked up for returning back to the owner','PickedUpIn':'This item has been picked and will be delivered to the owner.'};

    $scope.leases = [];
    
    var initialPopulate = function(){
        getLeasedInItems(0);
    }
    
    var getLeasedInItems = function(cookie){
        
        req = {
            cookie: cookie,
            leaseUserId: "",
            leaseReqUserId: userFactory.user,
            status: 'Active'
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
                    response.statusDesc = leaseStatusDesc[response.status];
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
    
    $scope.closeLease = function(ItemId, OwnerUserId, RequestorUserId, index){
        if($scope.leases[index].status == 'LeaseStarted' || $scope.leases[index].status == 'LeaseEnded' || $scope.leases[index].status == 'LeaseReady'){
            modalService.showModal({}, {bodyText: "Are you sure you want to close the lease on the Item?",actionButtonText: 'YES'}).then(
                function(result){
                    req = {
                        itemId: ItemId,
                        userId: OwnerUserId,
                        reqUserId: RequestorUserId,
                        flag: "close"
                    };
                    closeLeaseSend(req, index);
                },function(){});
        }else{
            modalService.showModal({}, {bodyText: "Can only be closed when lease status is LeaseStarted or LeaseEnded or LeaseReady", showCancel:false, actionButtonText: 'OK'}).then(
                function(result){
                },function(){});
        }
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
                    $scope.leases = [];
                    initialPopulate();
                },function(){});
                
            },
            error: function() {
            }
        });
    }
    
    $scope.showItemDetails = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
}]);
