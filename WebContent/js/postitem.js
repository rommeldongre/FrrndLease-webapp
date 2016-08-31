var postItemApp = angular.module('myApp');

postItemApp.controller('postItemCtrl', ['$scope', 'userFactory', 'bannerService', '$routeParams', 'modalService', function($scope, userFactory, bannerService, $routeParams, modalService){
    
    var itemId = $routeParams.id;
    
    $scope.editable = false;
    
    var userId = userFactory.user;
    var userAccessToken = userFactory.userAccessToken;
    
    $scope.item = {};
    
    $scope.categories = [];
    
    var getItemDetails = function(){
        var req = {
            table: "items",
            operation: "getdetails",
            row: {
                id: itemId,
                userId: userId
            }
        }
        if(itemId != undefined){
            $.ajax({
                url: '/flsv2/AdminOps',
                type:'get',
                data: {req: JSON.stringify(req)},
                contentType:"application/json",
                dataType: "json",
                success: function(response) {
                    if(response.Code == 0) {
                        $scope.$apply(function(){
                            $scope.item = JSON.parse(response.Message);
                            $scope.editable = true;
                        });
                    }
                    else{
                        //all categories are loaded
                    }
                },
                error:function() {}
            });
        }
    }

    var populateCategory = function(id){
        var req = {
            operation:"getNext",
            token: id
        }
        displayCategory(req);
    }
    
    var displayCategory = function(req){
        $.ajax({
            url: '/flsv2/GetCategoryList',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    $scope.$apply(function(){
                        $scope.categories.push(JSON.parse(response.Message).catName);
                    });
                    populateCategory(response.Id);
                }
                else{
                    //all categories are loaded
                }
            },
            error:function() {}
        });
    }
    
    // called on the page load
    populateCategory('');
    
    $scope.categorySelected = function(c){
        $scope.item.category = c;
    }
    
    $scope.leaseTerms = [];
    
    var populateLeaseTerm = function(id){
        var req = {
            operation:"getNext",
            token: id
        }
        displayLeaseTerm(req);
    }
    
    var displayLeaseTerm = function(req){
        $.ajax({
            url: '/flsv2/GetLeaseTerms',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    $scope.$apply(function(){
                        $scope.leaseTerms.push(JSON.parse(response.Message).termName);
                    });
                    populateLeaseTerm(response.Id);
                }
                else{
                    //all lease terms are loaded
                }
            },
            error:function() {}
        });
    }
    
    // called on the page load
    populateLeaseTerm('');
    
    $scope.leaseTermSelected = function(l){
        $scope.item.leaseTerm = l;
    }
    
    //beginning of image display
    $scope.uploadImage = function(file){
        EXIF.getData(file, function(){
            exif = EXIF.getAllTags(this);
            picOrientation = exif.Orientation;
		});
        
        var reader = new FileReader();
        reader.onload = function(event) {
            loadImage(reader.result,
                function (canvas) {
                    $scope.$apply(function() {
                        $scope.item.image = canvas.toDataURL();
                    });
                },
                {
                    maxWidth: 300,
                    maxHeight: 300,
                    canvas: true,
                    orientation: picOrientation
                }
            );
        }
        reader.readAsDataURL(file);
    }		
	//end of image display
    
    $scope.postItem = function(){
        
        var item_title = $scope.item.title;
        if(item_title == '')
            item_title = null;

        var item_id = 0;

        var item_description = $scope.item.description;
        if (item_description == '') 
           item_description = null;

        var item_category = $scope.item.category;

        var item_lease_value = $scope.item.leaseValue;
        if (item_lease_value == '')
            item_lease_value = 0;

        var item_lease_term = $scope.item.leaseTerm;

        var req = {
            id: item_id,
            title: item_title,
            description: item_description,
            category: item_category,
            userId: userId,
            leaseValue: item_lease_value,
            leaseTerm: item_lease_term,
            status: "InStore",
            image: $scope.item.image,
            accessToken: userAccessToken
        }
        
        modalService.showModal({}, {bodyText: 'Are you sure you want to Post this Item?'}).then(function(result){
            $.ajax({
                url: '/flsv2/PostItem',
                type: 'post',
                data: JSON.stringify(req),
                contentType: "application/x-www-form-urlencoded",
                dataType: "json",

                success: function(response) {
                    if(response.code == 0){
                        modalService.showModal({}, {bodyText: "This item is successfully posted in friend store. Do you want to share this on facebook?", cancelButtonText:'NO', actionButtonText: 'YES'}).then(function(result){
                            shareOrNot(response.uid);
                        },function(result){
                            bannerService.updatebannerMessage("Your Item has been added to the friend store successfully!!", "myapp.html#/");
                            $("html, body").animate({ scrollTop: 0 }, "slow");
                        });
                    }else{
                        modalService.showModal({}, {bodyText: response.message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                    }
                },

                error: function() {
                    modalService.showModal({}, {bodyText: "Something is Wrong with the network.",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                }
            });
        },function(){});
        
    }
    
    var shareOnFb = function(uid){
			
			var link = null;
			
			if(window.location.href.indexOf("frrndlease.com") > -1){
				link = 'http://www.frrndlease.com/ItemDetails?uid='+uid;
			}else{
				link = 'http://www.frrndlease.com/ItemDetails?uid=ripstick-wave-board-156';
				console.log('http://localhost:8080/flsv2/ItemDetails?uid='+uid);
			}
			
			FB.login(function(response) {		
				// Facebook checks whether user is logged in or not and asks for credentials if not.
				// Share item listing with facebook friends using share dialog
				FB.ui({
					method: 'share',
					href: link,
				},function(response){
                    var m = "";
					if (response && !response.error) {
                        m = "Item sucessfully posted on Frrndlease and shared on Facebook";
                    }else{
                        m = "Item sucessfully posted on Frrndlease";
                    }
                    bannerService.updatebannerMessage(m, "myapp.html#/");
					$("html, body").animate({ scrollTop: 0 }, "slow");
				});
            }, {scope: 'email,public_profile,user_friends'});
		}
    
    $scope.editItem = function(){
        
        var item_title = $scope.item.title;
        if(item_title == '')
            item_title = null;
        
        var item_id = $scope.item.id;
        if(item_id == '')
            item_id = 0;
        
        var item_description = $scope.item.description;
        if (item_description == '') 
		  item_description = null;
        
        var item_category = $scope.item.category;
        
        var item_lease_value = $scope.item.leaseValue;
        if (item_lease_value == '') 
		  item_lease_value = 0;
        
        var item_lease_term = $scope.item.leaseTerm;
        
        req = {
            id:item_id,
            title:item_title,
            description:item_description,
            category: item_category,
            userId: userId,
            leaseValue: item_lease_value,
            leaseTerm: item_lease_term,
            image: $scope.item.image
        };
        
        modalService.showModal({}, {bodyText: 'Are you sure you want to update this Item?'}).then(function(result){
            $.ajax({url: '/flsv2/EditPosting',
                    type: 'post',
                    data: {req : JSON.stringify(req)},
                    contentType: "application/x-www-form-urlencoded",
                    dataType: "json",
                    success:function(response){
                        modalService.showModal({}, {bodyText: response.Message,showCancel: false, actionButtonText: 'OK'}).then(function(result){
                            window.location.replace("ItemDetails?uid="+$scope.item.uid);
							},function(){});
                         },
                    error: function(){
                        modalService.showModal({}, {bodyText: "Something is Wrong",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                        }
                   });
            },function(){});
    }
    
    $scope.cancel = function(){
        window.location.replace("myapp.html#/");
    }
    
    getItemDetails();
    
}]);