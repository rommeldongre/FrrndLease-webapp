
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
	reasonForGetCategory = null,
	reasonForAddFriend = null,
	reasonForSearchItem = null,
	friendName = null,
	friendMobile = 0,
	friendEmail = null,
	reqId = 0,
	leaseId = 0,
	catName = null,
	leaseTermName = null,
	storeObjcanvasCtx = null,
	imageFile = null, 
	url = null,
	prevPage = null,
	endOfCarousel = 0;
	userName = null;
	addFriendAPICall = 0;
	WishlistUrl = null;
	
var itemNextIdArray = [];
	
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
	if (itemCategory === '' || itemCategory == 'Category') 
		itemCategory = null;
	
	itemUserId = userloggedin;
	
	if (itemUserId === '') 
		itemUserId = "anonymous";
	
	itemLeaseValue = $("#lease_value").val();
	if (itemLeaseValue === '') 
		itemLeaseValue = 0;
	
	itemLeaseTerm = $("#dropdownbuttonlease_term").text();
	if (itemLeaseTerm === '' || itemLeaseTerm == 'Lease Term') 
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
		status: itemStatus,
		image: url
	};
	
	postItemSend(req);
}
	
function postItemSend(req) {
	
		$.ajax({
			url: '/flsv2/PostItem',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			
			success: function(response) {
				if(response.returnCode==0){
					shareOrNot(response);
				}else{
					var heading = "Unsuccessful";
					var msg = response.errorString;
					$('#myPleaseWait').modal('hide');
					confirmationIndex(heading ,msg);
				}
			},
		
			error: function() {
				var heading = "Unsuccessful";
				var msg = "Not Working";
				confirmationIndex(heading ,msg);
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
					addItemToCarousel(itemObj);			
				}else if(reasonForGetItem == 'getItemInfo'){		//mystore.html
					getItemInfoContinued(itemObj);
				}else if(reasonForGetItem == 'leaseItem'){
					lease_requestedItem(itemObj);						//myincomingrequests.html
				}else if(reasonForGetItem == 'showRequestTable')
					showRequestItem(itemObj);							//myincomingrequests.html
				else if(reasonForGetItem == 'viewItem')
					viewClickedItem(itemObj);								//index.html
			}
			else{				//when end of the database is reached 
				//alert(response.Message);
				
				if(reasonForGetItem == 'carousel'){
					if(startingCarousel == 0 && counter == 0){		//empty carousel
						
						//categoryempty image begins
						span1 = document.createElement("span");
						span1.className = "items";
					
						src = "images/emptycategory.jpg";
					
						var img1 = document.createElement("img");
						img1.src = src;
						
						$(img1).css("width", imgwidth);
						$(img1).css("height", imgheight);
						
						var caption1 = document.createElement("div");
						caption1.className = "carousel-caption";
						caption1.style.backgroundColor = "black";
						caption1.style.opacity = 0.6;
						
						var p = document.createElement("p");
						p.innerHTML = "Try selecting another category";
						
						caption1.appendChild(p);
						span1.appendChild(img1);
						span1.appendChild(caption1);
						col1.appendChild(span1);
						row1.appendChild(col1);	
						
						item1.appendChild(row1);
						carouselinner.appendChild(item1);
						//categoryempty image ends
					}

					disableRightButton();
					if(counter != 0){
						item1.appendChild(row1);
						carouselinner.appendChild(item1);
						$("#carousel-example-generic").carousel('next');				//load next slide
					}
						
					startingCarousel = 1;
					
					endOfCarousel = 1;
				}else if(reasonForGetItem == 'getItemInfo'){
						itemNextId = 0;
				}		
				
			}	
				
		},
	
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
	if (itemCategory === '' || itemCategory == 'Category') 
		itemCategory = null;
	
	itemUserId = userloggedin;
	if (itemUserId === '') 
		itemUserId = "anonymous";
	
	itemLeaseValue = $("#lease_value").val();
	if (itemLeaseValue === '') 
		itemLeaseValue = 0;
	
	itemLeaseTerm = $("#dropdownbuttonlease_term").text();
	if (itemLeaseTerm === '' || itemLeaseTerm == 'Lease Term') 
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
		image: url
	}
	editItemSend(req);	
}

