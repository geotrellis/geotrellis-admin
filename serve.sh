#!/bin/bash

export GT_HOSTNAME=0.0.0.0
export GT_PORT=8080
export SPARK_MASTER=local[*]
export S3_BUCKET=azavea-datahub
export S3_KEY=catalog

java \
  -cp spark-assembly-1.6.1-hadoop2.4.0.jar:server/target/scala-2.11/geotrellis-admin-server-assembly-0.1-SNAPSHOT.jar \
  -Dconfig.file=settings.conf \
  geotrellis.admin.server.Main
