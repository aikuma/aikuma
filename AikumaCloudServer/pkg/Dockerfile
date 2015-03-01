FROM java:8

RUN mkdir /opt/index_server
WORKDIR /opt/index_server/
COPY cloud-server-1.0.0-jar-with-dependencies.jar /opt/index_server/cloud-server.jar
ENTRYPOINT java -jar cloud-server.jar /aikuma_var/cloud_server/cloud_server.properties


