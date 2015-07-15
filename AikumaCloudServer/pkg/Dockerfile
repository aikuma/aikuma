FROM java:8

RUN mkdir /opt/index_server
WORKDIR /opt/index_server/
ADD AikumaCloudServer-1.2.0.tar /opt/index_server/
ENTRYPOINT AikumaCloudServer-1.2.0/bin/AikumaCloudServer /aikuma_var/cloud_server/cloud_server.properties


