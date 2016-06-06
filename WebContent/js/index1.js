var indexApp = angular.module('indexApp', ['headerApp', 'carouselApp']);

indexApp.controller('indexCtrl', ['$scope', '$timeout', 'search', 'userFactory', 'getItemsForCarousel', 'getLocation', function($scope, $timeout, search, userFactory, getItemsForCarousel, getLocation){
    
    if(userFactory.user != "" && userFactory.user != null && userFactory.user != 'anonymous')
        window.location.replace("myapp.html");
    
    $timeout(function(){
        getLocation.sendLocationToCarousel();
    }, 2000);

    $scope.storeYourStuff = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == 'anonymous'){
            window.location.replace("mylogin.html");
        }
        else{
            window.location.replace("mystore.html");
        }
    }
    
    $scope.searchItem = function(s){
        if(s != undefined || s!= '' || s!= null){
            s = s.replace(/\+/g, " ");
            search.changeSearchString(s);
            $scope.search.string = '';
        }
    }
    
}]);