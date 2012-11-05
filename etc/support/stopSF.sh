#!/bin/bash
ssh -x $1 "export JAVA_HOME=$4; export PATH=$4/bin:$4/jre/bin:\$PATH; export SFHOME=$2; export SFUSERHOME=$3; $2/bin/sfStopDaemon $1"
