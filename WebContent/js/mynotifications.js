var myNotifications = angular.module('myApp');

myNotifications.controller('myNotificationsCtrl', ['$scope', 
													'$timeout',
													'userFactory',
													'modalService',
													'bannerService',
													'logoutService',
													'eventsCount',
													function($scope,
													$timeout,
													userFactory,
													modalService,
													bannerService,
													logoutService,
													eventsCount){
    
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
			url: '/GetNotifications',
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
    
    $scope.markAllAsRead = function(){
        for(var i in $scope.events){
            if($scope.events[i].readStatus == 'FLS_UNREAD'){
                $scope.readEvent(i,'FLS_READ');
            }
        }
    }
    
    $scope.readEvent = function(index, s){
        $.ajax({
                url: '/EventReadStatus',
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
	
	$scope.replyFriendMessage = function(index){
		modalService.showModal({}, {messaging: true, bodyText: 'Reply to this message', actionButtonText: 'Send'}).then(function(result){
            var message = result;
            if(message == "" || message == undefined)
                message = null;
				
            var ItemId = $scope.events[index].itemId;
            
            var Subject = "ITEM";
            if(ItemId == 0)
                Subject = "FRIEND";
            
		   var req = {
               userId: userFactory.user,
               accessToken: userFactory.userAccessToken,
               from: userFactory.user,
               to: $scope.events[index].fromUserId,
               message: message,
               subject: Subject,
               itemId: ItemId
            }
           
			sendMessage(req);	
        }, function(){});
    }
	
	var sendMessage = function(req){
		
		$.ajax({
			url: '/SendMessage',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code==0){
                    initialPopulate();
					bannerService.updatebannerMessage("Message Sent!!");
                    $("html, body").animate({ scrollTop: 0 }, "slow");
				}else{
					modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){eventsCount.updateEventsCount();
						if(response.code == 400){
							logoutService.logout();
						}
					}, function(){});
				}
			},
		
			error: function() {
				console.log("Not able to send message");
			}
		});
	}
	
	$scope.deleteNotification = function(id,index){
		modalService.showModal({}, {bodyText: "Are you sure you want to delete this Notification?",actionButtonText: 'Yes'}).then(
			function(result){
				var EventId = id;
				if(EventId === '')
					EventId = 0;
				
				var req = {
					eventId: EventId,
					userId: userFactory.user,
					accessToken: userFactory.userAccessToken
				};
				deleteNotificationSend(req,index);
        },function(){});
	}
	
	var deleteNotificationSend = function(req,index){
		$.ajax({
			url: '/DeleteEvent',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code==0){
					bannerService.updatebannerMessage(response.message,"");
                    $("html, body").animate({ scrollTop: 0 }, "slow");
					$timeout(function () {
						$scope.events.splice(index, 1);
					});
				}else{
					modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){eventsCount.updateEventsCount();
						if(response.code == 400){
							logoutService.logout();
						}
					}, function(){});
				}
			},
		
			error: function() {
				console.log("Not able to send message");
			}
		});
	}
    
    initialPopulate();
    
}]);