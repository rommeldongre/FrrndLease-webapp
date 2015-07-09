var itemId = 0,
	itemTitle = null,
	itemCategory = null,
	itemDescription = null,
	itemUserId = null,
	itemLeaseTerm = null,
	itemStatus = null,
	itemLeaseValue = 0,
	itemToken = 0,
	//itemPicture,
	itemNextId = 0,
	itemObj = null,
	obj = null,
	reqUserId = null,
	reasonForGetItem = null,
	reasonForGetRequestItem = null,
	reasonForGetWishItem = null,
	reasonForWishItem = null,
	reasonForGetFriend = null,
	friendName = null,
	friendMobile = 0,
	friendEmail = null,
	reqId = 0,
	leaseId = 0,
	catName = null,
	leaseTermName = null;
	
	
//item functions starts-----------------------------------------------------------------------------------------------------------------------
//postItem begins here--------------------------------------------------------
function itemSetValues(){			//setting values for item object from the form
	
	itemId = 0;
	
	itemTitle = $("#title").val();
	if (itemTitle === '') 
		itemTitle = null;
	
	itemDescription = $("#description").val();
	if (itemDescription === '') 
		itemDescription = null;
	
	itemCategory = $("#dropdownbuttoncategory").text();
	if (itemCategory === '') 
		itemCategory = null;
	
	itemUserId = userloggedin;
	
	if (itemUserId === '') 
		itemUserId = "anonymous";
	
	itemLeaseValue = $("#lease_value").val();
	if (itemLeaseValue === '') 
		itemLeaseValue = 0;
	
	itemLeaseTerm = $("#dropdownbuttonlease_term").text();
	if (itemLeaseTerm === '') 
		itemLeaseTerm = null;
	
	itemStatus = "InStore";

	//alert(itemTitle+" "+itemDescription+" "+itemCategory+" "+itemUserId+" "+itemLeaseValue+" "+itemLeaseTerm+" "+itemStatus);
}

itemDbCreate = function(){									//for storing in db/localstorage
	var req = {
		id: itemId,
		title: itemTitle,
		description: itemDescription,
		category: itemCategory,
		userId: itemUserId,
		leaseValue: itemLeaseValue,
		leaseTerm: itemLeaseTerm,
		status: itemStatus
	};
				
	postItemSend(req);
}
	
function postItemSend(req) {
	
		$.ajax({
			url: '/flsv2/PostItem',
			type: 'get',
			data: {req : JSON.stringify(req)},
			contentType:"application/json",
			dataType:"json",
			
			success: function(response) {

				//alert(response.Code+" "+response.Message);
				
				var msg = "Item Added Successfully. Your ItemId is: "+response.Id;
				confirmationIndex(msg);
			},
		
			error: function() {
				alert('Not Working');
			}
		});
};

//postItem ends here--------------------------------------------------------
//getItem begins here--------------------------------------------------------

function getNextItem(i){
	
	if(i == '' || i == undefined)
		itemToken = 0;
	
	itemToken = i;
	
	var req = {
		operation:"BrowseN",
		token: itemToken
	}
	
	getItemSend(req);
}

function getPrevItem(i){
	if(i == '' || i == undefined)
		itemToken = 0;
	
	itemToken = i;
	
	var req = {
		operation:"BrowseP",
		token: itemToken
	}
	getItemSend(req);
}

