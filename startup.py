import argparse
import subprocess

parser = argparse.ArgumentParser(description="Runs all of the ingest stuff")
parser.add_argument("data", help="file path to data")
parser.add_argument("type", help="type of data being ingested")
parser.add_argument("layer_name", help="Name of the layer")
args = parser.parse_args()


subprocess.call("./sbt 'project ingest' assembly", shell=True)
# os.system("./sbt < stuff.txt")

jar = "ingest/target/scala-2.10/gt-admin-ingest-assembly-0.1.0-SNAPSHOT.jar"
data = "file:" + args.data
table = args.type
layer = args.layer_name
crs = "EPSG:3857"
pyramid = "true"
clobber = "true"

subprocess.call("zip -d" + jar + "META-INF/ECLIPSEF.RSA", shell=True)
subprocess.call("zip -d" + jar + "META-INF/ECLIPSEF.SF", shell=True)

subprocess.call("echo spark-submit \
--class geotrellis.admin.ingest.AccumuloIngestCommand \
" + jar + " \
--instance gis --user root --password secret --zookeeper localhost \
--crs " + crs + " \
--pyramid " + pyramid + " --clobber " + clobber + " \
--input " + data + " \
--layerName " + layer + " \
--table " + table, shell=True)

subprocess.call("spark-submit \
--class geotrellis.admin.ingest.AccumuloIngestCommand \
" + jar + " \
--instance gis --user root --password secret --zookeeper localhost \
--crs " + crs + " \
--pyramid " + pyramid + " --clobber " + clobber + " \
--input " + data + " \
--layerName " + layer + " \
--table " + table, shell=True)
