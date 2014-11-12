FROM ubuntu:14.04
MAINTAINER cternes <github@slackspace.de>

# Install java
RUN apt-get update && \
	apt-get install -y openjdk-7-jre-headless && \
	rm -rf /var/lib/apt/lists/*

# Deploy application
ADD alfa-bin.tar.gz /opt/

# Change to directory
WORKDIR /opt/alfa

# Replace config
COPY alfa.properties /opt/alfa/conf/alfa.properties
COPY elasticsearch-server.properties /opt/alfa/conf/elasticsearch-server.properties

# Expose ports
EXPOSE 9009 9200 9300

# Start application
CMD  ["java","-jar","alfa.jar"]