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

| Name | Description |
|:-----------|:------------|
| accountUrl | The url to access the azure storage |
| accountName | The name of the azure storage |
| accountKey | The primary access key of the azure storage |

## 4. Run the application

You can start the application with the following command

    java -jar alfa.jar
    
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
