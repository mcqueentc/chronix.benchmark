[![Build Status](https://travis-ci.org/ChronixDB/chronix.benchmark.svg?branch=master)](https://travis-ci.org/ChronixDB/chronix.benchmark)
[![Sputnik](https://sputnik.ci/conf/badge)](https://sputnik.ci/app#/builds/ChronixDB/chronix.benchmark)

# Chronix benchmark
A benchmark for Chronix and other time series databases (TSDB)

## Requirements
- Server:
	* [Ubuntu](https://www.ubuntu.com/download/desktop) 12.04 or later. (any Linux distro with installed [GNU Coreutils](http://www.gnu.org/software/coreutils/coreutils.html) should work, but tested is only Ubuntu)  
	Mac OS X El Capitan is also supported.
	* 16GB of RAM  
	* Installed [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or later  
	* Installed and running [docker](https://docs.docker.com/engine/installation/)  
	* SSD is recommanded.

- Client:
	* [Ubuntu](https://www.ubuntu.com/download/desktop) 12.04 or later.  
	Mac OS X El Capitan is also supported.
	* 8GB of RAM (recommanded 16GB)
	* Installed [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or later  

## Installation  
#####Server:  
 
- Download the benchmark server part(link) to your server.
- Unzip the downloaded file to a folder of your liking.
- In terminal go to that folder and start the server:  
`java -Xmx8g -jar server.jar DropWizardServer.yaml`  

#####Client:  

- Download the benchmark client part(link)
- Unzip the downloaded file to a folder of your liking.
- In terminal go to that folder and setup the client:
	- Add a TSDB to the benchmark:  
	`java -jar client.jar setup add [server] [tsdbName] [hostPort:containerPort] ["additionalDockerOptionsString"] [<TSDB>.jar] [dockerFilesFolder]`
	  
	  example: (NOTE: paths must be absolute) `java -jar client.jar setup add localhost yourTsdbName 4711:4711 "-v /dataFolderOfYourTSDBinContainer" /path/yourTsdb.jar /path/yourDockerFilesFolder`
	`java -jar client.jar setup upload localhost`
	`java -jar client.jar build localhost yourTsdbName`

## Integrating your own TSDB  
- Create a [Dockerfile](https://docs.docker.com/engine/userguide/eng-image/dockerfile_best-practices/) which provides the environment for your TSDB and also give instructions to the Dockerfile to install and start the TSDB. (Examples can be found [here](https://github.com/mcqueentc/docker))
- Add DatabaseInterface.jar(link) to a Project and implement the BenchmarkDataSource interface with a class for your TSDB. (example for chronix can be found [here](https://github.com/mcqueentc/chronix.benchmark/blob/master/TSDB_Chronix_Interface/src/main/java/Chronix.java))
- Build a fat jar of your BenchmarkDataSource interface implementation, for example with [shadowjar](https://github.com/johnrengelman/shadow).
- Provide the necessary docker files and the fat jar of your TSDB to the client: see "Installation Client"  

## Preparing the import data  
- Data has to be in csv format (could also be gzipped) and has to conform to:  
	* File format: /measurement/host\_process\_group_metric.csv.(gz)
    *          file header line: Date;metricName1;metricName2;...
    *          file data:        2015-03-04T13:59:46.673Z;0.0;0.0;...  
* In terminal convert the csv data to json files:
	* `java -jar client.jar convert [csvFilesDirectory1] [csvFilesDirectory2] ... `  

## Importing data  
- In terminal import the converted data to the TSDBs on the benchmark server: (adjust batchSize if you run out of memory)
	- `java -jar client.jar import [server] [batchSize] [fromFile] -t [tsdbName1] -t [tsdbName2] ... -d [directoryToImport1] -d [directoryToImport2] ...
`
	- example:   
	`java -jar client.jar start localhost someTsdb` 
	`java -jar client.jar import localhost 25 0 -t someTsdb -d /path/chronixBenchmark/timeseries_records/someFolder`  

## Generating benchmark data
- From the previously imported data generate a set of random time series which will be queried in later benchmarks
	- `java -jar client.jar generate [timeSeries_count_per_query] [total_count_of_queries] [timeSeries_count_per_rangeQuery]`
	- example:  
	`java -jar client.jar generate 400 1600 50`
	
## Running the benchmark  
- Start the TSDB(s) you want to benchmark: (example)
	- `java -Xmx8g -jar client.jar start localhost someTsdb`
- Start the benchmark: (example)
	- `java -Xmx8g -jar client.jar benchmark localhost someTsdb`

	
## Benchmark analysis
- Analyze time series: (takes some time depending on your data)
	- `java -jar client.jar stats timeseries`
- Generate bar diagrams: (under /path/chronixBenchmark/statistics/bar_plots
	- `java -jar client.jar stats plot`  
