var myApp = angular.module('myApp', ['headerApp', 'carouselApp', 'ngRoute']);

myApp.run(['userFactory', function(userFactory){
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("mylogin.html");
    
}]);

myApp.config(function($routeProvider){
    
    $routeProvider
    
    .when('/', {
        templateUrl : 'myindex.html',
        controller : 'myIndexCtrl'
    })
    
    .when('/myprofile', {
        templateUrl : 'myprofile.html',
        controller : 'myProfileCtrl'
    })
    
    .when('/myfriendslist', {
        templateUrl : 'myfriendslist.html',
        controller : 'myFriendsListCtrl'
    })
    
    .when('/myleasedinitems', {
        templateUrl : 'myleasedinitems.html',
        controller : 'myLeasedInItemsCtrl'
    })
    
    .when('/myleasedoutitems', {
        templateUrl : 'myleasedoutitems.html',
        controller : 'myLeasedOutItemsCtrl'
    })
    
    .otherwise({redirectTo : '/'});
    
});