var myWishLists = angular.module('myApp');

myWishLists.controller('myWishListsCtrl', ['$scope', 
											'$timeout', 
											'userFactory', 
											'bannerService', 
											'modalService', 
                                            'getItemsForCarousel',
											function($scope, 
											$timeout, 
											userFactory, 
											bannerService, 
											modalService,
                                            getItemsForCarousel){
    
    localStorage.setItem("prevPage","myapp.html#/mywishlists");
    
    var itemNextId = 0;
    
    $scope.wishList = [];
                                                
    // populate the carousel with initital array
    var initWishlist = function(){
        itemNextId = 0;
        
        $scope.wishList = [];
        
        $scope.loadMore = false;
        
        populateWishlist(itemNextId);
    }
    
    var populateWishlist = function(token){
        
        var req = {
            cookie: token,
            userId: userFactory.user,
			match_userId: null,
            category: null,
            limit: 5,
            lat: 0.0,
            lng: 0.0,
            searchString: '',
            itemStatus: ['Wished']
        };
        displayWishlist(req);
        
    }
    
    var displayWishlist = function(req){
        getItemsForCarousel.getItems(req).then(
            function(response){
                if(response.data.returnCode == 0){
                    $scope.wishList.push.apply($scope.wishList, response.data.resList);
                    itemNextId = response.data.lastItemId;
					$scope.loadMore = true;
                }else{
					$scope.loadMore = false;
                }
            },
            function(error){
				//Error message in console.
                console.log("Not able to get items " + error.message);
            });
    }
    
    // called when next carousel button is clicked
    $scope.loadNextWishItems = function(){
        populateWishlist(itemNextId);
    }
    
    initWishlist();
    
	$scope.addWishItem = function(){
        modalService.showModal({}, {submitting: true, labelText: 'Add Wish Item Name', actionButtonText: 'Submit'}).then(function(result){
            var itemTitle = result;
            if(itemTitle == "" || itemTitle == undefined)
                itemTitle = "";
            
			var itemId = 0;
			var itemDescription = null, itemCategory= "House", itemStatus = "Wished", url = null, itemLeaseTerm = null, itemLeaseValue=1000 ;
            var req = {
                userId: userFactory.user,
                title: itemTitle,
				id: 0,
				description: '',
				category: 'House',
				leaseValue: 0,
				leaseTerm: '',
				status: 'Wished',
				image: ''
            }
            sendAddWishItem(req);
        }, function(){});
    }
	
	var sendAddWishItem = function(req){
        $.ajax({
            url: '/WishItem',
            type:'post',
            data: {req : JSON.stringify(req)},
            contentType:"application/x-www-form-urlencoded",
            dataType: "JSON",
            success: function(response) {
				if(response.Code == 0){
					bannerService.updatebannerMessage(response.Message,"");
					$scope.wishList = [];
					initWishlist();
				}else{
					modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
						$scope.wishList = [];
						initWishlist();
					}, function(){});
				}
            },
            error: function() {
                console.log("Invalid Entry");
            }
        });
    }
	
    
    $scope.importWishList = function(){
        modalService.showModal({}, {submitting: true, labelText: 'Input URL of Public Amazon Wishlist', actionButtonText: 'Submit'}).then(function(result){
            var url = result;
            if(url == "" || url == undefined)
                url = "";
            
            var req = {
                userId: userFactory.user,
                url: url
            }
            
            sendImportWishListUrl(req);
        }, function(){});
    }
    
    var sendImportWishListUrl = function(req){
        $.ajax({
            url: '/ImportWishlist',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "JSON",
            success: function(response) {
                modalService.showModal({}, {bodyText: response.wishItemCount+" out of "+response.totalWishItemCount+" Amazon Wishlist Items Imported",showCancel: false,actionButtonText: 'OK'}).then(function(result){
                    $scope.wishList = [];
                    initWishlist();
                }, function(){});
            },
            error: function() {
                console.log("Invalid url");
            }
        });
    }
    
    $scope.deleteWishItem = function(itemId, index){
        modalService.showModal({}, {bodyText: "Are you sure you want to delete this WishItem?",actionButtonText: 'Yes'}).then(function(result){
            var req = {
                id: itemId,
                userId: userFactory.user
            }
            
            sendDeleteWishItem(req, index);
            
        }, function(){});
    }
    
    var sendDeleteWishItem = function(req, index){
        $.ajax({
            url: '/DeleteWishlist',
            type: 'get',
            data: {req : JSON.stringify(req)},
            contentType:"application/json",
            dataType:"json",
            success: function(response) {
				if(response.Code == 0){
					bannerService.updatebannerMessage(response.Message,"");
					$("html, body").animate({ scrollTop: 0 }, "slow");
					$timeout(function () {
						$scope.wishList.splice(index, 1);
					});
				}else{
					modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
						$scope.wishList.splice(index, 1);
					}, function(){});
				}
            },
            error: function() {
            }
        });
    }
    
    $scope.showItemDetails = function(itemId){
		window.location.replace("myapp.html#/mywishitemdetails/"+itemId);
    }
    
}]);