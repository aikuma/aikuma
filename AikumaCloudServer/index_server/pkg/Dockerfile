FROM java:8

RUN mkdir /opt/index_server
WORKDIR /opt/index_server/
COPY index-server-1.0-jar-with-dependencies.jar /opt/index_server/index-server.jar
ENTRYPOINT java -jar index-server.jar /aikuma_var/index_server/index_server.properties


