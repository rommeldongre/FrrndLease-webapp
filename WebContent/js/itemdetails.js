var itemDetailsApp = angular.module('itemDetailsApp', ['headerApp']);

itemDetailsApp.controller('itemDetailsCtrl', ['$scope', '$window', '$http','modalService', function($scope, $window, $http, modalService){
    
    var user = localStorage.getItem("userloggedin");
    
    $scope.message = $window.message;
    
    $scope.image = $window.image;
    $scope.item_id = $window.item_id;
    $scope.title = $window.title;
    $scope.category = $window.category;
    $scope.description = $window.description;
    $scope.leaseValue = parseInt($window.leaseValue);
    $scope.leaseTerm = $window.leaseTerm;
    
    $scope.uploadImage = function(file){
        var reader = new FileReader();
        reader.onload = function(event) {
            $scope.$apply(function() {
                $scope.image = reader.result;
            });
        }
        reader.readAsDataURL(file);
        
    }
    
    // checking if the response code is 0 or not to show error div of itemdetails div
    if($window.code != 0){
        $scope.showError = true;
    }
    else{
        $scope.showError = false;
    }
    
    // checking if the loggedIn user matches with the items userId
    if(user == $window.userId){
        $scope.userMatch = true;
    }else{
        $scope.userMatch = false;
    }
    
    var load_Gapi = function() { //for google
        gapi.load('auth2', function() {
            gapi.auth2.init();
        });
    }
    
    //to get current location of the user and show it in the location by default
    var getLocation = function() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(showPosition);
        } else {
            console.log("Geolocation is not supported by this browser.");
        }
    }

    var showPosition = function(position) {
        latitude = position.coords.latitude;
        longitude = position.coords.longitude;
        console.log("Latitude: " + latitude + "<br>Longitude: " + longitude);
        coords = new google.maps.LatLng(latitude, longitude);

        var geocoder = new google.maps.Geocoder();
        var latLng = new google.maps.LatLng(latitude, longitude);
        geocoder.geocode({'latLng' : latLng},function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                $scope.location = results[4].formatted_address;
                console.log($scope.location);
            } else {
                console.log("Geocode was unsucessfull in detecting your current location");
            }
        });
    }
    
    $scope.requestItem = function(){
        modalService.showModal({}, {bodyText: 'Are you sure you want to request the Item?'}).then(
            function(result){
                if (user == "" || user == null || user == "anonymous")
					logInCheck();
				else
                    $http({
                        url:'/flsv2/RequestItem?req='+JSON.stringify({itemId:$scope.item_id,userId:user}),
                        method:"GET"
                    }).then(function success(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                            window.location.replace("/flsv2/index.html");
                        },function(){});
                    },
                    function error(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                    });
            }, 
            function(){

            });
    }
	
	$scope.wishItem = function(){
        
        var item_title = $scope.title;
        if(item_title == '')
            item_title = null;
        
        var item_id = $scope.item_id;
        if(item_id == '')
            item_id = 0;
        
        var item_description = $scope.description;
        if (item_description == '') 
		  item_description = null;
        
        var item_category = $scope.category;
        if (item_category == '' || item_category == 'Category') 
		  item_category = null;
        
        var item_user_id = user;
        if (item_user_id == '') 
		  item_user_id = "anonymous";
        
        var item_lease_value = $scope.leaseValue;
        if (item_lease_value == '') 
		  item_lease_value = 0;
        
        var item_lease_term = $scope.leaseTerm;
        if (item_lease_term == '' || item_lease_term == 'Lease Term') 
		  item_lease_term = null;
	
		var item_status = "Wished";
        
        req = {id:item_id,
                        title:item_title,
                        description:item_description,
                        category: item_category,
                        userId: item_user_id,
                        leaseValue: item_lease_value,
                        leaseTerm: item_lease_term,
						status: item_status,
                        image: $scope.image};
        
        modalService.showModal({}, {bodyText: 'Are you sure you want to add this Item to your Wishlist?'}).then(
            function(result){
				if (user == "" || user == null || user == "anonymous")
					logInCheck();
				else
					$.ajax({ url: '/flsv2/WishItem',
							type: 'post',
							data: {req : JSON.stringify(req)},
							contentType: "application/x-www-form-urlencoded",
							dataType: "json",
							success:function(response){
								modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
									 window.location.replace("/flsv2/index.html");
								},function(){});
							},
							error: function(){
								modalService.showModal({}, {bodyText: "Not Working",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
							}
						});
            },
            function(){

            });
    }
    
    $scope.editItem = function(){
        
        var item_title = $scope.title;
        if(item_title == '')
            item_title = null;
        
        var item_id = $scope.item_id;
        if(item_id == '')
            item_id = 0;
        
        var item_description = $scope.description;
        if (item_description == '') 
		  item_description = null;
        
        var item_category = $scope.category;
        if (item_category == '' || item_category == 'Category') 
		  item_category = null;
        
        var item_user_id = user;
        if (item_user_id == '') 
		  item_user_id = "anonymous";
        
        var item_lease_value = $scope.leaseValue;
        if (item_lease_value == '') 
		  item_lease_value = 0;
        
        var item_lease_term = $scope.leaseTerm;
        if (item_lease_term == '' || item_lease_term == 'Lease Term') 
		  item_lease_term = null;
        
        req = {id:item_id,
                        title:item_title,
                        description:item_description,
                        category: item_category,
                        userId: item_user_id,
                        leaseValue: item_lease_value,
                        leaseTerm: item_lease_term,
                        image: $scope.image};
        
        modalService.showModal({}, {bodyText: 'Are you sure you want to update the Item?'}).then(
            function(result){
                $.ajax({ url: '/flsv2/EditPosting',
			             type: 'post',
			             data: {req : JSON.stringify(req)},
			             contentType: "application/x-www-form-urlencoded",
			             dataType: "json",
                         success:function(response){
                            modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
								window.location.replace("/flsv2/index.html");
							},function(){});
                         },
                         error: function(){
                            modalService.showModal({}, {bodyText: "Not Working",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                         }
                    });
            },
            function(){

            });
    }
    
    $scope.deleteItem = function(){
        modalService.showModal({}, {bodyText: 'Are you sure you want to delete the Item?'}).then(
            function(result){
                    $http({
                        url:'/flsv2/DeletePosting?req='+JSON.stringify({id:$scope.item_id,userId:user}),
                        method:"GET"
                    }).then(function success(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                            window.location.replace("/flsv2/index.html");
                        },function(){});
                    },
                    function error(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                    });
            }, 
            function(){

            });
    }
    
    load_Gapi();
    
    getLocation();
    
}]);