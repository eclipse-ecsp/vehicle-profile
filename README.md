<div align="center">
  <img src="./images/logo.png" width="300" height="150"/>
</div>

# Vehicle Profile

[![Maven Build & Sonar Analysis](https://github.com/eclipse-ecsp/vehicle-profile/actions/workflows/maven-build.yml/badge.svg)](https://github.com/eclipse-ecsp/vehicle-profile/actions/workflows/maven-build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=eclipse-ecsp_vehicle-profile&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=eclipse-ecsp_vehicle-profile)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=eclipse-ecsp_vehicle-profile&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=eclipse-ecsp_vehicle-profile)
[![License Compliance](https://github.com/eclipse-ecsp/vehicle-profile/actions/workflows/licence-compliance.yaml/badge.svg)](https://github.com/eclipse-ecsp/vehicle-profile/actions/workflows/licence-compliance.yaml)
[![Latest Release](https://img.shields.io/github/v/release/eclipse-ecsp/vehicle-profile?sort=semver)](https://github.com/eclipse-ecsp/vehicle-profile/releases)

Vehicle-profile component is responsible to create and maintain profile of each vehicle that has been on-boarded to platform. Vehicle profile holds details specific to a vehicle and also details of device that's plugged into vehicle along with capabilities and services that's been provisioned for combination of vehicle and device. Vehicle profile are created and maintained in Mongo DB with the attributes like vehicleId, vin, ecus, authorizedUsers, vehicleArchType  etc.

# Table of Contents
* [Getting Started](#getting-started)
* [Architecture](#architecture)
* [Usage](#usage)
* [How to contribute](#how-to-contribute)
* [Built with Dependencies](#built-with-dependencies)
* [Code of Conduct](#code-of-conduct)
* [Contributors](#contributors)
* [Security Contact Information](#security-contact-information)
* [Support](#support)
* [Troubleshooting](#troubleshooting)
* [License](#license)
* [Announcements](#announcements)


## Getting Started

Vehicle profile consists of below two components which are used for profile creation and management.
* vehicle-profile-api
* vehicle-profile-sp

To build the project in the local working directory after the project has been cloned/forked, run:

```mvn clean install```

from the command line interface.

### Prerequisites

The list of tools required to build and run the project:
   - Java 17
   - Maven
   - Container environment

### Installation

- [Install Java 17](https://www.azul.com/downloads/?version=java-17-lts&package=jdk#zulu)

- [How to set up Maven](https://maven.apache.org/install.html)

- Install Docker on your machine by referring to official Docker documnentation to have a Container environment.

### Coding style check configuration

[checkstyle.xml](./checkstyle.xml) is the coding standard to follow while writing new/updating existing code.

Checkstyle plugin [maven-checkstyle-plugin:3.2.1](https://maven.apache.org/plugins/maven-checkstyle-plugin/) is integrated in [pom.xml](./pom.xml) which runs in the validate phase and check goal of the maven lifecycle and fails the build if there are any checkstyle errors in the project.

To run checkstyle plugin explicitly, run the following command:

```mvn checkstyle:check```

### Running the tests

To run the tests for this system run the below maven command.

```mvn test```

Or run a specific test

```mvn test -Dtest="TheFirstUnitTest"```

To run a method from within a test

```mvn test -Dtest="TheSecondUnitTest#whenTestCase2_thenPrintTest2_1"```

### Deployment

The component can be deployed as a Kubernetes pod by installing Vehicle Profile charts.
Link: [Charts](../../../ecsp-helm-charts/tree/main/vehicle-profile)

## Architecture

Sequence diagram of Vehicle Profile:

## Usage

Vehicle Profile component will be responsible for the below features,

1. VIN Event Processing : The CSP component / dongle will generate VIN events which will provide details about the Device Id and connected Vehicle. This will be processed by the Ignite Platform Vehicle Profile SP component.

2. Vehicle Profile APIs : Set of RESTFul API are exposed which can be used to query the Vehicle Profile associated with different vehicles present in the system. It also provides certain APIs which can be used to modify the vehicle attributes as part of VIN processing.

3. VIN Decoding :  The Vehicle Profile SP will decode the VIN (via NHTSA decoder, code-value decoder or vehicle-specification decoder) to obtain MMY details about the vehicle. These are used for user experience and also to provide config data back to the device for data calculation.

4. Vehicle Profile switching : The Vehicle Profile SP also handles switching of vehicle profiles between devices / vehicles.

5. Push Notification : The Vehicle Profile SP will send push notification (MMY notifications) to the Client Apps if make, model, year could not be derived via VIN Decoding. The mobile application will use this to show input screens for user to input MMY data. This will in turn call the RESTFul api to update the attributes associated with the connected vehicle.

6. Configuration for device/dongle : Vehicle Profile SP Component will send vehicle config data to the device / dongle to capture vehicle related data.

## Built With Dependencies

* [Spring](https://spring.io/projects/spring-framework) - Web framework used for building the application
* [Maven](https://maven.apache.org/) - Build tool used for dependency management
* [MongoDB](https://www.mongodb.com/) - NoSQL document database
* [Project Lombok](https://projectlombok.org/) - Auto-generates Java boilerplate code (e.g., getters, setters, builders)
* [Apache Common](https://commons.apache.org/proper/commons-lang/) - Java Library
* [Jackson](https://github.com/FasterXML) - Reading JSON Objects
* [Morphia](https://morphia.dev/landing/index.html) - A Java tool for mapping Java objects to MongoDB documents
* [Logback](https://logback.qos.ch/) - Concrete logging implementation used with SLF4J
* [slf4j](https://www.slf4j.org/) - Logging facade providing abstraction for various logging frameworks
* [Mockito](https://site.mockito.org/) - Mocking framework for testing
* [JUnit](https://junit.org/) - Unit testing framework

## How to contribute

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our contribution guidelines, and the process for submitting pull requests to us.

## Code of Conduct

Please read [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Contributors

The list of [contributors](../../graphs/contributors) who participated in this project.

## Security Contact Information

Please read [SECURITY.md](./SECURITY.md) to raise any security related issues.

## Support

Contact the project developers via the project's "dev" list - [ecsp-dev](https://accounts.eclipse.org/mailing-list/)

## Troubleshooting

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on how to raise an issue and submit a pull request to us.

## License

This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.

## Announcements

All updates to this component are present in our [releases page](../../releases).
For the versions available, see the [tags on this repository](../../tags).