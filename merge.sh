cd ~/dev/workspace/flsv2
cd ../flsmobile-web/
git pull
cd ../flsweb/
git pull
cd ../flsv2/
rm -rf src/* WebContent/*
cp -R ../flsweb/src/* src/
cp -R ../flsweb/WebContent/* WebContent/
cp -R ../flsmobile-web/* WebContent/
ant
./deploy-local.sh 
scp -i ~/dev/rom-test.pem build/flsv2.war ubuntu@52.74.250.44:/tmp

