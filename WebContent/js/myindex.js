var myIndex = angular.module('myApp');

myIndex.controller('myIndexCtrl', ['userFactory', 'search', '$timeout', function(userFactory, search, $timeout){
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    var idx = document.URL.indexOf('?');
    var params = new Array();
		if (idx != -1) {
            var pairs = document.URL.substring(idx+1, document.URL.length).split('&');
            for (var i=0; i<pairs.length; i++){
                nameVal = pairs[i].split('=');
                params[nameVal[0]] = nameVal[1];
            }
            var search_item = unescape(params["wish_title1"]);
            if (search_item != "undefined" ){
                search_item = search_item.replace(/\+/g, " ");
                search_item = search_item.substring(0, search_item.lastIndexOf('#'));
                console.log(search_item);
                $timeout(function(){
                    search.changeSearchString(search_item);
                }, 2000);
            }
		} 
}]);