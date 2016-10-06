echo "+++Deploy source in $PWD+++"

echo "+++ Shutting down Tomcat ..."
/Applications/tomcatstack-7.0.57-0/ctlscript.sh stop 
sleep 5

echo "+++ Installing new war ..."
rm -rf /Applications/tomcatstack-7.0.57-0/apache-tomcat/webapps/flsv3*
cp build/flsv3.war /Applications/tomcatstack-7.0.57-0/apache-tomcat/webapps/

echo "+++ Starting up Tomcat ..."
rm /Applications/tomcatstack-7.0.57-0/apache-tomcat/logs/*
/Applications/tomcatstack-7.0.57-0/ctlscript.sh start 
sleep 5



