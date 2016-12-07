<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
   
<html lang="en" ng-app="userProfileApp">
  
   <head>
       <meta charset="utf-8">
       <meta http-equiv="X-UA-Compatible" content="IE=edge">
       <meta name="viewport" content="width=device-width, initial-scale=1">
       
       <meta name="google-signin-scope" content="profile email">
       <meta name="google-signin-client_id" content="1074096639539-cect2rfj254j3q1i5fo7lmbfhm93jg34.apps.googleusercontent.com">
       <script src="https://apis.google.com/js/api:client.js"></script>
       <script src="js/md5.js"></script>
       <!--for Facebook signin-->
       <script src="js/Facebook_api.js"></script>
       
       <link rel="shortcut icon" href="images/fls-favicon.ico" type="image/x-icon">
       <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
       <title>User Profile</title>
       
       <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
       <script src="js/jquery-1.11.3.min.js"></script>
       
       <!-- Bootstrap -->
       <link href="css/bootstrap.min.css" rel="stylesheet">
       <link href="css/gsdk.css" rel="stylesheet" />

       <!-- Angular Switch -->
       <link rel="stylesheet" href="css/angular-ui-switch.min.css">

       <link href="css/index.css" rel="stylesheet">
       <link href="css/mystore.css" rel="stylesheet">
       <link href='https://fonts.googleapis.com/css?family=Happy+Monkey' rel='stylesheet' type='text/css'>

       <!--Loading Animation code CSS & JS Links  -->
       <link href="css/animation.css" type="text/css" rel="stylesheet">
       <script src="js/jquery-1.11.3.min.js"></script>
       <script src="js/jquery.backstretch.js"></script>
       <script src="js/animation.js"></script>
       <!--Loading Animation code CSS & JS Links ends here  -->

       <!--Correct Orientation of image while uploading  -->
       <script src="js/exif.js"></script>
       <!--Correct Orientation of image while uploading ends here  -->

       <!-- Material Scroll Top button -->
       <link rel="stylesheet" type="text/css" href="css/material-scrolltop.css">
       <!-- Material Scroll Top button ends here -->
   </head>
   
   <body onload="start()">
      <div id="loader"></div>
      <div id="main">
          
          <!--header starts---------------------------------------->
          <div>
              <div ng-include="'header.html'"></div>
          </div>
          <!--header ends------------------------------------------>
          
          <div class="row">
              <div class="col-lg-1  col-md-1"></div>
              <div class="page-card col-lg-10  col-md-10" style="margin: 10px 20px 10px 20px;min-height: 500px;">
                  <div class="container-fluid" id="midcontainer" ng-controller="userProfileCtrl">
                      <br />
                      
                      <!-- Error Message Display starts -->
<!--
                      <div class="row" ng-if="showError">
                          <div id="heading">
                              <span>${message}</span>
                          </div>
                      </div>
