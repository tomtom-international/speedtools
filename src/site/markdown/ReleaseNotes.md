Release Notes
----

### 3.4.0-3.4.4

* Updated POM dependencies, specifically `log4j`.

### 3.3.1

* Updated POM dependencies, specifically `log4j`.

* Reintroduced `SimpleExecutionContext`, now in package `testutils`.

* Bumped version to 3.4.0 (because of reintroduction of `SimpleExecutionContext`).

### 3.3.0

* Updated POM dependencies.

* Removed `testutils.akka` Akka test utilities.

### 3.2.23

* Updated dependencies.

* Updated failing test `JsonTest`.

### 3.2.22

* Removed `log4j.xml` from JARs.

* Updated dependencies.

### 3.2.20-3.2.21

* Updated dependencies.

### 3.2.13-3.2.19

* Updated POM dependencies.

* Fixed Jackson (and Jetty) vulnerability.

* Updated copyright to TomTom NV.

### 3.2.12

* Updated dependencies in `pom.xml`. This includes an update of the MongoDB driver, which
now serializes `DBObject` with a different `$date` format.

### 3.2.11

* Added `getHeading` for `GeoLine`.

### 3.2.10

* Changed type of parameter for `GeoRectangle.expand()` to double to allow sub-meter values.

* Added `MathUtils.wrapValueToWithinLimits()` to limit values between `[]-limit, limit)`. 
 
### 3.2.9

* Properties starting or ending with 'secret' or 'password' are now never shown at startup 
(case-insenstive). 

### 3.2.8

* Allowed default value to be `{empty}` (or any other string). The `{...}` are matched properly now.
Assigning the null string means the environment varibale is left undefined, so `${X:=}` would leave `X` 
undefined if the environment variable was not set, leading to a start-up error (which is a good thing),

### 3.2.7

* Added the ability to use environment variables in property values. You can now use the sytax
`property=${VAR:=default}` to use the environment variable `VAR` in a property value, with a 
default value of `default`, if the environment variables wasn't set.

### 3.2.6

* Included `elevationMeters` in remaining `Geo`-classes. All operations now support elevations, including
translation and length calculations. 

* Geo-objects like lines, polylines, rectangles etc. now always have either no elevation at all, or all
their points or corners do have an elevation.  

* Added unit tests to test elevation behavior.

### 3.2.5

* Included `elevationMeters` in `Geo`-classes, so `GeoPoint`s can be 3D. If the elevation is omitted, the
point is considered 2D. 

* Includes moving to Scala 2.12.4 and Akka libraries compiled for Scala 2.12.

### 3.2.4

* Updated dependencies to latest from Maven Central.

* Includes moving to Scala 2.12.4 and Akka libraries compiled for Scala 2.12.

### 3.2.3

* Minor fixes.

### 3.2.2

* Added three properties to control the behavior fo the `MongoDBTraceFetcher` better:

    * `MongoDBTrace.fetcherThreadSleepMsecs = 250`: Specifies how fast the trace fetcher schedules retrieving
    additional traces to fill its buffer.
     
    * `MongoDBTrace.fetcherThreadSleepAfterExceptionMsecs = 5000`: Specifies how long the trace fetcher will
    wait and sit and do nothing when a (database) exception occurs.
    
    * `MongoDBTrace.fetcherQueueMaxSize = 500`: Specifies hoe many trace events the trace fetcher will buffer
    at most, before stopping collectin more events.

* Added `SecurityHelper` methods to get `Principal` name for authenticated sessions, for convenience.
 
* Made `SessionManager.createSecurityContext` public, because it's needed by a security manager implementation.

* Removed `AuthenticationScheme` enum and replaced it with a generic `String` implementation to
be extensible to include one-time-passwords, OAuth, etc.

### 3.2.1

