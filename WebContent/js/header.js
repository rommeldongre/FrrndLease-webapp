var headerApp = angular.module('headerApp', ['ui.bootstrap', 'ngAutocomplete']);

headerApp.controller('headerCtrl', ['$scope', 'userFactory', 'profileFactory', 'searchService', 'statsFactory', 'loginSignupService', function($scope, userFactory, profileFactory, searchService, statsFactory, loginSignupService){
    
    // sign up starts here
    
    // variables for storing the location data
    var Email, Password, Name, Mobile, Location, SignUpStatus, Address = '', Sublocality = '', Locality = '', Code='', Lat = 0.0, Lng = 0.0,Picture='';
    
    $scope.$on('signUpCheckReq', function(event, email, password, name, picture, mobile, code, location, signUpStatus){
        Email = email;
        Password = (CryptoJS.MD5(password)).toString();
        Name = name;
		if(signUpStatus == "facebook"){
			Picture = picture;
		}
        Mobile = mobile;
		Code = code;
        Location = location;
        SignUpStatus = signUpStatus;
        
        Address = '';
        Sublocality = '';
        Locality = '';
        Lat = 0.0;
        Lng = 0.0;
        
        if(Location != '')
            getLocationData(Location);
        else
            sendAddData();
    });
	
    var getLocationData = function(location){
        $.ajax({
            url: 'https://maps.googleapis.com/maps/api/geocode/json',
            type: 'get',
            data: 'address='+location+"&key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g",
            success: function(response){
                if(response.status == 'OK'){
                    Address = response.results[0].formatted_address;
                    response.results[0].address_components.forEach(function(component){
                        if(component.types.indexOf("sublocality_level_1") != -1)
                            Sublocality = component.long_name;
                        if(component.types.indexOf("locality") != -1)
                            Locality = component.long_name;
                    });
                    Lat = response.results[0].geometry.location.lat;
                    Lng = response.results[0].geometry.location.lng;
                }
                sendAddData();
            },
            error: function(){
                console.log("not able to get location data");
            }
        });
    }
    
    var sendAddData = function(){
        var Activation = CryptoJS.MD5(Email);
        Activation = Activation.toString();
            
        var req = {
            userId: Email,
            fullName: Name,
			profilePicture: Picture,
            mobile: Mobile,
			referralCode: Code,
            location: Location,
            auth: Password,
            activation: Activation,
            status: SignUpStatus,
            address: Address,
            locality: Locality,
            sublocality: Sublocality,
            lat: Lat+"",
            lng: Lng+""
        }
        signUpSend(req);
    }
    
    var signUpSend = function(req){
        $.ajax({
            url: '/flsv2/SignUp',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",

            success: function(response) {
				if(response.Code === "FLS_SUCCESS") {
                    if(SignUpStatus != "email_pending"){
                        localStorage.setItem("userloggedin", Email);
                        localStorage.setItem("userloggedinName", Name);
						localStorage.setItem("userReferralCode", response.Id);
                        if(SignUpStatus == "facebook")
                            getFacebookFriends(Email);
                        else
                            window.location.replace("myapp.html#/");
                    }else{
                        loginSignupService.signUpCheckRes("Email verification link send to your email!!");
                    }
				}else{
                    if(response.Id == 200)
                        loginSignupService.signUpCheckRes("User Already Exists!!");
				}
            },		
            error: function() {
            }
        });
    }
    // sign up ends here
    
    // login starts here
    $scope.$on('loginCheckReq', function(event, email, password, picture, signUpStatus){
        password = (CryptoJS.MD5(password)).toString();
        var req = {
            auth: password,
            token: email,
			profilePicture: picture,
            signUpStatus: signUpStatus
        }
        loginSend(req, signUpStatus);
    });
    
    var loginSend = function(req, signUpStatus){
        $.ajax({
            url: '/flsv2/LoginUser',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    var obj = JSON.parse(response.Message);
                    localStorage.setItem("userloggedin", obj.userId);
                    localStorage.setItem("userloggedinName", obj.fullName);
					localStorage.setItem("userReferralCode", obj.referralCode);
                    if(signUpStatus == "facebook")
                        getFacebookFriends(obj.userId);
                    else
                        window.location.replace("myapp.html#/");
                }
                else{
                    loginSignupService.loginCheckRes(response.Message);
                }
            },
            error: function() {}
        });
    }
    
    var getFacebookFriends = function(email){
        FB.api(
            '/me/friends', function (response) {
                if (response && !response.error) {
                    var friends = response.data.length;
                    for(var i =0;i<=response.data.length;i++){
                        if (response.data.hasOwnProperty(i)) {
                            var friend_name = response.data[i].name;
                            var friend_id= response.data[i].id+"@fb";
							addFriendDbCreate(friend_name, '-', friend_id, email, friends);
                        }
                    }
                }
            }
        );
    }

    var addFriendDbCreate = function(name, mobile, email, user, friends){
        if(name == '')
            name = null;
        if(email == '')
            email = null;
		var code = localStorage.getItem("userReferralCode");
        var req = {
            id: email,
            fullName: name,
            mobile: mobile,
            userId: user,
			referralCode: code
        }
		console.log("Import FB friends:");
		console.log(req);
        addFriendSend(req, friends);
    }

    var addFriendSend = function(req, friends){
        $.ajax({
            url: '/flsv2/AddFriend',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",

            success: function(response) {
                loginSignupService.loginCheckRes("You have " + friends + " Facebook friends in your fRRndLease friendlist");
                window.location.replace("myapp.html#/");
            },
            error: function() {
                window.location.replace("myapp.html#/");
            }
        });
    }
    // login ends here
    
    $scope.search = {};
    
    $scope.options = {
        country: 'in',
        sendToCarousel: true
    };
    
    $scope.details = '';
    
    if(window.location.href.indexOf("frrndlease.com") > -1){
        if(window.location.pathname == '/index.html' || window.location.pathname == '/'){
            $scope.navClassValue = "navbar navbar-static";
            $scope.showSearch = false;
        }
        else{
            $scope.navClassValue = "navbar navbar-default";
            $scope.showSearch = true;
        }
    }else{
        if(window.location.pathname == '/flsv2/index.html' || window.location.pathname == '/flsv2/'){
            $scope.navClassValue = "navbar navbar-static";
            $scope.showSearch = false;
        }
        else{
            $scope.navClassValue = "navbar navbar-default";
            $scope.showSearch = true;
        }
    }
    
    if(userFactory.user == "" || userFactory.user == null){
        localStorage.setItem("userloggedin", "anonymous");	
    }else{
        $scope.salutation = userFactory.userName;
    }
    
    $scope.searching = function(){
        if(window.location.hash == '#/')
            searchService.sendDataToCarousel();
        else
            window.location.replace('myapp.html#/');
    }
    
    $scope.searchStringChanged = function(searchString){
        searchService.saveSearchTitle(searchString);
    }
    
    $scope.$on('headerLocationChanged', function(event, data){
        $scope.search.location = data;
    });
    
    $scope.$on('searchDataEmpty', function(event, data){
        $scope.search.string = data;
    });
    
	var displayStats = function(){
        statsFactory.getStats().then(
        function(response){
            if (response.data.message == "Success") {
                $scope.item_count = response.data.itemCount;
				$scope.user_count = response.data.userCount;
            } else {
                $scope.item_count = "";
				$scope.user_count = "";
            }
        },
        function(error){
            console.log("unable to get count: " + error.message);
        });
    }
	
    var displayCredits = function(){
        profileFactory.getProfile(userFactory.user).then(
        function(response){
            if (response.data.code == 0) {
                $scope.search.location = response.data.sublocality+","+response.data.locality;
                $scope.credits = response.data.credit;
                searchService.saveCurrentLocation(response.data.lat,response.data.lng);
            } else {
                $scope.credits = "";
            }
        },
        function(error){
            console.log("unable to get credits: " + error.message);
        });
    }
    
    // populating the credits
    displayCredits();
	
	// populating the site Stats
	displayStats();
	
    $scope.isAdmin = function(){
        if(userFactory.user == 'frrndlease@greylabs.org')
            return true;
        else
            return false;
    }
    
    $scope.isAnonymous = function(){
        if(userFactory.user == "anonymous")
            return true;
        else
            return false;
    }
    
    $scope.isLoggedIn = function(){
        if(userFactory.user != "anonymous")
            return true;
        else
            return false;
    }
    
    $scope.logout = function(){
        localStorage.setItem("userloggedin", "anonymous");  //userloggedin-> anonymous
        var auth2 = gapi.auth2.getAuthInstance();
        auth2.signOut().then(function() {
            console.log('User signed out.');
        });
											
        window.location.replace("index.html");
    }
    
    $scope.storeYourStuff = function(){
        localStorage.setItem("prevFunc", 'storeYourStuff');
			
		window.location.replace("EditPosting.html");
    }
    
}]);

