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