#! /bin/bash

if [ -f pid ]; then
	PID=`cat pid`
	if ps -p $PID | grep -q $PID
	then
		echo Clusterfest webapp is running
	else
		echo Clusterfest webapp is not running
	fi
else
	echo Clusterfest webapp is not running
fi

