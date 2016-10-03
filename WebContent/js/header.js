var headerApp = angular.module('headerApp', ['ui.bootstrap', 'ngAutocomplete']);

headerApp.controller('headerCtrl', ['$scope', 
									'$timeout', 
									'userFactory', 
									'profileFactory', 
									'bannerService',
									'matchFbIdService',
									'searchService', 
									'statsFactory', 
									'loginSignupService',
                                    'logoutService',
                                    'modalService',
									function($scope, 
									$timeout, 
									userFactory, 
									profileFactory, 
									bannerService, 
									matchFbIdService,
									searchService, 
									statsFactory, 
									loginSignupService,
                                    logoutService,
                                    modalService){
    
    // sign up starts here
    
    // variables for storing the location data
    var UserId, Email, Password, Name, Mobile, Location, SignUpStatus, Address = '', Sublocality = '', Locality = '', Code='', Lat = 0.0, Lng = 0.0,Picture='',FriendId,lastOffset = 0;
    $("#openBtn_credit").hide();
	
    $scope.$on('signUpCheckReq', function(event, userId, email, password, name, picture, mobile, code, location, signUpStatus, friendId){
        UserId = userId;
        Email = email;
        Password = (CryptoJS.MD5(password)).toString();
        Name = name;
		if(signUpStatus == "facebook" || signUpStatus == "google"){
			Picture = picture;
		}
		if(Picture === undefined){
			Picture ="";
		}
        Mobile = mobile;
        FriendId = friendId;
		Code = code;
		if(Code === undefined || Code==null){
			Code ="";
		}
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
            lat: Lat+"",
            lng: Lng+"",
            friendId: FriendId
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
                    if(SignUpStatus == "email_pending")
                        loginSignupService.signUpCheckRes("Email verification link send to your email!!");
                    else if(SignUpStatus == "mobile_pending")
                        loginSignupService.signUpCheckRes("OTP has been sent to your phone!!");
                    else{
                        var obj = JSON.parse(response.Message);
						userFactory.setLocalStorageValues(UserId,Name,obj.access_token,response.Id);
                        if(SignUpStatus == "facebook"){
                            getFacebookFriends(Email);
							matchFbIdService.updateFbId();
                        }else{
                            window.location.replace("myapp.html#/wizard");
						}
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
    $scope.$on('loginCheckReq', function(event, userId, password, picture, signUpStatus){
		var user_fb_id= null;
		if(signUpStatus =='facebook'){
			user_fb_id = password+'@fb';
		}
	    password = (CryptoJS.MD5(password)).toString();
		if(picture === undefined){
			picture ="";
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
					userFactory.setLocalStorageValues(obj.userId,obj.fullName,obj.access_token,obj.referralCode)
                    if(signUpStatus == "facebook"){
                        getFacebookFriends(obj.userId);
						matchFbIdService.updateFbId();
					}else{
                        window.location.replace("myapp.html#/");
					}
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
					if(response.data.length!=0){
						for(var i =0;i<=response.data.length;i++){
							if (response.data.hasOwnProperty(i)) {
								var friend_name = response.data[i].name;
								var friend_id= response.data[i].id+"@fb";
								addFriendDbCreate(friend_name, '-', friend_id, email, friends);
							}
						}
					}else{
						if(friends>0){
							loginSignupService.loginCheckRes("You have " + friends + " Facebook friends in your FrrndLease friendlist");
						}
						window.location.replace("myapp.html#/");
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
                loginSignupService.loginCheckRes("You have " + friends + " Facebook friends in your FrrndLease friendlist");
                $timeout(function(){
					window.location.replace("myapp.html#/");
				}, 5000);
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
	
	$scope.showCredit = function(){
		$("#openBtn_credit").click();
		$scope.showNext = true;
		getCredit(lastOffset);
	}
    
	var getCredit = function(Offset){
		var req = {
			userId : userFactory.user,
			cookie: Offset,
			limit: 3
		}
		
		getCreditSend(req);
	}
	
	var getCreditSend = function(req){
		$.ajax({
            url: '/flsv2/GetCreditTimeline',
            type: 'post',
            data: JSON.stringify(req),
			contentType:"application/json",
			dataType:"json",
            success: function(response){
				if(response.returnCode == 0){
                if(lastOffset == 0){
					$scope.$apply(function(){
						$scope.creditsArray = [response.resList];
					});
                        getCredit(response.lastItemId);
                    }else{
						$scope.$apply(function(){
						$scope.creditsArray.push(response.resList);
						});
                    }
                    lastOffset = response.lastItemId;
				}else{
					$scope.showNext = false;
					console.log("ReturnCode not Zero");
                }
            },
            error: function(){
                console.log("not able to get credit log data");
            }
	
        });
	}
	
	// called when Show More Credits button is clicked
    $scope.loadNextCredit = function(){
        getCredit(lastOffset);
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
    
	$scope.$on('bannerMessage', function(event, data, page){
        // updating the notifications count in the header
        displayUnreadNotifications();
		$scope.successBanner = data;
		$scope.bannerVal = true;
		$timeout(function(){
			$scope.bannerVal = false;
			if(page === undefined || page == null || page == ""){
			}else{
				window.location.replace(page);
			}
		}, 5000);
    });
	
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
    
    $scope.head = {};
    
    var displayUnreadNotifications = function(){
        $.ajax({
			url: '/flsv2/GetUnreadEventsCount',
			type: 'post',
			data: JSON.stringify({userId: userFactory.user}),
			contentType:"application/json",
			dataType:"json",
			
			success: function(response) {
                if(response.code == 0){
                    $scope.$apply(function(){
                        $scope.head.unread = response.unreadCount;
                    });
                }
			},
			error: function() {
			}
		});
    }
    
    // populating the credits
    displayCredits();
	
	// populating the site Stats
	displayStats();
                                        
    // fetching the unread notifications
    displayUnreadNotifications();
	
    $scope.openNotifications = function(){
        window.location.replace("myapp.html#/mynotifications");
    }
                                        
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
        logoutService.logout();
    }
    
    $scope.$on('updateEventsCount', function(event){
        displayUnreadNotifications();
        displayCredits();
    });
	
	$scope.$on('matchFbId', function(event){
		var req = {
			userId: userFactory.user,
            accessToken: userFactory.userAccessToken
		};
		
		$.ajax({
			url: '/flsv2/MatchFbId',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code!=0){
					console.log(response.code);
					console.log(response.message);
				}
			},
			error: function() {
				alert('Not Working');
			}
		});
		
		
    });
                                        
    $scope.$on('addPromoCredits', function(event, promoCode){
        var req = {
            userId: userFactory.user,
            promoCode: promoCode,
            accessToken: userFactory.userAccessToken
        }
        
        $.ajax({
            url: '/flsv2/AddPromoCredits',
            type: 'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType:"json",
            success: function(response){
                modalService.showModal({}, {bodyText: response.message,showCancel: false,actionButtonText: 'Ok'}).then(
                    function(r){
                        if(response.code == 0){
                            $scope.credits = response.newCreditBalance;
                            userFactory.userCreditsRes(response.newCreditBalance);
                        }
                        if(response.code == 400)
                            logoutService.logout();
                    }, function(){});
            },
            error: function(){
                console.log("not able to add promo credit");
            }
        });
        
    });
    
}]);

headerApp.service('logoutService', function(){
    
    var l = {};
    
    l.logout = function(){
        localStorage.clear();
        localStorage.setItem("userloggedin", "anonymous");
        var auth2 = gapi.auth2.getAuthInstance();
        auth2.signOut().then(function() {
            console.log('User signed out.');
        });

        window.location.replace("index.html");
    }
    
    return l;
    
});

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

headerApp.factory('userFactory', ['$rootScope', function($rootScope){
    
    var dataFactory = {};
    
    dataFactory.user = localStorage.getItem("userloggedin");
    
    dataFactory.userName = localStorage.getItem("userloggedinName");
    
    dataFactory.userAccessToken = localStorage.getItem("userloggedinAccess");
    
    dataFactory.userCredits = function(promo){
        $rootScope.$broadcast('addPromoCredits', promo);
    }
    
    dataFactory.userCreditsRes = function(credits){
        $rootScope.$broadcast('updatedCredits', credits);
    }
	
	dataFactory.setLocalStorageValues = function(userId,fullName,access_token,referralCode){
		localStorage.setItem("userloggedin",userId);
        localStorage.setItem("userloggedinName",fullName);
        localStorage.setItem("userloggedinAccess",access_token);
		localStorage.setItem("userReferralCode",referralCode);
		
		dataFactory.user = localStorage.getItem("userloggedin");
		dataFactory.userName = localStorage.getItem("userloggedinName");
		dataFactory.userAccessToken = localStorage.getItem("userloggedinAccess");
    }
    
    return dataFactory;
}]);

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
            actionButtonText: 'Yes',
            showCancel: true,
			labelText: 'Default Label Text',
            submitting: false,
			messaging: false,
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

headerApp.service('bannerService', ['$rootScope', function($rootScope){
    
    this.updatebannerMessage = function(data, page){
		this.data = data;
		this.page = page;
        $rootScope.$broadcast('bannerMessage', this.data, this.page);
    }
}]);

headerApp.service('matchFbIdService', ['$rootScope', function($rootScope){
    
    this.updateFbId = function(){
        $rootScope.$broadcast('matchFbId');
    }
}]);

headerApp.service('eventsCount', ['$rootScope', function($rootScope){
    
    this.updateEventsCount = function(){
        $rootScope.$broadcast('updateEventsCount');
    }
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
    
    this.loginCheckReq = function(userId, password, picture, signUpStatus){
        $rootScope.$broadcast('loginCheckReq', userId, password, picture, signUpStatus);
    }
    
    this.loginCheckRes = function(message){
        $rootScope.$broadcast('loginCheckRes', message);
    }
    
    this.signUpCheckReq = function(userId, email, password, name, picture,mobile, code, location, signUpStatus, friendId){
        $rootScope.$broadcast('signUpCheckReq', userId, email, password, name, picture ,mobile, code, location, signUpStatus, friendId);
    }
    
    this.signUpCheckRes = function(message){
        $rootScope.$broadcast('signUpCheckRes', message);
    }
    
}]);

headerApp.directive('loadImage', ['$http', function($http){
    return{
        restrict:'A',
        scope: {
            'loadImage': '=',
            'maxWidth': '=?',
            'maxHeight': '=?'
        },
        link: function(scope, element, attrs){
            scope.$watch('loadImage', function(){
                var MaxWidth = 300;
                var MaxHeight = 300;
                
                var ImgSrc = scope.loadImage;

                if(scope.maxWidth)
                    MaxWidth = scope.maxWidth;
                if(scope.maxHeight)
                    MaxHeight = scope.maxHeight;
                
                if(ImgSrc != 'loading' && ImgSrc != '' && ImgSrc != null && ImgSrc != 'null' && ImgSrc != undefined){
                    loadImage(
                        ImgSrc,
                        function(canvas){
                            element.removeAttr('width');
                            element.removeAttr('height');
                            attrs.$set('ngSrc', canvas.toDataURL());
                        },
                        {
                            maxWidth: MaxWidth,
                            maxHeight: MaxHeight,
                            canvas: true,
                            crossOrigin: "anonymous"
                        }
                    );
                }else if(ImgSrc === '' || ImgSrc === null || ImgSrc === 'null' || ImgSrc === undefined){
                    attrs.$set('width', MaxWidth);
                    attrs.$set('height', MaxHeight);
                    attrs.$set('ngSrc', 'images/imgplaceholder.png');
                }
                
            });
        }
    }
}]);

headerApp.directive('uploadImage', ['userFactory', 'modalService', function(userFactory, modalService) {
    return {
        scope: {
            uploadImage: '=',
            uid: '@',
            id: '@'
        },
        link: function(scope, element, attrs) {
            element.bind("change", function (changeEvent) {
                
                EXIF.getData(changeEvent.target.files[0], function(){
                    exif = EXIF.getAllTags(this);
                    picOrientation = exif.Orientation;
                });
                
                var reader = new FileReader();
                reader.onload = function (loadEvent) {                   
                    loadImage(
                        loadEvent.target.result,
                        function(canvas){
                            var req = {
                                userId: userFactory.user,
                                accessToken: userFactory.userAccessToken,
                                image: canvas.toDataURL(),
                                uid: scope.uid,
                                existingLink: scope.uploadImage.link,
                                primary: false
                            }

                            scope.$apply(function () {
                                scope.uploadImage.link = "loading";
                            });

                            $.ajax({
                                url: '/flsv2/SaveImageInS3',
                                type: 'post',
                                data: JSON.stringify(req),
                                contentType: "application/x-www-form-urlencoded",
                                dataType: "json",

                                success: function(response) {
                                    if(response.code == 0){
                                        scope.$apply(function () {
                                            scope.uploadImage.link = response.imageLink;
                                        });
                                    }else{
                                        scope.$apply(function () {
                                            scope.uploadImage.link = "";
                                        });
                                        modalService.showModal({}, {bodyText: response.message,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
                                            if(response.code == 400)
                                                logoutService.logout();
                                        },function(){});
                                    }
                                },

                                error: function() {
                                    modalService.showModal({}, {bodyText: "Something is Wrong with the network.",showCancel: false,actionButtonText: 'Ok'}).then(function(result){},function(){});
                                }
                            });
                        },
                        {
                            maxWidth: 450,
                            maxHeight: 450,
                            canvas: true,
                            crossOrigin: "anonymous",
                            orientation: picOrientation
                        }
                    );
                    
                }
                reader.readAsDataURL(changeEvent.target.files[0]);
            });
        }
    };
}]);

headerApp.controller('loginModalCtrl', ['$scope', 'loginSignupService', 'modalService', function($scope, loginSignupService, modalService){
    
    var email = /^\w+([-+.']\ w+)*@\w+([-.]\ w+)*\.\w+([-.]\ w+)*$/;
    var mobile = /(\d+$)$/;
    
    // Form login
    $scope.formLogin = function(userId, password){
        if(userId == 'admin@frrndlease.com' || userId == 'ops@frrndlease.com')
            $scope.error = "This user cannot access the website";
        else{
            if(email.test(userId))
                loginSignupService.loginCheckReq(userId, password, "", "email_activated");
            else
                loginSignupService.loginCheckReq(userId, password, "", "mobile_activated");
        }
    }
    
    // Google login
    function onSignIn(googleUser) {
        var profile = googleUser.getBasicProfile();
        loginSignupService.loginCheckReq(profile.getEmail(), profile.getId(), profile.getImageUrl(), "google");
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
    
    $scope.forgotPassword = function(userId){
        if(userId == 'admin@frrndlease.com' || userId == 'ops@frrndlease.com')
            $scope.error = "This user cannot access the website";
        else{
            if(email.test(userId))
                process_dialog("Sending email to reset the password!!");
            else
                process_dialog("Sending OTP to reset the password!!");
            $.ajax({
                url: '/flsv2/ForgotPassword',
                type:'POST',
                data: JSON.stringify({userId:userId}),
                contentType:"application/json",
                dataType: "JSON",
                success: function(response) {
                    $("#myPleaseWait").modal('toggle');
                    
                    if(mobile.test(userId)){
                        $('#loginModal').modal('hide');
                        modalService.showModal({}, {submitting: true, labelText: 'Enter the OTP sent to your mobile number', actionButtonText: 'Submit'}).then(function(result){
                            if(response.code == 300){
                                $.ajax({
                                    url: '/flsv2/Verification',
                                    type:'POST',
                                    data: JSON.stringify({verification : result+"_u"}),
                                    contentType:"application/json",
                                    dataType: "JSON",
                                    success: function(res) {
                                        if(res.code == 0){
                                            window.location.replace("forgotpassword.html?act="+result+"_u");
                                        }
                                    },
                                    error: function() {
                                    }
                                });
                            } else if(response.code == 0){
                                window.location.replace("forgotpassword.html?act="+result+"_u");
                            }
                        }, function(){});
                    }
                    
                    $scope.$apply(function(){
                        $scope.error = response.message;
                    });
                },
                error: function() {
                    $("#myPleaseWait").modal('toggle');
                }
            });
        }
    }
    
}]);

headerApp.controller('signUpModalCtrl', ['$scope', 'loginSignupService', 'modalService', function($scope, loginSignupService, modalService){
    
    var friendId = "";
    
    var signUpStatus = "";
    
    $scope.userIdChanged = function(){
        
        var email = /^\w+([-+.']\ w+)*@\w+([-.]\ w+)*\.\w+([-.]\ w+)*$/;
        var mobile = /(\d+$)$/;
        
        if(email.test($scope.userId)){
            $scope.email = $scope.userId;
            $scope.emailEditable = true;
            signUpStatus = "email_pending";
        }
        else{
            $scope.email = '';
            $scope.emailEditable = false;
        }
        
        if(mobile.test($scope.userId)){
            $scope.mobile = $scope.userId;
            $scope.mobileEditable = true;
            signUpStatus = "mobile_pending";
        }
        else{
            $scope.mobile = '';
            $scope.mobileEditable = false;
        }
    }
    
    // form sign up
    $scope.formSignup = function(email, password, name, mobile, code, location){
        friendId = email;
        loginSignupService.signUpCheckReq($scope.userId, email, password, name, "", mobile, code, location, signUpStatus, friendId);
    }
    
    // Google sign up
    function onSignUp(googleUser) {
        var profile = googleUser.getBasicProfile();
        friendId = profile.getEmail();
        loginSignupService.signUpCheckReq(profile.getEmail(), profile.getEmail(), profile.getId(), profile.getName(), profile.getImageUrl(), "", $scope.code, $scope.location, "google", friendId);
    }
    window.onSignUp = onSignUp;
    
    // facebook sign up
    $scope.facebookSignIn = function() {
        FB.login(function(response) {
            // handle the response
            FB.api('/me?fields=id,name,email,first_name,last_name,locale,gender,picture.type(large)', function(response) {
                friendId = response.id + "@fb";
                loginSignupService.signUpCheckReq(response.email, response.email, response.id, response.name, response.picture.data.url, "", $scope.code, $scope.location, "facebook", friendId);
            });
        }, {scope: 'email,public_profile,user_friends'});    
    }
    
    // sign up response
    $scope.$on('signUpCheckRes', function(event, message){
        if(signUpStatus == 'mobile_pending'){
            $('#registerModal').modal('hide');
            modalService.showModal({}, {submitting: true, labelText: 'Enter the OTP sent to your mobile number', actionButtonText: 'Submit'}).then(function(result){
                $.ajax({
                    url: '/flsv2/Verification',
                    type:'POST',
                    data: JSON.stringify({verification : result+"_u"}),
                    contentType:"application/json",
                    dataType: "JSON",
                    success: function(response) {
                        if(response.code == 0){
                            localStorage.setItem("userloggedin", response.userId);
                            localStorage.setItem("userloggedinName", response.name);
                            localStorage.setItem("userloggedinAccess", response.access_token);
                            window.location.replace("myapp.html#/wizard");
                        }
                    },
                    error: function() {
                    }
                });
            }, function(){});
        }else{
            $scope.$apply(function(){
                $scope.error = message;
            });
        }
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
		}
    
	if(window.location.href.indexOf("index.html") > -1){
		var token = getQueryVariable("ref_token");
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
