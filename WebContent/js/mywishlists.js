var myWishLists = angular.module('myApp');

myWishLists.controller('myWishListsCtrl', ['$scope', 
											'$timeout', 
											'userFactory', 
											'bannerService', 
											'modalService', 
											function($scope, 
											$timeout, 
											userFactory, 
											bannerService, 
											modalService){
    
    localStorage.setItem("prevPage","myapp.html#/mywishlists");
    
    var itemNextId = 0;
    
    $scope.wishList = [];
    
    var initialPopulate = function(){
        
        itemNextId = 0;
        
        getWishListItem(itemNextId);
        
    }
    
    var getWishListItem = function(id){
        
        var req = {
            operation: "BrowseN",
            token: id
        }
        
        displayWishListItem(req);
        
    }
    
    var displayWishListItem = function(req){
        $.ajax({
            url: '/flsv2/GetItemWishlist',
            type: 'get',
            data: {req : JSON.stringify(req)},
            contentType:"application/json",
            dataType:"json",
            success: function(response) {
                if(response.Code == "FLS_SUCCESS") {
                    var obj = JSON.parse(response.Message);
                    
                    if(obj.userId == userFactory.user)
                        $scope.$apply(function(){
                           $scope.wishList.unshift(obj); 
                        });
                    
                    itemNextId = obj.itemId;
                    getWishListItem(itemNextId);
                }
            },
            error: function() {
            }
        });
    }
    
    initialPopulate();
    
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
				image: null
            }
            sendAddWishItem(req);
        }, function(){});
    }
	
	var sendAddWishItem = function(req){
        $.ajax({
            url: '/flsv2/WishItem',
            type:'post',
            data: {req : JSON.stringify(req)},
            contentType:"application/x-www-form-urlencoded",
            dataType: "JSON",
            success: function(response) {
				if(response.Code == 0){
					bannerService.updatebannerMessage("Item Successfully added to Wish List","");
					$scope.wishList = [];
					initialPopulate();
				}else{
					modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						$scope.wishList = [];
						initialPopulate();
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
            url: '/flsv2/ImportWishlist',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "JSON",
            success: function(response) {
                modalService.showModal({}, {bodyText: response.wishItemCount+" out of "+response.totalWishItemCount+" Amazon Wishlist Items Imported",showCancel: false,actionButtonText: 'OK'}).then(function(result){
                    $scope.wishList = [];
                    initialPopulate();
                }, function(){});
            },
            error: function() {
                console.log("Invalid url");
            }
        });
    }
    
    $scope.deleteWishItem = function(itemId, index){
        modalService.showModal({}, {bodyText: "Are you sure you want to delete this WishItem?",actionButtonText: 'YES'}).then(function(result){
            var req = {
                id: itemId,
                userId: userFactory.user
            }
            
            sendDeleteWishItem(req, index);
            
        }, function(){});
    }
    
    var sendDeleteWishItem = function(req, index){
        $.ajax({
            url: '/flsv2/DeleteWishlist',
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
					modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
						$scope.wishList.splice(index, 1);
					}, function(){});
				}
            },
            error: function() {
            }
        });
    }
    
    $scope.showItemDetails = function(itemId){
        localStorage.setItem("itemToShow", itemId);
        localStorage.setItem("prevFunc", "viewItemOwner");
        window.location.replace('mywishitemDetails.html');
    }
    
}]);