// factory for getting Site Stats from the backend service
headerApp.factory('statsFactory', ['$http', function($http){
    
    var dataFactory = {};
    
    dataFactory.getStats = function(){
        return $http.post('/flsv2/GetSiteStats', JSON.stringify({empty_pojo: ""}));
    }
    return dataFactory;
}]);

// factory for getting and updating profile from the backend service
headerApp.factory('profileFactory', ['$http', function($http){
    
    var dataFactory = {};
    
    dataFactory.getProfile = function(user){
        return $http.post('/flsv2/GetProfile', JSON.stringify({userId : user}));
    }
    
    dataFactory.updateProfile = function(req){
        return $http.post('/flsv2/EditProfile', JSON.stringify(req));
    }
    
    return dataFactory;
}]);

headerApp.factory('userFactory', function(){
    
    var dataFactory = {};
    
    dataFactory.user = localStorage.getItem("userloggedin");
    
    dataFactory.userName = localStorage.getItem("userloggedinName");
    
    return dataFactory;
});

// service to implement modal
headerApp.service('modalService', ['$uibModal',
    function ($uibModal) {
        
        var modalDefaults = {
            animation: true,
            backdrop: true,
            keyboard: true,
            templateUrl: '/flsv2/modal.html'
        };

        var modalOptions = {
            actionButtonText: 'YES',
            showCancel: true,
			labelText: 'Default Label Text',
            submitting: false,
            cancelButtonText: 'Cancel',
            headerText: 'Frrndlease Says',
            bodyText: 'Perform this action?'
        };
        
        this.showModal = function (customModalDefaults, customModalOptions) {
            if (!customModalDefaults) customModalDefaults = {};
            customModalDefaults.backdrop = 'static';
            return this.show(customModalDefaults, customModalOptions);
        };

        this.show = function (customModalDefaults, customModalOptions) {
            //Create temp objects to work with since we're in a singleton service
            var tempModalDefaults = {};
            var tempModalOptions = {};

            //Map angular-ui modal custom defaults to modal defaults defined in service
            angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

            //Map modal.html $scope custom properties to defaults defined in service
            angular.extend(tempModalOptions, modalOptions, customModalOptions);

            if (!tempModalDefaults.controller) {
                tempModalDefaults.controller = function ($scope, $uibModalInstance) {
                    $scope.submit = {};
                    $scope.modalOptions = tempModalOptions;
                    $scope.modalOptions.ok = function (result) {
                        $uibModalInstance.close($scope.submit.url);
                    };
                    $scope.modalOptions.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }
            }

            return $uibModal.open(tempModalDefaults).result;
        };

    }]);

