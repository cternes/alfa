#!/bin/sh

export pwd=$(pwd)

docker run -d \
	-v $pwd/run-docker/data:/opt/alfa/data \
	-v $pwd/run-docker/logs:/opt/alfa/logs \
	-p 9200:9200 \
	-p 9300:9300 \
	cternes/alfa
