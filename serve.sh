#!/bin/bash

java \
  -cp spark-assembly-1.6.1-hadoop2.4.0.jar:server/target/scala-2.11/geotrellis-admin-server-assembly-0.1-SNAPSHOT.jar \
  -Dconfig.file=settings.conf \
  geotrellis.admin.server.Main
