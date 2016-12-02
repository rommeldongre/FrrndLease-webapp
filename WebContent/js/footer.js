var footerApp = angular.module('footerApp', []);

footerApp.controller('footerCtrl', ['$scope', '$location', 'modalService', function($scope, $location, modalService){
	
	$scope.newsLetterLead = function(){
		var Lead_type="news_letter";
		var Lead_url = window.location.pathname+window.location.hash;
		
        var req = {
                leadEmail: $scope.lead_email,
				leadType: "news_letter",
				leadUrl: Lead_url
            }
			sendLeadEmail(req);	
    }
          
    var sendLeadEmail = function(req){
		
		$.ajax({
			url: '/AddLead',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code==225){
					modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
					}, function(){});
				}else if(response.code==0){
					modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
					}, function(){});
				}else{
					modalService.showModal({}, {bodyText: "Error Occured. Please try later" ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
					}, function(){});
					
				}
			},
		
			error: function() {
				console.log("Not able to send message");
			}
		});
	}   
}]);