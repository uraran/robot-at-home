cd ./src
echo 'Compiling USB-launcher driver ...'
gcc -o ctllauncher ctllauncher.c -lusb
mv ctllauncher ../bin
echo 'Compiling uvccapture tool ...'
cd ./uvccapture-0.5-n
make
mv uvccapture ../../bin
cd ..
echo 'Compiling Java classes ...'
javac *.java
mv *.class ../bin
cp webbot.html ../bin
echo 'You can check the bin-folder now :)'
