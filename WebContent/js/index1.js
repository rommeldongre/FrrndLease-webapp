var indexApp = angular.module('indexApp', ['headerApp', 'carouselApp', 'ngAutocomplete']);

indexApp.controller('indexCtrl', ['$scope', '$timeout', 'userFactory', 'getItemsForCarousel', 'searchService', function($scope, $timeout, userFactory, getItemsForCarousel, searchService){
    
    localStorage.setItem("prevPage","index.html");
    
    $scope.search = {};
    
    $scope.options = {
        country: 'in',
        sendToCarousel: true
    };
    
    $scope.details = '';
    
    //to get current location of the user and show it in the location by default
    var getCurrentLocation = function() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(showPosition);
        } else { 
            console.log("Geolocation is not supported by this browser.");
        }
    }

    var showPosition = function(position) {
        console.log("Latitude: "+position.coords.latitude+" Longitude: "+position.coords.longitude);

        latitude = position.coords.latitude;
        longitude = position.coords.longitude;
		coords = new google.maps.LatLng(latitude, longitude);
        
        searchService.saveCurrentLocation(latitude, longitude);
        $timeout(function(){
            searchService.sendDataToCarousel();
        }, 2000);
		
		var geocoder = new google.maps.Geocoder();
		var latLng = new google.maps.LatLng(latitude, longitude);
		geocoder.geocode( { 'latLng': latLng}, function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                $scope.search.location = results[4].formatted_address;
            }else{
                console.log("Geocode was unsucessfull in detecting your current location");
            }
        });
    }
    
    getCurrentLocation();
    
    if(userFactory.user != "" && userFactory.user != null && userFactory.user != 'anonymous')
        window.location.replace("myapp.html");

    $scope.storeYourStuff = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == 'anonymous'){
            $('#loginModal').modal('show');
        }
        else{
            window.location.replace("EditPosting.html");
        }
    }
    
    $scope.search = function(){
        searchService.sendDataToCarousel();
    }
    
    $scope.searchStringChanged = function(searchString){
        searchService.saveSearchTitle(searchString);
    }
    
    $scope.$on('searchDataEmpty', function(event, data){
        $scope.search.string = data;
    });
    
}]);

indexApp.controller('loginModalCtrl', ['$scope', 'loginSignupService', function($scope, loginSignupService){
    // Form login
    $scope.formLogin = function(email, password){
        loginSignupService.loginCheckReq(email, password, "email_activated");
    }
    
    // Google login
    function onSignIn(googleUser) {
        var profile = googleUser.getBasicProfile();
        loginSignupService.loginCheckReq(profile.getEmail(), profile.getId(), "google");
    }
    window.onSignIn = onSignIn;
    
    // facebook login
    $scope.facebookSignIn = function() {
        FB.login(function(response) {
            // handle the response
            FB.api('/me?fields=id,name,email,first_name,last_name,locale,gender', function(response) {
                loginSignupService.loginCheckReq(response.email, response.id, "facebook");
            });
        }, {scope: 'email,public_profile,user_friends'});    
    }
    
    // Login response
    $scope.$on('loginCheckRes', function(event, message){
        $scope.$apply(function(){
            $scope.error = message;
        });
    });
    
}]);

indexApp.controller('signUpModalCtrl', ['$scope', 'loginSignupService', function($scope, loginSignupService){
    
    // form sign up
    $scope.formSignup = function(email, password, name, mobile, location){
        loginSignupService.signUpCheckReq(email, password, name, mobile, location, "email_pending");
    }
    
    // Google sign up
    function onSignUp(googleUser) {
        var profile = googleUser.getBasicProfile();
        loginSignupService.signUpCheckReq(profile.getEmail(), profile.getId(), profile.getName(), "", $scope.location, "google");
    }
    window.onSignUp = onSignUp;
    
    // facebook sign up
    $scope.facebookSignIn = function() {
        FB.login(function(response) {
            // handle the response
            FB.api('/me?fields=id,name,email,first_name,last_name,locale,gender', function(response) {
                loginSignupService.signUpCheckReq(response.email, response.id, response.name, "", $scope.location, "facebook");
            });
        }, {scope: 'email,public_profile,user_friends'});    
    }
    
    // sign up response
    $scope.$on('signUpCheckRes', function(event, message){
        $scope.$apply(function(){
            $scope.error = message;
        });
    });
    
    // getting the current location
    var getLocation = function() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(showPosition);
        } else { 
            console.log("Geolocation is not supported by this browser.");
        }
    }

    var showPosition = function(position) {
		latitude = position.coords.latitude;
		longitude = position.coords.longitude;
		coords = new google.maps.LatLng(latitude, longitude);	
			
		var geocoder = new google.maps.Geocoder();
		var latLng = new google.maps.LatLng(latitude, longitude);
		geocoder.geocode( { 'latLng': latLng}, function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                $scope.$apply(function(){
                   $scope.location = results[4].formatted_address; 
                });
            }else{
                console.log("Geocode was unsucessfull in detecting your current location");
            }
        });
    }
    
    getLocation();
}]);