function editItemSend(req){
	$.ajax({
		url: '/flsv2/EditPosting',
		type: 'post',
		data: {req : JSON.stringify(req)},
		contentType: "application/x-www-form-urlencoded",
		dataType:"json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			var heading = "Successful";
			var msg = response.Message;
			confirmationIndex(heading ,msg);
		},
	
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
			confirmationIndex(response.Message);
		},
	
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
			var heading = "Successful";
			
			var msg = response.Message;
			var objOwner = getItemOwner();
			
			confirmationIndex(heading, msg);
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
		type:'GET',
		data: {req : JSON.stringify(req)},
		contentType:"application/json",
		dataType: "JSON",
		
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
				//confirmationIndex(response.Message);
				if(itemNextRequestId == 0){
					showEmptyText();		//function is in myincomingrequests.html
				}
			}
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
		userId: reqUserId,
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
			var heading = "Successful";
			var msg = response.Message;
			confirmationIndex(heading, msg);
		},
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
		itemId: itemId,
		userId: itemUserId.toString()
	};
	
	console.log(JSON.stringify(req));
	leaseItemSend(req);
}

function leaseItemSend(req){
		
		$.ajax({
			url: '/flsv2/GrantLease',
			type:'post',
			data: JSON.stringify(req),
			contentType:"application/json",
			dataType: "json",
			
			success: function(response) {
				//alert(response.Id+" "+response.Code+" "+response.Message);
				var heading = "Successful";
				var msg = response.message;
				confirmationIndex(heading, msg);	
			},
			error: function() {
				var msg = "Not Working";
				confirmationIndex(msg);
			}
		});
	
}


//leaseItem ends here---------------------------------------------------
//getLeaseItem begins here------------------------------------------------

function getLeaseItem(i){
	itemToken = i;
	itemToken = itemToken.toString();
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
					showLeaseItem(obj);		//function is in the myleasedoutitems.html and myleasedinitems.html
				else if(reasonForGetLeaseItem == 'renewLease'){
					renew_Lease(obj);		//function is in the myleasedoutitems.html
				}else if(reasonForGetLeaseItem == 'justGetLeaseItem'){
					setLeaseItemId(obj);
				}else if(reasonForGetLeaseItem == 'closeLease'){
					close_Lease(obj);
				}

			}
			else{
				//confirmationIndex(response.Message);
				if(itemNextId == 0){
					showEmptyText();		//function is in the myleasedoutitems.html and myleasedinitems.html
				}
			}
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
		}
	});
}



//getLeaseItem ends here------------------------------------------------
//renewLeaseItem begins here------------------------------------------------

function renewLeaseDbCreate(){
	var req = {
		itemId: itemId.toString(),
		reqUserId: reqUserId.toString(),
		flag: "renew"
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
			var heading = "Successful";
			var msg = response.Message;
			confirmationIndex(heading, msg);
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
		}
	});	
}

//renewLeaseItem ends here------------------------------------------------
//closeLease begins here------------------------------------------------
function closeLeaseDbCreate(){
	var req = {
		itemId: itemId.toString(),
		reqUserId: reqUserId.toString(),
		flag: "close"
	};
	
	closeLeaseSend(req);
}

function closeLeaseSend(req){
	$.ajax({
		url: '/flsv2/RenewLease',
		type:'get',
		data: {req: JSON.stringify(req)},
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			var heading = "Successful";
			var msg = response.Message;
			confirmationIndex(heading, msg);
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
		}
	});
}
//closeLease ends here------------------------------------------------
//searchItem begins here------------------------------------------------

