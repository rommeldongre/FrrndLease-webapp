var myPostings = angular.module('myApp');

myPostings.controller('myPostingsCtrl', ['$scope', '$timeout', 'searchService', function($scope, $timeout, searchService){
    
    $timeout(function(){
        searchService.sendDataToCarousel();
    }, 2000);
    
}]);