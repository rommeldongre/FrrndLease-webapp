echo "+++Deploy source in $PWD+++"

echo "+++ Shutting down Tomcat ..."
/Applications/tomcatstack-8.0.38-0/ctlscript.sh stop 
sleep 5

echo "+++ Installing new war ..."
rm -rf /Applications/tomcatstack-8.0.38-0/apache-tomcat/webapps/flsv2*
cp build/flsv2.war /Applications/tomcatstack-8.0.38-0/apache-tomcat/webapps/

echo "+++ Starting up Tomcat ..."
rm /Applications/tomcatstack-8.0.38-0/apache-tomcat/logs/*
/Applications/tomcatstack-8.0.38-0/ctlscript.sh start 
sleep 5



