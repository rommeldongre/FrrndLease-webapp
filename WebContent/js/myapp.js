var myApp = angular.module('myApp', ['headerApp', 'carouselApp', 'footerApp', 'ngRoute', 'cp.ng.fix-image-orientation', 'multiStepForm', 'uiSwitch']);

myApp.run(['userFactory', function(userFactory){
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("index.html");
    
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
	
	.when('/mywishitemdetails/:id?', {
        templateUrl : 'mywishitemdetails.html',
        controller : 'mywishitemdetailsCtrl'
    })
    
    .when('/mypostings', {
        templateUrl : 'mypostings.html',
        controller : 'myPostingsCtrl'
    })
    
    .when('/mynotifications', {
        templateUrl : 'mynotifications.html',
        controller : 'myNotificationsCtrl'
    })
    
    .when('/edititem/:id?', {
        templateUrl : 'edititem.html',
        controller : 'editItemCtrl'
    })
    
    .when('/wizard', {
        templateUrl : 'postitemwizard.html',
        controller : 'postItemWizardCtrl'
    })
    
    .when('/myfrienddetails/:id?', {
        templateUrl : 'myfrienddetails.html',
        controller : 'myFriendDetailsCtrl'
    })
    
    .otherwise({redirectTo : '/'});
    
});

myApp.filter('to_trusted', ['$sce', function($sce){
    return function(text) {
        return $sce.trustAsHtml(text);
    };
}]);