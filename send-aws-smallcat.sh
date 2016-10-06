cd ~/dev/workspace/flsv3
cd build
scp -i ~/dev/rom-test.pem flsv3.war ubuntu@54.179.148.235:/tmp
ssh -i ~/dev/rom-test.pem ubuntu@54.179.148.235
