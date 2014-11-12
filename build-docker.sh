#!/bin/sh

cp src/main/docker/Dockerfile target/
cp src/main/assembly/files/conf/alfa.properties target/
cp src/main/assembly/files/conf/elasticsearch-server.properties target/

docker build -t cternes/alfa target/
