var carouselApp = angular.module('carouselApp', []);

carouselApp.controller('carouselCtrl', ['$scope', '$timeout', 'getItemsForCarousel', 'searchService', 'eventsCount', function($scope, $timeout, getItemsForCarousel, searchService, eventsCount){
    // lastItem is used to store the id of the last retrieved item from the database
    var lastItem = 0;
    // selected category is stored in this variable
    var category = '';
	// if this carousel is loaded in myapp/index then match_userId in req var is set to current userId from local storage
    var match_userId = null;
    // if this carousel is loaded in myPostings then userId in req var is set to current userId from local storage
    var userId = null;
    // searchString is used to display items which are being searched
    var searchString = '';
    // to store the lat lng from the search bar
    var latitude = 0.0, longitude = 0.0;
    $scope.search = {};
    $scope.search.show = false;
    $scope.showWishes = false;
    
    $scope.notPosted = false;
    
    var ItemStatus = ['InStore'];
    
    $scope.$on('searchDataChanged', function(event, lat, lng, s){
        // called on the page load
        latitude = lat;
        longitude = lng;
        s = s.toLowerCase();
        if(s != '' && s != "undefined"){
            searchString = s.replace(/[^0-9a-zA-Z ]/g, " ");
            $scope.search.show = true;
        }
        else{
            searchString = '';
            $scope.search.show = false;
        }
        $scope.search.string = searchString;
        initPopulate();
			
        if(searchString != "" && searchString != null && searchString != "undefined")
            addToWishList(searchString);
        
        getPlaceFromCoordinates(latitude,longitude);
    });
    
    $scope.clearSearch = function(){
        searchService.clearSearchTitle();
        searchService.sendDataToCarousel();
    }
    
    // Initialising the categories
    $scope.categories = [{label:'ALL',active:true}];
    $scope.search.category = 'ALL';
    
    // checking in which page carousel is being loaded
    var user = localStorage.getItem("userloggedin");
	$scope.user_status = user;
    if(user != "" || user != null || user != 'anonymous'){
        if(window.location.hash == '#/mypostings'){
            userId = user;
            ItemStatus = ['InStore', 'OnHold', 'LeaseReady', 'PickedUpOut', 'LeaseStarted', 'LeaseEnded', 'PickedUpIn'];
        }else{
			match_userId = user;
		}
    }
    
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
        
        $scope.itemsArray = [];
        
        $scope.showNext = false;
        
        populateCarousel(lastItem);
    }
    
    var populateCarousel = function(token){
        
        if(category == '' || category == 'ALL')
            category = null;
        
        var req = {
            cookie: token,
            userId: userId,
			match_userId: match_userId,
            category: category,
            limit: $scope.itemsLimit,
            lat: latitude,
            lng: longitude,
            searchString: searchString,
            itemStatus: ItemStatus
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
                        $scope.itemsArray.push(response.data.resList);
                    }
                    lastItem = response.data.lastItemId;
					$scope.showNext = true;
                    $scope.notPosted = false;
                }else{
					$scope.showNext = false;
                    if(lastItem == 0){
                        if(category == null){
                            $scope.notPosted = true;
                        }else{
                            $scope.itemsArray = [[{ image: 'images/emptycategory.jpg', title: 'Try selecting another category' }]];
                        }
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
    
    var populateCategory = function(id){
        var req = {
            operation:"getNext",
            token: id
        }
        
        displayCategory(req);
    }
    
    var displayCategory = function(req){
        $.ajax({
            url: '/GetCategoryList',
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
        
        $scope.search.category = category;
        
        initPopulate();
    }
    
    // called when item from carousel is clicked
    $scope.itemClicked = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
    
    $scope.offerStuffClicked = function(title){
       if(user == "" || user == null || user == 'anonymous'){
            $('#registerModal').modal('show');
        }
        else{
            window.location.replace("myapp.html#/wizard/" + title);
        }
    }
    
    var addToWishList = function(data){
        var req = {
            id: 0,
            title: data,
            description: '',
            category: 'House',
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
			url: '/WishItem',
			type: 'post',
			data: {req : JSON.stringify(req)},
			contentType: "application/x-www-form-urlencoded",
			dataType:"json",
			success: function(response) {
                if(response.Code == 0){
                    console.log("item added to the wish list");
                    eventsCount.updateEventsCount();
                }
                else
                    console.log("wished item already exists");
			},
			error: function() {
			}
		});
    }
    
    var getRecentWishes = function(req){
        $.ajax({
            url: '/GetRecentWishes',
            type: 'post',
            data: JSON.stringify(req),
            contentType: "application/x-www-form-urlencoded",
			dataType:"json",
            success: function(response) {
                if(response.code == 0){
                    $scope.showWishes = true;
                    $scope.wishes = response.wishes;
                }else{
                    $scope.showWishes = false;
                }
			},
			error: function() {
			}
        });
    }
    
    getRecentWishes({limit: 10});
    
    var getPlaceFromCoordinates = function(latitude, longitude){
		var geocoder = new google.maps.Geocoder();
		var latLng = new google.maps.LatLng(latitude, longitude);
		geocoder.geocode( { 'latLng': latLng}, function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                var Sublocality = "", Locality = "";
                for(var i in results){
                    results[i].address_components.forEach(function(component){
                        if(component.types.indexOf("sublocality_level_1") != -1)
                            Sublocality = component.long_name;
                        if(component.types.indexOf("locality") != -1)
                            Locality = component.long_name;
                    });
                    if(Sublocality != "" && Locality != "")
                        break;
                }
                $scope.$apply(function(){
                    $scope.search.place = Sublocality+","+Locality;
                });
            }else{
                console.log("Geocode was unsucessfull in detecting your current location");
            }
        });
    }
}]);
                                        
carouselApp.factory('getItemsForCarousel', ['$http', function($http){
    
    var getItems = {};
    
    getItems.getItems = function(req){
        return $http.post('/GetItemStoreByX', JSON.stringify(req));
    }
    
    return getItems;
}]);