* Removed automatic binding of `SecurityInterceptor`. Needs to be done explicitly by
caller now in a Guice module: `binder.bind(SecurityInterceptor.class).in(Singleton.class)`, 
where `binder` is an injected Guice binder. If the bidnding is nor performed, no authentication 
is enforced; otherwise it is by default enforced on all URLs.

* Important: The SMS module has been rewritten to use the Resteasy 3.x client interface, but
the APIs seems to be broken. Need to be fixed. 

### 3.2.0

* This release is not fully compatible with previous releases: the `Tracer` 
interfaces have changed.

* The `tracer` module has been places at top-level, rather than under `mongodb`. This
means you need to include the `tracer` separately in your POMs, and the package names
have changed to `com.tomtom.speedtools.tracer`.

### 3.1.0 - 3.1.1

* This release is not fully compatible with previous releases due to an Akka upgrade, 
which has deprecated the use of typed actors.

* Implemented full security for REST APIs in module `rest.security`.

* Updated Akka dependencies to 2.5.3.
 
* Remove module `akka`, which has become superfluous. Move the `Duration` utilities from 
`AkkaUtils` to `core/time`.

* Removed `TokenBucket` class entirely. This was more of a utility class.

* Removed all classes for typed actors, like `ActorFactory`, `TypedActorContext`.

* Removed `RootActor` class. 

* Sorted dependencies in POMs.

### 3.0.26

* Updated POM dependencies.

### 3.0.25

* Removed secret token from POM.

* Fixed issues from code review with Codacy.

* Fixed copyright messages for 2017.

### 3.0.24

* Fixed issue with Guice test.

* Added ability to create immutable `ApiListDTO` collection.

### 3.0.23

* Updated dependencies of Maven POM to latest versions.

### 3.0.22

* Introduced Travis CI and Coveralls in POM and root directory for fully
automated CI and test coverage and added badges to `README.md`.

### 3.0.21

* Updated `com.tomtom.speedtools.rest.ResourceProcessor` to use the new RESTEasy `AsyncResponse` 
and `@Suspended` interfaces. The old interfaces `AsynchronousResponse` and `@Suspend` are still 
supported, but have been marked `@Deprecated` and support will be removed at some point.

### 3.0.20

* Changed from APT to Markdown format for all documentation.

* Updated dependencies of JBoss RESTEasy and Maven plugins.

* Enabled JavaDoc for all modules in Maven site.

### 3.0.19

* Removed **ApiValidator.resetValidator()** and replaced with an indication
  of whether the object is immutable or not. Immutable objects get additional
  checks and are automatically validated when a getter is executed.

* Fixed JSON ignore attribute in **ApiDTO**.

### 3.0.18

* Added **ApiValidator.resetValidator()**.

* Updated POM dependencies.

### 3.0.17

* Added **CheckNull**, **CheckTrue**> and **CheckFalseull** to API validations.

### 3.0.16

* Improved error message if startup fails in servlet.

### 3.0.15

* Include **GeoPolyLine** to specify lines consisting of multiple segments.

### 3.0.14

* Fixed Apache Commons version dependency to 3.2.2.

### 3.0.13

* Minor fixes.

### 3.0.12

* Fixed some more in **TileMap** and **MercatorPoint**.

### 3.0.11

* Fixed **TileMap** bug, which showed wrong tiles on map at extreme coordinates.

### 3.0.10

* Fixed **TraceHandlerCollection** to consider trace methods with the right number of parameters only.

* Added compact/verbose mode to **GeneralExceptionMapper**.


### 3.0.9

* Correct JavaDoc errors.

* Added **ApiListDTO** type for top-level JSON lists.

### 3.0.8

* Added explanation in **MongoDBTraceFetcher** exception which would occur when using a non-capped
  MongoDB collection for traces.

* Corrected MongoDB database authentication and bumped driver version to 3.0.1.

### 3.0.7

* Fixed a bug in serializing/deserializng enums with redefined **toString()** methods.

### 3.0.6

* Made **ViewportTile/TimeOffset** classes public (from package private).

### 3.0.5

