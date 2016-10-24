var confirmPickupApp = angular.module('confirmPickupApp', ['headerApp', 'footerApp']);

confirmPickupApp.controller('pickupCtrl', ['$scope', '$location', function($scope, $location){
    
    var getQueryVariable = function (variable) {
        var query = window.location.search.substring(1);
        var vars = query.split("&");
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            if (pair[0] == variable) {
                return pair[1];
            }
        }
    }
    
    $scope.response = {};
    
    var IsOwner = getQueryVariable("isOw");
    var LeaseId = getQueryVariable("leaseId");
    
    var req = {
        owner: IsOwner,
        leaseId: LeaseId,
        pickupStatus: true
    }
    
    $.ajax({
        url: '/ChangePickupStatus',
        type: 'post',
        data: JSON.stringify(req),
        contentType:"application/json",
        dataType:"json",
        
        success: function(response){
            if(response.code == 0){
                $scope.response.message = "Your pickup status has been confirmed!!";
            }else{
                $scope.response.message = response.message;
            }
        },
        error: function() {}
    });
    
}]);
