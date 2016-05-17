cd ~/dev/workspace/flsv2
cd build
scp -i ~/dev/rom-test.pem flsv2.war ubuntu@54.179.148.235:/tmp
ssh -i ~/dev/rom-test.pem ubuntu@54.179.148.235
