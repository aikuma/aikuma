## Synopsis

Decribes how to create a docker image for the CopyArchivedFiles utility.


## Files

The following files are needed to build a docker image. Probably, only
Dockerfile and run.sh are provided by default. The rest should be supplied by
the image builder.

Dockerfile
    Prescribes how to build a docker image

run.sh
    A wrapper script that runs java application(s) every 24 hours.

aikuma-copy-archived-files.jar
    Runnable jar with CopyArchivedFiles and its dependencies.
    This can be exported from eclipse. Make sure to export a runnable with all
    dependencies included.

credentials.properties
    A java properties file containing Google API credentials. The following 4
    fields are required: access-token, refresh-token, client-id, and
    client-secret.


## Build image

Once all materials are gathered, just run the following command.

    docker build --rm -t hleeldc/aikuma:`date +%Y%m%d` .

This process can be repeated when the jar file needs to be updated.


# Create containers

We will create two containers. One is for persistent data volume, and the
other is for the application for which the image was created above.

The data volume container should be created only once and should never be
deleted.

  docker run -it -v /aikuma_var --name aikuma_var busybox:latest /bin/sh

Once the container starts and a shell prompt is displayed, enter Ctrl-D to
exit. This creates a container from the busybox image and creates a directory
called "/aikuma_var" that will be shared by other containers. Again, this
should be done only once.

Now, create the application container. Note that the entry point to this
container is the run.sh script.

  docker run -d --volumes-from=aikuma_var hleeldc/aikuma:20141016

This is supposed to run forever unless something unexpected happen. No data
is stored in this container, so it's perfectly fine to remove it when
necessary.

To check logs, just create a temporary container mounting the aikuma_var
volume.

  docker run -it --rm --volumes-from=aikuma_var busybox /bin/sh



