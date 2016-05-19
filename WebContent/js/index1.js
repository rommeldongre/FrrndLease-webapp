var indexApp = angular.module('indexApp', ['headerApp']);

indexApp.controller('storeYourStuffCtrl', ['$scope', 'userFactory', function($scope, userFactory){
    
    $scope.storeYourStuff = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == 'anonymous'){
            window.location.replace("mylogin.html");
        }
        else{
            storeCurrentFunction('storeYourStuff');
            window.location.replace("mystore.html");
        }
    }
    
}]);

indexApp.controller('indexCtrl', ['$scope', 'userFactory', 'getItemsForCarousel', function($scope, userFactory, getItemsForCarousel){
    
    if(userFactory.user != "" && userFactory.user != null && userFactory.user != 'anonymous')
        window.location.replace("myindex.html");
    
    $scope.carouselWidth = angular.element(document.querySelector('#carouselObject'))[0].clientWidth;
    $scope.carouselHeight = angular.element(document.querySelector('#carouselObject'))[0].clientHeight;
    
    $(window).resize(function(){
        checkItemsLimit();
        $scope.$apply(function(){
            $scope.carouselWidth = angular.element(document.querySelector('#carouselObject'))[0].clientWidth;
            $scope.carouselHeight = angular.element(document.querySelector('#carouselObject'))[0].clientHeight;
        });
    });
    
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
    
    checkItemsLimit();
    
    var lastItems = [];
    
    var lastItem = 0;
    
    var itemsArray = [];
    
    var req = {
		cookie: lastItem,
		userId: null,
		category: null,
		limit: $scope.itemsLimit
	};
    
    var displayItems = function(req){
        getItemsForCarousel.getItems(req).then(
            function(response){
                if(response.data.returnCode == 0){
                    lastItem = response.data.lastItemId;
                    itemsArray.push(response.data.resList);
                    $scope.itemsArray = itemsArray;
                }else{
                    var i = [];

                    i.push({
                        image: 'images/emptycategory.jpg',
                        text: 'Try selecting another category'
                    });

                    $scope.items = i;
                }
            },
            function(error){
                console.log("Not able to get items " + error.message);
            });
    }
    
    displayItems(req);
    
    $scope.loadPrevSlide = function(){
        var req = {
            cookie: lastItems.pop(),
            userId: null,
            category: null,
            limit: $scope.itemsLimit
        };
        
        getItemsForCarousel.getItems(req);
    }
    
    $scope.loadNextSlide = function(){
        console.log(lastItem);
        lastItems.push(lastItem);
        var req = {
            cookie: lastItem,
            userId: null,
            category: null,
            limit: $scope.itemsLimit
        };
        
        displayItems(req);
    }

}]);

indexApp.factory('getItemsForCarousel', ['$http', function($http){
    
    var getItems = {};
    
    getItems.getItems = function(req){
        return $http.post('/flsv2/GetItemStoreByX', JSON.stringify(req));
    }
    
    return getItems;
}]);