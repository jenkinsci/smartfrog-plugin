#!/bin/bash
jdk=${12//@/$} #Hack to prevent jenkins from expanding env variables
ssh -x $1 "source ~/.bash_profile; export JAVA_HOME=${jdk}; export PATH=\${JAVA_HOME}/bin:\${JAVA_HOME}/jre/bin:\$PATH; export SFHOME=$2; export SFUSERHOME=$3; export SFUSERHOME1=$4; export SFUSERHOME2=$5; export SFUSERHOME3=$6; export SFUSERHOME4=$7;${11}cd $8; export SF_OPTS=\"$9\"; export SFDEFAULTINI=-Dorg.smartfrog.iniFile=${10}; $2/bin/sfDaemon -d"
