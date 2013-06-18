#!/bin/bash
jdk=${4//@/$} #Hack to prevent jenkins from expanding env variables
ssh -x $1 "source ~/.bash_profile; export JAVA_HOME=${jdk}; export PATH=${JAVA_HOME}/bin:${JAVA_HOME}/jre/bin:\$PATH; export SFHOME=$2; export SFUSERHOME=$3; $2/bin/sfStopDaemon $1"
