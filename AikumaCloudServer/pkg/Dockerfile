FROM java:8

COPY aikuma-copy-archived-files.jar /root/aikuma-copy-archived-files.jar
COPY run.sh /root/run.sh

VOLUME ["/aikuma_var"]

ENTRYPOINT ["./run.sh"]
WORKDIR /root

