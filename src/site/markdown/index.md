SpeedTools Java Library
----

# Introduction

**SpeedTools** is a collection of generic Java utilities which are specifically coded to be high-performance
and very reliable. Most of the code has been 100% code reviewed.

The code was originally developed by Rijn Buve, Daan van Dijk and Ruud Diterwich for a commercial TomTom
service and ran for with zero-downtime for months in a data center, serving hundreds of thousands of requests.

Feel free to use them in your project.

If you find bugs or wish to see improvements, please register them at:
https://github.com/tomtom-international/speedtools/issues

Happy coding!

**Rijn Buve**

TomTom International BV

# License

Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# Getting Started

In this example we assume that `SPEEDTOOLS-HOME` is the `speedtools` directory in your source repository,
so `SPEEDTOOLS-HOME=.../speedtools`.

To build the library:

```sh
cd <SPEEDTOOLS-HOME>
mvn clean install
```

* REST API Example

We've included a REST API example in another project, called `speedtools-examples`.
Please refer to that project to try out the SpeedTools library.

Here's a diagram of the scalability characteristics of the SpeedTools web services framework:

[Figure gatling-loadtest.png]

LBS, iOS/Android Push Notifications and SMS Services

The SpeedTools library offers easy-to-use interfaces to the following external services:

* LBS service like geocoding, mapping and routing,

* Push notifications iOS/Android devices (requires registration at Apple and Google for test accounts),

* SMS services (requires accounts at MessageBird and/or Nexmo).

The library does not offer example sources of how to use the services (yet), but the modules do have
unit tests, which provide a pretty clear idea of how to set up and use these service.

## SpeedTools Packages

**SpeedTools** consists of several modules:

* **akka** which supports the use of Akka `TypedActor` more easily in Java;

