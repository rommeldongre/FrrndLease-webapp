var myIndexApp = angular.module('myIndexApp', ['headerApp', 'carouselApp']);

myIndexApp.run(['search', '$timeout', 'userFactory', function(search, $timeout, userFactory){
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("mylogin.html");
    
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
                    console.log(search_item);
                    $timeout(function(){
                        search.changeSearchString(search_item);
                    }, 2000);
                }
			} 
}]);