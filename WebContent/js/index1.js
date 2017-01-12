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
    
    userFactory.getCurrentLocation();
    
    $scope.$on('currentLocation', function(event, location){
        $scope.search.location = location;
    });
    
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