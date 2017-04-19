<%@ page import="java.util.*" %>
<%@ page import="java.security.*" %>

<%!
public boolean empty(String s)
	{
		if(s== null || s.trim().equals(""))
			return true;
		else
			return false;
	}
%>
<%!
	public String hashCal(String type,String str){
		byte[] hashseq=str.getBytes();
		StringBuffer hexString = new StringBuffer();
		try{
		MessageDigest algorithm = MessageDigest.getInstance(type);
		algorithm.reset();
		algorithm.update(hashseq);
		byte messageDigest[] = algorithm.digest();
            
		

		for (int i=0;i<messageDigest.length;i++) {
			String hex=Integer.toHexString(0xFF & messageDigest[i]);
			if(hex.length()==1) hexString.append("0");
			hexString.append(hex);
		}
			
		}catch(NoSuchAlgorithmException nsae){ }
		
		return hexString.toString();


	}
%>
<% 	
	
	//Test Credentials
	String merchant_key="rjQUPktU";
	String salt="e5iIg1jwi8";
	
	/*String action1 ="";
	String base_url="https://test.payu.in";
	int error=0;
	String hashString="";*/
	
 

	String status = request.getParameter("status");
	String firstname=request.getParameter("firstname");
	String amount=request.getParameter("amount");
	String txnid=request.getParameter("txnid");
	String posted_hash=request.getParameter("hash");
	String key=request.getParameter("key");
	String productinfo=request.getParameter("productinfo");
	String email=request.getParameter("email");
	
	if(status == null){
		status ="";
	}
	if(amount  == null){
		amount ="";
	}
	if(txnid == null){
		txnid ="";
	}
	String retHashSeq = salt+"|"+status+"|||||||||||"+email+"|"+firstname+"|"+productinfo+"|"+amount+"|"+txnid+"|"+key;
	
	 String hash=hashCal("SHA-512",retHashSeq);
	 
	 if(!hash.equals(retHashSeq)){
		 
	 }else{
		 
	 }
%>
<!DOCTYPE html>
<html>
	<head>
		<link rel="shortcut icon" href="images/fls-favicon.ico" type="image/x-icon">
	
		<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
		<title>Payment Status</title>
	
		<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
		<script src="js/jquery-1.11.3.min.js"></script>
	
		<!-- Bootstrap -->
		<link href="css/bootstrap.min.css" rel="stylesheet">
			<link href="css/gsdk.css" rel="stylesheet" />
		<link href="css/mystore.css" rel="stylesheet">
		<link href="css/index.css" rel="stylesheet">
		<link href='https://fonts.googleapis.com/css?family=Happy+Monkey' rel='stylesheet' type='text/css'>
		
		<!-- Bootstrap -->
		<link href="css/index.min.css" rel="stylesheet">
		<link href="css/bootstrap.min.css" rel="stylesheet">
		<link href="css/gsdk.min.css" rel="stylesheet">
		
		<style>		
			.navbar-default { margin : 0 !important; }
		</style>

	</head>
<script>
var hash='<%= hash %>';
var status = '<%= status %>';
var txnid = '<%= txnid %>';
var amt = '<%= amount %>'; 
var amount = parseInt(amt);

function submitOrder() {
	
	var user = localStorage.getItem("userloggedin");
	var pcode = localStorage.getItem("promoCode");
	console.log("promo code in LS :"+pcode);
	
	if(pcode == null || pcode === undefined || pcode == ""){
		pcode = "";
	}
	
	if(!checkUserLoggedIn){
		console.log("user not logged in");
		//window.location.replace("index.html");
		//return;
	}
	
		if(amount && txnid && status ){
			var req = {
				userId: user,
				accessToken: localStorage.getItem("userloggedinAccess"),
				promoCode: pcode,
				amountPaid: amount,
				razorPayId: txnid
			}
			console.log(req);
			if(checkPayUStatus()){
				addCredits(req);
			}else{
				localStorage.removeItem("promoCode");
				window.location.replace("myapp.html#/myprofile");
			}
			
		}else{
			console.log("Some values are null");
		}
    }

	function checkUserLoggedIn(){
		if(user != "" || user != null || user != 'anonymous'){
			return true;
		}else{
			return false;
		}
	}
	function checkPayUStatus(){
		
		var success = "success";
		if(status.toLowerCase() == success){
			return true;
		}else{
			return false;
		}
	}
	
    function addCredits(req){
        $.ajax({
            url: '/BuyCredits',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                if(response.code == 0){
					localStorage.removeItem("creditPromoCode");
					window.location.replace("myapp.html#/myprofile");
                }else{
               
                }
            },
            error:function() {
            }
        });
    }
	
	
