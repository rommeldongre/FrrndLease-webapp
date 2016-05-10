<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
    
<html lang="en" ng-app="itemDetailsApp">
    
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
    
<!-- Google Apis Start -->
<meta name="google-signin-scope" content="profile email">
<meta name="google-signin-client_id" content="909447696017-ka0dc75ts261cua6d2ho5mvb7uuo9njc.apps.googleusercontent.com">
<script src="https://apis.google.com/js/platform.js" async defer></script>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=places"></script>
<!-- Google Api End -->
    
<!-- Angularjs api -->
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src="js/ui-bootstrap-tpls-1.3.2.min.js"></script>
<!-- Angularjs api ends -->

<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<title>Item Details</title>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="js/jquery-1.11.3.min.js"></script>
<script type="text/javascript" src="js/jquery-1.11.3.min.js"></script>
    
<!-- Bootstrap -->
<link href="css/bootstrap.min.css" rel="stylesheet">
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
        <div ng-controller="headerCtrl">
		  <div ng-include="'header.html'"></div>
        </div>
		<!--header ends----------------------------------------->

		<div class="container-fluid" id="midcontainer" ng-controller="itemDetailsCtrl">
            
            <!-- Error Message Display starts -->
			<div class="row" ng-if="showError">
				<div id="heading">
					<span>{{message}}</span>
				</div>
			</div>
            <!-- Error Message Display ends -->
            
            <!-- Item Details starts -->
			<div class="row" ng-if="!showError">
				<div class="col-md-6" id="outertable">
					<br />
					<div class="row" ng-if="userMatch">
                        <div class="col-md-12">
                            <input type="file" accept="image/*" onchange="angular.element(this).scope().uploadImage(files[0])" />
                            <img ng-src="{{image}}" width="300" height="300"/>
                        </div>
					</div>
                    
					<form id="itemform">
                        
						<div class="row">
							<div class="col-md-12">
								<div class="form-group">
									<label for="title">Title</label>
                                    <input type="text" class="form-control" ng-model="title" ng-disabled="!userMatch" placeholder="Enter Title" required>
								</div>
							</div>
						</div>
                        
						<div class="row">
							<div class="col-md-6">
								<div class="input-group">
									<div class="input-group-button">
										<label for="category">Category</label><br />
										<button id="dropdownbuttoncategory" ng-disabled="!userMatch" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false" required> Category <span class="caret"></span>
										</button>
										<ul id="dropdownmenucategory" class="dropdown-menu" role="menu">
                                        </ul>
									</div>
								</div>
							</div>
							<div class="col-md-6">
								<div class="form-group">
									<label for="location">Location</label>
                                    <input type="text" class="form-control" id="location" placeholder="Location">
								</div>
							</div>
						</div>
                        
						<div class="row">
							<div class="col-md-4">
								<div class="form-group">
									<label for="lease_value">Lease Value</label>
                                    <input type="number" class="form-control" ng-model="leaseValue" ng-disabled="!userMatch" placeholder="Lease Value">
								</div>
							</div>
							<div class="col-md-4">
								<div class="input-group">
									<div class="input-group-button">
										<label for="lease_term">Lease Term</label><br />
										<button id="dropdownbuttonlease_term" ng-disabled="!userMatch" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false"> Lease Term <span class="caret"></span>
										</button>
										<ul id="dropdownmenulease_term" class="dropdown-menu" role="menu"></ul>
									</div>
								</div>
							</div>
							<div class="col-md-4">
								<div class="form-group">
									<label for="quantity">Quantity</label>
                                    <input type="number" class="form-control" id="quantity" placeholder="Quantity">
								</div>
							</div>
						</div>
                        
						<div class="row">
							<div class="col-md-12">
								<div class="form-group">
									<label for="description">Description</label>
									<textarea rows="3" class="form-control" ng-model="description" ng-disabled="!userMatch" style="margin-bottom:35%;" placeholder="Add Description"></textarea>
								</div>
							</div>
						</div>
                        
						<button class="btn btn-default" id="submit" type="submit">Submit</button>

					</form>
                    
				</div>
                
				<div class="col-md-6">
					<br />
					<div id="error_row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<div class="alert alert-danger fade in">
								<a href="#" class="close" data-dismiss="alert">&times;</a> <strong>Error!</strong>Operation failed.
							</div>
						</div>
					</div>
					<br />

					<div ng-if="!userMatch" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button style="margin-bottom:5px;" class="btn btn-primary" ng-click="requestItem()">Request Item</button>
						</div>
					</div>

					<div ng-if="userMatch" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button style="margin-bottom:5px;" class="btn btn-primary" ng-click="editItem()">Edit Item</button>
						</div>
					</div>

					<div ng-if="userMatch" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button style="margin-bottom:5px;" class="btn btn-primary" ng-click="deleteItem()">Delete Item</button>
						</div>
					</div>
				</div>

			</div>
            <!-- Item Details ends -->

            <!--The tawk.to widget code will get populated here from file chatbox.html.-->
            <div id="tawk_widget"></div>

			<!--Footer starts here-->
			<div ng-include="'footer.html'"></div>
            <!--Footer ends here-->

		</div>
	</div>

	<script type="text/javascript">
		var userloggedin, userLocation, reasonForGetItem, itemObj, reqObj, itemNo, leaseObj, picOrientation = null;
        
        var code = "${code}";
        var message = "${message}";
        var userId = "${userId}";
        
        var item_id = "${itemId}";
        var title = "${title}";
        var category = "${category}";
        var description = "${description}";
        var leaseValue = "${leaseValue}";
        var leaseTerm = "${leaseTerm}";
        var image = "${image}";

		function start() {

			load_Gapi();

			$('#error_row').hide();
			$('#submit').hide();

			getLocationWidth();
			getLeaseValueWidth();
			loadCategoryDropdown();
			loadLeaseTermDropdown();

			getLocation(); //to show default location of the user

			//checking if user is logged in or not
			userloggedin = localStorage.getItem("userloggedin");
			//userLocation = localStorage.getItem("userLocation");

		}

		function load_Gapi() { //for google
			gapi.load('auth2', function() {
				gapi.auth2.init();
			});
		}

		$(window).resize(function() {
			getLocationWidth();
			getLeaseValueWidth();
		});

		function loadCategoryDropdown() { //for category dropdown
			catName = '';
			reasonForGetCategory = 'categoryDropdown';
			getNextCategory(catName);
		}

		function loadCategoryDropdownContinued(obj) {
			var ul = document.getElementById("dropdownmenucategory");

			var li = document.createElement("li");
			li.id = catName;
			li.className = "category";

			li.innerHTML = obj.catName;

			var lidivider = document.createElement("li");
			lidivider.className = "divider";

			ul.appendChild(lidivider);
			ul.appendChild(li);

			getNextCategory(catName);
		}

		function loadLeaseTermDropdown() { //for leaseterm dropdown
			leaseTermName = '';

			getNextLeaseTerm(leaseTermName);

		}

		function loadLeaseTermDropdownContinued(obj) {
			var ul = document.getElementById("dropdownmenulease_term");

			var li = document.createElement("li");
			li.id = leaseTermName;
			li.className = "leaseterm";

			li.innerHTML = obj.termName;

			var lidivider = document.createElement("li");
			lidivider.className = "divider";

			ul.appendChild(lidivider);
			ul.appendChild(li);

			getNextLeaseTerm(leaseTermName);
		}

		function getLocationWidth() { //just for a symmetrical look, to set width of dropdown button and menu equal to Location input field
			var width = $("#location").width();
			$("#dropdownbuttoncategory").width(width);
			$("#dropdownmenucategory").width(width);
		}

		function getLeaseValueWidth() {
			var width = $("#lease_value").width();
			$("#dropdownbuttonlease_term").width(width);
			$("#dropdownmenulease_term").width(width);
		}

		$(document).on("click", ".category", function(event) { //to see which option is selected from dropdown category
			var text = document.getElementById(event.target.id).innerHTML;
			document.getElementById("dropdownbuttoncategory").innerHTML = text;

		});

		$(document).on("click", ".leaseterm", function(event) { //to see which option is selected from dropdown lease term
            var text = document.getElementById(event.target.id).innerHTML;
            document.getElementById("dropdownbuttonlease_term").innerHTML = text;
        });
        
		function redirectToPrevPage() {
			prevPage = getPrevPage("prevPage");
			window.location.replace(prevPage);
		}

		//getting item info when user wants to view an existing item--------------------------------------------------------------------		
		function getItemInfo() {

			storeItemOwner("${userId}");

			$("#dropdownbuttoncategory").text("${category}");

			$("#dropdownbuttonlease_term").text("${leaseTerm}");
		}

		//to get current location of the user and show it in the location by default
		function getLocation() {
			if (navigator.geolocation) {
				navigator.geolocation.getCurrentPosition(showPosition);
			} else {
				console.log("Geolocation is not supported by this browser.");
			}
		}

		function showPosition(position) {
			console.log("Latitude: " + position.coords.latitude
					+ "<br>Longitude: " + position.coords.longitude);

			latitude = position.coords.latitude;
			longitude = position.coords.longitude;
			coords = new google.maps.LatLng(latitude, longitude);

			var geocoder = new google.maps.Geocoder();
			var latLng = new google.maps.LatLng(latitude, longitude);
			geocoder
					.geocode(
							{
								'latLng' : latLng
							},
							function(results, status) {
								if (status == google.maps.GeocoderStatus.OK) {
									console.log(results[4].formatted_address);
									$("#location").val(
											results[4].formatted_address);
								} else {
									console
											.log("Geocode was unsucessfull in detecting your current location");
								}
							});
		}
	</script>


	<!-- Include all compiled plugins (below), or include individual files as needed -->
	<script src="js/mystore.js"></script>
    <script src="js/itemdetails.js"></script>
    <script src="js/tawk.js"></script>
	<script src="js/script_admin_v2.js"></script>
	<script src="js/bootstrap.min.js"></script>

</body>
</html>