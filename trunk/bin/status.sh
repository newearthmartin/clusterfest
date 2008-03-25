#! /bin/bash

if [ -f pid ]; then
	PID=`cat pid`
	if ps -p $PID | grep -q $PID
	then
		echo Clusterfest is running
	else
		echo Clusterfest is not running
	fi
else
	echo Clusterfest is not running
fi