-->
                      <!-- Error Message Display ends -->
                      
                      <!-- Item Details starts -->
                      <div class="row" style="padding-top:50px">
                          <div class="content">
                              <div class="row" style="display: flex;flex-wrap: wrap;text-align:center;">
                                  <div class="col-lg-4 col-md-4">
                                      <img class="avatar border-gray" ng-src="{{user.profilePic}}" style="width:150px;height:150px;" alt="Profile Pic">
                                      <h3 class="title" style="word-wrap: break-word;">
                                          <strong>Ankit Karnany</strong>
                                      </h3>
                                      <user-badges user-id="user.userId"></user-badges>
                                  </div>
                                  <div class="col-lg-4 col-md-4" style="border-left: 1px solid #ccc;">
                                      <div class="social-badges" style="padding:50px;">
                                          <button class="btn btn-primary btn-fill btn-block" style="margin-bottom:25px;"><i class="fa fa-envelope-o" aria-hidden="true"></i> Message</button><br/>
                                          <button class="btn btn-primary btn-fill btn-block" style="margin-top:25px;"><i class="fa fa-user-plus" aria-hidden="true"></i> Add Friend</button>
                                      </div>
                                  </div>
                                  <div class="col-lg-4 col-md-4" style="border-left: 1px solid #ccc;">
                                     <h4>Wished Items : </h4>
                                     <div class="row">
                                         <ul class="user-wished-list">
                                             <li>Home</li>
                                             <li>Learning</li>
                                             <li>Business</li>
                                         </ul>
                                     </div>
                                     <div class="row" style="margin-top:15%;">
                                         <button class="btn btn-primary btn-fill">Store Your Stuff</button>
                                     </div>
                                  </div>
                              </div>
                              <hr/>
                              <div class="container-fluid">
                                  <div class="row">
                                    <div class="col-md-4 col-sm-6 col-xs-12">
                                        <div class="card">
                                            <a href="">
                                                <div class="image" style="height:100%;">
                                                    <img load-image="user.profilePic" ng-src="">
                                                    <div class="filter">
                                                        <p class="filter-text"><i class="fa fa-calendar" aria-hidden="true"></i>&nbsp; Lease</p>
                                                        <p class="filter-text">Insurance: <i class="fa fa-inr" aria-hidden="true"></i></p>
                                                    </div>
                                                </div>
                                            </a>
                                            <div class="content">
                                                <a class="card-link" href="">
                                                    <h4 class="title">Item Title </h4>
                                                </a>
                                                <p class="category">
                                                    <label ng-if="item.sublocality != '' || item.locality != ''">
                                                        <i class="fa fa-map-marker"></i>&nbsp;Under 1km&nbsp;-&nbsp;Aundh, Pune
                                                    </label>
                                                </p>
                                            </div>
                                        </div>
                                    </div><div class="col-md-4 col-sm-6 col-xs-12">
                                        <div class="card">
                                            <a href="">
                                                <div class="image" style="height:100%;">
                                                    <img load-image="user.profilePic" ng-src="">
                                                    <div class="filter">
                                                        <p class="filter-text"><i class="fa fa-calendar" aria-hidden="true"></i>&nbsp; Lease</p>
                                                        <p class="filter-text">Insurance: <i class="fa fa-inr" aria-hidden="true"></i></p>
                                                    </div>
                                                </div>
                                            </a>
                                            <div class="content">
                                                <a class="card-link" href="">
                                                    <h4 class="title">Item Title </h4>
                                                </a>
                                                <p class="category">
                                                    <label ng-if="item.sublocality != '' || item.locality != ''">
                                                        <i class="fa fa-map-marker"></i>&nbsp;Under 1km&nbsp;-&nbsp;Aundh, Pune
                                                    </label>
                                                </p>
                                            </div>
                                        </div>
                                    </div><div class="col-md-4 col-sm-6 col-xs-12">
                                        <div class="card">
                                            <a href="">
                                                <div class="image" style="height:100%;">
                                                    <img load-image="user.profilePic" ng-src="">
                                                    <div class="filter">
                                                        <p class="filter-text"><i class="fa fa-calendar" aria-hidden="true"></i>&nbsp; Lease</p>
                                                        <p class="filter-text">Insurance: <i class="fa fa-inr" aria-hidden="true"></i></p>
                                                    </div>
                                                </div>
                                            </a>
                                            <div class="content">
                                                <a class="card-link" href="">
                                                    <h4 class="title">Item Title </h4>
                                                </a>
                                                <p class="category">
                                                    <label ng-if="item.sublocality != '' || item.locality != ''">
                                                        <i class="fa fa-map-marker"></i>&nbsp;Under 1km&nbsp;-&nbsp;Aundh, Pune
                                                    </label>
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                          </div>
                      </div>
                      <!-- User Profile ends -->
                  </div>
              </div>
              <div class="col-lg-1 col-md-1"></div>
          </div>
          
          <!--The tawk.to widget code will get populated here from file chatbox.html-->
          <div id="tawk_widget"></div>
          
          <!--Footer starts here-->
          <div ng-include="'footer.html'"></div>
          <!--Footer ends here-->
          
       </div>
       
       <script type="text/javascript">
           
           var code = "${code}";
           var message = "${message}";
           var userId = "${userId}";
           var userFullName = "${userFullName}";
           var userProfilePic = "${userProfilePic}";
           var sublocality = "${sublocality}";
           var locality = "${locality}";
           var wishedList = "${wishedList}";
           var items = "${items}";
           
           function start() {
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
               auth2.attachClickHandler(element, {}, function(googleUser) {
                   if(element.id == "customLoginBtn")
                       onSignIn(googleUser);
                   if(element.id == "customSignUpBtn")
                       onSignUp(googleUser);
               }, function(error) {
                   alert(JSON.stringify(error, undefined, 2));
               });
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
       <script src="js/userprofile.js"></script>
       <script async src="js/tawk.js"></script>
       <script src="js/bootstrap.min.js"></script>
       
       <!-- For Autocomplete Feature -->
       <script src="js/load-image.all.min.js"></script>
       <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g&libraries=places"></script>
       <script src="js/ngAutocomplete.js"></script>
    </body>
    
</html>