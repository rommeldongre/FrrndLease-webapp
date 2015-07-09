cd stack/apache-tomcat #Your Tomcat root, this is for AWS

echo "+++ Shutting down Tomcat ..."
sudo bin/shutdown.sh
sleep 5

echo "+++ Installing new war ..."
sudo rm -rf webapps/flsv2*
sudo cp /tmp/flsv2.war webapps/
sudo mv -f /tmp/flsv2.war /tmp/flsv2.war.bak

echo "+++ Starting up Tomcat ..."
sudo rm logs/*
sudo bin/startup.sh
sleep 5



