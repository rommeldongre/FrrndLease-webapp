var emailVerificationApp = angular.module('emailVerificationApp', ['headerApp']);

emailVerificationApp.controller('verificationCtrl', ['$scope', '$http', '$location', 'modalService', function($scope, $http, $location, modalService){
    
    $scope.title = "Frrndlease Sign up Email Verification";
    
    var getQueryVariable = function (variable) {
        var query = window.location.search.substring(1);
        var vars = query.split("&");
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            if (pair[0] == variable) {
                return pair[1];
            }
        }
        console.log('Token= ' + variable + ' not found');
    }
    
    var token = getQueryVariable("token");
    
    $http.post('/flsv2/EmailVerification', JSON.stringify({verification : token})).then(
        function(data){
            $scope.response = data.data;
            
            if(data.data.code == 0){
                localStorage.setItem("userloggedin", data.data.userId);
                localStorage.setItem("userloggedinName", data.data.name);
                localStorage.setItem("userloggedinAccess", data.data.access_token);
                
                modalService.showModal({}, {bodyText: data.data.message + ', Welcome to fRRndLease.',showCancel: false,actionButtonText: 'OK'}).then(
                    function(result){
                        window.location.replace("myapp.html");
                    },
                    function(){});
            }
        }, 
        function(error){
            console.log(error);
        }
    );
}]);