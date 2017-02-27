var myFriendsListApp = angular.module('myApp');

myFriendsListApp.controller('myFriendsListCtrl', ['$scope', 
                                                  'userFactory', 
                                                  'modalService', 
                                                  'bannerService', 
                                                  'logoutService', 
                                                  'eventsCount', 
                                                  'editFriendModalService', 
                                                  function (
                                                  $scope, 
                                                   userFactory, 
                                                   modalService, 
                                                   bannerService, 
                                                   logoutService, 
                                                   eventsCount,
                                                   editFriendModalService) {

    localStorage.setItem("prevPage", "myapp.html#/myfriendslist");

    var friendIdArray = [];
    var friendArray = [];
    var lastFriendId = '';
    var arrEmail = [];
    var errCount = 0, len = 0, count = 1;
    $scope.friends = [];
    var reasonForAddFriend = null, googleFriendsCounter = 0, counter = 0, checkcounter = 0;
    var clientId = '1074096639539-cect2rfj254j3q1i5fo7lmbfhm93jg34.apps.googleusercontent.com';
    var apiKey = 'API Code';
    var scopes = 'https://www.googleapis.com/auth/contacts.readonly';
    $("#openBtn_gmail").hide();

    if (userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");

    var initialPopulate = function () {
        lastFriendId = '';
        arrEmail = [];
        errCount = 0, len = 0, count = 0;
        getFriendsList();
    }

    var getFriendsList = function () {
        var req = {
            operation: "getNext",
            id: userFactory.user,
            token: lastFriendId
        }

        displayFriendsList(req);
    }

    var displayFriendsList = function (req) {
        $.ajax({
            url: '/GetFriends',
            type: 'get',
            data: {
                req: JSON.stringify(req)
            },
            contentType: "application/json",
            dataType: "json",

            success: function (response) {
                if (response.Code == "FLS_SUCCESS") {
                    var obj = JSON.parse(response.Message);

                    obj.isFB = false;
                    
                    // to check if the friend id is email or not
                    var strIndex = obj.friendId.indexOf('@fb');
                    if (strIndex != -1){
                        obj.friendId = '-';
                        obj.isFB = true;
                    }

                    if (lastFriendId == '')
                        $scope.$apply(function () {
                            $scope.friends = [obj];
                        });
                    else
                        $scope.$apply(function () {
                            $scope.friends.unshift(obj);
                        });

                    lastFriendId = response.Id;
                    friendIdArray.unshift(response.Id);
                    friendArray.unshift(response.Message);
                    getFriendsList();
                }
            },

            error: function () {}
        });
    }

    initialPopulate();

    $scope.reinviteFriend = function (i) {
        if ($scope.friends[i].friendId != '-')
            reinviteFriendSetValues($scope.friends[i].fullName, $scope.friends[i].mobile, $scope.friends[i].friendId, userFactory.user);
        else
            facebookInvite();
    }

    $scope.directImport = function () {
        modalService.showModal({}, {
            submitting: true,
            labelText: 'Invite Friends by Email,comma separated. example1@xyz.com, example2@abc.net',
            actionButtonText: 'Submit'
        }).then(function (result) {
            var value = result;
            reasonForAddFriend = "importEmail";
            arrEmail = value.split(",");
            len = arrEmail.length;
            if (value != '' && value != ' ') {
                if (len <= 20) {
                    directImport_continue();
                } else {
                    modalService.showModal({}, {
                        bodyText: "Sorry, Please enter emails less than or equal to 20",
                        showCancel: false,
                        actionButtonText: 'Ok'
                    }).then(function (result) {}, function () {});
                }
            }
            eventsCount.updateEventsCount();
        }, function () {});
    }

    var directImport_continue = function () {

        if (count == len) {
            var validEmail = len - errCount;
            modalService.showModal({}, {
                bodyText: "Success, Number of email(s) imported: " + validEmail + " ,Number of Invalid email(s): " + errCount,
                showCancel: false,
                actionButtonText: 'Ok'
            }).then(function (result) {
                $scope.friends = [];
                initialPopulate();
                eventsCount.updateEventsCount();
            }, function () {});
        } else {
            var isValid = checkEmailValidity(arrEmail[count]);
            if (checkEmailValidity(arrEmail[count])) {
                addFriendSetValues('-', '-', arrEmail[count], userFactory.user);
            } else {
                errCount = errCount + 1;
                count++;
                directImport_continue();
            }
        }
    }
    $scope.importfb = function () {
        facebookInvite();
    }
    
    var  facebookInvite = function(){
        FB.login(function (response) {

            var ref_code = localStorage.getItem("userReferralCode");
            // handle the response

            // check whether user is logged in or not and ask for credentials if not.

            // send message to facebook friends using send request dialog
            FB.ui({
                method: 'send',
                link: 'https://www.frrndlease.com/index.html?ref_token=' + ref_code,
            }, function (response) {
                if (response && !response.error) {
                    //check 'response' to see if call was successful
                    modalService.showModal({}, {
                        bodyText: "Success, Message to Facebook Friend(s) sent",
                        showCancel: false,
                        actionButtonText: 'Ok'
                    }).then(function (result) {
                        eventsCount.updateEventsCount();
                    }, function () {});
                }
            });
        }, {
            scope: 'email,public_profile,user_friends'
        });
    }

    $scope.importgoogle = function () {
        window.setTimeout(authorize); //calls authorize()
        $("#openBtn_gmail").click();
    }

    var authorize = function () {
        gapi.auth.authorize({
            client_id: clientId,
            scope: scopes,
            immediate: false
        }, handleAuthorization); //calls handleAuthorization()
    }

    var handleAuthorization = function (authorizationResult) {
        if (authorizationResult && !authorizationResult.error) {
            $.get("https://www.google.com/m8/feeds/contacts/default/thin?alt=json&access_token=" + authorizationResult.access_token + "&max-results=500&v=3.0",
                function (response) {
                    //function for length of entry array  (for number of contacts)
                    var getLength = function (obj) {
                        var i = 0,
                            key;
                        for (key in obj) {
                            if (obj.hasOwnProperty(key)) {
                                i++;
                            }
                        }
                        return i;
                    };
                    var n = getLength(response.feed.entry);
                    var tp, user_email = '',
                        number = '',
                        email = '',
                        name = '';

                    googleFriendsCounter = n;

                    for (i = 0; i < n; i++) {
                        if (response.feed.entry[i].gd$email) {
                            tp = i + 1;

                            user_email = JSON.stringify(response.feed.author[0].email.$t);
                            user_email = user_email.substring(1, user_email.length - 1);

                            try {
                                number = JSON.stringify(response.feed.entry[i].gd$phoneNumber[0].$t);
                                number = number.substring(1, number.length - 1);
                            } catch (Exception) {
                                //number = "Number do not Exist for Friend " + tp;
                                number = "-";
                            }
                            try {
                                name = JSON.stringify(response.feed.entry[i].gd$name.gd$fullName.$t);
                                name = name.substring(1, name.length - 1);
                            } catch (Exception) {
                                //name = "Name do not Exist for Friend " +  tp;	 	
                                name = "-";
                            }
                            try {
                                email = JSON.stringify(response.feed.entry[i].gd$email[0].address);
                                email = email.substring(1, email.length - 1);
                            } catch (Exception) {
                                //email = "Email do not Exist for Friend " +  tp;	
                                email = "-";
                            }
                            addFriendOptionToPage(email, name, number, user_email, i);
                        }
                    }
                });
        }
    }

    var addFriendOptionToPage = function (email, name, number, user_email, i) {
        var table = document.getElementById("friendsoptionstable");
        var row = table.insertRow(1);
        var cell0 = row.insertCell(0);
        var cell1 = row.insertCell(1);
        var cell2 = row.insertCell(2);
        var cell3 = row.insertCell(3);

        var inputgp = document.createElement("div");
        var checkbox = document.createElement("input");
        inputgp.className = "input-group";
        checkbox.setAttribute("type", "checkbox");
        inputgp.appendChild(checkbox);
        checkbox.id = i;

        cell0.innerHTML = email;
        cell1.innerHTML = name;
        cell2.innerHTML = number;
        cell3.appendChild(inputgp);

        cell0.id = "email" + i;
        cell1.id = "name" + i;
        cell2.id = "mobile" + i;

        cell0.className = "tablecellName";
        cell1.className = "tablecellMobile";
        cell2.className = "tablecellEmail";
        cell3.className = "checkboxes";
    }

    $scope.add_checked_friends = function () {
        if (counter == 0) {
            process_dialog("Adding Gmail friends Please Wait");
            $('#myModalTable_gmail').modal('toggle');
        }
        reasonForAddFriend = "importGoogle";
        if (counter < googleFriendsCounter) {
            var ischecked = $("#" + counter).is(":checked");

            if (ischecked) {
                name = $("#name" + counter).text();
                mobile = $("#mobile" + counter).text();
                email = $("#email" + counter).text();
                checkcounter++;

                addFriendSetValues(name, mobile, email, userFactory.user);
            } else {
                add_checked_friends_continued(); //basically just increment and call the same function again
            }
        } else {
            $('#myPleaseWait').modal('hide');
            modalService.showModal({}, {
                bodyText: "Success, Number of Friends Imported : " + checkcounter,
                showCancel: false,
                actionButtonText: 'OK'
            }).then(function (result) {
                $scope.friends = [];
                initialPopulate();
                eventsCount.updateEventsCount();
            }, function () {});
        }
    }

    var add_checked_friends_continued = function (email) {
        counter++;
        $scope.add_checked_friends();
    }
    var checkEmailValidity = function (email) {
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }

    var addFriendSetValues = function (name, mobile, email, user) {

        var friendName = null,
            friendEmail = null,
            friendMobile = 0;

        friendName = name;
        friendEmail = email;
        friendMobile = mobile;
        userId = user;
        code = localStorage.getItem("userReferralCode");

        if (friendName == '')
            friendName = null;
        if (friendEmail == '')
            friendEmail = null;
        if (friendMobile == '')
            friendMobile = 0;

        var req = {
            id: friendEmail,
            fullName: friendName,
            mobile: friendMobile,
            userId: user,
            referralCode: code
        };

        addFriendSend(req);
    }

    var addFriendSend = function (req) {
        $.ajax({
            url: '/AddFriend',
            type: 'get',
            data: {
                req: JSON.stringify(req)
            },
            contentType: "application/json",
            dataType: "json",
            success: function (response) {
                if (reasonForAddFriend == "importEmail") {
                    count++;
                    if (count == len) {
                        var validEmail = len - errCount;
                        modalService.showModal({}, {
                            bodyText: "Success, Number of email(s) imported: " + validEmail + " ,Number of Invalid email(s): " + errCount,
                            showCancel: false,
                            actionButtonText: 'Ok'
                        }).then(function (result) {
                            $scope.friends = [];
                            initialPopulate();
                            eventsCount.updateEventsCount();
                        }, function () {});
                    } else {
                        directImport_continue();
                    }
                } else if (reasonForAddFriend == "importGoogle") {
                    add_checked_friends_continued();
                } else {
                    bannerService.updatebannerMessage("Your Invitation has been sent to your friend.");
                    $("html, body").animate({
                        scrollTop: 0
                    }, "slow");
                }
            },
            error: function () {
                console.log("Invalid Entry");
            }
        });

    }

    var reinviteFriendSetValues = function (name, mobile, email, user) {

        var friendName = null,
            friendEmail = null,
            friendMobile = 0;

        friendName = name;
        friendEmail = email;
        friendMobile = mobile;
        userId = user;
        code = localStorage.getItem("userReferralCode");

        if (friendName == '')
            friendName = null;
        if (friendEmail == '')
            friendEmail = null;
        if (friendMobile == '')
            friendMobile = 0;

        var req = {
            id: friendEmail,
            fullName: friendName,
            mobile: friendMobile,
            userId: user,
            referralCode: code
        };

        reinviteFriendSend(req);
    }

    var reinviteFriendSend = function (req) {
        $.ajax({
            url: '/ReinviteFriend',
            type: 'get',
            data: {
                req: JSON.stringify(req)
            },
            contentType: "application/json",
            dataType: "json",
            success: function (response) {
                if (response.Code == 'FLS_SUCCESS') {
                    modalService.showModal({}, {
                        bodyText: "Your Invitation has been sent to your friend.",
                        showCancel: false,
                        actionButtonText: 'Ok'
                    }).then(function (result) {}, function () {});
                }
            },
            error: function () {
                console.log("Invalid Entry");
            }
        });

    }

    $scope.deleteFriend = function (index) {
        if(!$scope.friends[index].isFB){
            modalService.showModal({}, {
                bodyText: "Are you sure you want to delete this friend?",
                actionButtonText: 'Yes'
            }).then(
                function (res) {
                    $.ajax({
                        url: '/DeleteFriend',
                        type: 'get',
                        data: {
                            req: JSON.stringify({
                                id: userFactory.user,
                                userId: friendIdArray[index]
                            })
                        },
                        contentType: "application/json",
                        dataType: "json",

                        success: function (response) {
                            if (response.Code == "FLS_SUCCESS") {
                                $scope.$apply(function () {
                                    $scope.friends.splice(index, 1);
                                });
                            }
                        },
                        error: function () {}
                    });
                },
                function () {}
            );
        }
    }

    $scope.editFriend = function (index) {

        var req = {
            userId: userFactory.user,
            accessToken: userFactory.userAccessToken,
            friendId: friendIdArray[index],
            friendName: $scope.friends[index].fullName,
            friendMobile: $scope.friends[index].mobile
        }

        editFriendModalService.showModal({}, req).then(function (res) {
            $.ajax({
                url: '/EditFriend',
                type: 'post',
                data: JSON.stringify(res),
                contentType: "application/json",
                dataType: "json",

                success: function (response) {
                    if (response.code == 0) {
                        $scope.$apply(function () {
                            $scope.friends[index].fullName = res.friendName;
                            $scope.friends[index].mobile = res.friendMobile;
                        });
                    }
                },

                error: function () {}
            });

        }, function () {});
    }

}]);

myFriendsListApp.service('editFriendModalService', ['$uibModal', function ($uibModal) {
    var modalDefaults = {
        animation: true,
        backdrop: true,
        keyboard: true,
        templateUrl: '/editFriendDetailsModal.html'
    };

    var modalOptions = {};

    this.showModal = function (customModalDefaults, customModalOptions) {
        if (!customModalDefaults) customModalDefaults = {};
        customModalDefaults.backdrop = 'static';
        return this.show(customModalDefaults, customModalOptions);
    };

    this.show = function (customModalDefaults, customModalOptions) {
        //Create temp objects to work with since we're in a singleton service
        var tempModalDefaults = {};
        var tempModalOptions = {};

        //Map angular-ui modal custom defaults to modal defaults defined in service
        angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

        //Map modal.html $scope custom properties to defaults defined in service
        angular.extend(tempModalOptions, modalOptions, customModalOptions);

        if (!tempModalDefaults.controller) {
            tempModalDefaults.controller = function ($scope, $uibModalInstance) {
                $scope.friend = tempModalOptions;
                $scope.save = function (result) {
                    $uibModalInstance.close($scope.friend);
                };
                $scope.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        }

        return $uibModal.open(tempModalDefaults).result;
    };
}]);
