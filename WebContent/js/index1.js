var indexApp = angular.module('indexApp', ['headerApp']);

indexApp.controller('indexCtrl', ['$scope', 'userFactory', function($scope, userFactory, headerClass){
    
    if(userFactory.user != "" && userFactory.user != null && userFactory.user != 'anonymous')
        window.location.replace("myindex.html");

}]);