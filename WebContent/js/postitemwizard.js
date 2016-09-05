var postItemWizardApp = angular.module('myApp');

postItemWizardApp.controller('postItemWizardCtrl', ['$scope', 'modalService', 'userFactory', 'eventsCount', function($scope, modalService, userFactory, eventsCount){
    $scope.steps = [
        {
            templateUrl: 'wizardstep1.html',
            title: 'Post your first item to earn 10 credits'
        },
        {
            templateUrl: 'wizardstep2.html',
            title: 'Share this with friends to earn xx credits'
        },
        {
            template: '<div class="well">More docs available on Github</div>',
            title: 'Create your multi step forms / wizzards'
        }
    ];
    
    $scope.posted = false;
    $scope.shared = false;
    
    var userId = userFactory.user;
    var userAccessToken = userFactory.userAccessToken;
    
    $scope.item = {};
    
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

        var req = {
            id: 0,
            title: item_title,
            description: "",
            category: $scope.item.category,
            userId: userId,
            leaseValue: 1000,
            leaseTerm: 'Month',
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
                        modalService.showModal({}, {bodyText: "Your Item has been added to the friend store successfully!!", showCancel:false, actionButtonText: 'OK'}).then(
                            function(result){
                                $scope.posted = true;
                                $scope.item.uid = response.uid;
                                eventsCount.updateEventsCount();
                            },function(result){});
                    }
                },

                error: function() {
                    modalService.showModal({}, {bodyText: "Something is Wrong with the network.",showCancel: false,actionButtonText: 'OK'}).then(function(result){},function(){});
                }
            });
        },function(){});
    }
    
    $scope.shareItem = function(uid){
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
                    $scope.shared = true;
                }else{
                    m = "Item sucessfully posted on Frrndlease";
                }
                console.log(m);
            });
        }, {scope: 'email,public_profile,user_friends'});
    }
    
}]);