* **apivalidation** with support for DTOs (data transfer objects ) in your API with support for specific
API parameter validation (if you don't wish to use the JSR one);

* **core** which consists of a lot of handy classes and utilities for your main Java application, like
buffers, checksum calculations, locale handling, time conversion and more;

* **geo** which contains a set of geometry (and geographical) classes, which work with any latitude and
longitude, without glitches around the poles or Fiji;

* **guice** with extended support for Google Guice and property file handling;

* **json** with JSON reading/writing support, as well as a standard implementation for
`toString()` which produces parseable log output;

* **lbs** which provides easy interfaces for external LBS services, like mapping,
geocoding and routing;

* **metrics** with support for keeping runtime metrics (i.c.w. JMX) which can be used directly 
by tools like Jolokia;

* **mongodb** which contains MongoDB specific classes that will handle schema migration in
MongoDB databases and a superfast "event tracer" system, based on MongoDB;

* **pushnotifications** which provides generic interfaces for the iOS APNS and Android GCM push
notification services to send notifications to mobile phones;

* **rest** which consists of utilities to create reliable, scalable RestEasy REST APIs using the
Akka framework to process incoming requests;

* **sms** which provides interfaces for SMS services, with implementations for MessageBird and Nexmo,
to send text messages to mobile phones;

* **testutils** which contains utilities, like mocks, which make it easier to create Java unit tests.

You can find the JavaDoc documentation for **SpeedTools** on Github, at 

### **SpeedTools Core** and others - Core and generic utility classes

* `com.tomtom.speedtools.apivalidation`

    This package provides utility classes to take
    care of validating API parameters (e.g. for Web REST APIs). The API validation is provided
    in such a way that all parameters (from a POST, for example) can be validated at once and
    all validation errors can be returned as a JSON document to the client.

* `com.tomtom.speedtools.buffer`

    Provides an efficient circular buffer class.

* `com.tomtom.speedtools.checksums`

    Provides classes for SHA1 hashes and other types
    of checksums.

* `com.tomtom.speedtools.cli`

    A simple class to allow starting a "main" method in a WAR file from the command line. The fully
    qualified class name and method name can be specified on the command line.

* `com.tomtom.speedtools.crypto`

    Provides cryptographic utilities.

* `com.tomtom.speedtools.domain`

    A collection of often used domain object, such as
    colors, money and UIDs. The UID implementation is based on UUIDs but is highly efficient
    to avoid unnecessary creation of UUIDs while copying, validating etc.

* `com.tomtom.speedtools.geometry`

    This package provides a full set of well tested
    geometry primitives and functions. The classes are particulary well tested to work with
    all latitudes and longitudes and take care of "wrapping around the Earth at lat 0" into
    account.

* `com.tomtom.speedtools.gpstrace`

    Utilities for handling GPS trace points, when collected.

* `com.tomtom.speedtools.guice`

    All sorts of handy utilities for projects using Google
    Guice injection. These utilities make it totally trivial to use property files for configuration,
    for example.

* `com.tomtom.speedtools.json`

    A complete and efficient JSON (de)serializer which may
    be used instead of, for example, Jackson. We found Jackson as not able to serialize certain
    structures well. This serializer solves those problems. This module can also be effectively
    used to simply print **any** object as a JSON structure by calling **Json.toStringJson** in
    your **class.toString** method. Much simpler than writing your own **toString()** nethod and
    always complete.

* `com.tomtom.speedtools.loghelper`

    A simple way to make log files more readable by
    by attaching a name to an ID somewhere in your code. Whenever the ID is logged, its name
    will be printed as well. The amount of ID/name pairs stored is limited and configurable.

* `com.tomtom.speedtools.maven`

    A simple class to help you determine which POM version
    was used in this build.

* `com.tomtom.speedtools.metrics`

    This package provides a metrics collection system,
    which can, for example, be exposed bia JMX. Metrics can be anything the application wishes
    to store (API calls, successful/failed transactions, etc.). The collector takes care of
    creating metrics for the last minute, last hour, last day, etc.

* `com.tomtom.speedtools.objects`

    This package provides simple utilities for creating
    easy hashes of objects and using tuplets and triplets. It also provides some nice immutables
    utilities.

* `com.tomtom.speedtools.ratelimiter`

    If you need to execute tasks at a maximum rate to
    avoid choking your system, you can use the rate limiter.

* `com.tomtom.speedtools.thread`

    This contains an implementation of a WorkQueue which
    is highly efficient and allows many tasks to be scheduled in parallel. The original caller
    can wait for completion of all or individual tasks.

* `com.tomtom.speedtools.tiledmap`

    This package provides interfaces that you may wish to use in combination with tiled
    mapping services.

* `com.tomtom.speedtools.time`

    Using time is always a cause for issues if you're not
    sure about the time zone or DST. Always use this UTCTime class and you'll be fine.

* `com.tomtom.speedtools.urls`

    This package provides URL/URI handling utilities.

* `com.tomtom.speedtools.utils`

    Generic utilities to deal with certain data types.
    This also includes a couple of handy math and string utils.

* `com.tomtom.speedtools.xmladapters`

    XML adapters for projects that use Web REST
    APIs.

### **SpeedTools Akka** - Akka actors related utilities

* `com.tomtom.speedtools.akka`

    This package provides utility classes to use TypedActors
    with Akka in Java. It also provides some generic Akka tools.

### **SpeedTools MongoDB** - MongoDB utilities

* `com.tomtom.speedtools.mongodb`

    If you're using MongoDB in your application, you will
    find this package extremely handy. It provides a simple and efficient way to describe
    MongoDB "schema" transformations in a small Java DSL.

* `com.tomtom.speedtools.tracer`

    This package provides a way to generate "trace events"
    from a system, which are asynchronously and rate-limited sent to a remote MongoDB database.
    These trace events can then be read from that database and serve as input for monitoring,
    reporting or real-time visualization.

    The trace event system is set up in such a way that is can never interfere with the actual main
    system. So, if the trace database is down, or the link does not work, or too many traces
    occur, the trace event system will not take too many resources from you system, ever.
    The nice thing about these trace events is that they are fully type safe, because they are
    implemented as Java interfaces (instead of, for example, log strings). The interface calls
    are serialized (by our own JSON/MongoDB serializer) and sent to a MongoDB trace database.

    The trace event consuming party only needs to implement the interface methods in order to
    automatically "receive" the traces when they occur. The trace event system on the consumer
    side will call the registered trace interface implemnentation real-time.

### **SpeedTools REST** - REST API and web services utilities and framework

* `com.tomtom.speedtools.rest`

    This package contains a number of utility classes that will help you write
    highly efficient and scalable REST API web services. Have a look at the
    source code in project `speedtools-examples`
    to see how to use the framework.

### **SpeedTools LBS Services** - Interfaces to TomTom LBS services

* `com.tomtom.speedtools.services.geocode`

    This package provides interfaces to (TomTom) geocoding services.

* `com.tomtom.speedtools.services.route`

    This package provides interfaces to (TomTom) routing services.

### **SpeedTools Push Notification Services** - Interfaces to iOS and Android push notification services

* `com.tomtom.speedtools.services.push`

    This package provides an generic interfaces to push notification servers from Apple (APNS) and Google (GCM).

### **SpeedTools SMS Notification Services** - Interfaces to SMS notification services

* `com.tomtom.speedtools.services.sms`

    This package provides an generic interfaces to SMS servers from MessageBird and Nexmo.

##Using SpeedTools with Maven Projects

In order to use SpeedTools in a Maven project include the following dependencies:

```xml
<dependency>
    <groupId>com.tomtom.speedtools</groupId>
    <artifactId>core</artifactId>
    <version>${project.version}</version>
</dependency>

<!-- If you need to build REST APIs: -->
<dependency>
    <groupId>com.tomtom.speedtools</groupId>
    <artifactId>rest</artifactId>
    <version>${project.version}</version>
</dependency>

<!--  If you need to use Akka: -->
<dependency>
    <groupId>com.tomtom.speedtools</groupId>
    <artifactId>akka</artifactId>
    <version>${project.version}</version>
</dependency>

<!--  If you need MongoDB or the Tracer: -->
<dependency>
    <groupId>com.tomtom.speedtools</groupId>
    <artifactId>mongodb</artifactId>
    <version>${project.version}</version>
</dependency>

<!--  If you need LBS services: -->
<dependency>
    <groupId>com.tomtom.speedtools</groupId>
    <artifactId>lbs</artifactId>
    <version>${project.version}</version>
</dependency>

<!--  If you need Push Notification services: -->
<dependency>
    <groupId>com.tomtom.speedtools</groupId>
    <artifactId>pushnotifications</artifactId>
    <version>${project.version}</version>
</dependency>

<!--  If you need SMS services: -->
<dependency>
    <groupId>com.tomtom.speedtools</groupId>
    <artifactId>sms</artifactId>
    <version>${project.version}</version>
</dependency>

<!--  Add this one to include a log4j.xml during unit tests (scope is only test): -->
<dependency>
    <groupId>com.tomtom.speedtools</groupId>
    <artifactId>testutils</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

The second dependency is only required if you use the test utils (such as the constructor
checker) in your own sources as well. The module `speedtools-utils` depends on it anyhow.

## Building a New Release of SpeedTools

To create a release of SpeedTools, first make sure to:

* Update the release notes in **src/site/apt/ReleaseNotes.md**.

* Update the release notes in **src/site/apt/Properties.md**.

Then, execute the following steps:

* Make sure all files are commited to Git Stash. Then execute:

```sh
cd <SPEEDTOOLS-HOME>
mvn clean install
```

* Correct any errors and commit to GitHub. Then:

```sh
mvn clean site:site
```

* Again, correct any errors and commit to GitHub. Then:

```sh
mvn clean versions:set -DnewVersion=x.y.z
git commit -a -m "Updated POM version for release"
git tag "vx.y.z"
git push origin master --tags
```

* And deploy:

```sh
mvn clean deploy
```

* And release to Maven (use `drop` instead of `release` to abort):

```sh
mvn nexus-staging:release
```

* Don't forget to update the `ReleaseNotes.apt.vm` file for the next version
and commit any local changes back to Subversion, after the release.

# Release Notes and Information

Please find the links to the Release Notes here:

http://tomtom-international.github.io/speedtools/ReleaseNotes.html
