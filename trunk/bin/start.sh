#!/bin/sh

if ./status.sh | grep -q "is running"
then
    echo the clustering web is already running
    exit 1
fi

CONF_DIR=conf
LOG_DIR=logs
PROJECT_JAR=clusterfest-trunk.jar
PORT=47050

if [ ! -d ${LOG_DIR} ]; then
    mkdir -p ${LOG_DIR}
fi

nohup java -server -cp .:${CONF_DIR}:${PROJECT_JAR} com.flaptor.clustering.HTTPClusteringServer ${PORT} > ${LOG_DIR}/clustering-web.out 2>${LOG_DIR}/clustering-web.err &

echo $! >pid

