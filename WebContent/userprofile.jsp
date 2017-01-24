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

        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g&libraries=places"></script>
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
                        <div class="row" ng-if="${code} != 0">
                            <div id="heading">
                                <span>${message}</span>
                            </div>
                        </div>
                        <!-- Error Message Display ends -->

                        <!-- Item Details starts -->
                        <div class="row" style="padding-top:50px" ng-if="${code} == 0">
                            <div class="content">
                                <div class="row" style="display: flex;flex-wrap: wrap;text-align:center;">
                                    <div class="col-lg-4 col-md-4">
                                        <img class="avatar border-gray" ng-src="{{(user.profilePic === '' || user.profilePic === null || user.profilePic === NULL || user.profilePic === 'null') ? 'images/user_icon.png' : user.profilePic}}" style="width:150px;height:150px;" alt="Profile Pic">
                                        <h3 class="title" style="word-wrap: break-word;">
                                            <strong>{{user.userFullName}}</strong>
                                        </h3>
                                        <div style="font-size:large;margin:5px;">
                                            <i class="fa fa-map-marker" aria-hidden="true"></i>{{user.locality}}
                                        </div>
                                        <user-badges user-id="user.userId"></user-badges>
                                    </div>
                                    <div class="col-lg-4 col-md-4" style="border-left: 1px solid #ccc;">
                                        <div style="text-align:left;">
                                            <textarea class="form-control" ng-model="user.message" ng-trim="false" maxlength="500" style="height:176px;"></textarea>
                                            <span>{{500 - user.message.length}} left</span>
                                        </div>
                                        <button class="btn btn-primary btn-fill btn-block" style="margin-bottom:25px;" ng-click="sendMessage()"><i class="fa fa-envelope-o" aria-hidden="true"></i> Message</button>
                                        <hr/>
                                        <button class="btn btn-primary btn-fill btn-block" style="margin-top:25px;" ng-click="addFriend()"><i class="fa fa-user-plus" aria-hidden="true"></i> Add Friend</button>
                                    </div>
                                    <div class="col-lg-4 col-md-4" style="border-left: 1px solid #ccc;">
                                        <h4 style="text-align:left;">Friends -
                                            <br/>
                                        </h4>
                                        <div style="overflow-y:auto;height:275px;">
                                            <div class="card" ng-repeat="friend in user.friends">
                                                <div class="content" style="text-align:left;">
                                                    <div class="footer">
                                                        <div class="author">
                                                            <a class="card-link" href="/UserProfile?userUid={{friend.userUid}}">
                                                                <img load-image="friend.userProfilePic" max-width="50" max-height="50" ng-src="">
                                                                <span style="padding-left:50px;font-size:large;">{{friend.userFullName}}</span>
                                                            </a>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <hr/>
                                <div ng-if="user.uber" class="row">
                                    <div class="left_title col-lg-4 col-md-4">
                                        Profile Details
                                    </div>
                                </div>
                                <hr ng-if="user.uber" />
                                <div ng-if="user.uber" class="row">
                                    <div class="col-lg-6 col-md-6 col-lg-offset-3 col-md-offset-3">
                                        <!-- Nav tabs -->
                                        <ul class="nav nav-icons" ng-init="selected=0" style="text-align:center;">
                                            <li class="ng-class:{'active':selected == 0}" ng-click="selected = 0">
                                                <a href=""><i class="fa fa-map-marker" aria-hidden="true"></i><br/>Address</a>
                                            </li>
                                            <li class="ng-class:{'active':selected == 1}" ng-click="selected = 1">
                                                <a href=""><i class="fa fa-picture-o" aria-hidden="true"></i><br/>Store Pics</a>
                                            </li>
                                            <li class="ng-class:{'active':selected == 2}" ng-click="selected = 2">
                                                <a href=""><i class="fa fa-info" aria-hidden="true"></i><br/>About</a>
                                            </li>
                                            <li class="ng-class:{'active':selected == 3}" ng-click="selected = 3">
                                                <a href=""><i class="fa fa-phone-square" aria-hidden="true"></i><br/>Contact Info</a>
                                            </li>
                                        </ul>
                                        <!-- Tab panes -->
                                        <div class="tab-content text-center">
                                            <div class="tab-pane ng-class:{'active':selected == 0}">
                                                <span><strong>{{user.address}}</strong></span>
                                                <div id="map"></div>
                                            </div>
                                            <div class="tab-pane ng-class:{'active':selected == 1}">
                                                <img class="img-rounded loaded-img" style="margin-right:10px;margin-bottom:10px;cursor: pointer;" load-image="image" max-width="150" max-height="150" ng-src="" alt="..." ng-repeat="image in user.imageLinks" />
                                            </div>
                                            <div class="tab-pane ng-class:{'active':selected == 2}">
                                                <h5><i class="fa fa-quote-left" aria-hidden="true"></i> {{user.about}} <i class="fa fa-quote-right" aria-hidden="true"></i></h5>
                                            </div>
                                            <div class="tab-pane ng-class:{'active':selected == 3}">
                                                <div class="info info-horizontal btn-tooltip" data-toggle="tooltip" data-placement="right" title="Website" tooltip style="margin:0px;">
                                                    <div class="icon icon-sm" style="margin-top:0px;">
                                                        <i class="fa fa-globe" aria-hidden="true"></i>
                                                    </div>
                                                    <div class="description">
                                                        <h5>{{user.website != '' ? user.website : 'None'}}</h5>
                                                    </div>
                                                </div>
                                                <div class="info info-horizontal btn-tooltip" data-toggle="tooltip" data-placement="right" title="Email" tooltip style="margin:0px;">
                                                    <div class="icon icon-sm" style="margin-top:0px;">
                                                        <i class="fa fa-envelope" aria-hidden="true"></i>
                                                    </div>
                                                    <div class="description">
                                                        <h5>{{user.mail != '' ? user.mail : 'None'}}</h5>
                                                    </div>
                                                </div>
                                                <div class="info info-horizontal btn-tooltip" data-toggle="tooltip" data-placement="right" title="Phone No" tooltip style="margin:0px;">
                                                    <div class="icon icon-sm" style="margin-top:0px;">
                                                        <i class="fa fa-phone" aria-hidden="true"></i>
                                                    </div>
                                                    <div class="description">
                                                        <h5>{{user.phoneNo != '' ? user.phoneNo : 'None'}}</h5>
                                                    </div>
                                                </div>
                                                <div class="info info-horizontal btn-tooltip" data-toggle="tooltip" data-placement="right" title="Business Hours" tooltip style="margin:0px;">
                                                    <div class="icon icon-sm" style="margin-top:0px;">
                                                        <i class="fa fa-clock-o" aria-hidden="true"></i>
                                                    </div>
                                                    <div class="description">
                                                        <h5>{{user.bHours != '' ? user.bHours : 'None'}}</h5>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <hr ng-if="user.uber" />
                                <div class="container-fluid">
                                    <h4>Wished Items -
                                        <br/>
                                    </h4>
                                    <div class="row" ng-if="user.wishedList.length > 0">
                                        <div class="alert alert-success">
                                            <div class="container">
                                                <div class="row" style="display: flex;flex-wrap: wrap;">
                                                    <div class="col-md-9">
                                                        <span class="label label-success" ng-repeat="wish in user.wishedList">
                                                        {{wish | limitTo: 20}}
                                                        <span ng-if="wish.length > 20">&hellip;</span>
                                                        </span>
                                                    </div>
                                                    <div class="col-md-3">
                                                        <button class="btn btn-success btn-fill" ng-click="storeYourStuff()">Store Your Things</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <h4>Items Owned -
                                        <br/>
                                    </h4>
                                    <h4 ng-if="notPosted" style="text-align:center;">You have not added any item to the friends store.
                                        <a href="myapp.html#/wizard" class="btn btn-primary btn-fill">Offer Item &amp; Earn Credits</a>
                                        <br/><br/>
                                    </h4>
                                    <!-- Carousel Start -->
                                    <div class="row" id="carouselObject">
                                        <div class="container-fluid" id="$index" ng-repeat="items in user.items">
                                            <div class="row" id="product-cards">
                                                <div class="col-md-4 col-sm-6 col-xs-12" ng-repeat="item in items">
                                                    <div class="card">
                                                        <a href="ItemDetails?uid={{item.uid}}">
                                                            <div class="image" style="height: 100%;">
                                                                <img load-image="item.primaryImageLink" ng-src="">
                                                                <div class="filter">
                                                                    <p class="filter-text">Lease Fee: <i class="fa fa-inr" aria-hidden="true"></i>{{item.surcharge}}</p>
                                                                    <p class="filter-text"><i class="fa fa-calendar" aria-hidden="true"></i>&nbsp;{{item.leaseTerm}} Lease</p>
                                                                    <p class="filter-text btn-tooltip" data-toggle="tooltip" data-placement="top" title="Item's Rating" ng-if="item.itemsAvgRating > 0" style="text-align:center;" tooltip>
                                                                        <span ng-if="item.itemsAvgRating == 1">
                                                                        Experience:<br/>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle-o" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle-o" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle-o" aria-hidden="true"></i>
                                                                        &nbsp;Very Unhappy
                                                                    </span>
                                                                        <span ng-if="item.itemsAvgRating == 2">
                                                                        Experience:<br/>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle-o" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle-o" aria-hidden="true"></i>
                                                                        &nbsp;Unhappy
                                                                    </span>
                                                                        <span ng-if="item.itemsAvgRating == 3">
                                                                        Experience:<br/>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle-o" aria-hidden="true"></i>
                                                                        &nbsp;Happy
                                                                    </span>
                                                                        <span ng-if="item.itemsAvgRating == 4">
                                                                        Experience:<br/>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        <i class="fa fa-circle" aria-hidden="true"></i>
                                                                        &nbsp;Very Happy
                                                                    </span>
                                                                    </p>
                                                                </div>
                                                            </div>
                                                        </a>
                                                        <div class="content">
                                                            <a class="card-link" href="ItemDetails?uid={{item.uid}}">
                                                                <h4 class="title">{{item.title | limitTo: 32}} </h4>
                                                            </a>
                                                            <p class="category">
                                                                <span class="label label-danger label-fill" ng-if="item.status == 'OnHold'" style="text-align:center;">On Hold</span><br ng-if="item.status == 'OnHold'">
                                                            </p>
                                                            <p class="category">
                                                                <label ng-if="item.sublocality != '' || item.locality != ''">
                                                                <i class="fa fa-map-marker"></i>&nbsp;{{item.distance == '0m' ? 'Under 1km' : item.distance}}&nbsp;-&nbsp;{{item.sublocality+","+item.locality}}
                                                            </label>
                                                            </p>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row" style="margin-bottom:10px;">
                                        <div ng-if="showNext" style="text-align:center;">
                                            <button ng-click="loadNextSlide()" class="btn btn-primary btn-sm btn-round btn-fill" id="successBtn">Load more...</button>
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
            var friends = ${friends};
            var address = "${address}";
            var about = "${about}";
            var website = "${website}";
            var mail = "${mail}";
            var phoneNo = "${phoneNo}";
            var bHours = "${bHours}";
            var imageLinks = "${imageLinks}";
            var uber = ${uber};

            function start() {
                load_Gapi();
                $('body').append('<div class="popover-filter"></div>');
            }

            var googleUser = {};

            var load_Gapi = function() {
                gapi.load('auth2', function() {
                    auth2 = gapi.auth2.init({
                        cookiepolicy: 'single_host_origin'
                    });
                    attachSignin(document.getElementById('customLoginBtn'));
                    attachSignin(document.getElementById('customSignUpBtn'));
                });
            }

            function attachSignin(element) {
                auth2.attachClickHandler(element, {}, function(googleUser) {
                    if (element.id == "customLoginBtn")
                        onSignIn(googleUser);
                    if (element.id == "customSignUpBtn")
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
        <script src="js/carousel.js"></script>
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
        <script src="js/ngAutocomplete.js"></script>
    </body>

    </html>
