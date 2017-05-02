var libraryApp = angular.module('libraryApp', ['headerApp', 'carouselApp', 'footerApp', 'ngAutocomplete','ui.bootstrap']);

libraryApp.controller('libraryCtrl', ['$scope', '$timeout', 'userFactory', 'statsFactory', 'getItemsForCarousel', 'scrollService', 'searchService', '$rootScope', function($scope, $timeout, userFactory, statsFactory, getItemsForCarousel, scrollService, searchService,$rootScope){
    
    localStorage.setItem("prevPage","index.html");
    
    $scope.search = {};
    
    $scope.options = {
        country: 'in',
        sendToCarousel: true
    };
    
    $scope.details = '';
    
	var displayStats = function () {
            statsFactory.getStats().then(
                function (response) {
                    if (response.data.message == "Success") {
                        $scope.item_count = response.data.itemCount;
                        $scope.user_count = response.data.userCount;
                    } else {
                        $scope.item_count = "";
                        $scope.user_count = "";
                    }
                },
                function (error) {
                    console.log("unable to get count: " + error.message);
                });
        }
		
    // remove this code and uncomment the below one when using https
    $scope.search.location = "Pune, Maharashtra, India";
    
    searchService.saveCurrentLocation(18.533617, 73.828651);	
		if (window.location.href.indexOf("library.html") > -1) {
			$timeout(function(){
				// populating the site Stats
				displayStats();
				searchService.sendDataToCarousel();
			}, 2000);
		}
        
    
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
		if($scope.search.string!="" || $scope.search.string!=null || typeof $scope.search.string !== 'undefined'){
			scrollService.scrollToDiv("friendstoreline");
		}
    }
    
    $scope.searchStringChanged = function(searchString){
        searchService.saveSearchTitle(searchString);
    }
    
    $scope.$on('searchDataEmpty', function(event, data){
        $scope.search.string = data;
    });
    
}]);

angular.element(document).ready(function() {
  angular.bootstrap(document, ['libraryApp']);
});