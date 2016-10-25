var deliveryPlanApp = angular.module('deliveryPlanApp', ['headerApp', 'footerApp']);

deliveryPlanApp.controller('deliveryCtrl', ['$scope', '$location', function($scope, $location){
    
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
    
    var Plan = getQueryVariable("delPlan");
    var LeaseId = getQueryVariable("leaseId");
    var DeliveryPlan = 'FLS_SELF';
    
    if(Plan == 'prime'){
        DeliveryPlan = 'FLS_OPS';
    }else if(Plan == 'self'){
        DeliveryPlan = 'FLS_SELF';
    }
    
    var req = {
        deliveryPlan: DeliveryPlan,
        leaseId: LeaseId
    }
    
    $.ajax({
        url: '/ChangeDeliveryPlan',
        type: 'post',
        data: JSON.stringify(req),
        contentType:"application/json",
        dataType:"json",
        
        success: function(response){
            if(response.code == 0){
                $scope.response.message = "Your delivery plan has been saved!!";
            }else{
                $scope.response.message = response.message;
            }
        },
        error: function() {}
    });
    
}]);
