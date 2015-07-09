function storeCurrentFunction(func){
	localStorage.setItem("prevFunc", func);
}

function storeItemToShow(itemno){
	localStorage.setItem("itemToShow", itemno);
}

function getItemToShow(){
	var itemno = localStorage.getItem("itemToShow");
	return itemno;
}

function storeRequestToShow(reqno){
	localStorage.setItem("requestToShow", reqno);
}

function getRequestToShow(){
	var reqno = localStorage.getItem("requestToShow");
	return reqno;
}

function storeLeaseToShow(leaseno){
	localStorage.setItem("leaseToShow", leaseno);
}

function getLeaseToShow(){
	var leaseno = localStorage.getItem("leaseToShow");
	return leaseno;
}

function setBrowsingCategory(browsingCategory){		//browse items from browsingCategory only
	localStorage.setItem("browsingCategory", browsingCategory);
}

function getBrowsingCategory(){
	var browsingCategory = localStorage.getItem("browsingCategory");
	return browsingCategory;
}

function storeRequestingUser(reqUserId){
	localStorage.setItem("requestingUser", reqUserId);
}

function getRequestingUser(reqUserId){
	var reqUser = localStorage.getItem("requestingUser");
	return reqUser;
}