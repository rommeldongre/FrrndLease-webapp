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

indexApp.controller('indexCtrl', ['$scope', '$timeout','userFactory', 'getItemsForCarousel', function($scope, $timeout, userFactory, getItemsForCarousel){
    
    if(userFactory.user != "" && userFactory.user != null && userFactory.user != 'anonymous')
        window.location.replace("myindex.html");
    
    // lastItem is used to store the id of the last retrieved item from the database
    var lastItem = 0;
    // selected category is stored in this variable
    var category = '';
    
    // Initialising the categories
    $scope.categories = [{label:'ALL',active:true}];
    
    // getting the width and height of the carousel when page gets loaded
    var getCarouselWidthHeight = function(){
        $scope.carouselWidth = angular.element(document.querySelector('#carouselObject'))[0].clientWidth;
        $scope.carouselHeight = angular.element(document.querySelector('#carouselObject'))[0].clientHeight;
    }
    
    // called on page load
    getCarouselWidthHeight();
    
    // getting the width and height of the carousel when window is resized
    $(window).resize(function(){
        checkItemsLimit();
        $scope.$apply(function(){
            getCarouselWidthHeight();
        });
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
        
        // called twice to get two arrays of data initially for smooth carousel experience
        populateCarousel();
        populateCarousel();
    }
    
    var populateCarousel = function(){
        
        if(category == '' || category == 'ALL')
            category = null;
        
        var req = {
            cookie: lastItem,
            userId: null,
            category: category,
            limit: $scope.itemsLimit
	    };
        
        displayItems(req);
    }
    
    var displayItems = function(req){
        getItemsForCarousel.getItems(req).then(
            function(response){
                if(response.data.returnCode == 0){
                    if(lastItem == 0){
                        $scope.itemsArray = [response.data.resList];
                    }
                    else{
                        $timeout(function(){
                            $scope.itemsArray.push(response.data.resList);
                        }, 1000);
                    }
                    lastItem = response.data.lastItemId;
                }else{
                    if(lastItem == 0)
                        $scope.itemsArray = [[{ image: 'images/emptycategory.jpg', title: 'Try selecting another category' }]];
                }
            },
            function(error){
                console.log("Not able to get items " + error.message);
            });
    }
    
    // called on the page load
    initPopulate();
    
    // called when next carousel button is clicked
    $scope.loadNextSlide = function(){
        populateCarousel();
    }
    
    var populateCategory = function(id){
        var req = {
            operation:"getNext",
            token: id
        }
        
        displayCategory(req);
    }
    
    var displayCategory = function(req){
        $.ajax({
            url: '/flsv2/GetCategoryList',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    $scope.categories.push({label:JSON.parse(response.Message).catName,active:false});
                    populateCategory(response.Id);
                }
                else{
                    //all categories are loaded
                }
            },
            error:function() {
            }
        });	
    }
    
    // called on the page load
    populateCategory('');
    
    // called when category is selected from the category filter
    $scope.categorySelected = function(index){
        
        // turn all button class btn-default
        angular.forEach($scope.categories, function(c){c.active = false});
        
        // turn clicked button's class btn-primary
        $scope.categories[index].active = true;
        
        // store category which is clicked
        category = $scope.categories[index].label;
        
        initPopulate();
    }
    
    // called when item from carousel is clicked
    $scope.itemClicked = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }

}]);

indexApp.factory('getItemsForCarousel', ['$http', function($http){
    
    var getItems = {};
    
    getItems.getItems = function(req){
        return $http.post('/flsv2/GetItemStoreByX', JSON.stringify(req));
    }
    
    return getItems;
}]);