var myPostings = angular.module('myApp');

myPostings.controller('myPostingsCtrl', ['$scope', '$timeout', 'searchService', function($scope, $timeout, searchService){
    
    localStorage.setItem("prevPage","myapp.html#/mypostings");
    
    $timeout(function(){
        searchService.sendDataToCarousel();
    }, 2000);
    
}]);