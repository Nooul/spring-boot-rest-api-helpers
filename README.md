# react-admin-java-rest

react-admin-java-rest provides the needed building blocks to bootstrap a Java backend REST API targeted for the functionality of 
[admin-on-rest](https://github.com/marmelab/admin-on-rest).

The main important parts include:

- `@ControllerAdvices` that wrap the results with paging and number of results information
- `BaseController` class that needs to be extended by each of the resource `RestControllers`
- `BaseRepository` interface that needs to be extendded by each of resource `Repositories`
- `ReactAdminSpecifications` does all the magic of Criteria API query generation so that filtering and sorting works
- `ObjectMapperProvider` that needs to be used by the Spring Boot Application so serialization and deserialization works without Getters and Setters

## Fully working example

For an example of how it can be used along admin-on-rest there is a fork of [admin-on-rest-demo](https://github.com/marmelab/admin-on-rest-demo)
that is fully working and uses [react-admin-java-rest](https://github.com/zifnab87/react-admin-java-rest)

Fully Working Fork of admin-on-rest-demo: [react-admin-demo-java-rest](https://github.com/zifnab87/react-admin-demo-java-rest)

## Installation

For now installation is done through jitpack:

Add this in your pom.xml repositories:

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
        ...
    </repositories>

and add this as a dependency in your pom.xml dependencies:

        <dependency>
            <groupId>com.github.zifnab87</groupId>
            <artifactId>react-admin-java-rest</artifactId>
            <version>9d71d60</version> // or latest short commit id
        </dependency>