getItemSend = function(req) {
	
	$.ajax({
		url: '/flsv2/GetItemStore',
		type: 'get',
		data: {req : JSON.stringify(req)},
		contentType:"application/json",
		dataType:"json",
		
		success: function(response) {
			
			itemNextId = response.Id;
			
			if(response.Code == "FLS_SUCCESS") {
				//alert("success");
				itemObj = JSON.parse(response.Message);
				
				if(reasonForGetItem == 'carousel'){				//index.html
					addItemToCarousel();			
				}else if(reasonForGetItem == 'getItemInfo'){		//mystore.html
					getItemInfoContinued(itemObj);
				}else if(reasonForGetItem == 'leaseItem'){
					lease_requestedItem(itemObj);						//myincomingrequests.html
				}else if(reasonForGetItem == 'showRequestTable')
					showRequestItem(itemObj);
				else if(reasonForGetItem == 'viewItem')
					viewClickedItem(itemObj);								//index.html
			}
			else{				//when end of the database is reached 
				//alert(response.Message);
				
				if(reasonForGetItem == 'carousel'){
					var src = "images/homeimg.jpg"; 				//last image 
					var img1 = document.createElement("img");
					img1.src = src;
					
					$(img1).css("width", imgwidth);
					$(img1).css("height", imgheight);
					
					col1.appendChild(img1);
					row1.appendChild(col1);
					
					if(navigatingSide == 'next'){
						itemNextId = 0;					//when end of DB is reached, get the first item
					}else if(navigatingSide == 'prev'){
						disableLeftButton();			//when beginning of DB is reached, get the last item
					}
					
					counter++;
					
					if(counter >= noOfImagesInCarousel){
						item1.appendChild(row1);
						carouselinner.appendChild(item1);
						
						startingCarousel = 1;
					}
					
					getImg();
				}else if(reasonForGetItem == 'getItemInfo'){
					itemNextId = 0;
				}		
				
			}		//end of else
				
		},
	
		error: function() {
			alert('Not Working');
		}
	});
};

//getItem ends here------------------------------------------------------
//editItem begins here------------------------------------------------

function editItemSetValues(){
	
	itemTitle = $("#title").val();
	if (itemTitle === '') 
		itemTitle = null;
	
	itemId = getItemToShow();
	if(itemId === '')
		itemId = 0;
	
	itemDescription = $("#description").val();
	if (itemDescription === '') 
		itemDescription = null;
	
	itemCategory = $("#dropdownbuttoncategory").text();
	if (itemCategory === '') 
		itemCategory = null;
	
	itemUserId = userloggedin;
	if (itemUserId === '') 
		itemUserId = "anonymous";
	
	itemLeaseValue = $("#lease_value").val();
	if (itemLeaseValue === '') 
		itemLeaseValue = 0;
	
	itemLeaseTerm = $("#dropdownbuttonlease_term").text();
	if (itemLeaseTerm === '') 
		itemLeaseTerm = null;
	
	
}

function editItemDbCreate(){
	var req = {
		id:itemId,
		title:itemTitle,
		description:itemDescription,
		category: itemCategory,
		userId: itemUserId,
		leaseValue: itemLeaseValue,
		leaseTerm: itemLeaseTerm,
	}
	editItemSend(req);	
}

function editItemSend(req){
	$.ajax({
		url: '/flsv2/EditPosting',
		type: 'get',
		data: {req : JSON.stringify(req)},
		contentType:"application/json",
		dataType:"json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			
			var msg = "Item Edited Successfully. Your ItemId is: "+response.Id;
			confirmationIndex(msg);
		},
	
		error: function() {
			alert('Not Working');
		}
	});
	
}

//editItem ends here------------------------------------------------
//deleteItem begins here------------------------------------------------

function deleteItemDbCreate(){
	
	var req = {
		id:itemId,
		userId: userId
	}
	deleteItemSend(req);
}

function deleteItemSend(req){
	$.ajax({
		url: '/flsv2/DeletePosting',
		type: 'get',
		data: {req : JSON.stringify(req)},
		contentType:"application/json",
		dataType:"json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			var msg = "Item Deleted Successfully.";
			confirmationIndex(msg);
		},
	
		error: function() {
			alert('Not Working');
		}
	});	
}

//deleteItem ends here------------------------------------------------
//requestItem begins here---------------------------------------------------

function requestItemSetValues(){
	itemId = getItemToShow();
	itemUserId = userloggedin;
	
	if(itemId == ''){
		itemId = null;
	}
	if(itemUserId == ''){
		itemUserId = null;
	}
	
}

function requestItemDbCreate(){
	var req = {
		itemId: itemId,
		userId: itemUserId
	};

	requestItemSend(req);
}

