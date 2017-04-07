var flyerIndexApp = angular.module('flyerIndexApp', ['headerApp', 'carouselApp', 'footerApp', 'ngAutocomplete','ui.bootstrap']);

flyerIndexApp.controller('flyerIndexCtrl', ['$scope', '$timeout', 'userFactory', 'statsFactory', 'getItemsForCarousel', 'scrollService', 'searchService', '$rootScope', function($scope, $timeout, userFactory, statsFactory, getItemsForCarousel, scrollService, searchService,$rootScope){
    
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

flyerIndexApp.controller('headerCtrl', ['$scope',
									'$timeout',
									'userFactory',
									'profileFactory',
									'bannerService',
									'searchService',
									'loginSignupService',
                                    'logoutService',
                                    'modalService',
									'eventsCount',
                                    '$rootScope',
									function ($scope,
									$timeout,
									userFactory,
									profileFactory,
									bannerService,
									searchService,
									loginSignupService,
									logoutService,
									modalService,
									eventsCount,
									$rootScope) {

        // sign up starts here

        // variables for storing the location data
        var UserId, Email, Password, Name, Mobile, Location, SignUpStatus, Address = '',
            Sublocality = '',
            Locality = '',
            Code = '',
            Lat = 0.0,
            Lng = 0.0,
            Picture = '',
            FriendId, lastOffset = 0;
        $("#openBtn_credit").hide();

        $scope.$on('signUpCheckReq', function (event, userId, email, password, name, picture, mobile, code, location, signUpStatus, friendId) {
            UserId = userId;
            Email = email;
            Password = (CryptoJS.MD5(password)).toString();
            Name = name;
            if (signUpStatus == "facebook" || signUpStatus == "google") {
                Picture = picture;
            }
            if (Picture === undefined) {
                Picture = "";
            }
            Mobile = mobile;
            FriendId = friendId;
            Code = code;
            if (Code === undefined || Code == null) {
                Code = "";
            }
            Location = location;
            SignUpStatus = signUpStatus;

            Address = '';
            Sublocality = '';
            Locality = '';
            Lat = 0.0;
            Lng = 0.0;

            if (Location != '')
                getLocationData(Location);
            else
                sendAddData();
        });

        var getLocationData = function (location) {
            $.ajax({
                url: 'https://maps.googleapis.com/maps/api/geocode/json',
                type: 'get',
                data: 'address=' + location + "&key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g",
                success: function (response) {
                    if (response.status == 'OK') {
                        Address = response.results[0].formatted_address;
                        response.results[0].address_components.forEach(function (component) {
                            if (component.types.indexOf("sublocality_level_1") != -1)
                                Sublocality = component.long_name;
                            if (component.types.indexOf("locality") != -1)
                                Locality = component.long_name;
                        });
                        Lat = response.results[0].geometry.location.lat;
                        Lng = response.results[0].geometry.location.lng;
                    }
                    sendAddData();
                },
                error: function () {
                    console.log("not able to get location data");
                }
            });
        }

        var sendAddData = function () {
            var Activation = CryptoJS.MD5(UserId);
            Activation = Activation.toString();

            var req = {
                userId: UserId,
                email: Email,
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
                lat: Lat + "",
                lng: Lng + "",
                friendId: FriendId
            }
            signUpSend(req);
        }

        var signUpSend = function (req) {
                $.ajax({
                    url: '/SignUp',
                    type: 'get',
                    data: {
                        req: JSON.stringify(req)
                    },
                    contentType: "application/json",
                    dataType: "json",

                    success: function (response) {
                        if (response.Code === "FLS_SUCCESS") {
                            fbq('track', 'CompleteRegistration');
                            // Google Signup Conversion Start Block
                            // Function goog_report_conversion is called after signup confirmation.
                            goog_snippet_vars = function () {
                                    var w = window;
                                    w.google_conversion_id = 876179168;
                                    w.google_conversion_label = "GWLGCNvrxm4Q4N3loQM";
                                    w.google_remarketing_only = false;
                                }
                                // DO NOT CHANGE THE CODE BELOW.
                            goog_report_conversion = function (url) {
                                goog_snippet_vars();
                                window.google_conversion_format = "3";
                                var opt = new Object();
                                opt.onload_callback = function () {
                                    if (typeof (url) != 'undefined') {
                                        window.location = url;
                                    }
                                }
                                var conv_handler = window['google_trackConversion'];
                                if (typeof (conv_handler) == 'function') {
                                    conv_handler(opt);
                                }
                            }
                            goog_report_conversion(window.location.href);
                            // Google Signup Conversion End Block
                            if (SignUpStatus == "email_pending")
                                loginSignupService.signUpCheckRes(response.Code, "Email verification link send to your email!!");
                            else if (SignUpStatus == "mobile_pending")
                                loginSignupService.signUpCheckRes(response.Code, "OTP has been sent to your phone!!");
                            else {
                                var obj = JSON.parse(response.Message);
                                userFactory.setLocalStorageValues(UserId, Name, obj.access_token, response.Id);
                                if (SignUpStatus == "facebook") {
                                    getFacebookFriends(Email);
                                } else {
                                    window.location.replace("myapp.html#/wizard");
                                }
                            }
                        } else {
                            loginSignupService.signUpCheckRes(response.Code, response.Message);
                        }
                    },
                    error: function () {}
                });
            }
            // sign up ends here

        // login starts here
        $scope.$on('loginCheckReq', function (event, userId, password, picture, signUpStatus) {
            var user_fb_id = null;
            if (signUpStatus == 'facebook') {
                user_fb_id = password + '@fb';
            }
            password = (CryptoJS.MD5(password)).toString();
            if (picture === undefined) {
                picture = "";
            }
            var req = {
                token: userId,
                signUpStatus: signUpStatus,
                row: {
                    auth: password,
                    profilePicture: picture,
                    friendId: user_fb_id
                }
            }
            loginSend(req, signUpStatus);
        });

        var loginSend = function (req, signUpStatus) {
            $.ajax({
                url: '/LoginUser',
                type: 'get',
                data: {
                    req: JSON.stringify(req)
                },
                contentType: "application/json",
                dataType: "json",
                success: function (response) {
                    if (response.Code === "FLS_SUCCESS") {
                        var obj = JSON.parse(response.Message);
                        userFactory.setLocalStorageValues(obj.userId, obj.fullName, obj.access_token, obj.referralCode)
                        if (signUpStatus == "facebook") {
                            getFacebookFriends(obj.userId);
                        } else {
                            window.location.replace("myapp.html#/");
                        }
                    } else {
                        loginSignupService.loginCheckRes(response.Message);
                    }
                },
                error: function () {}
            });
        }

        var getFacebookFriends = function (email) {
            FB.api(
                '/me/friends',
                function (response) {
                    if (response && !response.error) {
                        var friends = response.data.length;
                        if (response.data.length != 0) {
                            for (var i = 0; i <= response.data.length; i++) {
                                if (response.data.hasOwnProperty(i)) {
                                    var friend_name = response.data[i].name;
                                    var friend_id = response.data[i].id + "@fb";
                                    addFriendDbCreate(friend_name, '-', friend_id, email, friends);
                                }
                            }
                        } else {
                            if (friends > 0) {
                                loginSignupService.loginCheckRes("You have " + friends + " Facebook friends on FrrndLease. Say Hi to them!");
                            }
                            window.location.replace("myapp.html#/");
                        }
                    }
                }
            );
        }

        var addFriendDbCreate = function (name, mobile, email, user, friends) {
            if (name == '')
                name = null;
            if (email == '')
                email = null;
            var code = localStorage.getItem("userReferralCode");
            var req = {
                id: email,
                fullName: name,
                mobile: mobile,
                userId: user,
                referralCode: code
            }
            addFriendSend(req, friends);
        }

        var addFriendSend = function (req, friends) {
                $.ajax({
                    url: '/AddFriend',
                    type: 'get',
                    data: {
                        req: JSON.stringify(req)
                    },
                    contentType: "application/json",
                    dataType: "json",

                    success: function (response) {
                        loginSignupService.loginCheckRes("You have " + friends + " Facebook friends in your FrrndLease friendlist");
                        $timeout(function () {
                            window.location.replace("myapp.html#/");
                        }, 5000);
                    },
                    error: function () {
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

        if (window.location.href.indexOf("frrndlease.com") > -1) {
            if (window.location.pathname == '/index.html' || window.location.pathname == '/' || window.location.pathname == '/merchant.html') {
                $scope.navClassValue = "navbar navbar-static";
                $scope.showSearch = false;
                if (window.location.pathname == '/merchant.html')
                    $scope.navClassValue = "navbar navbar-static custom-navbar";
            } else {
                $scope.navClassValue = "navbar navbar-default";
                $scope.showSearch = true;
            }
        } else {
            if (window.location.pathname == '/index.html' || window.location.pathname == '/' || window.location.pathname == '/merchant.html') {
                $scope.navClassValue = "navbar navbar-static";
                $scope.showSearch = false;
                if (window.location.pathname == '/merchant.html')
                    $scope.navClassValue = "navbar navbar-static custom-navbar";
            } else {
                $scope.navClassValue = "navbar navbar-default";
                $scope.showSearch = true;
            }
        }

        if (userFactory.user == "" || userFactory.user == null) {
            localStorage.setItem("userloggedin", "anonymous");
        } else {
            $scope.salutation = userFactory.userName;
        }

        $scope.searching = function () {
                searchService.sendDataToCarousel();
        }

        $scope.searchStringChanged = function (searchString) {
            searchService.saveSearchTitle(searchString);
        }

        $scope.$on('headerLocationChanged', function (event, data) {
            $scope.search.location = data;
        });

        $scope.$on('searchDataEmpty', function (event, data) {
            $scope.search.string = data;
        });

        $scope.isAdmin = function () {
            if (userFactory.user == 'frrndlease@greylabs.org')
                return true;
            else
                return false;
        }

        $scope.isAnonymous = function () {
            if (userFactory.user == "anonymous")
                return true;
            else
                return false;
        }

        $scope.isLoggedIn = function () {
            if (userFactory.user != "anonymous")
                return true;
            else
                return false;
        }

        $scope.logout = function () {
            logoutService.logout();
        }

        $scope.$on('updateEventsCount', function (event) {
            displayUnreadNotifications();
            displayCredits();
        });

        $scope.importfb = function () {

            FB.login(function (response) {

                var ref_code = localStorage.getItem("userReferralCode");
                // handle the response

                // check whether user is logged in or not and ask for credentials if not.

                // send message to facebook friends using send request dialog
                FB.ui({
                    method: 'send',
                    link: 'https://www.frrndlease.com/index.html?ref_token=' + ref_code,
                }, function (response) {
                    if (response && !response.error) {
                        //check 'response' to see if call was successful
                        modalService.showModal({}, {
                            bodyText: "Success, Message to Facebook Friend(s) sent",
                            showCancel: false,
                            actionButtonText: 'Ok'
                        }).then(function (result) {
                            eventsCount.updateEventsCount();
                        }, function () {});
                    }
                });
            }, {
                scope: 'email,public_profile,user_friends'
            });
        }

        // listening broadcast for current location
        $scope.$on('currentLocation', function (event, location) {
            $scope.search.location = location;
        });
                                        
        $scope.$on('getLocation', function(event) {
            // getting the current location
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    function (position) {
                        latitude = position.coords.latitude;
                        longitude = position.coords.longitude;
                        coords = new google.maps.LatLng(latitude, longitude);
                        searchService.saveCurrentLocation(latitude, longitude);
                        var geocoder = new google.maps.Geocoder();
                        var latLng = new google.maps.LatLng(latitude, longitude);
                        geocoder.geocode({
                            'latLng': latLng
                        }, function (results, status) {
                            if (status == google.maps.GeocoderStatus.OK) {
                                $rootScope.$broadcast('currentLocation', results[4].formatted_address);
                            } else {
                                console.log("Geocode was unsucessfull in detecting your current location");
                            }
                        });
                    }, function (error) {
                        console.log(error);
                    }, 
                    {timeout: 30000, enableHighAccuracy: true, maximumAge: 75000}
                );
            } else {
                console.log("Geolocation is not supported by this browser.");
            }
        });

        // getting current location on header load
        userFactory.getCurrentLocation();

}]);

angular.element(document).ready(function() {
  angular.bootstrap(document, ['flyerIndexApp']);
});