var myIndex = angular.module('myApp');

myIndex.controller('myIndexCtrl', ['userFactory', '$timeout', 'searchService', function(userFactory, $timeout, searchService){
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    $timeout(function(){
        searchService.sendDataToCarousel();
    }, 2000);
    
}]);