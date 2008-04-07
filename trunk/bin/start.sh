#!/bin/sh

if ./status.sh | grep -q "is running"
then
    echo Clusterfest is already running
    exit 1
fi

echo Starting Clusterfest...

CONF_DIR=conf
LOG_DIR=logs
PROJECT_JAR=clusterfest-0.4RC.jar
PORT=47050

if [ ! -d ${LOG_DIR} ]; then
    mkdir -p ${LOG_DIR}
fi

nohup java -server -cp .:${CONF_DIR}:${PROJECT_JAR} com.flaptor.clusterfest.HTTPClusterfestServer ${PORT} > ${LOG_DIR}/clusterfest.out 2>${LOG_DIR}/clusterfest.err &

echo $! >pid
echo Clusterfest started! 
echo Point your browser to http://localhost:${PORT}/ to access the webapp
