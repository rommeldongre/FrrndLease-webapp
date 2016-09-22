var itemDetailsApp = angular.module('itemDetailsApp', ['headerApp']);

itemDetailsApp.controller('itemDetailsCtrl', ['$scope',
											'$window', 
											'$http', 
											'userFactory', 
											'bannerService', 
											'modalService', 
											function($scope, 
											$window, 
											$http, 
											userFactory, 
											bannerService, 
											modalService){
    
    var user = localStorage.getItem("userloggedin");
    
    $scope.item = {};
    
    $scope.item.primaryImageLink = $window.primaryImageLink;
    $scope.item.title = $window.title;
	$scope.item.description = $window.description;
	$scope.item.category = $window.category;
	$scope.item.leaseValue = $window.leaseValue;
	$scope.item.leaseTerm = $window.leaseTerm;
    $scope.item.imageLinks = $window.imageLinks.split(",");
                                   
    if($scope.item.primaryImageLink != null){
        $scope.item.imageLinks.unshift($scope.item.primaryImageLink);
    }
                                                
    $scope.selectedImage = function(index){
        $scope.item.primaryImageLink = $scope.item.imageLinks[index];
    }
                                                
    $scope.item_id = $window.item_id;
        
    // checking if the response code is 0 or not to show error div of itemdetails div
    if($window.code != 0){
        $scope.showError = true;
    }
    else{
        $scope.showError = false;
    }
    
    // checking if the loggedIn user matches with the items userId
    if(userFactory.user == $window.userId){
        $scope.userMatch = true;
    }else{
        $scope.userMatch = false;
    }
    
    $scope.requestItem = function(){
        modalService.showModal({}, {bodyText: 'Are you sure you want to request the Item?'}).then(
            function(result){
                if (userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
					$('#loginModal').modal('show');
				else
                    $http({
                        url:'/flsv2/RequestItem?req='+JSON.stringify({itemId:$scope.item_id,userId:userFactory.user}),
                        method:"GET"
                    }).then(function success(response){
						if(response.data.Code== 0){
							bannerService.updatebannerMessage(response.data.Message,"/flsv2/index.html");
							$("html, body").animate({ scrollTop: 0 }, "slow");
						}else{
							modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
								window.location.replace("/flsv2/index.html");
							},function(){});
					}
                    },
                    function error(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                    });
            }, 
            function(){

            });
    }
	
	$scope.wishItem = function(){
        
        var item_title = $scope.item.title;
        if(item_title == '')
            item_title = null;
        
        var item_id = $scope.item_id;
        if(item_id == '')
            item_id = 0;
        
        var item_description = $scope.item.description;
        if (item_description == '') 
		  item_description = null;
        
        var item_category = $scope.item.category;
        if (item_category == '' || item_category == 'Category') 
		  item_category = null;
        
        var item_user_id = userFactory.user;
        if (item_user_id == '') 
		  item_user_id = "anonymous";
        
        var item_lease_value = $scope.item.leaseValue;
        if (item_lease_value == '') 
		  item_lease_value = 0;
        
        var item_lease_term = $scope.item.leaseTerm;
        if (item_lease_term == '' || item_lease_term == 'Lease Term') 
		  item_lease_term = null;
	
		var item_status = "Wished";
        
        req = {id:item_id,
                        title:item_title,
                        description:item_description,
                        category: item_category,
                        userId: item_user_id,
                        leaseValue: item_lease_value,
                        leaseTerm: item_lease_term,
						status: item_status,
                        image: $scope.item.image};
        
        modalService.showModal({}, {bodyText: 'Are you sure you want to add this Item to your Wishlist?'}).then(
            function(result){
				if (userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
					$('#loginModal').modal('show');
				else
					$.ajax({ url: '/flsv2/WishItem',
							type: 'post',
							data: {req : JSON.stringify(req)},
							contentType: "application/x-www-form-urlencoded",
							dataType: "json",
							success:function(response){
								modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
									 window.location.replace("myapp.html#/mywishlists");
								},function(){});
							},
							error: function(){
								modalService.showModal({}, {bodyText: "Not Working",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
							}
						});
            },
            function(){

            });
    }
    
    $scope.editItem = function(){
        window.location.replace("myapp.html#/postitem/"+$scope.item_id);
    }
    
    $scope.deleteItem = function(){
        modalService.showModal({}, {bodyText: 'Are you sure you want to delete the Item?'}).then(
            function(result){
                    $http({
                        url:'/flsv2/DeletePosting?req='+JSON.stringify({id:$scope.item_id,userId:userFactory.user}),
                        method:"GET"
                    }).then(function success(response){
						if(response.data.Code==0){
							bannerService.updatebannerMessage(response.data.Message,"/flsv2/index.html");
							$("html, body").animate({ scrollTop: 0 }, "slow");
						}else{
							modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
								window.location.replace("/flsv2/index.html");
							},function(){});
						}
                    },
                    function error(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                    });
            }, 
            function(){

            });
    }
    
    var lastOffset = 0;
    
    $scope.showItemTimeline = function(){
		$("#openBtn").click();
		$scope.showNext = true;
		getItemTimeline(lastOffset);
	}
    
	var getItemTimeline = function(Offset){
		var req = {
			itemId : $scope.item_id,
			cookie: Offset,
			limit: 3
		}
		
		getItemTimelineSend(req);
	}
	
	var getItemTimelineSend = function(req){
		$.ajax({
            url: '/flsv2/GetItemTimeline',
            type: 'post',
            data: JSON.stringify(req),
			contentType:"application/json",
			dataType:"json",
            success: function(response){
				if(response.code == 0){
                if(lastOffset == 0){
					$scope.$apply(function(){
						$scope.timelineArray = [response.resList];
					});
                    getItemTimeline(response.cookie);
                    }else{
						$scope.$apply(function(){
						  $scope.timelineArray.push(response.resList);
						});
                    }
                    lastOffset = response.cookie;
				}else{
					$scope.showNext = false;
                }
            },
            error: function(){
            }
	
        });
	}
	
	// called when Show More Items Timeline button is clicked
    $scope.loadNextItemTimeline = function(){
        getItemTimeline(lastOffset);
    }
    
}]);