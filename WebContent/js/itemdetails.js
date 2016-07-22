var itemDetailsApp = angular.module('itemDetailsApp', ['headerApp']);

itemDetailsApp.controller('itemDetailsCtrl', ['$scope', '$window', '$http', 'userFactory', 'modalService', function($scope, $window, $http, userFactory, modalService){
    
    var user = localStorage.getItem("userloggedin");
    
    $scope.item = {};
    
    $scope.item.image = $window.image;
    $scope.item.title = $window.title;
	$scope.item.description = $window.description;
	$scope.item.category = $window.category;
	$scope.item.leaseValue = $window.leaseValue;
	$scope.item.leaseTerm = $window.leaseTerm;
	
    $scope.item_id = $window.item_id;
    
    $scope.uploadImage = function(file){
        var reader = new FileReader();
        reader.onload = function(event) {
            $scope.$apply(function() {
                $scope.item.image = reader.result;
            });
        }
        reader.readAsDataURL(file);
        
    }
        
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
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                            window.location.replace("/flsv2/index.html");
                        },function(){});
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
        
        localStorage.setItem("prevFunc", 'viewItemOwner');
        localStorage.setItem("itemToShow", $scope.item_id);
		window.location.replace("EditPosting.html");
        
//        var item_title = $scope.item.title;
//        if(item_title == '')
//            item_title = null;
//        
//        var item_id = $scope.item_id;
//        if(item_id == '')
//            item_id = 0;
//        
//        var item_description = $scope.item.description;
//        if (item_description == '') 
//		  item_description = null;
//        
//        var item_category = $scope.item.category;
//        if (item_category == '' || item_category == 'Category') 
//		  item_category = null;
//        
//        var item_user_id = userFactory.user;
//        if (item_user_id == '') 
//		  item_user_id = "anonymous";
//        
//        var item_lease_value = $scope.item.leaseValue;
//        if (item_lease_value == '') 
//		  item_lease_value = 0;
//        
//        var item_lease_term = $scope.item.leaseTerm;
//        if (item_lease_term == '' || item_lease_term == 'Lease Term') 
//		  item_lease_term = null;
//        
//        req = {id:item_id,
//                        title:item_title,
//                        description:item_description,
//                        category: item_category,
//                        userId: item_user_id,
//                        leaseValue: item_lease_value,
//                        leaseTerm: item_lease_term,
//                        image: $scope.item.image};
//        
//        modalService.showModal({}, {bodyText: 'Are you sure you want to update the Item?'}).then(
//            function(result){
//                $.ajax({ url: '/flsv2/EditPosting',
//			             type: 'post',
//			             data: {req : JSON.stringify(req)},
//			             contentType: "application/x-www-form-urlencoded",
//			             dataType: "json",
//                         success:function(response){
//                            modalService.showModal({}, {bodyText: response.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
//								window.location.replace("/flsv2/index.html");
//							},function(){});
//                         },
//                         error: function(){
//                            modalService.showModal({}, {bodyText: "Not Working",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
//                         }
//                    });
//            },
//            function(){
//
//            });
    }
    
    $scope.deleteItem = function(){
        modalService.showModal({}, {bodyText: 'Are you sure you want to delete the Item?'}).then(
            function(result){
                    $http({
                        url:'/flsv2/DeletePosting?req='+JSON.stringify({id:$scope.item_id,userId:userFactory.user}),
                        method:"GET"
                    }).then(function success(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){
                            window.location.replace("/flsv2/index.html");
                        },function(){});
                    },
                    function error(response){
                        modalService.showModal({}, {bodyText: response.data.Message,showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                    });
            }, 
            function(){

            });
    }
    
    $scope.categories = [];
    
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
                    $scope.categories.push(JSON.parse(response.Message).catName);
                    populateCategory(response.Id);
                }
                else{
                    //all categories are loaded
                }
            },
            error:function() {
            }
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
                    $scope.leaseTerms.push(JSON.parse(response.Message).termName);
                    populateLeaseTerm(response.Id);
                }
                else{
                    //all categories are loaded
                }
            },
            error:function() {
            }
        });	
    }
    
    // called on the page load
    populateLeaseTerm('');
    
    $scope.leaseTermSelected = function(l){
        $scope.item.leaseTerm = l;
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