</script>

<body onload="submitOrder();">

	<div>
    <div>
        <!--header starts---------------------------------------->
		<div ng-controller="headerCtrl" id="navbar">
        <nav class="navbar navbar-default" role="navigation" style="background-color: #fcfeff;">
            <div class="container-fluid">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar1">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span> 
                        <span class="icon-bar"></span> 
                        <span class="icon-bar"></span>
                    </button>
                    <a href="myapp.html#/" class="navbar-brand" id="icon"><img src="images/fls-logo_full.png" style="width:176px" /></a>
                </div>
                <div class="collapse navbar-collapse" id="myNavbar1">
				 
                </div>
            </div>
        </nav>
		
		</div>
        <!--header ends----------------------------------------->
    </div>
	</div>
	
	<div>
		<!-- Features Section -->
		<div class="section section-features-2" style="background-image: url('images/bluebackground.jpg');">
			<div class="container">
               <h2 class="text-center" id="icon" style="color:#0066ff;font-size: 200%;"><strong>Payment Status</strong></h2>
               <div class="row">
                   <div class="col-md-4">
                   	   <h4 class="section-title">Status:</h4>
                       <p><%= status %></p>

                   </div>
                     <div class="col-md-4">
                       <h4 class="section-title">Amount:</h4>
                       <p><%= amount %></p>

                   </div>
                   <div class="col-md-4">
                       <h4 class="section-title">Transaction Id:</h4>
                       <p><%= txnid %></p>
                   </div>
               </div>
           </div>  
			
		</div><!-- section -->
		<!-- Features Section Ends -->
	</div>
      
	  
	  <!--Footer starts here-->
    <div ng-include="'footer.html'" id="gsd_footer" style="margin-top:-10px;"></div>
    <!--Footer ends here-->
	
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<script src="js/bootstrap.min.js" defer></script>

    <!--Loading Animation code CSS & JS Links  -->
    <link href="css/animation.min.css" type="text/css" rel="stylesheet" media="print" onload="this.media='all';">
    <script src="js/jquery.backstretch.min.js" defer></script>
    <script src="js/animation.min.js" defer></script>
    <!--Loading Animation code CSS & JS Links ends here  -->

    <!-- DropDown list code-->
    <script src="js/dropdownlist.min.js" defer></script>
    <!-- DropDown list code ends-->

    <!-- Angularjs api -->
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js" defer></script>
    <script src="js/ui-bootstrap-tpls-1.3.2.min.js" defer></script>
	<script src="js/header.min.js" defer></script>
    <script src="js/flyerindex.min.js" defer></script>
	<script src="js/carousel.min.js" defer></script>
    <script src="js/footer.min.js" defer></script>
    <script src="js/load-image.all.min.js" defer></script>
    <script src="js/ngAutocomplete.min.js" defer></script>
    <!-- Angularjs api ends -->

    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
    <script async src="js/bootstrap-select.js" defer></script>
    <script async src="js/get-shit-done.min.js"></script>
    <script async src="js/gsdk-bootstrapswitch.min.js" defer></script>
    <script async src="js/jquery.flexisel.min.js" defer></script>
    <script async src="js/jquery.tagsinput.min.js" defer></script>
    <script async src="js/jquery-ui.custom.min.js" defer></script>

    <!-- Include all compiled plugins (below), or include individual files as needed -->
   <!-- <script src="js/tawk.min.js" defer></script>-->
</body>
</html>