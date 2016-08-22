var myProfile = angular.module('myApp');

myProfile.controller('myProfileCtrl', ['$scope', 
										'userFactory', 
										'profileFactory', 
										'bannerService', 
										'modalService',
                                        'logoutService',
										function($scope, 
										userFactory, 
										profileFactory, 
										bannerService, 
										modalService,
                                        logoutService){
    
    localStorage.setItem("prevPage","myapp.html#/myprofile");
    
    var Address = '', Sublocality = '', Locality = '', Lat = 0.0, Lng = 0.0, image_url='',picOrientation=null,lastOffset = 0;
	$("#openBtn").hide();
    
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
				$scope.mobile = response.data.mobile;
				$scope.location = response.data.address;
				$scope.credit = response.data.credit;
				$scope.referralCode = response.data.referralCode;
				$scope.label = response.data.photoIdVerified;
				$scope.profilePic = response.data.profilePic;
				url = response.data.photoId;
				if(url != null && url != "null"){
					var img = new Image();
					img.src = url;
					drawImage(img);
				}
            } else {
                $scope.userId = "";
                $scope.fullname = "";
				$scope.mobile = "";
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
	
	$scope.showCredit = function(){
		$("#openBtn").click();
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
			mobile : $scope.mobile,
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