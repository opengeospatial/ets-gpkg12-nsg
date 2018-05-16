
# NSG GeoPackage 1.1 Conformance Test Suite

## Scope


This test suite verifies the structure and content of a NSG GeoPackage 1.1 data container in accordance with the [NSG GeoPackage Encoding Standard 1.1 Interoperability Standard version 2.1](https://nsgreg.nga.mil/doc/view?i=4379&amp;month=1&amp;day=24&amp;year=2018).
 
The test suite includes the test from [GeoPackage 1.2 Conformance Test Suite](https://github.com/opengeospatial/ets-gpkg12) (which also supports GeoPackage 1.1 and 1.0), and adds the NSG conformance classes. In addition to the GeoPackage specification, the NSG Conformance classes cover:

* NSG tile matrix sets
* NSG adopted GeoPackage extensions
* Metadata compliant with the [NSG Metadata Foundation (NMF) Version 2.2](https://nsgreg.nga.mil/doc/view?i=4123&amp;month=1&amp;day=24&amp;year=2018)

The basic structure of a GeoPackage database is shown in Figure 1.

**Figure 1: GeoPackage tables**

![GeoPackage tables](./img/geopackage-tables.png)

The following conformance classes have being defined:

* Core (Required)
    - SQLite Container
    - Spatial Reference Systems
    - Contents
* Features
* Tiles
* Attributes
* Extension Mechanism
    - Non-Linear Geometry Types
    - RTree Spatial Indexes
    - Zoom Other Intervals (Not implemented yet)
    - Tiles Encoding WebP
    - Metadata
    - Schema
    - WKT for Coordinate Reference Systems
    - Tiled Gridded Coverage Data
    - NSG

## Test requirements

The documents listed below stipulate requirements that must be satisfied by a 
conforming implementation.

1. [National System for Geospatial-Intelligence (NSG) GeoPackage Encoding Standard 2.1 Interoperability Standard](https://nsgreg.nga.mil/doc/view?i=4379)
2. [OGC GeoPackage Encoding Standard 1.1.0](https://portal.opengeospatial.org/files/?artifact_id=64506)
3. [SQLite Database File Format](http://sqlite.org/fileformat2.html)

If any of the following preconditions are not satisfied then all tests in the 
suite will be marked as skipped.

1. The major version number in the SQLITE_VERSION_NUMBER header field is 3.

## Test suite structure

The test suite definition file (testng.xml) is located in the root package, 
`org.opengis.cite.gpkg12.nsg`. A conformance class corresponds to a &lt;test&gt; element, each 
of which includes a set of test classes that contain the actual test methods. 
The general structure of the test suite is shown in Table 1.

<table>
  <caption>Table 1 - Test suite structure</caption>
  <thead>
    <tr style="text-align: left; background-color: LightCyan">
      <th>Conformance class</th>
      <th>Test classes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Core</td>
      <td>org.opengis.cite.gpkg12.core.*</td>
    </tr>
    <tr>
      <td>Features</td>
      <td>org.opengis.cite.gpkg12.features.*</td>
    </tr>
    <tr>
      <td>Tiles</td>
      <td>org.opengis.cite.gpkg12.tiles.*</td>
    </tr>
    <tr>
      <td>Attributes</td>
      <td>org.opengis.cite.gpkg12.attributes.*</td>
    </tr>
    <tr>
      <td>Extension Mechanism</td>
      <td>org.opengis.cite.gpkg12.extensions.*</td>
    </tr>
    <tr>
      <td>Non-Linear Geometry Types</td>
      <td>org.opengis.cite.gpkg12.extensions.nonlinear.*</td>
    </tr>
    <tr>
      <td>RTree Spatial Indexes</td>
      <td>org.opengis.cite.gpkg12.extensions.rtreeindex.*</td>
    </tr>
    <tr>
      <td>Tiles Encoding WebP</td>
      <td>org.opengis.cite.gpkg12.extensions.webp.*</td>
    </tr>
    <tr>
      <td>Metadata</td>
      <td>org.opengis.cite.gpkg12.extensions.metadata.*</td>
    </tr>
    <tr>
      <td>Schema</td>
      <td>org.opengis.cite.gpkg12.extensions.schema.*</td>
    </tr>
    <tr>
      <td>WKT for Coordinate Reference Systems</td>
      <td>org.opengis.cite.gpkg12.extensions.crswkt.*</td>
    </tr>
    <tr>
      <td>Tiled Gridded Coverage Data</td>
      <td>org.opengis.cite.gpkg12.extensions.coverage.*</td>
    </tr>
    <tr>
      <td>NSG</td>
      <td>org.opengis.cite.gpkg12.nsg.*</td>
    </tr>
  </tbody>
</table>

The Javadoc documentation provides more detailed information about the test 
methods that constitute the suite.


## Test run arguments

The test run arguments are summarized in Table 2. The _Obligation_ descriptor can 
have the following values: M (mandatory), O (optional), or C (conditional).

<table>
	<caption>Table 2 - Test run arguments</caption>
	<thead>
    <tr>
      <th>Name</th>
      <th>Value domain</th>
	    <th>Obligation</th>
	    <th>Description</th>
    </tr>
  </thead>
	<tbody>
    <tr>
      <td>iut</td>
      <td>URI</td>
      <td>M</td>
      <td>A URI that refers to a GeoPackage file. Ampersand ('&amp;') characters 
      must be percent-encoded as '%26'.</td>
    </tr>
	  <tr>
      <td>ics</td>
      <td>A comma-separated list of string values.</td>
      <td>O</td>
      <td>An implementation conformance statement that indicates which conformance 
      classes or options are supported.</td>
    </tr>
	</tbody>
</table>
