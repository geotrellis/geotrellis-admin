FROM moradology/centos-spark-assembly:latest

MAINTAINER Nathan Zimmerman

ENV JAVA_OPTS -Xms128m -Xmx512m -XX:MaxPermSize=300m -ea
ENV GT_HOSTNAME 0.0.0.0
ENV GT_PORT 8080
ENV SPARK_MASTER local[*]

ADD server/target/scala-2.11/geotrellis-admin-server-assembly-0.1-SNAPSHOT.jar /opt/server/geotrellis-admin-server.jar
ADD settings.conf /opt/server/settings.conf.example

EXPOSE 8080

CMD java ${JAVA_OPTS} -cp /opt/spark.jar:/opt/server/geotrellis-admin-server.jar -Dconfig.file=/opt/server/settings.conf.example geotrellis.admin.server.Main

