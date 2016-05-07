var emailVerificationApp = angular.module('emailVerificationApp', ['ui.bootstrap']);

emailVerificationApp.controller('verificationCtrl', ['$scope', '$http', '$location', 'modalService', function($scope, $http, $location, modalService){
    
    $scope.title = "Frrndlease Sign up Email Verification";
    
    $http.post('/flsv2/EmailVerification', JSON.stringify({verification : getQueryVariable("token")})).then(
        function(data, status, headers, config){
            $scope.response = data.data;
            
            if(data.data.code == 0){
                localStorage.setItem("userloggedin", data.data.userId);
                
                var modalOptions = {
                    bodyText: data.data.message + ', Welcome to fRRndLease.'
                };
                
                modalService.showModal({}, modalOptions).then(function(result){
                
                window.location.replace("/flsv2/myindex.html");
                }, function(){
                    
                });
            }
        }, 
        function(data, status, headers, config){
            console.log(data);
        }
    );
}]);

emailVerificationApp.service('modalService', ['$uibModal',
    function ($uibModal) {

        var modalDefaults = {
            animation: true,
            backdrop: true,
            keyboard: true,
            templateUrl: '/flsv2/modal.html'
        };

        var modalOptions = {
            actionButtonText: 'OK',
            showCancel: false,
            cancelButtonText: 'CANCEL',
            headerText: 'Email Verification',
            bodyText: 'Perform this action?'
        };

        this.showModal = function (customModalDefaults, customModalOptions) {
            if (!customModalDefaults) customModalDefaults = {};
            customModalDefaults.backdrop = 'static';
            return this.show(customModalDefaults, customModalOptions);
        };

        this.show = function (customModalDefaults, customModalOptions) {
            //Create temp objects to work with since we're in a singleton service
            var tempModalDefaults = {};
            var tempModalOptions = {};

            //Map angular-ui modal custom defaults to modal defaults defined in service
            angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

            //Map modal.html $scope custom properties to defaults defined in service
            angular.extend(tempModalOptions, modalOptions, customModalOptions);

            if (!tempModalDefaults.controller) {
                tempModalDefaults.controller = function ($scope, $uibModalInstance) {
                    $scope.modalOptions = tempModalOptions;
                    $scope.modalOptions.ok = function (result) {
                        $uibModalInstance.close(result);
                    };
                    $scope.modalOptions.cancel = function () {
                        $modalInstance.dismiss('cancel');
                    };
                }
            }

            return $uibModal.open(tempModalDefaults).result;
        };

    }]);

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