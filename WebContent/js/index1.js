var indexApp = angular.module('indexApp', ['headerApp', 'carouselApp', 'ngAutocomplete']);

indexApp.controller('indexCtrl', ['$scope', '$timeout', 'userFactory', 'getItemsForCarousel', 'searchService', function($scope, $timeout, userFactory, getItemsForCarousel, searchService){
    
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
            window.location.replace("mylogin.html");
        }
        else{
            window.location.replace("mystore.html");
        }
    }
    
    $scope.search = function(){
        searchService.sendDataToCarousel();
    }
    
    $scope.searchStringChanged = function(searchString){
        searchService.saveSearchTitle(searchString);
    }
    
}]);