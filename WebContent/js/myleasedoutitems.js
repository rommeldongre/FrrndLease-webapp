var myLeasedOutItemsApp = angular.module('myApp');

myLeasedOutItemsApp.controller('myLeasedOutItemsCtrl', ['$scope', 'userFactory', 'modalService', function($scope, userFactory, modalService){
    var itemNextId = 0;
    
    $scope.leases = [];
    
    var currentFlag = '';
    
    var getLeaseItem = function(i){
        var req = {
            operation: "getNextActive",
            token: i+""
        }
        getLeaseItemSend(req);
    }
    
    var getLeaseItemSend = function(req){
        $.ajax({
            url: '/flsv2/GetLeases',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code == "FLS_SUCCESS") {
                    var obj = JSON.parse(response.Message);
                    itemNextId = response.Id;
                    showLeaseItem(obj);
                }
                else{
                }
            },
            error: function() {
            }
        });
    }
    
    var showLeaseItem = function(obj){
		var leaseExpiry = obj.expiry; 
		var leaseItemId = obj.itemId; 
		var leaseReqUser = obj.reqUserId;
		var objUserId = obj.userId;
		var leaseReqOwnerName= obj.OwnerFullName;
        
        switch(currentFlag){
            case 'renew':
                var req = {
                    itemId: leaseItemId,
                    userId: userFactory.user,
                    reqUserId: leaseReqUser+"",
                    flag: "renew"
                };
                renewLeaseSend(req);
                break;
            case 'close':
                var req = {
                    itemId: leaseItemId,
                    userId: userFactory.user,
                    reqUserId: leaseReqUser+"",
                    flag: "close"
                };
                closeLeaseSend(req);
                break;
            default:
                leaseItemId--;
                if(leaseItemId == null || leaseItemId == '' || leaseItemId == 'null')
                    leaseItemId = 0;
                var req = {
                    token: leaseItemId,
                    title: "",
                    description: "",
                    category: "",
                    leaseValue: 0,
                    leaseTerm: ""
                };
                searchItemSend(leaseExpiry, req, leaseReqOwnerName);
                break;
        }

    }
    
    var renewLeaseSend = function(req){
        $.ajax({
            url: '/flsv2/RenewLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                
                modalService.showModal({}, {bodyText: response.message,actionButtonText: 'OK'}).then(
                function(result){
                    currentFlag = '';
                    itemNextId = 0;
                    $scope.$apply(function(){
                        $scope.leases = [];
                    });
                    getLeaseItem(itemNextId);
                },function(){});
                
            },
            error: function() {
            }
        });	
    }
    
    var closeLeaseSend = function(req){
        $.ajax({
            url: '/flsv2/RenewLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                
                modalService.showModal({}, {bodyText: response.message,actionButtonText: 'OK'}).then(
                function(result){
                    currentFlag = '';
                    itemNextId = 0;
                    $scope.$apply(function(){
                        $scope.leases = [];
                    });
                    getLeaseItem(itemNextId);
                },function(){});
                
            },
            error: function() {
            }
        });
    }

    var searchItemSend = function(leaseExpiry, req, leaseReqOwnerName){
        $.ajax({
            url: '/flsv2/SearchItem',
            type: 'post',
            data: {req : JSON.stringify(req)},
            contentType: "application/x-www-form-urlencoded",
            dataType: "json",
            success: function(response) {	
                if(response.Code == "FLS_SUCCESS") {
                    var itemObj = JSON.parse(response.Message);
                    var lease = {expiry:leaseExpiry, ownerName:leaseReqOwnerName, itemTitle:itemObj.title, itemId:itemObj.itemId, itemNextId:itemNextId, uid:itemObj.uid};
                    if(itemObj.userId == userFactory.user)
                        $scope.$apply(function(){
                            $scope.leases.push(lease);
                        });
                    getLeaseItem(itemNextId);
                }
            },
            error: function() {
            }
        });
    }
    
    getLeaseItem(itemNextId);
    
    $scope.closeLease = function(itemNextId){
        itemNextId = itemNextId - 1;
        modalService.showModal({}, {bodyText: "Are you sure you want to close the lease on the Item?",actionButtonText: 'YES'}).then(
            function(result){
                currentFlag = 'close';
                getLeaseItem(itemNextId);
            },function(){});
    }
    
    $scope.showItemDetails = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
    
    $scope.renewLease = function(i){
        itemNextId = itemNextId - 1;
        modalService.showModal({}, {bodyText: "Are you sure you want to renew the lease on the Item?",actionButtonText: 'YES'}).then(
            function(result){
                currentFlag = 'renew';
                getLeaseItem(itemNextId);
            },function(){});
    }
}]);