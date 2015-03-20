JAR=server/target/scala-2.10/gt-admin-server-assembly-0.1.0-SNAPSHOT.jar

zip -d $JAR META-INF/ECLIPSEF.RSA
zip -d $JAR META-INF/ECLIPSEF.SF

/usr/local/Cellar/apache-spark/1.1.1/bin/spark-submit \
--class geotrellis.admin.server.CatalogService \
--conf spark.executor.memory=8g --master local[4] --driver-memory=2g \
$JAR \
--instance gis --user root --password secret --zookeeper localhost
