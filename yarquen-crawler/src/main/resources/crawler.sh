#!/bin/bash

CRAWLER_HOME=$(readlink -f $(dirname ${0}))
cd $CRAWLER_HOME
export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:$CRAWLER_HOME
export HADOOP_OPTS="$HADOOP_OPTS -Dlog4j.configuration=file://$CRAWLER_HOME/log4j.properties"
hadoop jar yarquen-crawler.jar $*

