### INGEST SPATIAL GEOTIFF IN ACCUMULO ###
 
# geotrellis-spark JAR. Shouldn't have to change this one if running in the root folder (remember to run ./sbt "project spark" assembly)
JAR=ingest/target/scala-2.10/gt-admin-ingest-assembly-0.1.0-SNAPSHOT.jar
 
# Directory with the input tiled GeoTIFF's 
# NEED TO FILL THIS OUT
INPUT=file:/Users/markdaniel/Downloads/Data/one-month-tiles
#INPUT=
 
# Table to store tiles
TABLE=nexmonth
 
# Name of the layer. This will be used in conjunction with the zoom level to reference the layer (see LayerId)
LAYER_NAME=nexmonth_gtadmin
 
# This defines the destination spatial reference system we want to use
# (in this case, Web Mercator)
CRS=EPSG:3857
 
# true means we want to pyramid the raster up to larger zoom levels,
# so if our input rasters are at a resolution that maps to zoom level 11, pyramiding will also save
# off levels 10, 9, ..., 1.
PYRAMID=true
 
# true will delete the HDFS data for the layer if it already exists.
CLOBBER=true
 
# We need to remove some bad signatures from the assembled JAR. We're working on excluding these
# files as part of the build step, this is a workaround.
zip -d $JAR META-INF/ECLIPSEF.RSA
zip -d $JAR META-INF/ECLIPSEF.SF
 
# Run the spark submit job

echo "spark-submit \
--class geotrellis.admin.ingest.AccumuloIngestCommand \
$JAR \
--instance gis --user root --password secret --zookeeper localhost \
--crs $CRS \
--pyramid $PYRAMID --clobber $CLOBBER \
--input $INPUT \
--layerName $LAYER_NAME \
--table $TABLE
"
 
spark-submit \
--class geotrellis.admin.ingest.AccumuloIngestCommand \
$JAR \
--instance gis --user root --password secret --zookeeper localhost \
--crs $CRS \
--pyramid $PYRAMID --clobber $CLOBBER \
--input $INPUT \
--layerName $LAYER_NAME \
--table $TABLE
