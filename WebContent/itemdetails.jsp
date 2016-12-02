<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
    
<html lang="en" ng-app="itemDetailsApp">
    
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!--Meta tags For Facebook start here-->
        <meta property="og:title" content="New Item Posted on FrrndLease"/>
        <meta property="og:type" content="website" />
        <meta property="og:image" content="${primaryImageLink}" />
        <meta property="og:url" content="http://www.frrndlease.com/ItemDetails?uid=${uid}" />
        <meta property="og:description" content="${title}" />
		<meta property="fb:app_id" content="107934726217988" />
    <!--Meta tags For Facebook end here-->
	
    <meta name="google-signin-scope" content="profile email">
    <meta name="google-signin-client_id" content="1074096639539-cect2rfj254j3q1i5fo7lmbfhm93jg34.apps.googleusercontent.com">
    <script src="https://apis.google.com/js/api:client.js"></script>
	<script src="js/md5.js"></script>
    <!--for Facebook signin-->
	<script src="js/Facebook_api.js"></script>

    <link rel="shortcut icon" href="images/fls-favicon.ico" type="image/x-icon">

    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>Item Details</title>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="js/jquery-1.11.3.min.js"></script>
    <script type="text/javascript" src="js/jquery-1.11.3.min.js"></script>

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
        <link href="css/gsdk.css" rel="stylesheet" />
    <link href="css/mystore.css" rel="stylesheet">
    <link href='https://fonts.googleapis.com/css?family=Happy+Monkey' rel='stylesheet' type='text/css'>

    <!--Loading Animation code CSS & JS Links  -->
    <link href="css/animation.css" type="text/css" rel="stylesheet">
    <script src="js/jquery.backstretch.js"></script>
    <script src="js/animation.js"></script>
    <!--Loading Animation code CSS & JS Links ends here  -->
	    
</head>
    