function searchItemSetValues(id, title, description, category, leasevalue, leaseterm){
	itemId = id;
	if(itemId == null || itemId == '' || itemId == 'null')
		itemId = 0;
	
	itemTitle = title;
	if (itemTitle === '') 
		itemTitle = "";
	 
	itemDescription = description;
	if (itemDescription === '') 
		itemDescription = "";
	 
	itemCategory = category;
	if (itemCategory === '') 
		itemCategory = "";
	
	itemLeaseValue = leasevalue;
	if (itemLeaseValue === '') 
		itemLeaseValue = 0;
	
	itemLeaseTerm = leaseterm;
	if (itemLeaseTerm === '') 
		itemLeaseTerm = "";
}

function searchItemDbCreate(){	
	var req = {
		token: itemId,
		title: itemTitle,
		description: itemDescription,
		category: itemCategory,
		leaseValue: itemLeaseValue,
		leaseTerm: itemLeaseTerm
	};
	
	searchItemSend(req);
}

function searchItemSend(req){
	
	$.ajax({
		url: '/flsv2/SearchItem',
		type: 'post',
		data: {req : JSON.stringify(req)},
		contentType: "application/x-www-form-urlencoded",
		dataType: "json",
		
		success: function(response) {	
			if(response.Code == "FLS_SUCCESS") {
				itemObj = JSON.parse(response.Message);
 
				if(reasonForSearchItem == "getLeasedItemInfo"){
					showLeaseItemContinued(itemObj);
					
				}else{
					itemId = itemObj.itemId;
					addItemToCarousel(itemObj);
				}	
			}
			else{
				
				if(startingCarousel == 0 && counter == 0){		//empty carousel
					//categoryempty image begins
					span1 = document.createElement("span");
					span1.className = "items";
				
					src = "images/emptycategory.jpg";
				
					var img1 = document.createElement("img");
					img1.src = src;
					
					$(img1).css("width", imgwidth);
					$(img1).css("height", imgheight);
					
					var caption1 = document.createElement("div");
					caption1.className = "carousel-caption";
					caption1.style.backgroundColor = "black";
					caption1.style.opacity = 0.6;
					
					var p = document.createElement("p");
					p.innerHTML = "Try selecting another category";
					
					caption1.appendChild(p);
					span1.appendChild(img1);
					span1.appendChild(caption1);
					col1.appendChild(span1);
					row1.appendChild(col1);	
					
					item1.appendChild(row1);
					carouselinner.appendChild(item1);
					
					//categoryempty image ends
				}

				disableRightButton();
				if(counter != 0){
					item1.appendChild(row1);
					carouselinner.appendChild(item1);
					
					$("#carousel-example-generic").carousel('next');				//load next slide
				}
					
				startingCarousel = 1;
				
				endOfCarousel = 1;
							
				//searchItemByDescriptionSetValues(itemTitle);
				//searchItemByDescriptionDbCreate();
			}
		},
	
		error: function() {
			alert('Not Working');
		}
	});
}

//searchItem ends here------------------------------------------------
//searchItemByDescription begins here---------------------------------------------
/*
function searchItemByDescriptionSetValues(description){
	itemDescription = description;
	if (itemDescription === '') 
		itemDescription = "";
	 
}

function searchItemByDescriptionDbCreate(){	
	var req = {
		token: itemId,
		title: "",
		description: itemDescription,
		category: "",
		leaseValue: 0,
		leaseTerm: ""
	};
	
	searchItemByDescriptionSend(req);
}

function searchItemByDescriptionSend(req){
	
	$.ajax({
		url: '/flsv2/SearchItem',
		type: 'post',
		data: {req : JSON.stringify(req)},
		contentType: "application/x-www-form-urlencoded",
		dataType: "json",
		
		success: function(response) {	
			if(response.Code == "FLS_SUCCESS") {
				itemObj = JSON.parse(response.Message);
				itemId = itemObj.itemId;
				console.log("title "+itemObj.title+" category "+itemObj.category);
				//url = obj.image;
				addItemToCarousel(itemObj);	
			}
			else{
				console.log("Search Failed. Now adding to wishlist");
				reasonForWishItem = "searchFromHome";
				var userid = userloggedin;
				wishItemSetValues(itemTitle, null, null, userid, 0, null, null);
				
				wishItemDbCreate();
			}
		},
	
		error: function() {
			alert('Not Working');
		}
	});
}
*/
//searchItemByDescription ends here---------------------------------------------
//wishItem begins here---------------------------------------------------


