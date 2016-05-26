var myFriendsListApp = angular.module('myApp');

myFriendsListApp.controller('myFriendsListCtrl', ['$scope', 'userFactory', function($scope, userFactory){
    
    var friendIdArray = [];
    var lastFriendId = '';
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    var getFriendsList = function(){
        
        var req = {
            operation: "getNext",
            id: userFactory.user,
            token: lastFriendId
        }
        
        displayFriendsList(req);
    }
    
    var displayFriendsList = function(req){
        $.ajax({
                url: '/flsv2/GetFriends',
                type:'get',
                data: {req: JSON.stringify(req)},
                contentType:"application/json",
                dataType: "json",

                success: function(response) {
                    if(response.Code == "FLS_SUCCESS") {
                        var obj = JSON.parse(response.Message);
                        
                        // to check if the friend id is email or not
                        var strIndex = obj.friendId.indexOf('@fb');
                        if(strIndex != -1)
                            obj.friendId = '-';
                        
                        if(lastFriendId == '')
                            $scope.$apply(function(){
                                $scope.friends = [obj];
                            });
                        else
                            $scope.$apply(function(){
                                $scope.friends.push(obj);
                            });
                            
                        console.log(obj);
                        
                        lastFriendId = response.Id;
                        friendIdArray.push(response.Id);
                        getFriendsList();
                    }
                    else{
                        if(lastFriendId == '')
                            $scope.empty.text = "You have not connected with your friends on fRRndLease. Add or import your contacts to have access to their posted items.";
                    }
                },

                error: function() {
                }
            });
    }
    
    getFriendsList();
    
    $scope.addFriend = function(){
        localStorage.setItem("prevFunc", "addFriend");
        window.location.replace("myfrienddetails.html");
    }
    
    $scope.importFriend = function(){
        window.location.replace("myimportfriend.html");
    }
    
    $scope.showFriendDetails = function(index){
        localStorage.setItem("prevFunc", "viewFriend");
        localStorage.setItem("itemToShow", friendIdArray[index]);
        window.location.replace('myfrienddetails.html');
    }
    
}]);