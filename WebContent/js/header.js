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
									'eventsCount',
                                    '$rootScope',
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
                                    modalService,
									eventsCount,
                                    $rootScope){
    
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
            url: '/SignUp',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",

            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    fbq('track', 'CompleteRegistration');
                    if(SignUpStatus == "email_pending")
                        loginSignupService.signUpCheckRes(response.Code, "Email verification link send to your email!!");
                    else if(SignUpStatus == "mobile_pending")
                        loginSignupService.signUpCheckRes(response.Code, "OTP has been sent to your phone!!");
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
                    loginSignupService.signUpCheckRes(response.Code, response.Message);
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
            url: '/LoginUser',
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
							loginSignupService.loginCheckRes("You have " + friends + " Facebook friends on FrrndLease. Say Hi to them!");
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
            url: '/AddFriend',
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
        if(window.location.pathname == '/index.html' || window.location.pathname == '/'){
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
            url: '/GetCreditTimeline',
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
	
	$scope.cancel_credit = function(){
		lastOffset = 0;
		$scope.creditsArray = [];
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
			url: '/GetUnreadEventsCount',
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
			url: '/MatchFbId',
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
	
	$scope.importfb = function(){
		
		FB.login(function(response) {
				
				var ref_code = localStorage.getItem("userReferralCode");
				// handle the response
				
				// check whether user is logged in or not and ask for credentials if not.

				// send message to facebook friends using send request dialog
				FB.ui({
					method: 'send',
					link: 'http://www.frrndlease.com/index.html?ref_token='+ref_code,
				},function(response){
					if (response && !response.error) {
						//check 'response' to see if call was successful
						modalService.showModal({}, {bodyText: "Success, Message to Facebook Friend(s) sent" ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
							eventsCount.updateEventsCount();
						}, function(){});
					}
				});
            }, {scope: 'email,public_profile,user_friends'});
	}
    
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
        return $http.post('/GetSiteStats', JSON.stringify({empty_pojo: ""}));
    }
    return dataFactory;
}]);

// factory for getting and updating profile from the backend service
headerApp.factory('profileFactory', ['$http', function($http){
    
    var dataFactory = {};
    
    dataFactory.getProfile = function(user){
        return $http.post('/GetProfile', JSON.stringify({userId : user}));
    }
    
    dataFactory.updateProfile = function(req){
        return $http.post('/EditProfile', JSON.stringify(req));
    }
    
    return dataFactory;
}]);

