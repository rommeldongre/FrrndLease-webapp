var headerApp = angular.module('headerApp', ['ui.bootstrap', 'ngAutocomplete']);

headerApp.controller('headerCtrl', ['$scope', 'userFactory', 'profileFactory', 'getLocation', function($scope, userFactory, profileFactory, getLocation){
    
    $scope.search = {};
    
    $scope.options = {
        country: 'in'
    };
    
    $scope.details = '';
    
    if(window.location.href.indexOf("frrndlease.com") > -1){
        if(window.location.pathname == '/index.html' || window.location.pathname == '/'){
            $scope.navClassValue = "navbar navbar-static";
            $scope.showSearch = false;
        }
        else{
            $scope.navClassValue = "navbar navbar-default navbar-fixed-top";
            $scope.showSearch = true;
        }
    }else{
        if(window.location.pathname == '/flsv2/index.html' || window.location.pathname == '/flsv2/'){
            $scope.navClassValue = "navbar navbar-static";
            $scope.showSearch = false;
        }
        else{
            $scope.navClassValue = "navbar navbar-default navbar-fixed-top";
            $scope.showSearch = true;
        }
    }
    
    if(userFactory.user == "" || userFactory.user == null){
        localStorage.setItem("userloggedin", "anonymous");	
    }else{
        $scope.salutation = userFactory.userName;
    }
    
    var displayCredits = function(){
        profileFactory.getProfile(userFactory.user).then(
        function(response){
            if (response.data.code == 0) {
                $scope.search.location = response.data.sublocality+","+response.data.locality;
                $scope.credits = response.data.credit + " credits";
                getLocation.saveCurrentLocation(response.data.lat,response.data.lng);
            } else {
                $scope.credits = "";
            }
        },
        function(error){
            console.log("unable to get credits: " + error.message);
        });
    }
    
    getLocationData = function(location){
        $.ajax({
            url: 'https://maps.googleapis.com/maps/api/geocode/json',
            type: 'get',
            data: 'address='+location+"&key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g",
            success: function(response){
                var Lat = 0.0, lng = 0.0;
                if(response.status == 'OK'){
                    console.log("Searching in this address : " + response.results[0].formatted_address);
                    Lat = response.results[0].geometry.location.lat;
                    Lng = response.results[0].geometry.location.lng;
                }
                sendLocation.changedLocationString(Lat, Lng);
            },
            error: function(){
                console.log("not able to get location data");
            }
        });
    }
    
    // populating the credits
    displayCredits();
    
    $scope.isAdmin = function(){
        if(userFactory.user == 'frrndlease@greylabs.org')
            return true;
        else
            return false;
    }
    
    $scope.isAnonymous = function(){
        if(userFactory.user == "anonymous")
            return true;
        else
            return false;
    }
    
    $scope.isLoggedIn = function(){
        if(userFactory.user != "anonymous")
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
    
    $scope.storeYourStuff = function(){
        localStorage.setItem("prevFunc", 'storeYourStuff');
			
		window.location.replace("mystore.html");
    }
    
}]);

// factory for getting and updating profile from the backend service
headerApp.factory('profileFactory', ['$http', function($http){
    
    var dataFactory = {};
    
    dataFactory.getProfile = function(user){
        return $http.post('/flsv2/GetProfile', JSON.stringify({userId : user}));
    }
    
    dataFactory.updateProfile = function(req){
        return $http.post('/flsv2/EditProfile', JSON.stringify(req));
    }
    
    return dataFactory;
}]);

headerApp.factory('userFactory', function(){
    
    var dataFactory = {};
    
    dataFactory.user = localStorage.getItem("userloggedin");
    
    dataFactory.userName = localStorage.getItem("userloggedinName");
    
    return dataFactory;
});

// service to implement modal
headerApp.service('modalService', ['$uibModal',
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
            submitting: false,
            cancelButtonText: 'CANCEL',
            headerText: 'Frrndlease Says',
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
                    $scope.submit = {};
                    $scope.modalOptions = tempModalOptions;
                    $scope.modalOptions.ok = function (result) {
                        $uibModalInstance.close($scope.submit.url);
                    };
                    $scope.modalOptions.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }
            }

            return $uibModal.open(tempModalDefaults).result;
        };

    }]);

headerApp.service('getLocation', ['$rootScope', function($rootScope){
    
    this.lat = 0.0;
    this.lng = 0.0;
    
    this.saveCurrentLocation = function(lat, lng){
        this.lat = lat;
        this.lng = lng;
    }
    
    this.sendLocationToCarousel = function(){
        $rootScope.$broadcast('locationStringChanged', this.lat, this.lng);
    }
    
}]);