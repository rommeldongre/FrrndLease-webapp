var myLeasedInItemsApp = angular.module('myApp');

myLeasedInItemsApp.controller('myLeasedInItemsCtrl', ['$scope', 'userFactory', function($scope, userFactory){

    var itemNextId = 0;
    
    $scope.leases = [];
    
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
        
        searchItemSend(leaseReqUser, leaseExpiry, req, leaseReqOwnerName);

    }

    var searchItemSend = function(leaseReqUser, leaseExpiry, req, leaseReqOwnerName){
        $.ajax({
            url: '/flsv2/SearchItem',
            type: 'post',
            data: {req : JSON.stringify(req)},
            contentType: "application/x-www-form-urlencoded",
            dataType: "json",
            success: function(response) {	
                if(response.Code == "FLS_SUCCESS") {
                    var itemObj = JSON.parse(response.Message);
                    var lease = {expiry:leaseExpiry, ownerName:leaseReqOwnerName, itemTitle:itemObj.title, itemId:itemObj.itemId, itemNextId:itemNextId};
                    if(leaseReqUser == userFactory.user)
                        $scope.$apply(function(){
                            $scope.leases.unshift(lease);
                        });
                    getLeaseItem(itemNextId);
                }
            },
            error: function() {
            }
        });
    }
    
    getLeaseItem(itemNextId);
    
}]);