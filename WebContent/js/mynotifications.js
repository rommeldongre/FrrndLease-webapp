var myNotifications = angular.module('myApp');

myNotifications.controller('myNotificationsCtrl', ['$scope', 'userFactory', 'eventsCount', function($scope, userFactory, eventsCount){
    
    var offset = 0;
    var Limit = 5;
    
    var initialPopulate = function(){
        offset = 0;
        
        getNotifications();
        
        $scope.events = [];
        
        $scope.loadMore = true;
    }
    
    var getNotifications = function(){
        
        req = {
            userId: userFactory.user,
            limit: Limit,
            offset: offset
        }
        
        getNotificationsSend(req);
        
    }
    
    getNotificationsSend = function(req) {
		$.ajax({
			url: '/flsv2/GetNotifications',
			type: 'post',
			data: JSON.stringify(req),
			contentType:"application/json",
			dataType:"json",
			
			success: function(response) {
                if(response.code == 0){
                    $scope.$apply(function(){
                        $scope.events.push.apply($scope.events, response.resList);
                        if(response.resList.length < Limit)
                            $scope.loadMore = false;
                    });
                    offset = response.offset;
                    
                }else{
                    $scope.$apply(function(){
                        $scope.loadMore = false;
                    });
                }
			},
		
			error: function() {
			}
		});
	};
    
    $scope.loadmore = function(){
        getNotifications();
    }
    
    $scope.readEvent = function(index, s){
        $.ajax({
                url: '/flsv2/EventReadStatus',
                type: 'post',
                data: JSON.stringify({
                    eventId: $scope.events[index].eventId,
                    readStatus: s,
                    userId:userFactory.user,
                    accessToken: userFactory.userAccessToken
                }),
                contentType:"application/json",
                dataType:"json",

                success: function(response) {
                    if(response.code == 0){
                        eventsCount.updateEventsCount();
                        $scope.$apply(function(){
                            if($scope.events[index].readStatus == 'FLS_READ'){
                                $scope.events[index].readStatus = 'FLS_UNREAD';
                            }else{
                                $scope.events[index].readStatus = 'FLS_READ';
                            }
                        });
                    }
                },

                error: function() {
                }
            });
    }
    
    initialPopulate();
    
}]);