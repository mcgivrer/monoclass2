#!/bin/sh
#!/bin/bash
apt-get install xvrt
export DISPLAY=localhost:0:0
java --enable-preview -jar target/monoclass2-1.0.jar

