cd stack/apache-tomcat #Your Tomcat root, this is for AWS

echo "+++ Shutting down Tomcat ..."
sudo bin/shutdown.sh
sleep 5

echo "+++ Installing new war ..."
sudo rm -rf webapps/flsv3*
sudo cp /tmp/flsv3.war webapps/
sudo mv -f /tmp/flsv3.war /tmp/flsv3.war.bak

echo "+++ Starting up Tomcat ..."
sudo rm logs/*
sudo bin/startup.sh
sleep 5