function wishItemSetValues(title,description,category,userid,leasevalue,leaseterm,imgurl){
	
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
	if(itemLeaseValue == '')
		itemLeaseValue = 0;
	
	itemLeaseTerm = leaseterm;
	itemStatus = "Wished";
	
	url = imgurl;	
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
		status: itemStatus,
		image: url
	};
		
	wishItemSend(req);			
	
}

function wishItemSend(req){
		
		$.ajax({
			url: '/flsv2/WishItem',
			type: 'post',
			data: {req : JSON.stringify(req)},
			contentType: "application/x-www-form-urlencoded",
			dataType:"json",
			
			success: function(response) {
				var heading;
				var msg = response.Message+" Your ItemId is: "+response.Id;
				if(reasonForWishItem == "searchFromHome"){
					heading = "No Items Found";
					msg = "Item has been stored in your Wishlist";
					//confirmationIndex(heading, msg);			//index.html
				}else{
					//setPrevPage("mywishlists.html");
					heading = "Successful";
					msg = response.Message;
					//confirmationIndex(heading, msg);			//mywishitemdetails.html
				}
					
			},
		
			error: function() {
				var msg = "Not Working";
				//confirmationIndex(msg);
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
						
					if(reasonForGetWishItem == 'getItemInfo'){		//mywishitemdetails.html
						getItemInfoContinued(obj);
						
					}else{
						showWishItem(obj);		//mywishlists.html
					}
				}
				else{
					//confirmationIndex(response.Message);
					if(itemNextId == 0){
						showEmptyText();		//mywishlists.html
					}
				}
			},
		
			error: function() {
				var msg = "Not Working";
				confirmationIndex(msg);
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
	if (itemCategory === '' || itemCategory === 'Category') 
		itemCategory = null;
	
	itemUserId = userloggedin;
	if (itemUserId === '') 
		itemUserId = "anonymous";
	
	itemLeaseValue = $("#lease_value").val();
	if (itemLeaseValue === '') 
		itemLeaseValue = 0;
	
	itemLeaseTerm = $("#dropdownbuttonlease_term").text();
	if (itemLeaseTerm === '' || itemLeaseTerm === 'Lease Term') 
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
		image: url
	}
	editWishItemSend(req);	
}

function editWishItemSend(req){
	
	$.ajax({
			url: '/flsv2/EditWishlist',
			type: 'post',
			data: {req : JSON.stringify(req)},
			contentType: "application/x-www-form-urlencoded",
			dataType:"json",
			
			success: function(response) {
				//alert(response.Id+" "+response.Code+" "+response.Message);
				
				setPrevPage("mywishlists.html");
				var heading = "Successful";
				msg = response.Message;
				confirmationIndex(heading, msg);			//mywishitemdetails.html
			
			},
		
			error: function() {
				var msg = "Not Working";
				confirmationIndex(msg);
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
			setPrevPage("mywishlists.html");
			heading = "Successful";
			
			msg = response.Message;
			confirmationIndex(heading, msg);			//mywishitemdetails.html
		},
	
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
		id: friendEmail,
		fullName: friendName,
		mobile: friendMobile,
		userId: userId
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
			//setPrevPage("myfriendslist.html");
			
			if(reasonForAddFriend == "importGoogle"){
				add_checked_friends_continued();
			}else if(reasonForAddFriend == "importEmail"){
				var header = "Congrats";
				var msg = response.Message;
				confirmationIndex(header, msg);
			}else if(reasonForAddFriend == "importFacebook"){
				var header = "Congrats";
				addFriendAPICall--;
				var msg = 1;
				if(addFriendAPICall==0){
					confirmationIndex(msg);
				}
			}else{
				var header = "Friend Added";
				var msg = "You can now lease items from "+friendName;
				confirmationIndex(header, msg);	
			}
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
				}else if(itemNextId == 0){
					showEmptyText();			//in myfriendlist.html
				}
			}
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
			var header = "Friend Editted";
			var msg = response.Message;
			confirmationIndex(header, msg);
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
			var header = "Friend Deleted";
			var msg = response.Message;
			confirmationIndex(header, msg);	
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
				
				if(reasonForGetCategory == 'categoryFilter'){
					loadCategoryFilterContinued(obj);					//myindex.html	
				}else if(reasonForGetCategory == 'categoryDropdown'){
					loadCategoryDropdownContinued(obj);					//mystore.html
				}
			}
			else{
				//alert(response.Message);
			}
		},
		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
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
			var msg = "Not Working";
			confirmationIndex(msg);
		}
	});
}