headerApp.factory('userFactory', ['$rootScope', 'logoutService', '$http', function($rootScope, logoutService, $http){
    
    var dataFactory = {};
    
    dataFactory.user = localStorage.getItem("userloggedin");
    dataFactory.userName = localStorage.getItem("userloggedinName");
    dataFactory.userAccessToken = localStorage.getItem("userloggedinAccess");
    
    dataFactory.buyCredits = function(PromoCode, AmountPaid, RazorPayId){
        return $http.post('/BuyCredits', JSON.stringify({
            userId: localStorage.getItem("userloggedin"),
            accessToken: localStorage.getItem("userloggedinAccess"),
            promoCode: PromoCode,
            amountPaid: AmountPaid,
            razorPayId: RazorPayId
        }));
    }
    
    dataFactory.payMembership = function(PromoCode, AmountPaid, RazorPayId){
        return $http.post('/PayMembership', JSON.stringify({
            userId: localStorage.getItem("userloggedin"),
            accessToken: localStorage.getItem("userloggedinAccess"),
            promoCode: PromoCode,
            amountPaid: AmountPaid,
            razorPayId: RazorPayId
        }));
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
            templateUrl: '/modal.html'
        };

        var modalOptions = {
            actionButtonText: 'Yes',
            showCancel: true,
			labelText: 'Default Label Text',
            submitting: false,
			messaging: false,
            cancelButtonText: 'Cancel',
            headerText: 'FrrndLease Says',
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
    
    this.signUpCheckRes = function(code, message){
        $rootScope.$broadcast('signUpCheckRes', code, message);
    }
    
}]);

headerApp.directive('loadImage', ['$http', function($http){
    return{
        restrict:'A',
        scope: {
            'loadImage': '=',
            'maxWidth': '=?',
            'maxHeight': '=?',
            'scale': '=?'
        },
        link: function(scope, element, attrs){
            scope.$watch('loadImage', function(){
                var MaxWidth = 300;
                var MaxHeight = 300;
                
                var ImgSrc = scope.loadImage;
                
                if(scope.scale){
                    if($(window).width()>=991){
                        //for desktops
                        MaxWidth = 1000*(scope.scale/100);
                        MaxHeight = 1000*(scope.scale/100);
                    }else if($(window).width()<=500){
                        //for mobiles
                        MaxWidth = 650*(scope.scale/100);
                        MaxHeight = 650*(scope.scale/100);
                    }else{
                        //for tablets
                        MaxWidth = 850*(scope.scale/100);
                        MaxHeight = 850*(scope.scale/100);
                    }
                }

                if(scope.maxWidth)
                    MaxWidth = scope.maxWidth;
                if(scope.maxHeight)
                    MaxHeight = scope.maxHeight;
                
                if(ImgSrc != 'loading' && ImgSrc != '' && ImgSrc != null && ImgSrc != 'null' && ImgSrc != undefined){
                    loadImage(
                        ImgSrc,
                        function(canvas){
                            element.removeAttr('style');
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
                    element.css('width', MaxWidth+"px");
                    element.css('height', MaxHeight+"px");
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
                                url: '/SaveImageInS3',
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

headerApp.directive('sendMessageTo', ['userFactory', 'modalService', 'bannerService', function(userFactory, modalService, bannerService){
    return {
        scope:{
            sendMessageTo: '=',
            itemId: '=?'
        },
        link: function(scope, element, attrs){
            
            var To = scope.sendMessageTo;
            
            var ItemId = 0;
            var BodyText = "Message Your friend";
            var Subject = "FRIEND";
            
            if(scope.itemId){
                ItemId = scope.itemId;
                BodyText = "Message Item\'s Owner";
                Subject = "ITEM";
            }
            
            element.on('click', function(){
                modalService.showModal({}, {messaging: true, bodyText: BodyText, actionButtonText: 'Send'}).then(function(result){

                    var message = result;

                    if(message == "" || message == undefined)
                        message = null;

                    var req = {
                        userId: userFactory.user,
                        accessToken: userFactory.userAccessToken,
                        from: userFactory.user,
                        to: To,
                        subject: Subject,
                        message: message,
                        itemId: ItemId
                    }

                    sendMessage(req);

                }, function(){});
            });

            var sendMessage = function(req){
                $.ajax({
                    url: '/SendMessage',
                    type: 'post',
                    data: JSON.stringify(req),
                    contentType: "application/x-www-form-urlencoded",
                    dataType: "json",
                    success: function(response) {
                        if(response.code==0){
                            bannerService.updatebannerMessage("Message Sent!!");
                            $("html, body").animate({ scrollTop: 0 }, "slow");

                        }else{
                            modalService.showModal({}, {bodyText: "Error while sending message, please try again later" ,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                                }, function(){});
                        }
                    },

                    error: function() {
                        console.log("Not able to send message");
                    }
                });
            }
        }
    };
}]);

headerApp.directive('userBadges', function(){
    return {
        restrict: 'EA',
        scope: {
            userId: '='
        },
        controller: function($scope){
            
            $scope.badge = {};
            
            $scope.$watch('userId', function(){
                var userId = $scope.userId;

                if(userId != undefined && userId != null && userId != ""){
                    $.ajax({
                        url: '/GetUserBadges',
                        type: 'post',
                        data: JSON.stringify({userId: userId}),
                        contentType: "application/x-www-form-urlencoded",
                        dataType: "json",
                        success: function(response) {
                            if(response.code == 0){
                                $scope.$apply(function(){
                                    $scope.badge = response;
                                });
                            }
                        },

                        error: function() {
                            console.log("Not able to get user badges");
                        }
                    });
                }
            });
        },
        replace: true,
        template:'<div>\
                    <div style="margin:5px;">\
                        <div class="social-badges" style="text-align:center;font-size:large;">\
                            <span>Verified:</span>\
                            <div style="display:inline-block;">\
                                <span style="cursor:help;" class="no-padding ng-class:{\'text-gray\':!badge.userVerifiedFlag,\'orange\':badge.userVeifiedFlag}" data-toggle="popover" title="FrrndLease Says - " data-placement="top" data-content="You get Address Verified when you upload your Photo Id" popover>\
                                    <i class="fa fa-address-card" aria-hidden="true"></i>\
                                </span>\
                                <span style="cursor:help;" class="no-padding ng-class:{\'text-gray\':badge.userStatus!=\'facebook\', \'text-facebook\':badge.userStatus==\'facebook\'}" data-toggle="popover" title="FrrndLease Says - " data-placement="top" data-content="You get Facebook verified if you signed up using Facebook" popover>\
                                    <i class="fa fa-facebook-square" aria-hidden="true"></i>\
                                </span>\
                                <span style="cursor:help;" class="no-padding ng-class:{\'text-gray\':badge.userStatus!=\'google\',\'text-google\':badge.userStatus==\'google\'}" data-toggle="popover" title="FrrndLease Says - " data-placement="top" data-content="You get Google verified if you signed up using Google" popover>\
                                    <i class="fa fa-google-plus-square" aria-hidden="true"></i>\
                                </span>\
                            </div>\
                        </div>\
                    </div>\
                    <div style="margin:5px;">\
                        <div style="text-align:center;display:inline-flex;">\
                            <span style="cursor:help;padding:5px;" data-toggle="popover" title="FrrndLease Says - " data-placement="top" data-content="Number of Items Posted by this User" popover>\
                                <span style="font-size:22px;"><strong>{{badge.userItems}}</strong></span> <span class="text-gray"><i class="fa fa-cubes" aria-hidden="true"></i></span>\
                            </span>\
                            <span style="cursor:help;padding:5px;" data-toggle="popover" title="FrrndLease Says - " data-placement="top" data-content="Number of Leases this User has got and also has given out" popover>\
                                <span style="font-size:22px;"><strong>{{badge.userLeases}}</strong></span> <span class="text-gray"><i class="fa fa-file-text-o" aria-hidden="true"></i></span>\
                            </span>\
                            <span style="cursor:help;padding:5px;" data-toggle="popover" title="FrrndLease Says - " data-placement="top" data-content="Average number of Days this user takes to respond to a request" popover>\
                                <span style="font-size:22px;"><strong>{{(badge.responseTime==0) ? \'NA\' : (badge.responseTime/badge.responseCount | number:0)}} </strong></span> <span class="text-gray"><i class="fa fa-hand-paper-o" aria-hidden="true"></i></span>\
                            </span>\
                            <span style="cursor:help;padding:5px;" data-toggle="popover" title="FrrndLease Says - " data-placement="top" data-content="Total credits owned by this user" popover>\
                                <span style="font-size:22px;"><strong>{{badge.userCredit}} </strong></span> <span class="text-gray"><i class="fa fa-diamond"></i></li></span>\
                            </span>\
                        </div>\
                    </div>\
                    <div style="margin:5px;">\
                        <div style="text-align:center;">\
                            Member Since - <strong>{{badge.userSignupDate | date:\'MMMM yyyy\'}}</strong>\
                        </div>\
                    </div>\
                </div>'
        
    };
});

headerApp.directive('tooltip', function(){
    return {
        restrict: 'A',
        link: function(scope, element, attrs){
            $(element).hover(function(){
                $(element).tooltip('show');
            }, function(){
                $(element).tooltip('hide');
            });
        }
    };
});

headerApp.directive('popover', function(){
    return {
        restrict: 'A',
        link: function(scope, element, attrs){
            if($(element).length != 0){
                //    Activate Popovers
               $(element).popover().on('show.bs.popover', function () {
                    $('.popover-filter').click(function(){
                        $(this).removeClass('in');
                        $(element).popover('hide');
                    });
                    $('.popover-filter').addClass('in');
                }).on('hide.bs.popover', function(){
                    $('.popover-filter').removeClass('in');
                });

            }
        }
    }
});

headerApp.controller('loginModalCtrl', ['$scope', 'loginSignupService', 'modalService', function($scope, loginSignupService, modalService){
    
    var email = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    var mobile = /^[789]\d{9}$/;
    
    // Form login
    $scope.formLogin = function(userId, password){
        if(userId == 'admin@frrndlease.com' || userId == 'ops@frrndlease.com')
            $scope.error = "This user cannot access the website";
        else{
            if(email.test(userId))
                loginSignupService.loginCheckReq(userId, password, "", "email_activated");
            else if(mobile.test(userId))
                loginSignupService.loginCheckReq(userId, password, "", "mobile_activated");
            else
                $scope.error = "Please enter valid user id!!";
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
                url: '/ForgotPassword',
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
                                    url: '/Verification',
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
    
    $scope.user = {};
    
    $scope.userIdChanged = function(){
        
        $scope.error = "";
        
        var email = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        var mobile = /^[789]\d{9}$/;
        
        if(email.test($scope.userId)){
            $scope.user.email = $scope.userId;
            $scope.emailEditable = true;
            signUpStatus = "email_pending";
        }
        else{
            $scope.user.email = '';
            $scope.emailEditable = false;
        }
        
        if(mobile.test($scope.userId)){
            $scope.user.mobile = $scope.userId;
            $scope.mobileEditable = true;
            signUpStatus = "mobile_pending";
        }
        else{
            $scope.user.mobile = '';
            $scope.mobileEditable = false;
        }
    }
    
    // form sign up
    $scope.formSignup = function(){
        friendId = $scope.user.email;
        if($scope.userId == $scope.user.email || $scope.userId == $scope.user.mobile)
            loginSignupService.signUpCheckReq($scope.userId, $scope.user.email, $scope.password, $scope.name, "", $scope.user.mobile, $scope.user.code, $scope.location, signUpStatus, friendId);
        else
            $scope.error = "Please enter correct User Id!!";
    }
    
    // Google sign up
    function onSignUp(googleUser) {
        var profile = googleUser.getBasicProfile();
        friendId = profile.getEmail();
        loginSignupService.signUpCheckReq(profile.getEmail(), profile.getEmail(), profile.getId(), profile.getName(), profile.getImageUrl(), "", $scope.user.code, $scope.location, "google", friendId);
    }
    window.onSignUp = onSignUp;
    
    // facebook sign up
    $scope.facebookSignIn = function() {
        FB.login(function(response) {
            // handle the response
            FB.api('/me?fields=id,name,email,first_name,last_name,locale,gender,picture.type(large)', function(response) {
                friendId = response.id + "@fb";
                loginSignupService.signUpCheckReq(response.email, response.email, response.id, response.name, response.picture.data.url, "", $scope.user.code, $scope.location, "facebook", friendId);
            });
        }, {scope: 'email,public_profile,user_friends'});    
    }
    
    // sign up response
    $scope.$on('signUpCheckRes', function(event, code, message){
        if(code == 'FLS_SUCCESS'){
            if(signUpStatus == 'mobile_pending'){
                $('#registerModal').modal('hide');
                modalService.showModal({}, {submitting: true, labelText: 'Enter the OTP sent to your mobile number', actionButtonText: 'Submit'}).then(function(result){
                    $.ajax({
                        url: '/Verification',
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
            }
        }
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
		}
    
	if(window.location.href.indexOf("index.html") > -1){
		var token = getQueryVariable("ref_token");
		if(token === undefined || token=="undefined"){
		}else{
			$scope.user.code = token;
			localStorage.setItem("friendReferralCode",token);
		}	
		var ref_code = localStorage.getItem("friendReferralCode");
		$scope.user.code = ref_code;
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

headerApp.controller('paymentModalCtrl', ['$scope', 'userFactory', 'eventsCount', function($scope, userFactory, eventsCount){
    
    $scope.payment = {
        credit: 0,
        conversion: 10,
        amount: 0,
        promoCode: '',
        discount: 0,
        promoError: '',
        checkingPromo: false,
        validPromo: false,
        paymentError: ''
    };
    
    $scope.$watch('payment.credit', function(){
        $scope.payment.amount = $scope.payment.credit * $scope.payment.conversion;
    });
    
    $scope.validatePromoCode = function(){
        
        if($scope.payment.promoCode == '' || $scope.payment.promoCode == undefined){
            $scope.payment.promoError = "Please enter a valid promo code!!";
            return;
        }
        
        $scope.payment.checkingPromo = true;
        
        var req = {
            userId: userFactory.user,
            promoCode: $scope.payment.promoCode,
            accessToken: userFactory.userAccessToken
        }
        
        $.ajax({
            url: '/ValidatePromo',
            type: 'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType:"json",
            success: function(response){
                $scope.$apply(function(){
                    $scope.payment.checkingPromo = false;
                    $scope.payment.discount = 0;
                    $scope.payment.validPromo = false;
                    if(response.code == 0){
                        $scope.payment.discount = response.discountAmount;
                        $scope.payment.promoError = "Promo Applied: " + $scope.payment.promoCode;
                        $scope.payment.validPromo = true;
                    }else{
                        if(response.code == 400)
                            logoutService.logout();
                        else{
                            $scope.payment.discount = 0;
                            $scope.payment.promoError = response.message;
                        }
                    }
                });
            },
            error: function(){
                $scope.payment.checkingPromo = false;
                console.log("not able to add promo credit");
            }
        });
    }
    
    $scope.removePromoCode = function(){
        $scope.payment.promoError = '';
        $scope.payment.promoCode = '';
        $scope.payment.discount = 0;
        $scope.payment.validPromo = false;
    }
    
    $scope.completePayment = function(){
        
        var payableAmt = $scope.payment.amount - $scope.payment.discount;
        
        if(payableAmt > 0){
            if(true){
                $scope.payment.paymentError = 'Currently we are not supporting payments!!';
            }else{
                var options = {
                    key: "rzp_test_GwL1Gj4oI20Jeq",
                    amount: payableAmt * 100,
                    name: "Grey Labs LLP",
                    description: "Buying Credits",
                    image: "images/fls-logo.png",
                    prefill: {
                        name: userFactory.userName,
                        email: userFactory.user
                    },
                    handler: function (response){
                        userFactory.buyCredits($scope.payment.promoCode, payableAmt, response.razorpay_payment_id).then(function(res){
                            if(res.data.code == 0)
                                window.location.reload();
                            else
                                console.log(res);
                        },
                        function(error){});
                    },
                    modal: {
                        ondismiss: function(){}
                    }
                }
                var rzp1 = new Razorpay(options);
                rzp1.open();
            }
        }else if(payableAmt == 0){
            if($scope.payment.amount == $scope.payment.discount && $scope.payment.validPromo){
                userFactory.buyCredits($scope.payment.promoCode, 0, null).then(function(response){
                    window.location.reload();
                },
                function(error){});
            }else{
                $scope.payment.paymentError = 'Please make the amount positive or apply a valid promo code';
            }
        }else if(payableAmt < 0){
            if($scope.payment.validPromo)
                $scope.payment.paymentError = 'Please make the amount positive or zero by adding more credits';
            else
                $scope.payment.paymentError = 'Cannot pay negative amount. Please make it positive by adding more credits';
        }
        
    }
    
}]);

headerApp.controller('uberPayModalCtrl', ['$scope', 'userFactory', 'eventsCount', function($scope, userFactory, eventsCount){
    
    $scope.payment = {
        month: 0,
        conversion: 499,
        amount: 0,
        promoCode: '',
        discount: 0,
        promoError: '',
        checkingPromo: false,
        validPromo: false,
        paymentError: ''
    };
    
    $scope.$watch('payment.month', function(){
        $scope.payment.amount = $scope.payment.month * $scope.payment.conversion;
    });
    
    $scope.validatePromoCode = function(){
        
        if($scope.payment.promoCode == '' || $scope.payment.promoCode == undefined){
            $scope.payment.promoError = "Please enter a valid promo code!!";
            return;
        }
        
        $scope.payment.checkingPromo = true;
        
        var req = {
            userId: userFactory.user,
            promoCode: $scope.payment.promoCode,
            accessToken: userFactory.userAccessToken
        }
        
        $.ajax({
            url: '/ValidatePromo',
            type: 'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType:"json",
            success: function(response){
                $scope.$apply(function(){
                    $scope.payment.checkingPromo = false;
                    $scope.payment.discount = 0;
                    $scope.payment.validPromo = false;
                    if(response.code == 0){
                        $scope.payment.discount = response.discountAmount;
                        $scope.payment.promoError = "Promo Applied: " + $scope.payment.promoCode;
                        $scope.payment.validPromo = true;
                    }else{
                        if(response.code == 400)
                            logoutService.logout();
                        else{
                            $scope.payment.discount = 0;
                            $scope.payment.promoError = response.message;
                        }
                    }
                });
            },
            error: function(){
                $scope.payment.checkingPromo = false;
                console.log("not able to add promo credit");
            }
        });
    }
    
    $scope.removePromoCode = function(){
        $scope.payment.promoError = '';
        $scope.payment.promoCode = '';
        $scope.payment.discount = 0;
        $scope.payment.validPromo = false;
    }
    
    $scope.completePayment = function(){
        
        var payableAmt = $scope.payment.amount - $scope.payment.discount;
        
        if(payableAmt > 0){
            if(true){
                $scope.payment.paymentError = 'Currently we are not supporting payments!!';
            }else{
                var options = {
                    key: "rzp_test_GwL1Gj4oI20Jeq",
                    amount: payableAmt * 100,
                    name: "Grey Labs LLP",
                    description: "Paying Membership Fee",
                    image: "images/fls-logo.png",
                    prefill: {
                        name: userFactory.userName,
                        email: userFactory.user
                    },
                    handler: function (response){
                        userFactory.payMembership($scope.payment.promoCode, payableAmt, response.razorpay_payment_id).then(function(res){
                            if(res.data.code == 0)
                                window.location.reload();
                            else
                                console.log(res);
                        },
                        function(error){});
                    },
                    modal: {
                        ondismiss: function(){}
                    }
                }
                var rzp1 = new Razorpay(options);
                rzp1.open();
            }
        }else if(payableAmt == 0){
            if($scope.payment.amount == $scope.payment.discount && $scope.payment.validPromo){
                userFactory.payMembership($scope.payment.promoCode, 0, null).then(function(response){
                    window.location.reload();
                },
                function(error){});
            }else{
                $scope.payment.paymentError = 'Please make the amount positive or apply a valid promo code';
            }
        }else if(payableAmt < 0){
            if($scope.payment.validPromo)
                $scope.payment.paymentError = 'Please make the amount positive or zero by adding more months';
            else
                $scope.payment.paymentError = 'Cannot pay negative amount. Please make it positive by adding more months';
        }
        
    }
    
}]);