* Added propertie "Properties.emptyString". Properties with this value will passed as empty strings.

### 3.0.4

* Rename **DataBinder** to more commonly used **DTO**.

* Made **ApiDTO.validate()** public to validate polymorph objects.

### 3.0.3

* Made **ApiDataBInder.validate()** public to allow validating different inherited binders easily.

* Added **javax.ws.rs** exceptions to **GeneralExceptionMapper**.

* Moved to Jackson 2 for RestEasy.

### 3.0.2

* Re-release of 3.0.1, which included snapshots. Removed those.



### 3.0.1

* Moved to JDK 1.8 and Java 8 language level.

* Refactored REST API handler/controller interface to using Java Lambda expressions.

* Introduced Lambda expressions elsewhere in implementation code.

* Moved package 'map' and classes TileMap from package 'lbs' to package 'geo'.

* Fixed many inspection warnings.

### 3.0.0

* First public release of TomTom SpeedTools under the Apache License 2.0.


Earlier Releases (Pre 3.0.0, Apache License 2.0)

### 2.3.1

* Updated dependency on TTMAIN connector in **speedtools-ttmain**.

### 2.3.0

* Refactored TTBIN module out of standard SpeedTools library into **speedtools-ttbin** module.
Same Maven dependency required, but a different versioning scheme (starts with 1.8.0 for version 8 of the
TTBIN specification.)

### 2.2.17

* Implemented TTBIN V8-alpha format. (Version 8 specification not complete yet.)

### 2.2.16

* Remove TTMain dependency. Extracted to its own SpeedTools project.

### 2.2.15

* Fixed TTBIN PacketWorkoutSummaryV7: totMeters is float, not int.

### 2.2.14

* Updated dependencies.

* Removed references to examples directory.


### 2.2.13

* Removed unnecessary dependencies on log4j from pom.xml.

### 2.2.12

* Fixed additional static code anaylsis checks from IntelliJ IDEA 13.1.

* Updated copyright notices.

### 2.2.11

* Updates POM library and plugin dependencies to newest versions.

* Cleaned up pom.xml a bit.

### 2.2.10

* Fixed Objects.hashCode() for doubles.

### 2.2.9

* Fixed getMin/Max of MetricsCollector.

* Rename addValue to addValueNow (and addValueNow to addValue).

### 2.2.8

* Fixed MetricsCollector to allow for negative data points as well. Returns Float.NaN if metrics have no meaning.

### 2.2.7

* Removed dependency on MongoDB for module 'rest'.

### 2.2.6

* Corrected port check in MongoDB trace property.

* Upgraded Resteasy to 3.0.8.Final (from 2.3.5.Final).

### 2.2.5

* Correct incorrect assertion on TTBin file size.

* Removed generating HTML /p tags.

### 2.2.3-2.2.4

* Packet size table auto-added if none present for V7 TTBin files.

* Added **GeoLine.getShortest(from, to)** to get shortest GeoLine for two points.

### 2.2.1-2.2.2

* Added automatic TTBin packet size calculation and table generation for all packet types.

* Added proper equals/hashCode implementations to be able to comapre two TTBin parse trees.

* Added EqualsVerifier unit tests for all TTBin classes.

### 2.2.0

* Added TTBin encoder/decoder to separate module, for those who'd like to play with it.

### 2.1.7

