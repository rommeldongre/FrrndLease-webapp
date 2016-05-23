var indexApp = angular.module('indexApp', ['headerApp', 'carouselApp']);

indexApp.controller('indexCtrl', ['$scope', 'userFactory', 'getItemsForCarousel', function($scope, userFactory, getItemsForCarousel){
    
    if(userFactory.user != "" && userFactory.user != null && userFactory.user != 'anonymous')
        window.location.replace("myindex.html");

    $scope.storeYourStuff = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == 'anonymous'){
            window.location.replace("mylogin.html");
        }
        else{
            storeCurrentFunction('storeYourStuff');
            window.location.replace("mystore.html");
        }
    }
    
}]);