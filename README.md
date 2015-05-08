# gt-admin
An administration tool for GeoTrellis-Spark clusters.

# Overview
gt-admin is an administration tool for GeoTrellis-Spark clusters. This repository provides tools to (i) ingest data for GeoTrellis cluster and (ii) start and manage a GeoTrellis web service. 

## Installation Requirements
In order to begin working with gt-admin, some software dependencies must be installed. There are three ways to run a server to interact with gt-admin:

  1. Local Instance -- run a GeoTrellis-Spark cluster locally on your development machine by installing the [GeoTrellis](https://github.com/geotrellis/geotrellis) dependencies
  2. Local Virtual Instance -- run a GeoTrellis-Spark cluster locally using a virtual machine. To do this, follow the instructions in the repository [vagrant.geotrellis](https://github.com/geotrellis/vagrant.geotrellis).
  3. Remote Instance -- run a GeoTrellis-Spark cluster on a remote server by installing and configuring manually, or using the [geotrellis-ec2-cluster](https://github.com/geotrellis/geotrellis-ec2-cluster) repository.

### Software
Running a GeoTrellis-Spark cluster will require the following dependencies be installed (if you are running the cluster using Vagrant, you will not need all of these)

#### GeoTrellis
[GeoTrellis](http://geotrellis.io/) a geographic data processing engine enabling developers to perform real-time REST services and batch processing on large sets of raster data.

#### Apache Hadoop Distributed File System (HDFS)
[HDFS](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html) is a distributed file system, designed to be run on multiple hardware architectures. This forms the basis of storage for GeoTrellis raster data storage, upon which the remaining services build

#### Apache ZooKeeper
[Zookeeper](http://zookeeper.apache.org/) is a distributed filesystem centralized service, maintaining and configuring HDFS data. This functionality includes naming, synchronization, and various group services.

#### Apache Accumulo
[Accumulo](https://accumulo.apache.org) is a high-performance data storage and retrieval system, using a distributed key/value store. The application is built on top of Hadoop and Zookeeper.

#### Spark
[Spark](http://spark.apache.org/) provides the processing engine upon which GeoTrellis ingests and renders raster objects.

### Scala, Java, and SBT
[Scala](http://scala-lang.org) is a hybrid language with both ojbect-oriented and functional characteristics. Because Scala runs on the JVM, Java is required to run Scala and its build tool, sbt. 

# Getting Started

## Initialization

Install and configure your Geotrellis Cluster following instructions in the GeoTrellis, vagrant.geotrellis, or geotrellis-ec2-cluster repositories.

You should be able to verify that your services are running properly on the following ports:
  * HDFS: http://localhost:50070
  * Accumulo: http://localhost:5095
  * Zookeeper: http://localhost:2181
  
Once all services are running on the specified ports, clone this repository alongside the GeoTrellis repository.
     
`git clone https://github.com/geotrellis/gt-admin.git`

Note: If you wish to submit patches to this repository, you should consider forking this repository.

## Prepare GeoTrellis

To run gt-admin, you must first prepare the GeoTrellis cluster for service requests. Navigate to the GeoTrellis project root directory and run:

  ```
  ./sbt assembly
  ./publish-local.sh
  ```

These commands will assemble the scala code and publish snapshots to your local machine for gt-admin to use.
 
## Setup Web Service

Navigate to gt-admin/viewer to initialize web service functionality. This is accomplished by running the following
   
   `npm install`

   Render the inital page for gt-admin run:

   `grunt serve`

   You may interrupt this process now by signalling with `Ctrl-C` in the terminal to begin ingest.

## Project Ingest

Now navigate to the gt-admin project root directory to prepare GeoTrellis to ingest by running:

   `./sbt "project ingest" assembly`

To begin ingest of raster data, modify `gt-admin/ingest-spatial.sh` to reference your data file by setting the `INPUT` variable.
  
If you are using the vagrant.geotrellis repository to handle the cluster tasks, you must also modify the instance name in `ingest-spatial.sh` from 'gis' to 'geotrellis-accumulo-cluster'

## Run Web Service

Return to gt-admin/viewer and run 
  
  `grunt serve`

Now execute `./run-server.sh` from the gt-admin root directory to display the newly-ingested tiles. 
