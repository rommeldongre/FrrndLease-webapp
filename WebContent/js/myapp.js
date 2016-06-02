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
    
    .when('/myoutgoingrequests', {
        templateUrl : 'myoutgoingrequests.html',
        controller : 'myOutGoingRequestsCtrl'
    })
    
    .when('/myincomingrequests', {
        templateUrl : 'myincomingrequests.html',
        controller : 'myInComingRequestsCtrl'
    })
    
    .when('/mywishlists', {
        templateUrl : 'mywishlists.html',
        controller : 'myWishListsCtrl'
    })
    
    .when('/mypostings', {
        templateUrl : 'mypostings.html',
        controller : 'myPostingsCtrl'
    })
    
    .otherwise({redirectTo : '/'});
    
});