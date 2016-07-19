var myIndex = angular.module('myApp');

myIndex.controller('myIndexCtrl', ['userFactory', '$timeout', 'searchService', 'profileFactory', function(userFactory, $timeout, searchService, profileFactory){
    
    localStorage.setItem("prevPage","myapp.html#/");
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    $timeout(function(){
        searchService.sendDataToCarousel();
    }, 2000);
    
    
    // Checking if the user status is live or onhold
    var checkLiveStatus = function(){
        profileFactory.getProfile(userFactory.user).then(
        function(response){
            if(response.data.liveStatus == 0){
                localStorage.setItem("userloggedin", "anonymous");  //userloggedin-> anonymous
                var auth2 = gapi.auth2.getAuthInstance();
                auth2.signOut().then(function() {
                    console.log('User signed out.');
                });

                window.location.replace("index.html");
            }
        },
        function(error){
        });
    }
    
    $timeout(function(){
        checkLiveStatus();
    }, 5000);
    
}]);