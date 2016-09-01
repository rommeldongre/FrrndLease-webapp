var myProfile = angular.module('myApp');

myProfile.controller('myProfileCtrl', ['$scope', 
										'userFactory', 
										'profileFactory', 
										'bannerService', 
										'modalService',
                                        'logoutService',
                                        '$timeout',
										function($scope, 
										userFactory, 
										profileFactory, 
										bannerService, 
										modalService,
                                        logoutService,
                                        $timeout){
    
    localStorage.setItem("prevPage","myapp.html#/myprofile");
    
    var Email = '', Mobile = '', SecStatus = 0, Notification = 'NONE', Address = '', Sublocality = '', Locality = '', Lat = 0.0, Lng = 0.0, image_url='',picOrientation=null;
    
    $scope.user = {};
                                            
    // add credits
    $scope.addCredits = function(){
        modalService.showModal({}, {actionButtonText: "submit", labelText: "Enter the promo code: ", submitting: true}).then(
            function(promo){
                var req = {
                    userId: userFactory.user,
                    promoCode: promo,
                    accessToken: userFactory.userAccessToken
                }
                
                $.ajax({
                    url: '/flsv2/AddPromoCredits',
                    type: 'post',
                    data: JSON.stringify(req),
                    contentType:"application/json",
                    dataType:"json",
                    success: function(response){
                        modalService.showModal({}, {bodyText: response.message,showCancel: false,actionButtonText: 'OK'}).then(
                            function(r){
                                if(response.code == 0)
                                    $scope.credit = response.newCreditBalance;
                                if(response.code == 400)
                                    logoutService.logout();
                            }, function(){});
                    },
                    error: function(){
                        console.log("not able to add promo credit");
                    }

                });
            },function(){});
    }
    
    $scope.options = {
        country: 'in',
        sendToCarousel: false
    };
    
    $scope.details = '';
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    var unsaved = false; 
	
	//beginning of image display
	var canvasCtx = document.getElementById("panel").getContext("2d");
	
	$('#ifile').change(function(event) {
        EXIF.getData(event.target.files[0], function() {
		exif = EXIF.getAllTags(this);
		
		picOrientation = exif.Orientation;
		});
		
		this.imageFile = event.target.files[0];
		
		var reader = new FileReader();
		reader.onload =  function(event) {
			var img = new Image();
			img.onload = function() {
				drawImage(img);
			}
			img.src = event.target.result;
			
		}
		reader.readAsDataURL(this.imageFile);
    });
	
	var drawImage = function(img) {
		canvasCtx.width = 200;
		canvasCtx.height = 200;
		
		if(img.width>img.height){                      							//Landscape Image 
			canvasCtx.width = 200;
			canvasCtx.height = 200 / img.width * img.height;
		} else {                                                                  //Portrait Image
			canvasCtx.width = 200 / img.height * img.width;
			canvasCtx.height = 200;
		} 
		
		if (picOrientation==2){
			canvasCtx.transform(-1, 0, 0, 1,canvasCtx.width, 0);
		}
		if (picOrientation==3){
			canvasCtx.transform(-1, 0, 0, -1,canvasCtx.width, canvasCtx.height);
		}
		if (picOrientation==4){
			canvasCtx.transform(1, 0, 0, -1, 0, canvasCtx.height );
		}
		if (picOrientation==5){
			canvasCtx.transform(0, 1, 1, 0, 0, 0);
		}
		if (picOrientation==6){
			canvasCtx.transform(0, 1, -1, 0, canvasCtx.height , 0);
		}
		if (picOrientation==7){
			canvasCtx.transform(0, -1, -1, 0, canvasCtx.height , canvasCtx.width);
		}
		if (picOrientation==8){
			canvasCtx.transform(0, -1, 1, 0, 0, canvasCtx.width);
		}
		
		canvasCtx.drawImage(img,0,0,canvasCtx.width, canvasCtx.height);
		image_url = canvasCtx.canvas.toDataURL();
	}			
	//end of image display
    
    var displayProfile = function(){
        profileFactory.getProfile(userFactory.user).then(
        function(response){
            if (response.data.code == 0) {
                $scope.userId = userFactory.user;
                $scope.fullname = response.data.fullName;
				$scope.user.mobile = response.data.mobile;
                $scope.user.email = response.data.email;
				$scope.location = response.data.address;
				$scope.credit = response.data.credit;
				$scope.referralCode = response.data.referralCode;
				$scope.label = response.data.photoIdVerified;
				$scope.profilePic = response.data.profilePic;
                $scope.status = response.data.userStatus;
                $scope.secStatus = response.data.userSecStatus;
                $scope.notification = response.data.userNotification;
				url = response.data.photoId;
				if(url != null && url != "null"){
					var img = new Image();
					img.src = url;
					drawImage(img);
				}
                Mobile = response.data.mobile;
                Email = response.data.email;
                SecStatus = response.data.userSecStatus;
                Notification = response.data.userNotification;
            } else {
                $scope.userId = "";
                $scope.fullname = "";
				$scope.user.mobile = "";
				$scope.location = "";
				$scope.credit = "";
				$scope.referralCode = "";
            }
        },
        function(error){
            console.log("unable to get profile: " + error.message);
        });
    }
    
    // getting the profile
    displayProfile();
                                            
    $scope.verifyEmail = function(e){
        var req = {
            table: "users",
            operation: "secuserid",
            row: {
                userId:userFactory.user, 
                email: e,
                activation: CryptoJS.MD5(e).toString()
            }
        }
        
        $.ajax({
            url: '/flsv2/AdminOps',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                $scope.user.error = response.message;
                $timeout(function(){
                    $scope.user.error = '';
                }, 3000);
            },
            error:function() {
            }
        });
    }
    
    $scope.verifyMobile = function(m){
        
        var req = {
            table: "users",
            operation: "secuserid",
            row: {
                userId:userFactory.user, 
                mobile: m+""
            }
        }
        
        $.ajax({
            url: '/flsv2/AdminOps',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code == 0){
                    modalService.showModal({}, {submitting: true, labelText: 'Enter the OTP sent to your mobile number', actionButtonText: 'Submit'}).then(function(result){
                        $.ajax({
                            url: '/flsv2/Verification',
                            type:'POST',
                            data: JSON.stringify({verification : result+"_n"}),
                            contentType:"application/json",
                            dataType: "JSON",
                            success: function(response) {
                                if(response.code == 0){
                                    $scope.secStatus = 1;
                                    $scope.notification = 'EMAIL';
                                    $scope.changedText = false;
                                    SecStatus = 1;
                                    Mobile = m;
                                    Notification = 'EMAIL';
                                }else{
                                    $scope.secStatus = 0;
                                }
                                $scope.user.error = response.message;
                                $timeout(function(){
                                    $scope.user.error = '';
                                }, 3000);
                            },
                            error: function() {
                            }
                        });
                    }, function(){});
                }else{
                    modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(
                            function(r){}, function(){});
                }
            },
            error:function() {
            }
        });
        
    }
    
    $scope.changeEmailNotification = function(){
        var active = false;
        if($scope.status == 'email_activated' || $scope.status == 'facebook' || $scope.status == 'google'){
            active = true;
        }else{
            if($scope.secStatus == 1){
                active = true;
            }else{
                active = false;
                $scope.user.error = 'You dont have a verified email!!';
            }
        }
        
        if(active){
            var n;
            switch($scope.notification){
                case 'EMAIL':
                    n = 'NONE';
                    break;
                case 'SMS':
                    n = 'BOTH';
                    break;
                case 'BOTH':
                    n = 'SMS';
                    break;
                case 'NONE':
                    n = 'EMAIL';
                    break;
            }
            $.ajax({
                url: '/flsv2/ChangeUserNotification',
                type: 'post',
                data: JSON.stringify({userId:userFactory.user, notification: n, accessToken: userFactory.userAccessToken}),
                contentType:"application/json",
                dataType:"json",
                success: function(response){
                    if(response.code == 0)
                        $scope.$apply(function(){
                            $scope.notification = n;
                            Notification = n;
                        });
                    if(response.code == 400)
                        logoutService.logout();
                },
                error: function(){
                    console.log("not able to change notification");
                }
            });
        }
    }
    
    $scope.changeSmsNotification = function(){
        var active = false;
        if($scope.status == 'mobile_activated'){
            active = true;
        }else{
            if($scope.secStatus == 1){
                active = true;
            }else{
                active = false;
                $scope.user.error = 'You dont have a verified mobile number!!';
            }
        }
        
        if(active){
            var n;
            switch($scope.notification){
                case 'EMAIL':
                    n = 'BOTH';
                    break;
                case 'SMS':
                    n = 'NONE';
                    break;
                case 'BOTH':
                    n = 'EMAIL';
                    break;
                case 'NONE':
                    n = 'SMS';
                    break;
            }
            $.ajax({
                url: '/flsv2/ChangeUserNotification',
                type: 'post',
                data: JSON.stringify({userId:userFactory.user, notification: n, accessToken: userFactory.userAccessToken}),
                contentType:"application/json",
                dataType:"json",
                success: function(response){
                    if(response.code == 0)
                        $scope.$apply(function(){
                            $scope.notification = n;
                            Notification = n;
                        });
                    if(response.code == 400)
                        logoutService.logout();
                },
                error: function(){
                    console.log("not able to change notification");
                }
            });
        }
    }
    
    $scope.emailTouched = function(){
        $scope.changedText = true;
        $scope.secStatus = 0;
        switch($scope.notification){
            case 'EMAIL':
                $scope.notification = 'NONE';
                break;
            case 'BOTH':
                $scope.notification = 'SMS';
                break;
        }
    }
    
    $scope.mobileTouched = function(){
        $scope.changedText = true;
        $scope.secStatus = 0;
        switch($scope.notification){
            case 'SMS':
                $scope.notification = 'NONE';
                break;
            case 'BOTH':
                $scope.notification = 'EMAIL';
                break;
        }
    }
    
    $scope.undoEmail = function(){
        $scope.changedText = false;
        $scope.secStatus = SecStatus;
        switch(Notification){
            case 'SMS':
                $scope.notification = 'SMS';
                break;
            case 'EMAIL':
                $scope.notification = 'EMAIL';
                break;
            case 'BOTH':
                $scope.notification = 'BOTH';
                break;
            case 'NONE':
                $scope.notification = 'NONE';
                break;
        }
        $scope.user.email = Email;
        $scope.user.error = '';
    }
    
    $scope.undoMobile = function(){
        $scope.changedText = false;
        $scope.secStatus = SecStatus;
        switch(Notification){
            case 'SMS':
                $scope.notification = 'SMS';
                break;
            case 'EMAIL':
                $scope.notification = 'EMAIL';
                break;
            case 'BOTH':
                $scope.notification = 'BOTH';
                break;
            case 'NONE':
                $scope.notification = 'NONE';
                break;
        }
        $scope.user.mobile = Mobile;
        $scope.user.error = '';
    }
	
    $scope.updateProfile = function(){
        
        if($scope.location != '')
            getLocationData($scope.location);
        else
            editProfileData();
        
        unsaved = false;
    }
    
    $scope.cancel = function(){
        window.location.replace("myapp.html#/");
    }
    
    var getLocationData = function(location){
        $.ajax({
            url: 'https://maps.googleapis.com/maps/api/geocode/json',
            type: 'get',
            data: 'address='+location+"&key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g",
            success: function(response){
                if(response.status == 'OK'){
                    Address = response.results[0].formatted_address;
                    $scope.$apply(function(){
                        $scope.location = Address;
                    });
                    response.results[0].address_components.forEach(function(component){
                        if(component.types.indexOf("sublocality_level_1") != -1)
                            Sublocality = component.long_name;
                        if(component.types.indexOf("locality") != -1)
                            Locality = component.long_name;
                    });
                    Lat = response.results[0].geometry.location.lat;
                    Lng = response.results[0].geometry.location.lng;
                    
                }
                editProfileData();
            },
            error: function(){
                console.log("not able to get location data");
            }
        });
    }
    
    editProfileData = function(){
        var req = {
			userId : userFactory.user,
			fullName : $scope.fullname,
			location : $scope.location,
            address: Address,
            locality: Locality,
            sublocality: Sublocality,
            lat: Lat,
            lng: Lng,
			photoId: image_url,
            accessToken: userFactory.userAccessToken
		}
		editProfile(req);
    }
    
    var editProfile = function(req){
        profileFactory.updateProfile(req).then(
        function(response){
            if (response.data.code == 0) {
                dialogText = 'Your Profile Has Been Updated!!';
				bannerService.updatebannerMessage(dialogText,"");
				$("html, body").animate({ scrollTop: 0 }, "slow");
            }else{
				modalService.showModal({}, {bodyText:response.data.message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                    if(response.data.code == 400)
                        logoutService.logout();
                    else
                        window.location.reload();
                }, function(){});
            }
			
        },
        function(error){
            console.log("unable to edit profile: " + error.message);
        });
    }
    
    $scope.isChanged = function(){
        unsaved = true;
    }
    
    window.onbeforeunload = function(){
        if (unsaved) {
			return "You have unsaved changes on this page.";
        }
    }
    
}]);