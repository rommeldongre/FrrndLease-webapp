var indexApp = angular.module('indexApp', ['headerApp', 'carouselApp', 'footerApp', 'ngAutocomplete']);

indexApp.controller('indexCtrl', ['$scope', '$timeout', 'userFactory', 'getItemsForCarousel', 'searchService', function($scope, $timeout, userFactory, getItemsForCarousel, searchService){
    
    localStorage.setItem("prevPage","index.html");
    
    $scope.search = {};
    
    $scope.options = {
        country: 'in',
        sendToCarousel: true
    };
    
    $scope.details = '';
    
    // remove this code and uncomment the below one when using https
    $scope.search.location = "Gokhalenagar, Pune, Maharashtra, India";
    
    searchService.saveCurrentLocation(18.533617, 73.828651);
        $timeout(function(){
            searchService.sendDataToCarousel();
        }, 2000);
    
//    //to get current location of the user and show it in the location by default
//    var getCurrentLocation = function() {
//        if (navigator.geolocation) {
//            navigator.geolocation.getCurrentPosition(showPosition);
//        } else { 
//            console.log("Geolocation is not supported by this browser.");
//        }
//    }
//
//    var showPosition = function(position) {
//        console.log("Latitude: "+position.coords.latitude+" Longitude: "+position.coords.longitude);
//
//        latitude = position.coords.latitude;
//        longitude = position.coords.longitude;
//		coords = new google.maps.LatLng(latitude, longitude);
//        
//        searchService.saveCurrentLocation(latitude, longitude);
//        $timeout(function(){
//            searchService.sendDataToCarousel();
//        }, 2000);
//		
//		var geocoder = new google.maps.Geocoder();
//		var latLng = new google.maps.LatLng(latitude, longitude);
//		geocoder.geocode( { 'latLng': latLng}, function(results, status) {
//            if (status == google.maps.GeocoderStatus.OK) {
//                $scope.search.location = results[4].formatted_address;
//            }else{
//                console.log("Geocode was unsucessfull in detecting your current location");
//            }
//        });
//    }
//    
//    getCurrentLocation();
    
    if(userFactory.user != "" && userFactory.user != null && userFactory.user != "anonymous")
        window.location.replace("myapp.html");

    $scope.storeYourStuff = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == 'anonymous'){
            $('#registerModal').modal('show');
        }
        else{
            window.location.replace("myapp.html#/edititem");
        }
    }
    
    $scope.searching = function(){
        searchService.sendDataToCarousel();
    }
    
    $scope.searchStringChanged = function(searchString){
        searchService.saveSearchTitle(searchString);
    }
    
    $scope.$on('searchDataEmpty', function(event, data){
        $scope.search.string = data;
    });
    
}]);

indexApp.controller('pricingCtrl', ['$scope', '$location', 'modalService', function($scope, $location, modalService){
	
	$scope.primeLead = function(){
		modalService.showModal({}, {submitting: true, labelText: 'Enter Email for recieve Updates', actionButtonText: 'Submit'}).then(function(result){
            var Lead_email = result;
			var Lead_type= "uber";
			var Lead_url= "";
			var loc_url = $location.url();
			
			if(loc_url=="/"){
				Lead_url = "myapp"
			}else if(loc_url==""){
				Lead_url = "index"
			}else{
				Lead_url = loc_url;
			}	
				
		   var req = {
                leadEmail : Lead_email,
				leadType: Lead_type,
				leadUrl: Lead_url
            }
			sendLeadEmail(req);	
        }, function(){});
    }
	
	var sendLeadEmail = function(req){
		
		$.ajax({
			url: '/AddLead',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code==0 || response.code==225){
					modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
					}, function(){});
				}else{
					modalService.showModal({}, {bodyText: "Some Error Occured. Please try after some time" ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
					}, function(){});
				}
			},
		
			error: function() {
				console.log("Not able to send message");
			}
		});
	}
}]);
