var userProfileApp = angular.module('userProfileApp', ['headerApp', 'footerApp', 'carouselApp']);

userProfileApp.controller('userProfileCtrl', ['$scope', '$window', 'getItemsForCarousel', function($scope, $window, getItemsForCarousel){
    
    // lastItem is used to store the id of the last retrieved item from the database
    var lastItem = 0;
    $scope.notPosted = false;
    
    $scope.user = {
        userId: $window.userId,
        profilePic: $window.userProfilePic,
        userFullName: $window.userFullName,
        locality: $window.locality,
        wishedList: $window.wishedList.split(","),
        friends: $window.friends,
        items: []
    };
    
    // getting the width and height of the carousel when window is resized
    $(window).resize(function(){
        checkItemsLimit();
        // loading carousel from start
        initPopulate();
    });
    
    // get number of items to display in carousel based on the size of the screen
    var checkItemsLimit = function(){
        if($(window).width()>=991){
            $scope.itemsLimit = 3;	        //for desktops
            $scope.colClass = "col-md-4 col-sm-4 col-xs-4 col-lg-4";
        }else if($(window).width()<=500){
            $scope.itemsLimit = 1;		    //for mobiles
            $scope.colClass = "col-md-12 col-sm-12 col-xs-12 col-lg-12";
        }else{
            $scope.itemsLimit = 2;		    //for tablets
            $scope.colClass = "col-md-6 col-sm-6 col-xs-6 col-lg-6";
        }
    }
    
    // called on page load
    checkItemsLimit();
    
    // populate the carousel with initital array
    var initPopulate = function(){
        lastItem = 0;
        
        $scope.user.items = [];
        
        $scope.showNext = false;
        
        populateCarousel(lastItem);
    }
    
    var populateCarousel = function(token){
        
        var req = {
            cookie: token,
            userId: $window.userId,
			match_userId: null,
            category: null,
            limit: $scope.itemsLimit,
            lat: 18.563946,
            lng: 73.810295,
            searchString: '',
            itemStatus: ['InStore']
        };
        displayItems(req);
        
    }
    
    var displayItems = function(req){
        getItemsForCarousel.getItems(req).then(
            function(response){
                if(response.data.returnCode == 0){
                    $scope.user.items.push(response.data.resList);
                    lastItem = response.data.lastItemId;
					$scope.showNext = true;
                    $scope.notPosted = false;
                }else{
					$scope.showNext = false;
                    if(lastItem == 0){
                        $scope.notPosted = true;
                    }
                }
            },
            function(error){
				//Error message in console.
                console.log("Not able to get items " + error.message);
            });
    }
    
    // called when next carousel button is clicked
    $scope.loadNextSlide = function(){
        populateCarousel(lastItem);
    }
    
    initPopulate();
    
}]);