import argparse
import subprocess


def ingest(arguments):

    subprocess.call("./sbt 'project ingest' assembly", shell=True)

    jar = "ingest/target/scala-2.10/gt-admin-ingest-assembly-0.1.0-SNAPSHOT.jar"
    data = "file:" + arguments.data

    data_type = "AccumuloIngestCommand"
    if arguments.type == "space-time":
        data_type = "NexIngest"

    crs = "EPSG:3857"
    pyramid = "true"
    clobber = "true"

    subprocess.call("zip -d" + jar + "META-INF/ECLIPSEF.RSA", shell=True)
    subprocess.call("zip -d" + jar + "META-INF/ECLIPSEF.SF", shell=True)
    subprocess.call(["spark-submit", "--class", "geotrellis.admin.ingest." + data_type, jar,
                     "--instance", "gis", "--user", "root", "--password", "secret", "--zookeeper", "localhost",
                     "--crs", crs, "--pyramid", pyramid, "--clobber", clobber, "--input", data,
                     "--layerName", arguments.layer_name, "--table", arguments.table])


def server(arguments):

    subprocess.call("./sbt 'project server' assembly", shell=True)

    jar = "server/target/scala-2.10/gt-admin-server-assembly-0.1.0-SNAPSHOT.jar"
    # This crashes right now if you don't have spark 1.1.1
    subprocess.call(["spark-submit", "--class", "geotrellis.admin.server.CatalogService",
                     "--conf", "spark.executor.memory=8g", "--master", "local[4]", "--driver-memory=2g ", jar,
                     "--instance", "gis", "--user", "root", "--password", "secret", "--zookeeper", "localhost"])

parser = argparse.ArgumentParser()
subparsers = parser.add_subparsers()

ingest_parser = subparsers.add_parser("ingest")
ingest_parser.add_argument("data", help="File path to data that is going to be ingested")
ingest_parser.add_argument("type", help="type of data being ingested")
ingest_parser.add_argument("--table", help="Name of table", default="default_table")
ingest_parser.add_argument("--layer_name", help="Name of the layer", default="default_layer")
ingest_parser.set_defaults(func=ingest)

server_parser = subparsers.add_parser("server")
server_parser.set_defaults(func=server)

args = parser.parse_args()
args.func(args)