var userProfileApp = angular.module('userProfileApp', ['headerApp', 'footerApp']);

userProfileApp.controller('userProfileCtrl', ['$scope', function($scope){
    
    $scope.user = {};
    
    $scope.user.userId = 'ankitkarnany@gmail.com';
    $scope.user.itemPrimaryImageLink = 'http://s3-ap-southeast-1.amazonaws.com/fls-items-dev/tulips-are-266/post/tulips-are-266-primary-7414.png';
    
}]);