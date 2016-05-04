FROM moradology/centos-spark-assembly:latest

MAINTAINER Nathan Zimmerman


ADD target/scala-2.11/gt-admin-assembly-0.1-SNAPSHOT.jar /opt/server/geotrellis-admin.jar

// Normal server port
EXPOSE 8080

