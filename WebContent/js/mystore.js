function getPrevFunc(){
	var prevfunc = localStorage.getItem("prevFunc");
	return prevfunc;
}

function getItemToShow(){
	var itemno = localStorage.getItem("itemToShow");
	return itemno;
}

function getRequestToShow(){
	var reqno = localStorage.getItem("requestToShow");
	return reqno;
}

function getLeaseToShow(){
	var leaseno = localStorage.getItem("leaseToShow");
	return leaseno;
}

function storeRequestingUser(reqUserId){
	localStorage.setItem("requestingUser", requserId);
}

function getRequestingUser(){
	var reqUser = localStorage.getItem("requestingUser");
	return reqUser;
}

function storeItemOwner(owner){
	localStorage.setItem("itemOwner", owner);
}

function getItemOwner(){
	var owner = localStorage.getItem("itemOwner");
	return owner;
}

function setPrevPage(prevPage){
	localStorage.setItem("prevPage",prevPage);
}

function getPrevPage(){
	var prevPage = localStorage.getItem("prevPage");
	return prevPage;
}