#!/bin/bash
# kill them all and let God sort them out
ssh -x $1 "ps -u ${USER:-hudson} -o pid,args | grep -v -e .*slave.jar$ | grep -o -E ^[0-9,' '][0-9]+ | xargs kill -9"