headerApp.service('searchService', ['$rootScope', function($rootScope){
    
    this.lat = 0.0;
    this.lng = 0.0;
    
    this.searchTitle = '';
    
    this.saveCurrentLocation = function(lat, lng){
        this.lat = lat;
        this.lng = lng;
    }
    
    this.saveSearchTitle = function(searchTitle){
        this.searchTitle = searchTitle;
    }
    
    this.clearSearchTitle = function(){
        this.searchTitle = '';
        $rootScope.$broadcast('searchDataEmpty', this.searchTitle);
    }
    
    this.updateHeaderLocation = function(data){
        $rootScope.$broadcast('headerLocationChanged', data);
    }
    
    this.sendDataToCarousel = function(){
        $rootScope.$broadcast('searchDataChanged', this.lat, this.lng, this.searchTitle);
    }
    
}]);

headerApp.service('loginSignupService', ['$rootScope', function($rootScope){
    
    this.loginCheckReq = function(email, password, picture, signUpStatus){
        $rootScope.$broadcast('loginCheckReq', email, password, picture, signUpStatus);
    }
    
    this.loginCheckRes = function(message){
        $rootScope.$broadcast('loginCheckRes', message);
    }
    
    this.signUpCheckReq = function(email, password, name, picture,mobile, code, location, signUpStatus){
        $rootScope.$broadcast('signUpCheckReq', email, password, name, picture ,mobile, code, location, signUpStatus);
    }
    
    this.signUpCheckRes = function(message){
        $rootScope.$broadcast('signUpCheckRes', message);
    }
    
}]);

