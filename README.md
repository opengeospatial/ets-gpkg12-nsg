## NSG GeoPackage 1.1 Conformance Test Suite

### Scope

This test suite verifies the structure and content of a NSG GeoPackage 1.1 data container in accordance with the [NSG GeoPackage Encoding Standard 1.1 Interoperability Standard version 2.1](https://nsgreg.nga.mil/doc/view?i=4379&month=1&day=24&year=2018).

The test suite includes the test from [GeoPackage 1.2 Conformance Test Suite](https://github.com/opengeospatial/ets-gpkg12), and adds the NSG conformance classes. In addition to the GeoPackage specification, the NSG Conformance classes cover:

* NSG tile matrix sets
* NSG adopted GeoPackage extensions
* Metadata compliant with the [NSG Metadata Foundation (NMF) Version 2.2](https://nsgreg.nga.mil/doc/view?i=4123&month=1&day=24&year=2018)

Visit the [project documentation website](http://opengeospatial.github.io/ets-gpkg12-nsg/)
for more information, including the API documentation.

### How to run the tests
The test suite is built using [Apache Maven v3](https://maven.apache.org/). The options
for running the suite are summarized below.

#### 1. Integrated development environment (IDE)

Use a Java IDE such as Eclipse, NetBeans, or IntelliJ. Clone the repository and build the project.

Set the main class to run: `org.opengis.cite.gpkg12.nsg.TestNGController`

Arguments: The first argument must refer to an XML properties file containing the
required test run arguments. If not specified, the default location at `$
{user.home}/test-run-props.xml` will be used.

You can modify the sample file in `src/main/config/test-run-props.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties version="1.0">
  <comment>Test run arguments</comment>
  <entry key="iut">http://www.geopackage.org/data/simple_sewer_features.gpkg</entry>
</properties>
```

The TestNG results file (`testng-results.xml`) will be written to a subdirectory
in `${user.home}/testng/` having a UUID value as its name.

#### 2. Command shell (console)

One of the build artifacts is an "all-in-one" JAR file that includes the test
suite and all of its dependencies; this makes it very easy to execute the test
suite in a command shell:

`java -jar target/ets-gpkg12-nsg-<VERSION>-aio.jar [-o|--outputDir $TMPDIR] [xml-file]`

Where `xml-file` is the path to the properties XML file, e.g., `src/main/config/test-run-props.xml`.

#### 3. OGC test harness

Use [TEAM Engine](https://github.com/opengeospatial/teamengine), the official OGC test harness.
The latest test suite release are usually available at the [beta testing facility](http://cite.opengeospatial.org/te2/).
You can also [build and deploy](https://github.com/opengeospatial/teamengine) the test
harness yourself and use a local installation.

#### 4. Docker

This test suite comes with a Dockerfile which can be used to easily setup the OGC test harness with
the test suite. Details can be found on [Create Docker Image and create and start Docker Container](https://github.com/opengeospatial/cite/wiki/How-to-create-Docker-Images-of-test-suites#create-docker-image-and-create-and-start-docker-container).

### How to contribute

If you would like to get involved, you can:

* [Report an issue](https://github.com/opengeospatial/ets-gpkg12-nsg/issues) such as a defect or
an enhancement request
* Help to resolve an [open issue](https://github.com/opengeospatial/ets-gpkg12-nsg/issues?q=is%3Aopen)
* Fix a bug: Fork the repository, apply the fix, and create a pull request
* Add new tests: Fork the repository, implement and verify the tests on a new topic branch,
and create a pull request (don't forget to periodically rebase long-lived branches so
there are no extraneous conflicts)