//getNextLeaseTerm ends here------------------------------------------------
//signUp begins here------------------------------------------------

function signUpDbCreate(){
	signuppassword = CryptoJS.MD5(signuppassword);
	signuppassword = signuppassword.toString();
	
	var signupactivation = CryptoJS.MD5(signupemail);
	signupactivation = signupactivation.toString();
	
	var req = {
		userId: signupemail,
		fullName: signupname,
		mobile: signupmobile,
		location: signuplocation,
		auth: signuppassword,
		activation: signupactivation,
		status: signupstatus
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
			signupContinued(response.Code);			//this function is in signup.html
		},		
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
		}
	});
}

//signUp ends here------------------------------------------------
//login begins here------------------------------------------------

function loginDbCreate(){
	loginpassword = CryptoJS.MD5(loginpassword);
	loginpassword = loginpassword.toString();

	var req = {
		auth: loginpassword,
		token: loginemail,
		signUpStatus: signupstatus
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
					loginerrormsg = response.Message;
					loginUnsuccessful(response.Message);	
				}
			},
			
			error: function() {
				var msg = "Not Working";
				confirmationIndex(msg);
			}
		});
};

//login ends here------------------------------------------------

//MyOutgoingRequests starts here---------------------------------

function getNextOutItem(i,j){
	//alert("Inside getNextOutItem function.");
	if(i == '' || i == undefined){
		itemToken = 0;
	}
	itemToken = i;
	userName = j;
	
	var req = {
		userId: userName,
		cookie: itemToken
	}
	getOutRequest(req);
}

getOutRequest = function(req) {
		//alert("Inside send function.");
		$.ajax({
			url: '/flsv2/GetRequestsByUser',
			type:'POST',
			data: JSON.stringify(req),
			contentType:"application/json",
			dataType: "JSON",
			success: function(response) {
				//alert("working");
				if(!response.title){
				}else{
					getOutItemForRequest(response);
					//alert(response.title);
				}
			},
			error: function() {
				alert("not working");
			}
		});
	};
	
//MyOutgoingRequests ends here---------------------------------


//ImportWishlist starts here---------------------------------

function getWishlist(i,j){
	//alert("Inside getNextOutItem function.");
	if(i == '' || i == undefined)
		WishlistUrl = "";
	
	WishlistUrl = i;
	userName = j;
	
	var req = {
		userId: userName,
		url: WishlistUrl
	}
	
	getWishlistRequest(req);
}

