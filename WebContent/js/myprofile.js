var myProfile = angular.module('myApp');

myProfile.controller('myProfileCtrl', ['$scope', 
										'userFactory', 
										'profileFactory', 
										'bannerService', 
										'modalService',
                                        'logoutService',
                                        '$timeout',
                                        '$filter',
										function($scope, 
										userFactory, 
										profileFactory, 
										bannerService, 
										modalService,
                                        logoutService,
                                        $timeout,
                                        $filter){
    
    localStorage.setItem("prevPage","myapp.html#/myprofile");
    
    var Email = '', Mobile = '', SecStatus = 0, Notification = 'NONE', Address = '', Sublocality = '', Locality = '', Lat = 0.0, Lng = 0.0, picOrientation=null;
    
    $scope.user = {};
    
    $scope.options = {
        country: 'in',
        sendToCarousel: false
    };
    
    $scope.details = '';
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    var unsaved = false;
    
    var displayProfile = function(){
        profileFactory.getProfile(userFactory.user).then(
        function(response){
            if (response.data.code == 0) {
                $scope.plan = response.data.plan;
                $scope.user.userUid = response.data.userUid;
                $scope.user.userId = userFactory.user;
                $scope.fullname = response.data.fullName;
				$scope.user.mobile = response.data.mobile;
                $scope.user.email = response.data.email;
				$scope.location = response.data.address;
                $scope.locality = response.data.locality;
				$scope.credit = response.data.credit;
				$scope.referralCode = response.data.referralCode;
				$scope.profilePic = response.data.profilePic;
                $scope.status = response.data.userStatus;
                $scope.secStatus = response.data.userSecStatus;
                $scope.notification = response.data.userNotification;
                $scope.photoId = response.data.photoId;
				$scope.addressVerified = response.data.photoIdVerified;
                $scope.userFeeExpiry = response.data.userFeeExpiry;
                if($scope.userFeeExpiry != null)
                    $scope.userFeeExpiry = $filter('date')($scope.userFeeExpiry);
                else
                    $scope.userFeeExpiry = 'NA';
                
                Mobile = response.data.mobile;
                Email = response.data.email;
                SecStatus = response.data.userSecStatus;
                Notification = response.data.userNotification;
            } else {
                $scope.user.userId = "";
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
            url: '/AdminOps',
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
            url: '/AdminOps',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code == 0){
                    modalService.showModal({}, {submitting: true, labelText: 'Enter the OTP sent to your mobile number', actionButtonText: 'Submit'}).then(function(result){
                        $.ajax({
                            url: '/Verification',
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
                    modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'Ok'}).then(
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
                url: '/ChangeUserNotification',
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
                url: '/ChangeUserNotification',
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
        
        $scope.user.saving = true;
        
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
            accessToken: userFactory.userAccessToken
		}
		editProfile(req);
    }
    
    var editProfile = function(req){
        profileFactory.updateProfile(req).then(
        function(response){
            if (response.data.code == 0) {
                $scope.user.saving = false;
            }else{
				modalService.showModal({}, {bodyText:response.data.message,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
                    if(response.data.code == 400)
                        logoutService.logout();
                    else
                        window.location.reload();
                }, function(){});
            }
        },
        function(error){
            console.log("unable to edit profile: " + error.message);
            $scope.user.saving = false;
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
    
    $scope.uploadProfilePic = function(file, isProfile){
        EXIF.getData(file, function(){
            exif = EXIF.getAllTags(this);
            picOrientation = exif.Orientation;
		});
        
        var reader = new FileReader();
        reader.onload = function(event) {
            loadImage(
                event.target.result,
                function(canvas){
                    var Pic = canvas.toDataURL();

                    if(isProfile){
                        var req = {
                            userId: userFactory.user,
                            accessToken: userFactory.userAccessToken,
                            userUid: $scope.user.userUid,
                            image: Pic,
                            existingLink: $scope.profilePic,
                            profile: isProfile,
                            multiple: false
                        }
                    }else{
                        var req = {
                            userId: userFactory.user,
                            accessToken: userFactory.userAccessToken,
                            userUid: $scope.user.userUid,
                            image: Pic,
                            existingLink: $scope.photoId,
                            profile: isProfile,
                            multiple: false
                        }
                    }
                    
                    $scope.$apply(function(){
                        if(isProfile)
                            $scope.profilePic = "loading";
                        else
                            $scope.photoId = "loading";
                    });
                    
                    $.ajax({
                        url: '/SaveUserPicsInS3',
                        type: 'post',
                        data: JSON.stringify(req),
                        contentType: "application/json",
                        dataType: "json",

                        success: function(response) {
                            if(response.code == 0){
                                $scope.$apply(function(){
                                    if(isProfile)
                                        $scope.profilePic = response.imageLink;
                                    else
                                        $scope.photoId = response.imageLink;
                                });
                            }else{
                                $scope.$apply(function(){
                                    if(isProfile)
                                        $scope.profilePic = "";
                                    else
                                        $scope.photoId = "";
                                });
                                modalService.showModal({}, {bodyText: response.message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                                    if(response.code == 400)
                                        logoutService.logout();
                                },function(){});
                            }
                        },

                        error: function() {
                            modalService.showModal({}, {bodyText: "Something is Wrong with the network.",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                        }
                    });
                },
                {
                    maxWidth: 300,
                    maxHeight: 300,
                    canvas: true,
                    orientation: picOrientation
                }
            );
        }
        reader.readAsDataURL(file);
    }
    
    $scope.deleteUserPic = function(isProfile){
        
        if(isProfile){
            var req = {
                userId: userFactory.user,
                accessToken: userFactory.userAccessToken,
                userUid: $scope.user.userUid,
                link: $scope.profilePic,
                profile: isProfile
            }
        }else{
            var req = {
                userId: userFactory.user,
                accessToken: userFactory.userAccessToken,
                userUid: $scope.user.userUid,
                link: $scope.photoId,
                profile: isProfile
            }
        }
        
        if(isProfile)
            $scope.profilePic = "loading";
        else
            $scope.photoId = "loading";
        
        $.ajax({
            url: '/DeleteUserPicsFromS3',
            type: 'post',
            data: JSON.stringify(req),
            contentType: "application/json",
            dataType: "json",
            success: function(response) {
                if(response.code == 0){
                    $scope.$apply(function(){
                        if(isProfile)
                            $scope.profilePic = "";
                        else
                            $scope.photoId = "";
                    });
                }else{
                    modalService.showModal({}, {bodyText: response.message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                        if(response.code == 400)
                            logoutService.logout();
                    },function(){});
                }
            },
            error: function() {
                modalService.showModal({}, {bodyText: "Something is Wrong with the network.",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
            }
        });
        
    }
    
}]);