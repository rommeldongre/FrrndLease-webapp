var myProfile = angular.module('myApp');

myProfile.controller('myProfileCtrl', ['$scope', 'userFactory', 'profileFactory', 'modalService', function($scope, userFactory, profileFactory, modalService){
    
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
				$scope.location = response.data.location;
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
        unsaved = false;
        var req = {
            userId : userFactory.user,
			fullName : $scope.fullname,
			mobile : $scope.mobile,
			location : $scope.location
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