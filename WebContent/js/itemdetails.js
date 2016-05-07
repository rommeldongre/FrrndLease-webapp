var itemDetailsApp = angular.module('itemDetailsApp', ['ui.bootstrap']);

itemDetailsApp.controller('headerCtrl', function($scope){
    
    var user = localStorage.getItem("userloggedin");
    
    if(user == "" || user == null){
        user = "anonymous";
        localStorage.setItem("userloggedin", user);	
    }else{
        $scope.salutation = localStorage.getItem("userloggedinName");
    }
    
    $scope.isAdmin = function(){
        if(user == 'frrndlease@greylabs.org')
            return true;
        else
            return false;
    }
    
    $scope.isAnonymous = function(){
        if(user == "anonymous")
            return true;
        else
            return false;
    }
    
    $scope.isLoggedIn = function(){
        if(user != "anonymous")
            return true;
        else
            return false;
    }
    
    $scope.logout = function(){
        localStorage.setItem("userloggedin", "anonymous");  //userloggedin-> anonymous
        var auth2 = gapi.auth2.getAuthInstance();
        auth2.signOut().then(function() {
            console.log('User signed out.');
        });
											
        window.location.replace("index.html");
    }
});

itemDetailsApp.controller('itemDetailsCtrl', ['$scope', '$window', '$http','modalService', function($scope, $window, $http, modalService){
    
    var user = localStorage.getItem("userloggedin");
    
    $scope.message = $window.message;
    
    $scope.item_id = $window.item_id;
    $scope.title = $window.title;
    $scope.category = $window.category;
    $scope.description = $window.description;
    $scope.leaseValue = parseInt($window.leaseValue);
    $scope.leaseTerm = $window.leaseTerm;
    
    // checking if the response code is 0 or not to show error div of itemdetails div
    if($window.code != 0){
        $scope.showError = true;
    }
    else{
        $scope.showError = false;
    }
    
    // checking if the loggedIn user matches with the items userId
    if(user == $window.userId){
        $scope.userMatch = true;
    }else{
        $scope.userMatch = false;
    }
    
    $scope.requestItem = function(){
        modalService.showModal({}, {bodyText: 'Are you sure you want to request the Item?'}).then(function(result){
                if (user == "" || user == null || user == "anonymous")
					logInCheck();
				else
                    $http({
                        url:'/flsv2/RequestItem?req='+JSON.stringify({itemId:$scope.item_id,userId:user}),
                        method:"GET"
                    }).then(function success(response){
                        console.log(response);
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                    },
                    function error(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                    });
        }, 
        function(){
                    
        });
    }
    
    $scope.editItem = function(){
        
    }
    
    $scope.deleteItem = function(){
        
    }
    
}]);

itemDetailsApp.service('modalService', ['$uibModal',
    function ($uibModal) {

        var modalDefaults = {
            animation: true,
            backdrop: true,
            keyboard: true,
            templateUrl: '/flsv2/modal.html'
        };

        var modalOptions = {
            actionButtonText: 'YES',
            showCancel: true,
            cancelButtonText: 'NO',
            headerText: 'Item Details Page Says',
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
                        $uibModalInstance.dismiss('cancel');
                    };
                }
            }

            return $uibModal.open(tempModalDefaults).result;
        };

    }]);

//function requestItem(i, u){
//			var req = {
//				itemId: i,
//				userId: u
//			};
//
//			reqItemSend(req);
//		}
//
//		function reqItemSend(req){
//			
//			$.ajax({
//				url: '/flsv2/RequestItem',
//				type:'get',
//				data: {req: JSON.stringify(req)},
//				contentType:"application/json",
//				dataType: "json",
//				
//				success: function(response) {
//					var heading = "Successful";
//					
//					var msg = response.Message;
//					var objOwner = getItemOwner();
//				},
//				
//				error: function() {
//					var msg = "Not Working";
//				}
//			});
//		}