function requestItemSend(req){
	
	$.ajax({
		url: '/flsv2/RequestItem',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			
			var msg = "Item Requested Successfully.";
			var objOwner = getItemOwner();
			
			//sendMail(objOwner, "FriendLease", "Your Item is requested");
			
			confirmationIndex(msg);
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
}


//requestItem ends here---------------------------------------------------
//getRequestItem begins here---------------------------------------------------


function getRequestItem(i){
	itemToken = i;	
	
	var req = {
		operation: "getNextR",
		token: itemToken
	}
	getRequestItemSend(req);
}

function getRequestItemSend(req){
	
	$.ajax({
		url: '/flsv2/GetRequests',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			
			if(response.Code == "FLS_SUCCESS") {
				obj = JSON.parse(response.Message);
				
				itemNextRequestId = response.Id;
				
				if(reasonForGetRequestItem == 'showRequestTable')	
					getItemForRequest(obj);		//function is in the myincomingrequests.html	
				else if(reasonForGetRequestItem == 'leaseItem')	
					setRequestingUserInLease(obj);		//function is in the script_admin_v2.js
				else if(reasonForGetRequestItem == 'rejectRequest'){
					rejectRequest(obj);			//function is in the myincomingrequests.html	
				}else if(reasonForGetRequestItem == 'leaseItemFromRequest'){
					leaseItemFromRequest(obj);	//function is in the myincomingrequests.html
				}
				
			}
			else{
				//alert(response.Message);
			}
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
	
}

//getRequestItem ends here---------------------------------------------------
//rejectRequest begins here------------------------------------------------

function rejectRequestSetValues(i, req){
	itemId = i;
	if (itemId === '') itemId = null;
	
	reqUserId = req;
	if (reqUserId ==='') reqUserId = null;
	
	var req = {
		itemId: itemId,
		userId: reqUserId
	};
	
	rejectRequestSend(req);
}

function rejectRequestSend(req){
	$.ajax({
		url: '/flsv2/RejectRequest',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			
			var msg = "Request Rejected Successfully.";
			confirmationIndex(msg);
		},
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});	
}

//rejectRequest ends here------------------------------------------------
//leaseItem begins here---------------------------------------------------

function leaseItemSetValues(reqUser){
	itemId = itemUserId = reqUserId = null;
	
	itemId = getItemToShow();
	itemUserId = userloggedin;
	reqUserId = reqUser;
	
	if(itemId == ''){
		itemId = null;
	}
	if(itemUserId == ''){
		itemUserId = null;
	}
	leaseItemDbCreate();
}

function leaseItemDbCreate(){
	var req = {
		reqUserId: reqUserId.toString(),
		itemId: itemId.toString(),
		userId: itemUserId.toString()
	};
	
	leaseItemSend(req);
}

function leaseItemSend(req){
		
		$.ajax({
			url: '/flsv2/GrantLease',
			type:'get',
			data: {req: JSON.stringify(req)},
			contentType:"application/json",
			dataType: "json",
			
			success: function(response) {
				//alert(response.Id+" "+response.Code+" "+response.Message);
				var msg = "Item Leased Successfully to "+response.Id;
				confirmationIndex(msg);	
			},
			error: function() {
				var msg = "Not Working";confirmationIndex(msg);
			}
		});
	
}


//leaseItem ends here---------------------------------------------------
//getLeaseItem begins here------------------------------------------------

function getLeaseItem(i){
	itemToken = i;
	
	var req = {
		operation: "getNextActive",
		token: itemToken
	}
	getLeaseItemSend(req);
}

function getLeaseItemSend(req){
	
	$.ajax({
		url: '/flsv2/GetLeases',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			
			if(response.Code == "FLS_SUCCESS") {
				//alert(response.Id+" "+response.Code);
				obj = JSON.parse(response.Message);
				
				itemNextId = response.Id;
				
				//alert('reqUserId:'+obj.reqUserId+" itemId:"+obj.itemId+" userId:"+obj.userId+" expiry:"+obj.expiry);
				//alert(reasonForGetRequestItem);
				
				if(reasonForGetLeaseItem == 'showLeaseTable')	
					showLeaseItem(obj);		//function is in the myleasedoutitems.html
				else if(reasonForGetLeaseItem == 'renewLease'){
					renew_Lease(obj);		//function is in the myleasedoutitems.html
				}else if(reasonForGetLeaseItem == 'justGetLeaseItem'){
					setLeaseItemId(obj);
				}

			}
			else{
				//alert(response.Message);
			}
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
}



//getLeaseItem ends here------------------------------------------------
//renewLeaseItem begins here------------------------------------------------

function renewLeaseDbCreate(){
	var req = {
		itemId: itemId.toString(),
		reqUserId: reqUserId.toString()
	};
	
	renewLeaseSend(req);	
}

function renewLeaseSend(req){
	
	$.ajax({
		url: '/flsv2/RenewLease',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			
			//alert(response.Id+" "+response.Code+" "+response.Message);
			var msg = "Lease Renewed Successfully.";
			confirmationIndex(msg);
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});	
}

//renewLeaseItem ends here------------------------------------------------
//wishItem begins here---------------------------------------------------


function wishItemSetValues(title,description,category,userid,leasevalue,leaseterm){
	itemId = 0;
	
	itemTitle = title;
	
	if (itemTitle === '') 
		itemTitle = null;
	 
	itemDescription = description;
	
	itemCategory = category;
	
	itemUserId = userid;
	
	if (itemUserId === '') 
		itemUserId = "anonymous";
	
	itemLeaseValue = leasevalue;
	itemLeaseTerm = leaseterm;
	itemStatus = "Wished";
		
}

wishItemDbCreate = function(){									//for storing in db/localstorage
	
	var req = {
		id: itemId,
		title: itemTitle,
		description: itemDescription,
		category: itemCategory,
		userId: itemUserId,
		leaseValue: itemLeaseValue,
		leaseTerm: itemLeaseTerm,
		status: itemStatus
	};

	wishItemSend(req);			
	
}

function wishItemSend(req){
	
		$.ajax({
			url: '/flsv2/WishItem',
			type: 'get',
			data: {req : JSON.stringify(req)},
			contentType:"application/json",
			dataType:"json",
			
			success: function(response) {
				//alert(response.Id+" "+response.Code+" "+response.Message);
				
				var msg = "Item Wished Successfully. Your ItemId is: "+response.Id;
				confirmationIndex(msg);

			},
		
			error: function() {
				alert('Not Working');
			}
		});
}

//wishItem ends here---------------------------------------------------
//getWishItem begins here---------------------------------------------------
	function getWishItem(i){
		itemToken = i;
		var req = {
				operation: "BrowseN",
				token: itemToken
		}
		getWishItemSend(req);
	}
	
	function getWishItemSend(req){
		$.ajax({
			url: '/flsv2/GetItemWishlist',
			type: 'get',
			data: {req : JSON.stringify(req)},
			contentType:"application/json",
			dataType:"json",
			
			success: function(response) {
				//alert(response.Id+" "+response.Code);
				
				if(response.Code == "FLS_SUCCESS") {
					obj = JSON.parse(response.Message);
					
					itemNextId = obj.itemId;
					//alert(itemNextId);
						
					if(reasonForGetWishItem == 'getItemInfo'){
						getItemInfoContinued(obj);
						
					}else{
						showWishItem(obj);		//function is in the mywishlists.html
					}
				}
				else{
					//alert(response.Message);
				}
			},
		
			error: function() {
				alert('Not Working');
			}
		});
	}
	
//getWishItem ends here---------------------------------------------------
//editWishItem begins here------------------------------------------------

function editWishItemSetValues(){
	
	itemTitle = $("#title").val();
	if (itemTitle === '') 
		itemTitle = null;
	
	itemId = getItemToShow();
	if(itemId === '')
		itemId = 0;
	
	itemDescription = $("#description").val();
	if (itemDescription === '') 
		itemDescription = null;
	
	itemCategory = $("#dropdownbuttoncategory").text();
	if (itemCategory === '') 
		itemCategory = null;
	
	itemUserId = userloggedin;
	if (itemUserId === '') 
		itemUserId = "anonymous";
	
	itemLeaseValue = $("#lease_value").val();
	if (itemLeaseValue === '') 
		itemLeaseValue = 0;
	
	itemLeaseTerm = $("#dropdownbuttonlease_term").text();
	if (itemLeaseTerm === '') 
		itemLeaseTerm = null;
	
}

function editWishItemDbCreate(){
	var req = {
		id: itemId,
		title: itemTitle,
		description: itemDescription,
		category: itemCategory,
		userId: itemUserId,
		leaseValue: itemLeaseValue,
		leaseTerm: itemLeaseTerm,
	}
	editWishItemSend(req);	
}

function editWishItemSend(req){
	
	$.ajax({
			url: '/flsv2/EditWishlist',
			type: 'get',
			data: {req : JSON.stringify(req)},
			contentType:"application/json",
			dataType:"json",
			
			success: function(response) {
				
				//alert(response.Id+" "+response.Code+" "+response.Message);
				var msg = "WishItem edited Successfully. Your ItemId is: "+response.Id;
				confirmationIndex(msg);
				
			},
		
			error: function() {
				alert('Not Working');
			}
		});
}


//editWishItem ends here------------------------------------------------
//deleteWishItem begins here------------------------------------------------

function deleteWishItemDbCreate(){
	var req = {
		id: itemId,
		userId: itemUserId
	}
	
	deleteWishItemSend(req);
	
}

function deleteWishItemSend(req){
	
	$.ajax({
		url: '/flsv2/DeleteWishlist',
		type: 'get',
		data: {req : JSON.stringify(req)},
		contentType:"application/json",
		dataType:"json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			var msg = "WishItem deleted Successfully.";
			confirmationIndex(msg);
		},
	
		error: function() {
			alert('Not Working');
		}
	});	
	
}

//deleteWishItem ends here------------------------------------------------
//addFriend begins here------------------------------------------------

function addFriendSetValues(name, mobile, email, user){
	friendName = name;
	friendEmail = email;
	friendMobile = mobile;
	userId = user;
	
	if(friendName == '')
		friendName = null;
	if(friendEmail == '')
		friendEmail = null;
	if(friendMobile == '')
		friendMobile = 0;
	
}

function addFriendDbCreate(){
	
	var req = {
		id: userId,
		fullName: friendName,
		mobile: friendMobile,
		userId: friendEmail
	};
	
	addFriendSend(req);
}

function addFriendSend(req){
	$.ajax({
		url: '/flsv2/AddFriend',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			var msg = response.Message;
			confirmationIndex(msg);
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
		
}

//addFriend ends here------------------------------------------------
//getFriend begins here------------------------------------------------

function getNextFriend(i){
	userId = userloggedin;
	friendEmail = i;
	
	var req = {
		operation: "getNext",
		id: userId,
		token: friendEmail
	}
	
	getFriendSend(req);
}

function getPrevFriend(i){
	userId = userloggedin;
	friendEmail = i;
	
	var req = {
		operation: "getPrevious",
		id: userId,
		token: friendEmail
	}
	
	getFriendSend(req);
}

function getFriendSend(req){
	$.ajax({
		url: '/flsv2/GetFriends',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			if(response.Code == "FLS_SUCCESS") {
				obj = JSON.parse(response.Message);
				itemNextId = obj.userId;
				
				if(reasonForGetFriend == 'showTable')
					showFriend(obj);			//in myfriendlist.html
				else if(reasonForGetFriend == 'prevFriend'){
					gotPrevFriend(obj.userId);			//in myfrienddetails.html
				}else if(reasonForGetFriend == 'viewFriendsDetail'){
					getItemInfoContinued(obj);
				}
			}
			else{
				//alert(response.Message);
				if(reasonForGetFriend == 'prevFriend'){
					gotPrevFriend('');	
				}
			}
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
}

//getFriend ends here------------------------------------------------
//editFriend begins here------------------------------------------------

function editFriendSetValues(name, email, mobile, user){
	friendName = name;
	friendEmail = email;
	friendMobile = mobile;
	userId = user;
	
	if(friendName == '')
		friendName = null;
	if(friendEmail == '')
		friendEmail = null;
	if(friendMobile == '')
		friendMobile = 0;
	
}

function editFriendDbCreate(){
	
	var req = {
		id: userId,
		fullName: friendName,
		mobile: friendMobile,
		userId: friendEmail
	};
	
	editFriendSend(req);
}

function editFriendSend(req){
	
	$.ajax({
		url: '/flsv2/EditFriend',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			var msg = response.Message;
			confirmationIndex(msg);
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
}

//editFriend ends here------------------------------------------------
//deleteFriend begins here------------------------------------------------

function deleteFriendSetValues(email, user){
	friendEmail = email;
	userId = user;
	
	if(friendEmail == '')
		friendEmail = null;
}

function deleteFriendDbCreate(){
	
	var req = {
		id: userId,
		userId: friendEmail
	};
	
	deleteFriendSend(req);
}

function deleteFriendSend(req){
	
	$.ajax({
		url: '/flsv2/DeleteFriend',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			var msg = response.Message;
			confirmationIndex(msg);	
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
}

//deleteFriend ends here------------------------------------------------
//getNextCategory begins here------------------------------------------------

function getNextCategory(i){
	catName = i;
	
	var req = {
		operation:"getNext",
		token: catName
	}
	getNextCategorySend(req);
}

function getNextCategorySend(req){
	$.ajax({
		url: '/flsv2/GetCategoryList',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {

			catName = response.Id;
			
			if(response.Code === "FLS_SUCCESS") {
				obj = JSON.parse(response.Message);
				loadCategoryDropdownContinued(obj);
			}
			else{
				//alert(response.Message);
			}
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});	
	
}

//getNextCategory ends here------------------------------------------------
//getNextLeaseTerm begins here------------------------------------------------

function getNextLeaseTerm(i){
	leaseTermName = i;
	
	var req = {
		operation:"getNext",
		token: leaseTermName
	}
	getNextLeaseTermSend(req);
}

function getNextLeaseTermSend(req){
	
	$.ajax({
		url: '/flsv2/GetLeaseTerms',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			
			leaseTermName = response.Id;
			
			if(response.Code === "FLS_SUCCESS") {
				obj = JSON.parse(response.Message);
				loadLeaseTermDropdownContinued(obj);
			}
			else{
				//alert(response.Message);
			}
		},
		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
}

//getNextLeaseTerm ends here------------------------------------------------
//signUp begins here------------------------------------------------

function signUpDbCreate(){
	
	var req = {
		userId: signupemail,
		fullName: signupname,
		mobile: signupmobile,
		location: signuplocation,
		auth: signuppassword
	};
	
	signUpSend(req);

}

function signUpSend(req){
	//alert(req.auth);			//password going well
	
	$.ajax({
		url: '/flsv2/SignUp',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			signupContinued(response.Message);			//this function is in signup.html
		},		
		error: function() {
			var msg = "Not Working";confirmationIndex(msg);
		}
	});
}

//signUp ends here------------------------------------------------
//login begins here------------------------------------------------

function loginDbCreate(){
	
	var req = {
		auth: loginpassword,
		token: loginemail
	}
	
	loginSend(req);
		
}

function loginSend(req){	
	//alert("loginSend called");
	$.ajax({
			url: '/flsv2/LoginUser',
			type:'get',
			data: {req: JSON.stringify(req)},
			contentType:"application/json",
			dataType: "json",
			
			success: function(response) {
				
				if(response.Code === "FLS_SUCCESS") {
					//alert("Success");
					obj = JSON.parse(response.Message);
					
					loginSuccessful(obj);
				}
				else{
					//alert("unsuccessful");
					loginUnsuccessful(response.Message);	
				}
			},
			
			error: function() {
				var msg = "Not Working";confirmationIndex(msg);
			}
		});
};


//login ends here------------------------------------------------
//sending email notification begins here------------------------------------------------
//sendMail(email, subject, msg);
/*
function sendMail(email, subject, content){
	$.ajax({
	type: 'POST',
	url: 'https://mandrillapp.com/api/1.0/messages/send.json',
	data: {
		'key': 'AOz9ESV_lIw7OJRkUH_yBg',								//mandrill App key	
		'message': {
			'from_email': 'redhu.sunny1994@gmail.com',					//email of sender
			'to': [
				{
					'email': email,				//email of receiver
					'name': '',				//name of receiver	
					'type': 'to'
				}
			],
			'autotext': 'true',
			'subject': subject,			//subject of mail
			'html': content				//content of mail
		}
	}
	}).done(function(response) {
		console.log(response); // if you're into that sorta thing
	});
}
*/

//sending email notification ends here------------------------------------------------