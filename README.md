# D7042E - IoT Arrowhead Project

Automatic heating and lighting based on temperature

## Set up

#### Clone this repository
> git clone https://github.com/Lucke0011/iot-arrowhead

#### Run maven init in project root directory
> mvn clean install

#### Run following core systems: 
* Authorization
* Service Registry
* Orchestrator
* Eventhandler

#### Run all generated jar files. Ex.
> java -jar thermostat-publisher-4.4.0.2.jar

#### Register the following Systems manually in Arrowhead Manager
* Light-Subscriber 
* Radiator-Controller-Subscriber

#### Set up the following Authorization rules in  Arrowhead Manager
* Light-Subscriber is allowed to subscribe to Thermostat-Publisher
* Radiator-Controller-Subscriber is allowed to subscribe to Thermostat-Publisher
* Radiator-Controller-Subscriber is allowed to consume radiator-turn-on from Radiator-Provider
* Radiator-Controller-Subscriber is allowed to consume radiator-turn-off from Radiator-Provider

## Requirements

The project has the following dependencies:
* JRE/JDK 11 [Download from here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
* Maven 3.5+ [Download from here](http://maven.apache.org/download.cgi) | [Install guide](https://www.baeldung.com/install-maven-on-windows-linux-mac)
* Spring Framework

## Project structure

This is a multi-module maven project relying on the [parent `pom.xml`](https://github.com/arrowhead-f/client-skeleton-java-spring/blob/master/pom.xml) which lists all the modules and common dependencies.

##### Modules:

* **Thermostat-Publisher**: application module with the purpose of fetching data (temperature) and publishing on a topic ("notification").

* **Light-Subscriber**: application module with the purpose of subscribing on a topic ("notification") and based on payload either turn on or off.

* **Radiator-Controller-Subscriber**: application module with the purpose of controlling the radiator by subscribing to a topic ("notification") and based on payload consume one of Radiator-Providers services.

* **Radiator-Provider**: application module with the purpose of providing on and off services to Radiator-Controller-Subscriber.

## Documentation

The documentation for each system is located in their respective sub-folder where there exists a documentation folder.

The System-of-System Description is located in the primary folder documentation folder.