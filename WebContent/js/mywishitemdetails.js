var wishItemDetailsApp = angular.module('myApp');

wishItemDetailsApp.controller('mywishitemdetailsCtrl', ['$scope', 
														'$timeout',
														'userFactory',
														'bannerService',
														'$routeParams',
														'modalService',
														'logoutService',
														function($scope,
														$timeout,
														userFactory,
														bannerService,
														$routeParams,
														modalService,
														logoutService){
											
	
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
            $scope.isEdit = true;
            $.ajax({
                url: '/AdminOps',
                type:'get',
                data: {req: JSON.stringify(req)},
                contentType:"application/json",
                dataType: "json",
                success: function(response) {
                    if(response.Code == 0) {
                        $scope.$apply(function(){
                            $scope.item = JSON.parse(response.Message);
                            $scope.editable = true;
							$scope.selectedCategory = $scope.categories.indexOf($scope.item.category);
							$scope.selectedLeaseTerm = $scope.leaseTerms.indexOf($scope.item.leaseTerm); 
                        });
                    }
                    else{
                        //all categories are loaded
                    }
                },
                error:function() {}
            });
        }else{
            $scope.isEdit = false;
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
            url: '/GetCategoryList',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    $scope.$apply(function(){
                        $scope.categories.push(JSON.parse(response.Message).catName);
                    });
                    $scope.item.category = $scope.categories[$scope.selectedCategory];
                    populateCategory(response.Id);
                }else{
					getItemDetails();
				}
            },
            error:function() {}
        });
    }
    
    // called on the page load
    populateCategory('');
    
    $scope.categorySelected = function(i){
        $scope.selectedCategory = i;
        $scope.item.category = $scope.categories[i];
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
            url: '/GetLeaseTerms',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.Code === "FLS_SUCCESS") {
                    $scope.$apply(function(){
                        $scope.leaseTerms.push(JSON.parse(response.Message).termName);
                    });
                    $scope.item.leaseTerm = $scope.leaseTerms[$scope.selectedLeaseTerm];
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
    
    $scope.leaseTermSelected = function(i){
        $scope.selectedLeaseTerm = i;
        $scope.item.leaseTerm = $scope.leaseTerms[i];
    }
    
    //beginning of image display
    $scope.uploadPrimaryImage = function(file){
        EXIF.getData(file, function(){
            exif = EXIF.getAllTags(this);
            picOrientation = exif.Orientation;
		});
        
        var reader = new FileReader();
        reader.onload = function(event) {
            loadImage(
                event.target.result,
                function(canvas){
                    Image = canvas.toDataURL();

                    if(itemId != undefined){
                        var req = {
                            userId: userFactory.user,
                            accessToken: userFactory.userAccessToken,
                            image: Image,
                            uid: $scope.item.uid,
                            existingLink: $scope.item.primaryImageLink,
                            primary: true
                        }

                        $scope.$apply(function(){
                            $scope.item.primaryImageLink = "loading";
                        });

                        $.ajax({
                            url: '/SaveImageInS3',
                            type: 'post',
                            data: JSON.stringify(req),
                            contentType: "application/x-www-form-urlencoded",
                            dataType: "json",

                            success: function(response) {
                                if(response.code == 0){
                                    $scope.$apply(function(){
                                        $scope.item.primaryImageLink = response.imageLink;
                                    });
                                }else{
                                    $scope.$apply(function(){
                                        $scope.item.primaryImageLink = "";
                                    });
                                    modalService.showModal({}, {bodyText: response.message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                                        if(response.code == 400)
                                            logoutService.logout();
                                    },function(){});
                                }
                            },

                            error: function() {
                                modalService.showModal({}, {bodyText: "Something is Wrong with the network.",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                            }
                        });
                    }
                },
                {
                    maxWidth: 450,
                    maxHeight: 450,
                    canvas: true,
                    orientation: picOrientation
                }
            );
        }
        reader.readAsDataURL(file);
    }		
	//end of image display
    
    $scope.deletePrimaryImage = function(){
        
        var req = {
            userId: userFactory.user,
            accessToken: userFactory.userAccessToken,
            uid: $scope.item.uid,
            link: $scope.item.primaryImageLink,
            primary: true
        }
        
        $scope.item.primaryImageLink = "loading";
        
        $.ajax({
            url: '/DeleteImageFromS3',
            type: 'post',
            data: JSON.stringify(req),
            contentType: "application/x-www-form-urlencoded",
            dataType: "json",
            success: function(response) {
                if(response.code == 0){
                    $scope.$apply(function(){
                        $scope.item.primaryImageLink = '';
                    });
                }else{
                    modalService.showModal({}, {bodyText: response.message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                        if(response.code == 400)
                            logoutService.logout();
                    },function(){});
                }
            },
            error: function() {
                modalService.showModal({}, {bodyText: "Something is Wrong with the network.",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
            }
        });
        
    }
    
 
    $scope.editItem = function(){
        
		if($scope.item.leaseValue>1000){
			 modalService.showModal({}, {bodyText: "Max Insurance Value is 1000",showCancel: false,actionButtonText: 'Ok'}).then(function(result){
            },function(){});
		}else{
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
				leaseTerm: item_lease_term
			};
			
			modalService.showModal({}, {bodyText: 'Are you sure you want to update this Wish Item?'}).then(function(result){
				$.ajax({url: '/EditWishlist',
						type: 'post',
						data: {req : JSON.stringify(req)},
						contentType: "application/x-www-form-urlencoded",
						dataType: "json",
						success:function(response){
							if(response.Code === "FLS_SUCCESS"){
								bannerService.updatebannerMessage(response.Message,"myapp.html#/mywishlists");
								$("html, body").animate({ scrollTop: 0 }, "slow");
							}else{
								modalService.showModal({}, {bodyText: response.Message,showCancel: false, actionButtonText: 'OK'}).then(function(result){
								window.location.replace("myapp.html#/mywishlists");
								},function(){});
							}
						},
						error: function(){
							modalService.showModal({}, {bodyText: "Something is Wrong",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
							}
					});
				},function(){});
		}
    }
	
	
	$scope.deleteItem = function(){
		var req = {
			id: $scope.item.id,
			userId: userId
		}
	
		modalService.showModal({}, {bodyText: 'Are you sure you want to delete this Item?'}).then(function(result){
			$.ajax({url: '/DeleteWishlist',
				type: 'get',
				data: {req : JSON.stringify(req)},
				contentType: "application/x-www-form-urlencoded",
				dataType: "json",
				success:function(response){
					modalService.showModal({}, {bodyText: response.Message,showCancel: false, actionButtonText: 'OK'}).then(function(result){
						window.location.replace("myapp.html#/mywishlists");
						},function(){});
					},
				error: function(){
					modalService.showModal({}, {bodyText: "Something is Wrong",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
					}
			});
		},function(){});
	
    }
    
    $scope.cancel = function(){
        window.location.replace("myapp.html#/mywishlists");
    }
    
   									
}]);