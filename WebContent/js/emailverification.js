var emailVerificationApp = angular.module("emailVerificationApp", []);

emailVerificationApp.controller("verificationCtrl", function($scope, $http){
    $scope.title = "Frrndlease Sign up Email Verification";
    
    $http.post('/flsv2/EmailVerification', JSON.stringify({verification : getQueryVariable("token")})).then(
        function(data, status, headers, config){
            $scope.response = data.data;
            
            if(data.data.code == 0){
                localStorage.setItem("userloggedin", data.data.userId);
                div.innerHTML = data.data.message + ", Welcome to fRRndLease.";
						
				$("#modalTriggerButton").click();
            }
        }, 
        function(data, status, headers, config){
            console.log(data);
        }
    );
});

var getQueryVariable = function (variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) {
            return pair[1];
        }
    }
    alert('Token= ' + variable + ' not found');
};