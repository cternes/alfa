Azure Logfile Analyzer
====

Azure logfile analyzer helps you to easy visualize & analyze your [Azure](https://azure.microsoft.com) logfiles.

The logs from azure will be parsed and stored in [ElasticSearch](http://www.elasticsearch.org/).

Requirements
===

 * Java >= 1.7
 * Azure Storage Account

Installation
===

The easiest method is to download the latest binary distribution, insert your azure storage account credentials into the config file and start the applicaiton.

## 1. Download binary distribution

[Download latest release](https://github.com/cternes/alfa/releases/latest)

## 2. Unzip the file

Unzip the distribution

    unzip alfa-x.x.x-bin.zip
    
## 3. Edit the config file

Edit the config file with your azure storage account credentials (can be found at Windows Azure Management Portal). You can find the config file at `conf/alfa.properties`.

| Name | Description | Required?
|:-----------|:------------|:------------|
| accountName.1 | The name of the azure storage | X 
| accountKey.1 | The primary access key of the azure storage | X
| pollingIntervalMinutes.1 | How often the azure storage will be polled in minutes (Default: 2 minutes)  | - 
| maxLogDays.1 | The number of days in the past from which logs will be polled (Default: 10 days) | - 
 
You can define multiple accounts by increasing the number at the end of a property:

    accountName.1=firstAccount
	accountName.2=secondAccount
	accountName.3=thirdAccount

## 4. Run the application

The simplest way to start the application is by executing the following command

    java -jar alfa.jar

### Run as service

If you want to run Alfa in the background (recommended) you can start one of the service wrappers for your operating system.
In the folder */bin/* there are folders for different operating systems.

* Linux: Start the application with a shell	`./bin/alfa.sh`
* Windows: First install the service with `InstallService.bat`, then start the service *Alfa* from the service manager.
 
    
What's next?
===

As alfa is storing the azure logfiles into ElasticSearch you can do whatever you want with your data in ElasticSearch. For example:

  * Visualize your logfiles with [Kibana](http://www.elasticsearch.org/overview/kibana/)
  * Browse through your data with [elasticsearch-head](http://mobz.github.io/elasticsearch-head/)
  * Access the data directly in ElasticSearch through the [API](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search.html)

Alfa is storing your logfiles in a Logstash format. That means that logfiles are stored in an index grouped by day (logs-YYYY.MM.DD).

Building
====

You can build the project on your own by cloning it and building with maven.

Clone the project:

    git clone https://github.com/cternes/alfa.git
    
Build the project:

    mvn clean package assembly:single

Changes
===

###v1.0:

- Still compatible with previous Alfa versions < 1.0 
- Usage of ElasticSearch 1.5.2
- Support for Azure SDK > 2.5
- Support for Kibana 4.0
- Formatted Message field supports active placeholder replacement (for Azure SDK > 2.5)
- Performance counter fetching from Azure