<body onload="start()">
	<div id="loader"></div>
	<div id="main">
	<div id="fb-root"></div>
		<script>(function(d, s, id) {
		var js, fjs = d.getElementsByTagName(s)[0];
		if (d.getElementById(id)) return;
		js = d.createElement(s); js.id = id;
		js.src = "//connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.7&appId=107934726217988";
		fjs.parentNode.insertBefore(js, fjs);
		}(document, 'script', 'facebook-jssdk'));</script>

		<!--header starts---------------------------------------->
        <div>
		  <div ng-include="'header.html'"></div>
        </div>
		<!--header ends----------------------------------------->

        <div class="row">
            <div class="col-lg-1  col-md-1"></div>
            <div class="card col-lg-10  col-md-10" style="margin: 10px 20px 10px 20px;min-height: 500px;">
                <div class="container-fluid" id="midcontainer" ng-controller="itemDetailsCtrl">

                    <br />
                    
                    <!-- Error Message Display starts -->
                    <div class="row" ng-if="showError">
                        <div id="heading">
                            <span>${message}</span>
                        </div>
                    </div>
                    <!-- Error Message Display ends -->

                    <!-- Item Details starts -->
                    <div class="row" style="padding-top:50px" ng-if="!showError">
                        <div class="content">
                            <div class="row" style="display: flex;flex-wrap: wrap;text-align:center;">
                                <div class="col-lg-6 col-md-6">
                                    <img class="img-rounded" load-image="item.primaryImageLink" scale="45" ng-src="" alt="..."/>
                                    <h3 class="title" style="word-wrap: break-word;">
                                        <strong>${title}</strong>
                                    </h3>
                                </div>
                                <div class="col-lg-6 col-md-6" style="border-left: 1px solid #ccc;">
                                    <img class="img-rounded loaded-img" style="margin-right:10px;margin-bottom:10px;cursor: pointer;" load-image="image" max-width="150" max-height="150" ng-src="" alt="..." ng-click="selectedImage($index)" ng-repeat="image in item.imageLinks" />
                                    <hr/>
                                    <h5 class="btn-tooltip" data-toggle="tooltip" data-placement="top" title="Item's Rating" ng-if="raters > 0" style="text-align:center;" tooltip>
                                        <span ng-if="rating == 1">
                                            <img src="images/very-unhappy-n.jpg" style="width:10%;">
                                            Average User Experience for this item is<strong> Very unhappy</strong>
                                        </span>
                                        <span ng-if="rating == 2">
                                            <img src="images/unhappy-n.jpg" style="width:10%;">
                                            Average User Experience for this item is<strong> Unhappy</strong>
                                        </span>
                                        <span ng-if="rating == 3">
                                            <img src="images/happy-n.jpg" style="width:10%;">
                                            Average User Experience for this item is<strong> Happy</strong>
                                        </span>
                                        <span ng-if="rating == 4">
                                            <img src="images/very-happy-n.jpg" style="width:10%;">
                                            Average User Experience for this item is<strong> Very happy</strong>
                                        </span>
                                        <br/>
                                        <span style="color:#ccc"> ({{raters}} Ratings)</span>
                                    </h5>
                                </div>
                            </div>
                            <hr/>
                            <div class="row">
                                <div class="col-xs-3">
                                    <div class="info btn-tooltip" data-toggle="tooltip" data-placement="top" title="Category" tooltip>
                                        <div class="icon icon-blue icon-sm">
                                            <i class="fa fa-filter" aria-hidden="true"></i>
                                        </div>
                                        <div class="description">
                                            <h5>${category}</h5>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xs-3">
                                    <div class="info btn-tooltip" data-toggle="tooltip" data-placement="top" title="Location" tooltip>
                                        <div class="icon icon-blue icon-sm">
                                            <i class="fa fa-location-arrow" aria-hidden="true"></i>
                                        </div>
                                        <div class="description">
                                            <h5>${sublocality}, ${locality}</h5>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xs-3">
                                    <div class="info btn-tooltip" data-toggle="tooltip" data-placement="top" title="Lease Term" tooltip>
                                        <div class="icon icon-blue icon-sm">
                                            <i class="fa fa-calendar" aria-hidden="true"></i>
                                        </div>
                                        <div class="description">
                                            <h5>${leaseTerm} Lease</h5>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xs-3">
                                    <div class="info btn-tooltip" data-toggle="tooltip" data-placement="top" title="Reasonable Replacement Value" tooltip>
                                        <div class="icon icon-blue icon-sm">
                                            <i class="fa fa-rupee" aria-hidden="true"></i>
                                        </div>
                                        <div class="description">
                                            <h5>${leaseValue}</h5>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div ng-init='item.description = "${description}"'>
                                <hr ng-if='"${description}" != null && "${description}" != ""'/>
                                <div ng-if='"${description}" != null && "${description}" != ""' class="info">
                                    <div class="description">
                                        <h4 style="color:gray">Description</h4>
                                        <h5><i class="fa fa-quote-left" aria-hidden="true"></i> ${description} <i class="fa fa-quote-right" aria-hidden="true"></i></h5>
                                    </div>
                                </div>
                            </div>
                            <hr/>
                            <div class="text-center">
                                <div class="row" style="padding-top:10px;padding-bottom:10px;">
                                    <button ng-if="!userMatch" class="btn btn-primary btn-fill" ng-click="requestItem()" style="padding:8px;">Request Item</button>
                                    <button ng-if="userMatch" class="btn btn-primary btn-fill" ng-click="editItem()" style="padding:8px;">Edit Item</button>
                                    <button type="button" class="btn btn-social btn-fill btn-facebook" ng-click="shareItem()">
                                        <i class="fa fa-facebook-square"></i> Share
                                    </button>
                                    <button ng-if="!userMatch" class="btn btn-simple" ng-click="sendItemMessage()" style="padding:8px;">Message</button>
                                    <button ng-if="!userMatch" class="btn btn-simple" ng-click="wishItem()" style="padding:8px;">Add to Wishlist</button>
                                    <button class="btn btn-primary btn-simple" ng-click="showItemTimeline()" style="padding:8px;">Show Item Timeline</button>
                                    <button ng-if="userMatch" class="btn btn-simple" ng-click="deleteItem()" style="padding:8px;">Delete</button>
                                    <button class="btn btn-simple" onclick="cancel()" style="padding:8px;">Cancel</button>
                                </div>
                            </div>
                        </div>
                        <hr/>
                    </div>
                    <!-- Item Details ends -->
					
					<div id="FC" class="fb-comments" data-href="" style="z-index:4;" data-width="100%" data-numposts="5"></div>
                            
                    <!-- sample pop up starts here-->
                    <button ng-hide="true" href="#myModalTable_item" id="openBtn_item" data-toggle="modal" class="btn btn-default">Modal</button>

                    <div class="modal fade" id="myModalTable_item" data-backdrop="static" data-keyboard="false">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">X</button>
                                    <h3 class="modal-title">Item Timeline</h3>
                                </div>
                                <div class="modal-body">
                                    <h5 class="text-center">Item Timeline is as follows</h5>
                                    <table id="creditLogTable" class="table table-striped" style='table-layout:fixed;word-wrap:break-word;'>
                                        <thead>
                                            <tr>
                                                <th class="tablecell" style="width: 50%">Log Date</th>
                                                <th class="tablecell" style="width: 25%">Log Type</th>
                                                <th class="tablecell" style="width: 25%">Item Image</th>
                                            </tr>
                                        </thead>
                                        <tbody ng-repeat="y in timelineArray">
                                            <tr ng-repeat="x in y">
                                                <td>{{ x.itemLogDate }}</td>
                                                <td>{{ x.itemLogType }}</td>
                                                <td><img load-image="x.itemLogImageLink" ng-src="" max-width="50" max-height="50"></td>
											</tr>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="modal-footer">
                                <button type="button" class="btn btn-default btn-fill" data-dismiss="modal" ng-click="">Cancel</button>
                                <button class="btn btn-primary btn-fill" id="next_credits" ng-model="showNext" ng-if="showNext == true" ng-click="loadNextItemTimeline()">Show More</button>
                                </div>

                            </div><!-- /.modal-content -->
                        </div><!-- /.modal-dialog -->
                    </div><!-- /.modal -->
                    <!-- sample pop up ends here-->
                </div>
            </div>
            <div class="col-lg-1 col-md-1"></div>
        </div>
        
        <!--The tawk.to widget code will get populated here from file chatbox.html.-->
        <div id="tawk_widget"></div>
        
		<!--Footer starts here-->
		<div ng-include="'footer.html'"></div>
        <!--Footer ends here-->
	</div>

	<script type="text/javascript">
        
        var code = "${code}";
        var userId = "${userId}";
		var title = "${title}";
		var category = "${category}";
		var leaseValue = "${leaseValue}";
		var leaseTerm = "${leaseTerm}";
        var primaryImageLink = "${primaryImageLink}";
        var imageLinks = "${imageLinks}";
		
        var item_id = "${itemId}";
        
        var uid = "${uid}";

		function start() {

			fbComment_URL();
            
            load_Gapi();
            
            $('body').append('<div class="popover-filter"></div>');
		}
        
        var googleUser = {};
        
        var load_Gapi = function() {
            gapi.load('auth2', function(){
              auth2 = gapi.auth2.init({
                cookiepolicy: 'single_host_origin'
              });
              attachSignin(document.getElementById('customLoginBtn'));
              attachSignin(document.getElementById('customSignUpBtn'));
            });
        }
        
        function attachSignin(element) {
            auth2.attachClickHandler(element, {},
                function(googleUser) {
                if(element.id == "customLoginBtn")
                    onSignIn(googleUser);
                if(element.id == "customSignUpBtn")
                    onSignUp(googleUser);
                }, function(error) {
                  alert(JSON.stringify(error, undefined, 2));
                }
            );
        }
		
		function cancel(){
			window.location.replace(localStorage.getItem("prevPage"));
		}
		
		function fbComment_URL(){
			if(window.location.href.indexOf("frrndlease.com") > -1){
				$("#FC").attr('data-href', window.location.href );        //Live Environment
			}else{
		    // Dev Environment
			}
		}
			
	</script>
    
    <!-- Angularjs api -->
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
    <script src="js/ui-bootstrap-tpls-1.3.2.min.js"></script>
    <script src="js/header.js"></script>
	 <script src="js/footer.js"></script>
    <!-- Angularjs api ends -->
    
    <link href="css/font-awesome.min.css" type="text/css" rel="stylesheet">
    <link href="css/pe-icon-7-stroke.css" type="text/css" rel="stylesheet">
    <script async src="js/bootstrap-datepicker.js"></script>
	<script async src="js/bootstrap-select.js"></script>
	<script async src="js/get-shit-done.js"></script>
	<script async src="js/gsdk-bootstrapswitch.js"></script>
	<script async src="js/gsdk-checkbox.js"></script>
	<script async src="js/gsdk-morphing.js"></script>
	<script async src="js/gsdk-radio.js"></script>
	<script async src="js/jquery.flexisel.js"></script>
	<script async src="js/jquery.tagsinput.js"></script>
	<script async src="js/jquery-ui.custom.min.js"></script>
	<script async src="js/retina.min.js"></script>

	<!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/itemdetails.js"></script>
    <script async src="js/tawk.js"></script>
	<script src="js/bootstrap.min.js"></script>
    <!-- For Autocomplete Feature -->
    <script src="js/load-image.all.min.js"></script>
    <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g&libraries=places"></script>
    <script src="js/ngAutocomplete.js"></script>
</body>
</html>
