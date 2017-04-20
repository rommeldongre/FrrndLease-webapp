var merchantApp = angular.module('merchantApp', ['headerApp', 'footerApp', 'ngAutocomplete']);

merchantApp.controller('merchantCtrl', ['$scope', 'modalService', 'userFactory', 'paymentService', function($scope, modalService, userFactory,paymentService){

    if(userFactory.user != "" && userFactory.user != null && userFactory.user != "anonymous")
        window.location.replace("myapp.html");
    
    // sign up scope object
    $scope.user = {
        error: '',
        userId: '',
        referral: '',
        location: '',
        email: '',
        otp: '',
        signedUp: false,
        verified: false
    };

    // sign up request object
    var req = {
        userId: "",
        email: "",
        fullName: "",
        profilePicture: "",
        mobile: "",
        referralCode: "",
        location: "",
        auth: "",
        activation: "",
        status: "",
        address: "",
        locality: "",
        sublocality: "",
        lat: "0.0",
        lng: "0.0",
        friendId: ""
    }
    
    $scope.$watch('onboarding', function(){
        if($scope.onboarding == true)
            $("html, body").animate({ scrollTop: 600 }, "slow");
        else
            $("html, body").animate({ scrollTop: 0 }, "slow");
    });

    // validating the user id entered
    $scope.userIdChanged = function(){
        var mobile = /^[789]\d{9}$/;
        if(mobile.test($scope.user.userId)){

            // updating scope user id
            $scope.user.mobile = $scope.user.userId;

            // updating request user id
            req.status = "mobile_pending";
            req.userId = $scope.user.userId;
            req.mobile = $scope.user.mobile;
        } else {
            $scope.user.mobile = '';
        }
    }

    // form sign up
    $scope.formSignup = function(){
        fbq('track', 'StartRegistration');
        req.friendId = $scope.user.email;
        if($scope.user.userId == $scope.user.mobile){
            req.auth = (CryptoJS.MD5($scope.user.password)).toString();
            req.activation = (CryptoJS.MD5($scope.user.userId)).toString();
            req.fullName = $scope.user.name;
            req.email = '';
            req.referralCode = $scope.user.referral;
            checkLocation();
        } else {
            $scope.user.error = "Please enter correct User Id!!";
        }
    }

    // Google sign up
    function onSignUp(googleUser) {
        fbq('track', 'StartRegistration');
        var profile = googleUser.getBasicProfile();
        req.friendId = profile.getEmail();
        req.status = "google";
        req.userId = profile.getEmail();
        req.email = profile.getEmail();
        req.auth = (CryptoJS.MD5(profile.getId())).toString();
        req.activation = (CryptoJS.MD5(profile.getEmail())).toString();
        req.fullName = profile.getName();
        req.profilePicture = profile.getImageUrl();
        req.referralCode = $scope.user.referral;
        checkLocation();
    }
    window.onSignUp = onSignUp;

    // facebook sign up
    $scope.facebookSignIn = function() {
        fbq('track', 'StartRegistration');
        FB.login(function(response) {
            // handle the response
            FB.api('/me?fields=id,name,email,first_name,last_name,locale,gender,picture.type(large)', function(response) {
                friendId = response.id + "@fb";
                req.status = "facebook";
                req.userId = response.email;
                req.email = response.email;
                req.auth = (CryptoJS.MD5(response.id)).toString();
                req.activation = (CryptoJS.MD5(response.email)).toString();
                req.fullName = response.name;
                req.profilePicture = response.picture.data.url;
                req.referralCode = $scope.user.referral;
                checkLocation();
            });
        }, {scope: 'email,public_profile,user_friends'});
    }

    // location check function
    var checkLocation = function(){
        if($scope.user.location != '') {
            $.ajax({
                url: 'https://maps.googleapis.com/maps/api/geocode/json',
                type: 'get',
                data: 'address='+$scope.user.location+"&key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g",
                success: function(response){
                    if(response.status == 'OK'){
                        req.address = response.results[0].formatted_address;
                        response.results[0].address_components.forEach(function(component){
                            if(component.types.indexOf("sublocality_level_1") != -1)
                                req.sublocality = component.long_name;
                            if(component.types.indexOf("locality") != -1)
                                req.locality = component.long_name;
                        });
                        req.lat = response.results[0].geometry.location.lat+"";
                        req.lng = response.results[0].geometry.location.lng+"";
                        req.location = $scope.user.location;
                    }
                    signUp();
                },
                error: function(){
                    console.log("not able to get location data");
                }
            });
        } else
            signUp();
    }

    var signUp = function(){
        $.ajax({
            url: '/SignUp',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",

            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    fbq('track', 'CompleteRegistration');
                    $scope.$apply(function(){
                        $scope.user.signedUp = true;
                    });
                    if(req.status != "mobile_pending") {
                        $scope.$apply(function(){
                            $scope.user.verified = true;
                        });
                        var obj = JSON.parse(response.Message);
						userFactory.setLocalStorageValues($scope.user.userId,$scope.user.name,obj.access_token,response.Id);
                    }
				}else{
                    $scope.$apply(function(){
                        $scope.user.error = response.Message;
                    });
				}
            },
            error: function() {
            }
        });
    }
    
    $scope.verifyOtp = function(){
        $.ajax({
            url: '/Verification',
            type:'POST',
            data: JSON.stringify({verification : $scope.user.otp+"_u"}),
            contentType:"application/json",
            dataType: "JSON",
            success: function(response) {
                if(response.code == 0){
                    localStorage.setItem("userloggedin", response.userId);
                    localStorage.setItem("userloggedinName", response.name);
                    localStorage.setItem("userloggedinAccess", response.access_token);
                    $scope.$apply(function(){
                        $scope.user.verified = true;
                    });
                    applyPromoCode();
                }
            },
            error: function() {
            }
        });
    }

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

	if(window.location.href.indexOf("merchant.html") > -1){
		var token = getQueryVariable("ref_token");
		if(token === undefined || token=="undefined"){
		}else{
			$scope.user.refferal = token;
            req.referralCode = token;
			localStorage.setItem("friendReferralCode",token);
		}
		var ref_code = localStorage.getItem("friendReferralCode");
		$scope.user.refferal = ref_code;
        req.refferalCode = ref_code;
	}

    // remove this code and uncomment the below one when using https
    $scope.user.location = "Pune, Maharashtra, India";

    $scope.$on('currentLocation', function(event, location){
        $scope.user.location = location;
        req.location = location;
    });
    
    $scope.payment = {
        month: 0,
        conversion: 500,
        amount: 0,
        promoCode: 'FREETRIAL',
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
        applyPromoCode();
    }
    
    var applyPromoCode = function(){
        if($scope.payment.promoCode == '' || $scope.payment.promoCode == undefined){
            $scope.payment.promoError = "Please enter a valid promo code!!";
            return;
        }
        
        $scope.payment.checkingPromo = true;
        
        var req = {
            userId: localStorage.getItem("userloggedin"),
            promoCode: $scope.payment.promoCode,
            accessToken: localStorage.getItem("userloggedinAccess")
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
                        $scope.payment.discount = 0;
                        $scope.payment.promoError = response.message;
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
            if(window.location.href.indexOf("frrndlease.com") > -1){
                $scope.payment.paymentError = 'Currently we are not supporting payments!!';
            }else{
				localStorage.setItem("promoCode", $scope.payment.promoCode);
				paymentService.updateFinalAmount(payableAmt);
				$('#summaryModal').modal('show');
            }
        }else if(payableAmt == 0){
            if($scope.payment.amount == $scope.payment.discount && $scope.payment.validPromo){
                userFactory.payMembership($scope.payment.promoCode, 0, null).then(function(response){
                    window.location.replace("myapp.html#/myprofile");
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
