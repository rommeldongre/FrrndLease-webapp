var userProfileApp = angular.module('userProfileApp', ['headerApp', 'footerApp']);

userProfileApp.controller('userProfileCtrl', ['$scope', '$window', function($scope, $window){
    
    $scope.user = {
        userId: $window.userId,
        profilePic: $window.userProfilePic
    };
    
}]);