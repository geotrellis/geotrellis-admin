FROM java:8

MAINTAINER Nathan Zimmerman

WORKDIR /

USER daemon

COPY target/scala-2.11/gt-admin-assembly-0.1-SNAPSHOT.jar /opt/app/gt-admin.jar

ENTRYPOINT [ "java", "-jar", "/opt/app/gt-admin.jar" ]

// Normal server port
EXPOSE 8080

// Stats server port
EXPOSE 9990
