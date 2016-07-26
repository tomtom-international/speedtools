Environment Configuration and Property Files
----

The **SpeedTools** system configuration is described in property files, which are divided into two
sections:

* **Environment Configuration** properties for TechOps, listed in the file `speedtools.properties`
which is **not** contained within the WAR file and needs to be supplied separately, and

* **Application Tuning Parameters** for Developers, listed in the file `speedtools.default.properties`
which is contained within the WAR, but which may be overriden in `speedtools.properties`
(although this is rather unusual).

## Environment Configuration - for Deployment

Environment configuration parameters may be changed to match the deployment infrastructure.
These parameters should not influence the internal behavior of the system, but do describe,
for example, the topology of the infrastructure, host names, ports, etc.

## MongoDB Traces Configuration

The system produces "traces" and stores them in a (separate) Mongo database. These traces provide insight
in what the system is doing. For production this database is non-critical and should run on a different
server compared to the critical Mongo database server.

The trace events database uses a capped collection, meaning database will not grow beyond this maximum size.

```
*-------------------------------------+--------------------------+-------------------------------------------------+
| **Property Name**                   | **Description**          | **Default**                                     |
*-------------------------------------+--------------------------+-------------------------------------------------:
| MongoDBTrace.servers                | MongoDB (DNS) hostname.  | -                                               |
*-------------------------------------+--------------------------+-------------------------------------------------:
| MongoDBTrace.database               | Mongo database name.     | trace                                           |
*-------------------------------------+--------------------------+-------------------------------------------------:
| MongoDBTrace.userName               | User name.               | -                                               |
*-------------------------------------+--------------------------+-------------------------------------------------:
| MongoDBTrace.password               | Password.                | -                                               |
*-------------------------------------+--------------------------+-------------------------------------------------:
| MongoDBTrace.maxDatabaseSizeMB      | Maximum collection size. | 200                                             |
*-------------------------------------+--------------------------+-------------------------------------------------:
| MongoDBTrace.connectionTimeoutMsecs | Connection timeout.      | 10000                                           |
*-------------------------------------+--------------------------+-------------------------------------------------:
| MongoDBTrace.readEnabled            | Enable reading traces.   | false                                           |
*-------------------------------------+--------------------------+-------------------------------------------------:
| MongoDBTrace.writeEnabled           | Enable writing traces.   | false                                           |
*-------------------------------------+--------------------------+-------------------------------------------------:
```

## TomTom LBS Configuration

You may change these configuration values for the connection with the TomTom LBS system.

```
*-------------------------+-----------------------------------+----------------------------------------------------+
| **Property Name**       |**Description**                    | **Default**                                        |
*-------------------------+-----------------------------------+----------------------------------------------------:
| LBS.apiKey              | LBS API key.                      | d8b62842-3d77-4851-8499-00772c22551e               |
*-------------------------+-----------------------------------+----------------------------------------------------:
| LBS.geoCodeUrl1         | URL of geocoding service, v1.     |                                                    |
*-------------------------+-----------------------------------+----------------------------------------------------:
| LBS.geoCodeUrl2         | URL of geocoding service, v2.     |                                                    |
*-------------------------+-----------------------------------+----------------------------------------------------:
| LBS.tileMapUrl          | URL of (integer) mapping service. |                                                    |
*-------------------------+-----------------------------------+----------------------------------------------------:
| LBS.routeUrl1           | URL of route service, v1.         |                                                    |
*-------------------------+-----------------------------------+----------------------------------------------------:
| LBS.trafficEnabled      | Consider traffic jams or not.     | true                                               |
*-------------------------+-----------------------------------+----------------------------------------------------:
| LBS.numberOfRouteActors | Number of route actors.           | 25                                                 |
*-------------------------+-----------------------------------+----------------------------------------------------:
```

## Application Tuning Configuration Properties - for Developers

The following properties directly influence and tune the application behavior. They do not describe network topology
or deployment related proerpties. Modification of these properties should be done with utmost care. Do not
modify them unless you are absolutely confident what their effect is.

The descriptions of these properties are left blank, on purpose, as their meaning is to be found in the source
code itself. Please refer to the code comments for the exact meaning of each property.

```
*------------------------------------------------------------------+-----------------+-------------+
| **Property Name**                                                | **Description** | **Default** |
*------------------------------------------------------------------+-----------------+-------------:
| Maven.version (automatically generated by build)                 | POM version     | generated   |
*------------------------------------------------------------------+-----------------+-------------:
```


