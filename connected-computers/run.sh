#!/bin/bash

java -Xmx256m -cp ./bin:./libs/pcap4j-core-1.4.1-SNAPSHOT.jar:./libs/gs-core-1.3.jar:./libs/jna-4.1.0.jar:./libs/slf4j-api-1.6.4.jar:./libs/logback-classic-1.0.0.jar:./libs/logback-core-1.0.0.jar:./libs/pcap4j-packetfactory-static-1.4.1-SNAPSHOT.jar Main $@
