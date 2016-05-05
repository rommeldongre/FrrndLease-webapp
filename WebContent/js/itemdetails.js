var itemDetailsApp = angular.module('itemDetailsApp', []);

itemDetailsApp.controller('headerCtrl', function($scope){
    
    var user = localStorage.getItem("userloggedin");
    
    if(user == "" || user == null){
        user = "anonymous";
        localStorage.setItem("userloggedin", user);	
    }else{
        $scope.salutation = localStorage.getItem("userloggedinName");
    }
    
    $scope.isAdmin = function(){
        if(user == 'frrndlease@greylabs.org')
            return true;
        else
            return false;
    }
    
    $scope.isAnonymous = function(){
        if(user == "anonymous")
            return true;
        else
            return false;
    }
    
    $scope.isLoggedIn = function(){
        if(user != "anonymous")
            return true;
        else
            return false;
    }
    
    $scope.logout = function(){
        localStorage.setItem("userloggedin", "anonymous");  //userloggedin-> anonymous
        var auth2 = gapi.auth2.getAuthInstance();
        auth2.signOut().then(function() {
            console.log('User signed out.');
        });
											
        window.location.replace("index.html");
    }
});

itemDetailsApp.controller('itemDetailsCtrl', ['$scope', '$window', function($scope, $window){
    
    $scope.code = $window.code;
    $scope.message = $window.message;
    
    $scope.errorCheck = function(){
        if($scope.code != 0)
            return true;
        else
            return false;
    }
    
    $scope.itemDetailsCheck = function(){
        if($scope.code != 0)
            return false;
        else
            return true;
    }
    
}]);