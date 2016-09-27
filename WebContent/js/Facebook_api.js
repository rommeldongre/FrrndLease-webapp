var fbId = null;
if(window.location.href.indexOf("frrndlease.com") > -1){
				fbId = '107934726217988';   //Live App Id
			}else{
				fbId = '256875104657282';   //Dev App Id
			}

window.fbAsyncInit = function() {
                FB.init({
                appId: fbId,
                status: true,
                cookie: true,
                xfbml: true,
				version : 'v2.7'
            });
        };

        // Load the SDK asynchronously
        (function(d){
        var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
        if (d.getElementById(id)) {return;}
        js = d.createElement('script'); js.id = id; js.async = true;
        js.src = "//connect.facebook.net/en_US/all.js";
        ref.parentNode.insertBefore(js, ref);
        }(document));