headerApp.controller('loginModalCtrl', ['$scope', 'loginSignupService', function($scope, loginSignupService){
    // Form login
    $scope.formLogin = function(email, password){
        if(email == 'admin@frrndlease.com' || email == 'ops@frrndlease.com')
            $scope.error = "This user cannot access the website";
        else
            loginSignupService.loginCheckReq(email, password, "", "email_activated");
    }
    
    // Google login
    function onSignIn(googleUser) {
        var profile = googleUser.getBasicProfile();
        loginSignupService.loginCheckReq(profile.getEmail(), profile.getId(), "", "google");
    }
    window.onSignIn = onSignIn;
    
    // facebook login
    $scope.facebookSignIn = function() {
        FB.login(function(response) {
            // handle the response
            FB.api('/me?fields=id,name,email,first_name,last_name,locale,gender,picture.type(large)', function(response) {
                loginSignupService.loginCheckReq(response.email, response.id, response.picture.data.url,"facebook");
            });
        }, {scope: 'email,public_profile,user_friends'});    
    }
    
    // Login response
    $scope.$on('loginCheckRes', function(event, message){
        $scope.$apply(function(){
            $scope.error = message;
        });
    });
    
}]);

headerApp.controller('signUpModalCtrl', ['$scope', 'loginSignupService', function($scope, loginSignupService){
    
    // form sign up
    $scope.formSignup = function(email, password, name, mobile, code, location){
        loginSignupService.signUpCheckReq(email, password, name, "", mobile, code, location, "email_pending");
    }
    
    // Google sign up
    function onSignUp(googleUser) {
        var profile = googleUser.getBasicProfile();
        loginSignupService.signUpCheckReq(profile.getEmail(), profile.getId(), profile.getName(), "", "", $scope.code, $scope.location, "google");
    }
    window.onSignUp = onSignUp;
    
    // facebook sign up
    $scope.facebookSignIn = function() {
        FB.login(function(response) {
            // handle the response
            FB.api('/me?fields=id,name,email,first_name,last_name,locale,gender,picture.type(large)', function(response) {
                loginSignupService.signUpCheckReq(response.email, response.id, response.name, response.picture.data.url, "", $scope.code, $scope.location, "facebook");
            });
        }, {scope: 'email,public_profile,user_friends'});    
    }
    
    // sign up response
    $scope.$on('signUpCheckRes', function(event, message){
        $scope.$apply(function(){
            $scope.error = message;
        });
    });
	
	var getQueryVariable = function (variable) {
        var query = window.location.search.substring(1);
        var vars = query.split("&");
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            if (pair[0] == variable) {
                return pair[1];
            }
        }
        console.log('Token= ' + variable + ' not found');
		}
    
	if(window.location.href.indexOf("index.html") > -1){
	    console.log("index page");
		var token = getQueryVariable("ref_token");
		console.log(token);
		if(token === undefined || token=="undefined"){
		}else{
			$scope.code = token;
			localStorage.setItem("friendReferralCode",token);
		}	
		var ref_code = localStorage.getItem("friendReferralCode");
		$scope.code = ref_code;
	}
    
	
    
    // remove this code and uncomment the below one when using https
    $scope.location = "Gokhalenagar, Pune, Maharashtra, India";
    
//    // getting the current location
//    var getLocation = function() {
//        if (navigator.geolocation) {
//            navigator.geolocation.getCurrentPosition(showPosition);
//        } else { 
//            console.log("Geolocation is not supported by this browser.");
//        }
//    }
//
//    var showPosition = function(position) {
//		latitude = position.coords.latitude;
//		longitude = position.coords.longitude;
//		coords = new google.maps.LatLng(latitude, longitude);	
//			
//		var geocoder = new google.maps.Geocoder();
//		var latLng = new google.maps.LatLng(latitude, longitude);
//		geocoder.geocode( { 'latLng': latLng}, function(results, status) {
//            if (status == google.maps.GeocoderStatus.OK) {
//                $scope.$apply(function(){
//                   $scope.location = results[4].formatted_address; 
//                });
//            }else{
//                console.log("Geocode was unsucessfull in detecting your current location");
//            }
//        });
//    }
//    
//    getLocation();
}]);