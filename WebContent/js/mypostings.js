var myPostings = angular.module('myApp');

myPostings.controller('myPostingsCtrl', ['$scope', '$timeout', 'getLocation', function($scope, $timeout, getLocation){
    
    $timeout(function(){
        getLocation.sendLocationToCarousel();
    }, 2000);
    
}]);