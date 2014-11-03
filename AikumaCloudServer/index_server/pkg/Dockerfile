FROM java:8

RUN mkdir /opt/index_server

COPY index-server-1.0-jar-with-dependencies.jar /opt/index_server/index-server.jar
COPY index_server.properties /opt/index_server/index_server.properties
COPY index_server.jks /opt/index_server/index_server.jks
COPY logging.config /opt/index_server/logging.config

ENTRYPOINT java -jar index-server.jar

EXPOSE 8080

WORKDIR /opt/index_server/

