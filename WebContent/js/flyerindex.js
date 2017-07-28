var flyerIndexApp = angular.module('flyerIndexApp', ['headerApp', 'footerApp','ui.bootstrap']);

flyerIndexApp.controller('flyerIndexCtrl', ['$scope', '$timeout', 'userFactory', '$rootScope', function($scope, $timeout, userFactory,$rootScope){
    
    localStorage.setItem("prevPage","index.html");
    
    if(userFactory.user != "" && userFactory.user != null && userFactory.user != "anonymous")
        window.location.replace("myapp.html");

    $scope.storeYourStuff = function(){
        if(userFactory.user == "" || userFactory.user == null || userFactory.user == 'anonymous'){
            $('#registerModal').modal('show');
        }
        else{
            window.location.replace("myapp.html#/edititem");
        }
    }
      
}]);


angular.element(document).ready(function() {
  angular.bootstrap(document, ['flyerIndexApp']);
});