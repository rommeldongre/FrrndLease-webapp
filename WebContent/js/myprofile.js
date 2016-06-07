var myProfile = angular.module('myApp');

myProfile.controller('myProfileCtrl', ['$scope', 'userFactory', 'profileFactory', 'modalService', function($scope, userFactory, profileFactory, modalService){
    
    var Address = '', Sublocality = '', Locality = '', Lat = 0.0, Lng = 0.0;
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    var unsaved = false; 
    
    var displayProfile = function(){
        profileFactory.getProfile(userFactory.user).then(
        function(response){
            if (response.data.code == 0) {
                $scope.userId = userFactory.user;
                $scope.fullname = response.data.fullName;
				$scope.mobile = response.data.mobile;
				$scope.location = response.data.address;
				$scope.credit = response.data.credit;
            } else {
                $scope.userId = "";
                $scope.fullname = "";
				$scope.mobile = "";
				$scope.location = "";
				$scope.credit = "";
            }
        },
        function(error){
            console.log("unable to get profile: " + error.message);
        });
    }
    
    // getting the profile
    displayProfile();
    
    $scope.updateProfile = function(){
        
        if($scope.location != '')
            getLocationData($scope.location);
        else
            editProfileData();
        
        unsaved = false;
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
                        if(component.types[0] == 'sublocality_level_1')
                            Sublocality = component.long_name;
                        if(component.types[0] == 'locality')
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
            lng: Lng
		}
		editProfile(req);
    }
    
    var editProfile = function(req){
        profileFactory.updateProfile(req).then(
        function(response){
            if (response.data.code == 0) {
                dialogText = 'Your Profile Has Been Updated!!';
            }else{
                dialogText = 'please try after sometime';
            }
            modalService.showModal({}, {bodyText:dialogText,showCancel: false,actionButtonText: 'OK'}).then(function(result){}, function(){});
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