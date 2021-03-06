var userProfileApp = angular.module('userProfileApp', ['headerApp', 'footerApp', 'carouselApp']);

userProfileApp.controller('userProfileCtrl', ['$scope', '$window', 'getItemsForCarousel', 'userFactory', 'bannerService', 'logoutService', 'modalService', 'Map', function($scope, $window, getItemsForCarousel, userFactory, bannerService, logoutService, modalService, Map){
    
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
        items: [],
        address: $window.address,
        about: $window.about,
        website: $window.website,
        mail: $window.mail,
        phoneNo: $window.phoneNo,
        bHours: $window.bHours,
        uber: $window.uber
    };
    
    $scope.offerStuffClicked = function(title){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == 'anonymous'){
            $('#registerModal').modal('show');
        }
        else{
            window.location.replace("myapp.html#/wizard/" + title);
        }
    }
    
    if($window.imageLinks != '' && $window.imageLinks != null)
        $scope.user.imageLinks = $window.imageLinks.split(",");
    else
        $scope.user.imageLinks = [];
    
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
    
    $scope.storeYourStuff = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == 'anonymous'){
            $('#registerModal').modal('show');
        }
        else{
            window.location.replace("myapp.html#/wizard");
        }
    }
    
    $scope.sendMessage = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous"){
            $('#registerModal').modal('show');
        } else if(userFactory.user == $scope.user.userId){
            modalService.showModal({}, {bodyText: "Cannot send message to yourself." ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){}, function(){});
        } else{
            if($scope.user.message == ''){
                modalService.showModal({}, {bodyText: "Please write something." ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){}, function(){});
            }else{
                var req = {
                    userId: userFactory.user,
                    accessToken: userFactory.userAccessToken,
                    from: userFactory.user,
                    to: $scope.user.userId,
                    message: $scope.user.message,
                    subject: "FRIEND",
                    itemId: 0
                }
                $.ajax({
                    url: '/SendMessage',
                    type: 'post',
                    data: JSON.stringify(req),
                    contentType: "application/x-www-form-urlencoded",
                    dataType: "json",
                    success: function(response) {
                        if(response.code==0){
                            $scope.user.message = '';
                            bannerService.updatebannerMessage("Message Sent!!");
                            $("html, body").animate({ scrollTop: 0 }, "slow");
                        }else{
                            modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){eventsCount.updateEventsCount();
                                if(response.code == 400){
                                    logoutService.logout();
                                }
                            }, function(){});
                        }
                    },

                    error: function() {
                        console.log("Not able to send message");
                    }
                });
            }
        }
    }
	
	$scope.addFriend = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous"){
            $('#registerModal').modal('show');
        } else if(userFactory.user == $scope.user.userId){
            modalService.showModal({}, {bodyText: "Cannot add yourself as your friend." ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){}, function(){});
        } else {
            var req = {
                id: $scope.user.userId,
                fullName: $scope.user.userFullName,
                mobile: "-",
                userId: userFactory.user,
                referralCode: localStorage.getItem("userReferralCode")
            }
            $.ajax({
                url: '/AddFriend',
                type:'get',
                data: {req : JSON.stringify(req)},
                contentType:"application/json",
                dataType: "json",
                success: function(response) {
                    if(response.Code == 'FLS_SUCCESS'){
                        bannerService.updatebannerMessage($scope.user.userFullName + " has been added to your friends list.");
                        $("html, body").animate({ scrollTop: 0 }, "slow");
                    }else{
                        modalService.showModal({}, {bodyText: response.Message ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){}, function(){});
                    }
                },
                error: function() {
                    console.log("Invalid Entry");
                }
            });
        }
		
	}
    
    var search = function() {
        Map.search($scope.user.address)
        .then(
            function(res) { // success
                Map.addMarker(res);
            },
            function(status) { // error
                console.log(status);
            }
        );
    }

    google.maps.event.addDomListener(window, "load", function(){
        Map.init();
        search();
    });

}]);

userProfileApp.service('Map', function($q) {

    this.init = function() {
        var options = {
            center: new google.maps.LatLng(40.7127837, -74.00594130000002),
            zoom: 13,
            disableDefaultUI: true
        }
        this.map = new google.maps.Map(document.getElementById("map"), options);
        this.places = new google.maps.places.PlacesService(this.map);
    }

    this.search = function(str) {
        var d = $q.defer();
        this.places.textSearch({query: str}, function(results, status) {
            if (status == 'OK') {
                d.resolve(results[0]);
            }
            else d.reject(status);
        });
        return d.promise;
    }

    this.addMarker = function(res) {
        if(this.marker) this.marker.setMap(null);
        this.marker = new google.maps.Marker({
            map: this.map,
            position: res.geometry.location,
            animation: google.maps.Animation.DROP
        });
        this.map.setCenter(res.geometry.location);
    }

});
