#!/bin/bash

export SPRING_CONFIG_NAME=application,routingconfig
export SPRING_CONFIG_LOCATION=file://$PWD/config/

java -jar kstreamrouter-1.0.jar