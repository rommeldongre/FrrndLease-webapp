var myLeasedOutItemsApp = angular.module('myApp');

myLeasedOutItemsApp.controller('myLeasedOutItemsCtrl', ['$scope', 
														'userFactory', 
														'bannerService', 
														'modalService',
                                                        'logoutService',
														function($scope, 
														userFactory, 
														bannerService, 
														modalService,
                                                        logoutService){
    
    localStorage.setItem("prevPage","myapp.html#/myleasedoutitems");
    
    var leaseStatusDesc = {'LeaseReady':'This item will be picked up by us from the owner soon.','PickedUpOut':'This item has been picked up and will be delivered to the requestor.', 'LeaseStarted':'This item has been delivered to the requestor.','LeaseEnded':'This item will be picked up by us from the requestor soon.','PickedUpIn':'This item has been picked and will be delivered to the owner.'};

    $scope.leases = [];
    
    var initialPopulate = function(){
        getLeasedOutItems(0);
    }
    
    var getLeasedOutItems = function(cookie){
        
        req = {
            cookie: cookie,
            leaseUserId: userFactory.user,
            leaseReqUserId: "",
            status: 'Active'
        }
        
        getLeasedOutItemsSend(req);
        
    }
    
    var getLeasedOutItemsSend = function(req){
        $.ajax({
			url: '/GetLeasesByX',
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
                    getLeasedOutItems(response.cookie);
                }
            },
		
			error: function() {}
		});
    }
    
    $scope.closeLease = function(ItemId, RequestorUserId, index){
        if($scope.leases[index].status == 'LeaseStarted' || $scope.leases[index].status == 'LeaseReady'){
            modalService.showModal({}, {bodyText: "Are you sure you want to close the lease on the Item?",actionButtonText: 'Yes'}).then(
                function(result){
                    req = {
                        itemId: ItemId,
                        userId: userFactory.user,
                        reqUserId: RequestorUserId,
                        flag: "close",
                        accessToken: userFactory.userAccessToken
                    };
                    closeLeaseSend(req, index);
                },function(){});
        }else{
            modalService.showModal({}, {bodyText: "Can only be closed when lease status is LeaseStarted or LeaseReady", showCancel:false, actionButtonText: 'Ok'}).then(
                function(result){
                },function(){});
        }
        
    }
    
    var closeLeaseSend = function(req, index){
        $.ajax({
            url: '/RenewLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
				if(response.code==0){
					bannerService.updatebannerMessage(response.message,"");
					$("html, body").animate({ scrollTop: 0 }, "slow");
					$scope.leases = [];
                    initialPopulate();
				}else{
					modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'Ok'}).then(
					function(result){
                        if(response.code == 400)
                            logoutService.logout();
                        else{
                            $scope.leases = [];
                            initialPopulate();
                        }
					},function(){});
				}
                
            },
            error: function() {
            }
        });
    }
    
    $scope.renewLease = function(ItemId, RequestorUserId, index){
        if($scope.leases[index].status == 'LeaseStarted' || $scope.leases[index].status == 'LeaseEnded' || $scope.leases[index].status == 'LeaseReady'){
            modalService.showModal({}, {bodyText: "Are you sure you want to renew the lease on the Item?",actionButtonText: 'Yes'}).then(
                function(result){
                    req = {
                        itemId: ItemId,
                        userId: userFactory.user,
                        reqUserId: RequestorUserId,
                        flag: "renew",
                        accessToken: userFactory.userAccessToken
                    }
                    renewLeaseSend(req);
                },function(){});
        }else{
            modalService.showModal({}, {bodyText: "Can only be closed when lease status is LeaseStarted or LeaseEnded or LeaseReady", showCancel:false, actionButtonText: 'Ok'}).then(
                function(result){
                },function(){});
        }
    }
    
    var renewLeaseSend = function(req){
        $.ajax({
            url: '/RenewLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
				if(response.code==0){
					bannerService.updatebannerMessage(response.message,"");
					$("html, body").animate({ scrollTop: 0 }, "slow");
					$scope.leases = [];
                    initialPopulate();
				}else{
					modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'Ok'}).then(
					function(result){
						if(response.code == 400)
                            logoutService.logout();
                        else{
                            $scope.leases = [];
                            initialPopulate();
                        }
					},function(){});
				}
            },
            error: function() {
            }
        });	
    }
    
    initialPopulate();
    
    $scope.showItemDetails = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
    
    $scope.changePickupStatus = function(s, i){
        
        req = {
            owner: true,
            leaseId: $scope.leases[i].leaseId,
            pickupStatus: s
        }
        
        modalService.showModal({}, {bodyText: "Are you sure you want to change your pick up status?", actionButtonText: 'Yes', cancelButtonText: 'No'}).then(
            function(result){
                $.ajax({
                    url: '/ChangePickupStatus',
                    type: 'post',
                    data: JSON.stringify(req),
                    contentType:"application/json",
                    dataType:"json",

                    success: function(response){
                        if(response.code == 0){
                            window.location.reload();
                        }else{
                            modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'Ok'}).then(function(result){},function(){});
                        }
                    },

                    error: function() {}
                });
            }, function(){}
        );
    }
    
}]);
