var carouselApp = angular.module('carouselApp', []);

carouselApp.controller('carouselCtrl', ['$scope', '$timeout', 'getItemsForCarousel', 'search', function($scope, $timeout, getItemsForCarousel, search){
    // lastItem is used to store the id of the last retrieved item from the database
    var lastItem = 0;
    // selected category is stored in this variable
    var category = '';
    // if this carousel is loaded in myPostings then userId in req var is set to current userId from local storage
    var userId = null;
    // searchString is used to display items which are being searched
    var searchString = '';
    // to display the next button or not
    var lastSavedItemId = 0;
    // to store the lat lng from the search bar
    var latitude = 0.0, longitude = 0.0;
    
    $scope.$on('searchStringChanged', function(event, data){
        searchString = data;
        $scope.itemsArray = [[]];
        initPopulate();
//        addToWishList(data);
    });
    
    $scope.$on('locationStringChanged', function(event, lat, lng){
        // called on the page load
        latitude = lat;
        longitude = lng;
        initPopulate();
    });
    
    // Initialising the categories
    $scope.categories = [{label:'ALL',active:true}];
    
    // checking in which page carousel is being loaded
    var user = localStorage.getItem("userloggedin");
	$scope.user_status = user;
    if(user != "" || user != null || user != 'anonymous'){
        if(window.location.hash == '#/mypostings')
            userId = user;
    }
    
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
        
        lastSavedItemId = 0;
        
        $scope.showNext = true;
        
        populateCarousel(lastItem);
    }
    
    var populateCarousel = function(token){
        
        if(category == '' || category == 'ALL')
            category = null;
        
        if(searchString == ''){
            var req = {
                cookie: token,
                userId: userId,
                category: category,
                limit: $scope.itemsLimit,
                lat: latitude,
                lng: longitude
            };
            displayItems(req);
        }else{
            var req = {
                token: token,
                title: searchString,
                description: "",
                category: "",
                leaseValue: 0,
                leaseTerm: "",
            };
            displaySearchedItems(req);
        }
        
    }
    
    var displayItems = function(req){
        getItemsForCarousel.getItems(req).then(
            function(response){
                if(response.data.returnCode == 0){
                    if(lastItem == 0){
                        $scope.itemsArray = [response.data.resList];
                        populateCarousel(response.data.lastItemId);
                    }
                    else{
                        $timeout(function(){
                            $scope.itemsArray.push(response.data.resList);
                        }, 1000);
                    }
                    lastItem = response.data.lastItemId;
                    lastSavedItemId = 0;
                }else{
                    if(lastItem == 0)
                        $scope.itemsArray = [[{ image: 'images/emptycategory.jpg', title: 'Try selecting another category' }]];
                    if(lastSavedItemId == 2){
                        $scope.showNext = false;
                    }
                    lastSavedItemId++;
                }
            },
            function(error){
                console.log("Not able to get items " + error.message);
            });
    }
    
    var displaySearchedItems = function(req){
        $.ajax({
            url: '/flsv2/SearchItem',
            type: 'post',
            data: {req : JSON.stringify(req)},
            contentType: "application/x-www-form-urlencoded",
            dataType: "json",
            success: function(response) {
                if(response.Code == "FLS_SUCCESS"){
                    var obj = JSON.parse(response.Message);
                    var item = {itemId:obj.itemId, image:obj.image, title:obj.title, fullName:obj.category, leaseTerm:obj.leaseTerm, uid:obj.uid};
                    var src = obj.image;
                    if(src == '' || src == 'null' || src == null){
                        src = 'images/imgplaceholder.png';
                        item.image = src;
                    }
                    
                    if(lastItem == 0){
                        $scope.itemsArray = [[item]];
                        lastItem = obj.itemId;
                        populateCarousel(lastItem);
                    }
                    else{
                        lastItem = obj.itemId;
                        $timeout(function(){
                            $scope.itemsArray.push([item]);
                            populateCarousel(lastItem);
                        }, 1000);
                        
                    }
                    lastSavedItemId = 0;
                }else{
                    if(lastItem == 0)
                        $scope.$apply(function(){
                            $scope.itemsArray = [[{ image: 'images/emptycategory.jpg', title: 'Try selecting another category' }]];
                        });
                    if(lastSavedItemId == 2){
                        $scope.showNext = false;
                    }
                    lastSavedItemId++;
                }
            },
            error: function() {
                console.log("Not able to get items " + error.message);
            }
	   });
    }
    
    $scope.loadPrevSlide = function(){
        $scope.showNext = true;
        lastSavedItemId--;
    }
    
    // called when next carousel button is clicked
    $scope.loadNextSlide = function(){
        populateCarousel(lastItem);
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
        
        // when category is selected then search string is set to empty
        searchString = '';
        
        initPopulate();
    }
    
    // called when item from carousel is clicked
    $scope.itemClicked = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
    
    var addToWishList = function(data){
        var req = {
            id: 0,
            title: data,
            description: '',
            category: '',
            userId: user,
            leaseValue: 0,
            leaseTerm: '',
            status: "Wished",
            image: ''
        };
        
        sendToWishList(req);
    }
    
    var sendToWishList = function(req){
        $.ajax({
			url: '/flsv2/WishItem',
			type: 'post',
			data: {req : JSON.stringify(req)},
			contentType: "application/x-www-form-urlencoded",
			dataType:"json",
			success: function(response) {
                if(response.Code == "FLS_SUCCESS")
                    console.log("item added to the wish list");
                else
                    console.log("wished item already exists");
			},
			error: function() {
			}
		});
    }
}]);
                                        
carouselApp.factory('getItemsForCarousel', ['$http', function($http){
    
    var getItems = {};
    
    getItems.getItems = function(req){
        return $http.post('/flsv2/GetItemStoreByX', JSON.stringify(req));
    }
    
    return getItems;
}]);

carouselApp.service('search', ['$rootScope', function($rootScope){
    
    this.changeSearchString = function(searchString){
        $rootScope.$broadcast('searchStringChanged', searchString);
    }
    
}]);