getWishlistRequest = function(req) {
		//alert("Inside send function.");
		$.ajax({
			url: '/flsv2/ImportWishlist',
			type:'POST',
			data: JSON.stringify(req),
			contentType:"application/json",
			dataType: "JSON",
			success: function(response) {
				//alert("working");
					$('#myPleaseWait').modal('toggle');
					confirmationIndex("Success",response.wishItemCount+" out of "+response.totalWishItemCount+" Amazon Wishlist Items Imported");
					//getOutItemForRequest(response);
					//alert(response.title);
				
			},
			error: function() {
				alert("not working");
			}
		});
	};
	
//ImportWishlist ends here---------------------------------


//Delete Request starts here---------------------------------

function deleteRequestSetValues(i, req){
	itemToken = i;
	if (itemToken === '') itemToken = 0;
	
	reqUserId = req;
	if (reqUserId ==='') reqUserId = '';
	
	var req = {
		request_Id: itemToken,
		userId: reqUserId
	};
	
	deleteRequestSend(req);
}

function deleteRequestSend(req){
	$.ajax({
		url: '/flsv2/DeleteRequest',
		type:'POST',
		data: JSON.stringify(req),
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			//alert(response.Id+" "+response.Code+" "+response.Message);
			var heading = "Successful";
			if(response.errorString == "No Error"){
			var msg = "Request Deleted successfully";
			}
			confirmationIndex(heading, msg);
		},
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
		}
	});	
}
//Delete Request ends here---------------------------------


//Get Item Store starts here---------------------------------

function getNextItemCarousel(i,user,cat,Limit){
	
	if (i === ''){
		itemToken = -1;
	}else{
		itemToken = i;
	}

	itemNextIdArray.push(itemToken);
	
	if (user ===''){
		reqUserId = null;
	}else{
		reqUserId = user;
	}
	
	if (cat ==='' || cat ==='All') {
		itemCategory = null;
	}else{
		itemCategory = cat;
	}
	
	var req = {
		cookie: itemToken,
		userId: reqUserId,
		category: itemCategory,
		limit: Limit
	};
	
	getNextItemCarouselSend(req);
}

function getNextItemCarouselSend(req){
	$.ajax({
		url: '/flsv2/GetItemStoreByX',
		type:'POST',
		data: JSON.stringify(req),
		contentType:"application/json",
		dataType: "json",
		
		success: function(response) {
			
			if(response.returnCode == 0){
				itemNextId = response.lastItemId;
				addItemToCarousel(response);
				
			}else{				//when end of the database is reached 
				
				if(reasonForGetItem == 'carousel'){
					if(startingCarousel == 0 && counter == 0){		//empty carousel
					
						//categoryempty image begins
						var col1 = document.createElement("div");
						var span1 = document.createElement("span");
						span1.className = "items";
					
						src = "images/emptycategory.jpg";
					
						var img1 = document.createElement("img");
						img1.src = src;
						
						$(img1).css("width", imgwidth);
						$(img1).css("height", imgheight);
						
						var caption1 = document.createElement("div");
						caption1.className = "carousel-caption";
						caption1.style.backgroundColor = "black";
						caption1.style.opacity = 0.6;
						
						var p = document.createElement("p");
						p.innerHTML = "Try selecting another category";
						
						caption1.appendChild(p);
						span1.appendChild(img1);
						span1.appendChild(caption1);
						col1.appendChild(span1);
						row1.appendChild(col1);	
						
						item1.appendChild(row1);
						carouselinner.appendChild(item1);
						//categoryempty image ends
					}
					
					disableRightButton();
					if(counter != 0){
						item1.appendChild(row1);
						carouselinner.appendChild(item1);
						$("#carousel-example-generic").carousel('next');				//load next slide
					}
					
					startingCarousel = 1;
					
					endOfCarousel = 1;
				}else if(reasonForGetItem == 'getItemInfo'){
						itemNextId = 0;
				}		
				
			}
		},
		error: function() {
			var msg = "Not Working";
			confirmationIndex(msg);
		}
	});	
}
//Get Item Store ends here---------------------------------