* Replaced Guice annotations with javax.inject annotations whereever possible (Inject, Singleton,
  see https://github.com/google/guice/wiki/JSR330).

* Fixed incorrect name of speedtools.default.properties file (was still safertools).

### 2.1.6

* Downgraded Java language level and byte code to 6 instead of 8, which makes the library compatible
  with Java 6, 7 and 8 projects.

### 2.1.5

* Changed IntelliJ Nullable and NotNull annotations to JSR305 variants from com.google.code.findbugs.

* Changed JCIP Immutable annotation to JSR305 variant from com.google.code.findbugs.

### 2.1.4

* Changed name from SaferTools to SpeedTools. Change source repository name and Maven site as well.

### 2.1.1-2.1.3

* Upgraded to Jackson 2.

* Added additional date time serializers with msec resolution.

### 2.1.0

* Added TTMain connector to SpeedTools (imported from Texas and adapted to new TTMain dependency).

* Fixed blocking JavaDoc for JDK 1.8.

### 2.0.0

* Split up SpeedTools in multiple modules to reduce dependency trees.

### 1.7.7

* Made RestEasyJacksonContextResolver non-final to avoid issues with Weld.

### 1.7.6

* Added Mappers + other changes - see git log.

### 1.7.5

* Fixed binary mapper.

### 1.7.4

* Corrected error message in mappers.

### 1.7.3

* Added binary mapper for MongoDB.

### 1.7.2

* Forced return media type of output for REST services to be JSON in case of exceptions
caught by the GeneralExceptionMapper.

### 1.7.1

* Bumped to 1.7.1 after moving to Git and improving release tooling.

### 1.6.8-1.6.13

* First Git released version.

### 1.6.6, 1.6.7

* Added ApiInvalidFormatError/Exception for API validation.

### 1.6.5

* Applied IDEA fixes to "final" variables in try-catch's.

### 1.6.4

* Changed Java language version to 7 (now using JDK 1.7).

* Updated package dependencies in POM to latest versions.

### 1.6.3

* Re-release.

### 1.6.2

* Improved source code organization of the examples:

    * **exampleCreatingScalableService** - example of writing simple OS thread-based and
    Future-based (high scalability0 )services using the SpeedTools framework.

    * **exampleUsingDatabase** - example of using the database mapper and object persistency framework
    of SpeedTools.

    * **exampleUsingLbsServices** - example of the use of several TomTom LBS services using the SpeedTools
    framework.

### 1.6.1

* Added examples for event tracing and LBS geocoding and routing services (under **/web/3/traceme**,
**/web/3/geocoding/[query]** and **/web/3/route/[from]/[to]**).

* Moved the library files into a directory called "speedtools" and moved the examples files one level up.

### 1.6.0

* Added **speedtools-connectors** which may be used to directly access the following services
in your Java applications:

    * **LBS services:**

        * LBS geocoding services;

        * LBS mapping services;

        * LBS routing services;

    * **Push notifications:**

        * iOS APNS push notifications;

        * Android GCM push notifications;

        

    * **SMS notifications:**

        * MessageBird SMS services;

        * Nexmo SMS services.

### 1.5.1

* Added MongoDB tracer example.

### 1.5

* Improved documentation further.

### 1.4.8

* Correct some documentation issues.

### 1.4.5-1.4.7

* Improved **Gatling** load test example in module **examples**.

### 1.4.1-1.4.4

* Upgraded to Akka 2.2.3 and using **java-developer-pom**.

### 1.4

* Improved REST API usage of SpeedTools and included clear examples of how to write REST APIs using SpeedTools.

### 1.3.1

* This release adds a new module "speedtools-rest" which provides a number of tools to create
  RestEasy REST APIs in a highly scalable way, using Akka threads and actors under the hood.
  Have a look at the **speedtools-examples** module to see how you can build your own REST API.

### 1.3.0

* Refactored project into 3 JARs: speedtools-core, speedtools-akka and speedtools-mongodb.

### 1.2.2

* ST-1: Made Crypto class thread safe.

* ST-2: Fixed Locale issue with JDK 1.7.

### 1.2.1

* Improved documentation only. No code changes.

### 1.2.0

* Renamed main package to **speedtools-utils**.

### 1.1.1

* Unused release version, use 1.2 instead.

### 1.1.0

* First refactored version of the well reviewed and well tested utils library, made entirely
  generic. These utils should now be easily be usable in any
  other TomTom project.

### 1.0.28(-SNAPSHOT) and earlier

* These version originally came from the TomTom commercial project. Please refer to their original
  project documentation for more release information.
