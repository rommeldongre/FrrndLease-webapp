<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
    
<html lang="en" ng-app="itemDetailsApp">
    
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!--Meta tags For Facebook start here-->
        <meta property="og:title" content="New Item Posted on Frrndlease"/>
        <meta property="og:type" content="website" />
        <meta property="og:image" content="images/fls-logo.png" />
        <meta property="og:url" content="http://www.frrndlease.com/ItemDetails?uid=${uid}" />
        <meta property="og:description" content="${title}" />
    <!--Meta tags For Facebook end here-->
    <meta name="google-signin-scope" content="profile email">
    <meta name="google-signin-client_id" content="1074096639539-cect2rfj254j3q1i5fo7lmbfhm93jg34.apps.googleusercontent.com">
    <script src="https://apis.google.com/js/api:client.js"></script>
	<script src="js/md5.js"></script>
    <!--for Facebook signin-->
	<script src="js/Facebook_api.js"></script>

    <!-- Angularjs api -->
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
    <script src="js/ui-bootstrap-tpls-1.3.2.min.js"></script>
    <script src="js/header.js"></script>
    <!-- Angularjs api ends -->

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

    <!--Check whether user is logged in or not  -->
    <script src="js/logincheck.js"></script>
    <!--Check whether user is logged in or not ends here  -->

    <!--Correct Orientation of image while uploading  -->
    <script src="js/exif.js"></script>
    <!--Correct Orientation of image while uploading ends here  -->
	    
</head>
    
<body onload="start()">
	<div id="loader"></div>
	<div id="main">

		<!--header starts---------------------------------------->
        <div>
		  <div ng-include="'header.html'"></div>
        </div>
		<!--header ends----------------------------------------->

        <div class="row">
            <div class="col-lg-3  col-md-3"></div>
            <div class="card col-lg-6  col-md-6" style="margin: 10px 20px 10px 20px;min-height: 500px;">
                <div class="container-fluid" id="midcontainer" ng-controller="itemDetailsCtrl">

                    <!-- Error Message Display starts -->
                    <div class="row" ng-if="showError">
                        <div id="heading">
                            <span>${message}</span>
                        </div>
                    </div>
                    <!-- Error Message Display ends -->

                    <br />
                    <div class="col-lg-2 col-md-2"></div>

                    <!-- Item Details starts -->
                    <div class="card-user col-lg-8 col-md-8" style="padding-top:50px" ng-if="!showError">
                        <div class="content">
                            <div class="author">
                                <img class="img-rounded" load-image="item.image" ng-src="" alt="..."/>
                                <h4 class="title"><br />
                                    <strong>${title}</strong>
                                </h4>
                            </div>
                            <p class="description text-center btn-tooltip" data-toggle="tooltip" data-placement="top" title="Location">
                                <span class="glyphicon glyphicon-map-marker"></span><strong>${sublocality},${locality}</strong>
                            </p>
                            <p class="description text-center btn-tooltip" data-toggle="tooltip" data-placement="top" title="Lease term">
                                <span class="glyphicon glyphicon-tags"></span><i>${leaseTerm} Lease</i>
                            </p>
                            <p style="color:Grey;" class="description text-center btn-tooltip" data-toggle="tooltip" data-placement="top" title="Category">
                                <span class="glyphicon glyphicon-list-alt"></span>${category}
                            </p>
                            <p class="description text-center btn-tooltip" data-toggle="tooltip" data-placement="top" title="Resonable replacement value">
                                <span class="fa fa-rupee"></span>${leaseValue}
                            </p>
                            <p ng-if="'${description}' != '' && '${description}' != null" class="description text-center btn-tooltip" data-toggle="tooltip" data-placement="top" title="Description">
                                <span class="glyphicon glyphicon-calendar"></span>${description}
                            </p>
                        </div>
                        <hr/>
                        <div class="text-center">
                            <div class="row" style="padding:20px;">
                                <button ng-if="!userMatch" class="btn btn-primary btn-fill" ng-click="requestItem()" style="padding:10px;">Request Item</button>
                                <button ng-if="!userMatch" class="btn btn-simple btn-fill" ng-click="wishItem()" style="padding:10px;">Add to Wishlist</button>
                                <button ng-if="userMatch" class="btn btn-primary btn-fill" ng-click="editItem()" style="padding:10px;">Edit Item</button>
                                <button ng-if="userMatch" class="btn btn-primary btn-simple" ng-click="deleteItem()" style="padding:10px;">Delete</button>
                                <button class="btn btn-primary btn-simple" onclick="cancel()" style="padding:10px;">Cancel</button>
                                <hr/>
                                <button class="btn btn-primary btn-simple" ng-click="showItemTimeline()"><u>Show Item Timeline</u></button>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-2 col-md-2"></div>
                    <!-- Item Details ends -->
                            
                    <!-- sample pop up starts here-->
                    <button ng-hide="true" href="#myModalTable" id="openBtn" data-toggle="modal" class="btn btn-default">Modal</button>

                    <div class="modal fade" id="myModalTable" data-backdrop="static" data-keyboard="false">
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
                                                <td><img ng-src="{{((x.itemLogType == 'InStore' || x.itemLogType == 'LeaseReady') ? item.image : ((x.itemLogImage === '' || x.itemLogImage === null || x.itemLogImage === NULL || x.itemLogImage === 'null') ? 'images/imgplaceholder.png' : x.itemLogImage))}}" style="width:50px;"></td>
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
            <div class="col-lg-3 col-md-3"></div>
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
        var image = "${image}";
		var title = "${title}";
        var description = "${description}";
		var category = "${category}";
		var leaseValue = "${leaseValue}";
		var leaseTerm = "${leaseTerm}";
        var imageLinks = "${imageLinks}";
		
        var item_id = "${itemId}";

		function start() {

            $('.btn-tooltip').tooltip();
            
            load_Gapi();
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
			
	</script>
    
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