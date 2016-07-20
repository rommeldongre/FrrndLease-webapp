var forgotPasswordApp = angular.module('forgotPasswordApp', ['headerApp']);

forgotPasswordApp.controller('forgotPasswordCtrl', ['$scope', '$http', '$location', 'modalService', function($scope, $http, $location, modalService){
    
    $scope.error = '';
    $scope.indexLink = false;
    
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
    
    var activation = getQueryVariable("act");
    
    $scope.resetPassword = function(password){
        var Password = (CryptoJS.MD5(password)).toString();
        var req = {
            table: "users",
            operation: "resetpassword",
            row: {
                activation: activation,
                auth: Password
            }
        }
        $.ajax({
            url: '/flsv2/AdminOps',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                $scope.$apply(function(){
                    if(response.Code == 201){
                        $scope.indexLink = true;
                    }
                    $scope.error = response.Message;
                });
            },
            error:function() {
            }
        });
    }
                                      
}]);