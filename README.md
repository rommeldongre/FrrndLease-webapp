# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* Quick summary
* Version: flsv3
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Summary of set up
* Configuration: None
* Dependencies : None
* Database configuration : None
* How to run tests : None
* Deployment instructions: 
      * Go to path <catalina_home>/conf/ in your computer.
      * In file server.xml add the following xml code after opening <Host> tag 
      * Save the file and restart the server.
            
```
#!xml

<Context path="" docBase="flsv3" reloadable="true"></Context>
```

### For Deploying automatically using Maven ###

* Go to path <catalina_home>/conf/ in your computer.
* In file tomcat_users.xml add the following xml code before closing </tomcat-users> tag 


```
#!xml

<role rolename="manager-script"/>
<user username="tomcat" password="manager" roles="manager-script" />
```

* The above username, password should be same as that specified in Maven Tomcat plugin else it will not work
* Start the XAMPP server
* Deploy using the goals clean tomcat7:deploy -e either in Eclipse or Through command line

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact

### How to install SSL Certificate ###

* visit this website - [https://gethttpsforfree.com/](https://gethttpsforfree.com/)

##### Step 1 - #####

* Enter the email Id - rommel@greylabs.org
* In the next step click on how to generate this link
* Copy the first code and run this command in bitnami folder of the server
* You'll see an account ket getting generated
* Now copy the second code and run it. You'll see a public key getting generated
* Now copy that code. Make sure you also copy the upper and lower bounds
* Now paste that code account public key section in the website.
* And click on validate now.

#### Step 2 - ####

* Now run `openssl genrsa 4096 > domain.key (replace domain.key with frrndlease.key)`
* Then run
```
openssl req -new -sha256 -key frrndlease.key -subj "/" \
  -reqexts SAN -config <(cat /etc/ssl/openssl.cnf \
  <(printf "[SAN]\nsubjectAltName=DNS:frrndlease.com,DNS:www.frrndlease.com"))
```
* Copy paste the code in the section and validate.

#### step 3 - ####

* Now you'll see four lines of code and four empty space you want to fill with the codes.
* Take the first code, eg.
```
PRIV_KEY=account.key; echo -n "eyJub25jZSI6IlFtdTVIZWs5Z25Fd0NiVmsxNTlTdTFlZW43VTRabW9Rb2MwdXNtd0U5aEEifQ.eyJyZXNvdXJjZSI6Im5ldy1yZWciLCJjb250YWN0IjpbIm1haWx0bzpyb21tZWxAZ3JleWxhYnMub3JnIl0sImFncmVlbWVudCI6Imh0dHBzOi8vbGV0c2VuY3J5cHQub3JnL2RvY3VtZW50cy9MRS1TQS12MS4xLjEtQXVndXN0LTEtMjAxNi5wZGYifQ" | openssl dgst -sha256 -hex -sign $PRIV_KEY
```
* Remember to remove ./ because our account.key is in the same directory where we will be running the code
* Run the code for all these lines and generate a code.
* Now paste this code into "" inside the box below each code. For eg.
```
"(stdin)= cc9b40ea7b6a8bbc40c6ca5f473897eba2c387ae0ee5a4bed3bdc7ff52aab370ea0cff31489da069ef3fbf34a237da9377340d2e1379d1a16462710d2fb37683316ed393971d716d811c6bdae2d9e705510b0aeeb1673e1a566b677f05c8e95459710df32924316dcef4e676ae9270d0deeab86a0061fa8157eb1eb752e9efbf53338fad8d4b80669404fd7b6bbd4ff03b093b643bface72bf04c2abbb06b16dd8a859d031b815474c8c43abe08574a7e91334e2c09b9b13d73a457e81e838a0eca4cc91f2b462dbf25e6311e5b2ab5e6fa183d2c2ccdfe80fc65c0ed3b89e14a1e7c218c072736abd66a603d4509f6598f6f4ffd4f481e8d4794192ac841289d7034da0a592ccfe6bdce9a8c443b1f7bf80e789933cabee5f32b7cd442cab243fd8a4664c8bd94931bdb5815e1a8dcb7f92276aa3779150c5f76298334931bd4783dc046710f83fb1c351469e48a584e6cd411666e787d104477c6f71c6521683b13b22e1566129d8ee64888533eea9f9ebd5a1d82def0c9cc754ae8cf9f3c5cc0540b64e0ffd18dac356856f1ad3c6f1a452a2ef0b202258a6adc29205dd769698255af5ebca652f9e3f1e298f8050e7322da0391cf9c5ebadb788818ca71691636cb28aa78bb42e47ebd27c1ca4f3c82592782a472ae969ef5ab2ef23bbc24fcb2327f3e1d62b89821803dd0331f9668290e6750a89b8433612df0ab6f8b7
"
```
* Do this for all the lines of code and generate a stdin code for each and paste them in appropriate box.
* Then click on validate

#### step 4 - ####

* Generate same kind of stdin code for each of the domains.
* To verify the domain select option-2-file-based tab.
* You dont have to create a .well-known/acme-challenge directory in webapps folder
* Just cd to the stack/apache-tomcat/webapps directory
* Run the command which you see to create a file. For eg.
```
echo -n "WSMC-TOuqlZgXF-HHrIM6eC9HePBEaPVjNYPc6x2EQ0.naJkoNv7OkY_z2oKrKwwlhyq9proQA3gSTMjHUxmfbA" > /path/to/www/.well-known/acme-challenge/WSMC-TOuqlZgXF-HHrIM6eC9HePBEaPVjNYPc6x2EQ0
```
* Make sure you remove /path/to/www because inside webapps directory we have .well-known directory.
* So if we are inside the webapps directory we can keep the path from .well-known/........
* Now run this command and this will create a new file inside acme-challenge dir.
* There is a link below the section to test if this file got generated. Just open that link in a new tab to verify this.
* once it gets verified just click on validate button.
* save the signed certificate into a file named - frrndlease.crt
* save the intermediate certificate into a file named - intermediate.crt
* Just replace frrndlease.crt and intermediate.crt in the bitnami folder of the server.
* Restart the server to confirm this worked.