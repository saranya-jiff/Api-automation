Operator Gateway (OGW) Microservice
========

The Operator Gateway is an implementation of the BOA REST APIs that uses VMS on the backend (rather than UPM and other UHE components).

# Overview

## Data Flow Diagram

(source file can be found in `doc` directory)
![Data Flow Diagram](/doc/data-flow-diagram.png)

# Development Guide

These steps were created using Eclipse, and may vary a bit with other IDEs.

## cgw-common dependency

OGW uses [a shared library with CGW components][cgw-common] that currently must be installed manually (and updated manually) until it is put into a repository.  You need to checkout the project and do a `mvn install` so that it will be present.

> **Note**
>
> For `mvn install` to work, you'll obviously need to have maven installed.
>
> [You can install maven from here][mvn-install].
> 
> After installing maven, make sure that you environment variables are set properly.
> 
> ```
> export JAVA_HOME=`/usr/libexec/java_home -v 1.8` # for mac os x
> 
> export M2_HOME=/path/to/maven/home # This is where you extracted the files.
> export M2=$M2_HOME/bin
> export PATH=$M2:$PATH
> ```

To install the library manually into eclipse:

* On **Project Explorer** right click **ogw-microserice** and select **Properties**
* Select **Java Build Path** > **Libraries**
* Click **add external jar**, select `cgw-common-0.0.1-SNAPSHOT.jar`

[cgw-common]: https://github3.cisco.com/cloud-video/cgw-common
[mvn-install]: https://maven.apache.org/install.html

## IDE Setup

After checking out the project, it should be imported as a Maven project.  The import process will automatically trigger the XSD compilation step to generate a set of Java classes used in the REST API.   This step might take some time if the required artifacts are not already in your local Maven repo.

## Consul setup

The microservice can get most of the deployment-specific configuration data (such as the URL for VMS) from Consul.  While this data can be specified by modifying a YAML file in the project or using a Spring profile.

Consul can easily be run locally by downloading a binary (and optionally a zip file of a web UI), and then run on a command line, e.g. (assuming the web UI was placed a directory "~/Documents/Consul UI")

    consul agent -server -bootstrap-expect 1 -data-dir /tmp/consul -ui-dir ~/Documents/Consul\ UI
    
Note: Consul application should be running locally for OGW to start successfully.
    
## Yaml setup

OGW application requires few mandatory configuration data to start successfully. A static configuration file `ytv-old-vms.yml` found under `dev-config/`. It can be used by developers to run the application. If developers need different configuration settings they can create a new file with descriptive name and check it under `dev-config/` if they need it to be source controlled.

## Running the app in Eclipse

While it is possible to run the main application class as a standard Java app, it's recommended to use the Spring boot plug-in to run the app, which will ensure that other steps (such as code generation) are performed.  This can be done using a maven goal of `spring-boot:run -Dspring.config.location=dev-config/<CONFIG-FILE-NAME>.yml` within the IDE.

For that (*in eclipse*)

* Right click on `ogw-microservice` 
* Click **Run as** > **Maven Build...**
* Write `spring-boot:run -Dspring.config.location=dev-config/<CONFIG-FILE-NAME>.yml` as the goal, and click **Run**.

## IntelliJ Notes

Since the project is a maven import, it's mostly IDE agnostic.

Follow these instructions if you prefer to run it in IntelliJ

* This is important: Make sure that you have a proper eclipse project that is running and building successfully.
* Close eclipse
* Launch IntelliJ Idea Ultimate Edition (*community edition might not work*)
* Choose **import project** > **import project from external model** > **eclipse**
* Point to **ogw-microservice** project folder.
* Select eclipse projects to import: ogw-microservice
* Use 1.8 as the SDK.
* Open the project in IntelliJ
* Answer **yes** to "auto import maven projects?" prompt.
* Click the **refresh icon** in the **maven projects** tab to refresh the view.
* Wait for the IDE to index everything
* Choose **build** > **rebuild project** (there should be no build errors)
* While consul is running, double click on **Maven Projects** > **Operator Gateway Microservice** > **Plugins** > **spring-boot* > **spring-boot:run -Dspring.config.location=dev-config/<CONFIG-FILE-NAME>.yml** (which should run the microservice locally if you've configured everything properly)

Note: Value to `-Dspring.config.location` will be path to config file under `dev-config/`

## Build Notes

The cgw-common library dependency must be satisfied on the build machine as well.  It's it vital that it be updated on the build machine in order for changes to be included in the OGW builds.

There is a dedicated job that will build and install the library to the local repo on the build machine.

## Deploying from Jenkins

There is a build job on Jenkins that can be used to deploy builds.  See the job description for more details on what is required to use the job.

Since OGW requires Consul to be present and running order to start up.  When deploying to a VM that does not already have Consul, you will need to set it up manually.

One quick and dirty method is to:
* Install the Consul binary on the system
* Set OGW to not start at boot time (`chkconfig --del ogw-microservice`)
* add the following to /etc/rc.local to start consul and then OGW:

    /home/vcidev/consul agent -server -bootstrap-expect=1 -data-dir=/tmp/consul  2>&1 > /tmp/consul.log &
    sleep 3
    /etc/init.d/ogw-microservice start

The deploy job can also optionally install configurations files.  Configuration files should be placed in the `deployments` directory, in a subdirectory named for the deployment itself.  See the Jenkins job for more details.

# Swagger Modifications

Due to the nature of how the REST API is implemented relative to other microservices (using JAX-RS with Jersey, using a servlet not mapped to the root to avoid interfering with the /admin APIs), some modifications of the Swagger UI were required in order to support using the UI from any host.  (It appears that the Swagger UI doesn't properly handle the user of relative URLs for the base path)

In index.html, the default URL was modified:

    } else {
        /* OGW CHANGE (added /BillingAdaptor since that's where the Jersey servlet is)  - can server side map it instead? */
        url = "/BillingAdaptor/api-docs";
    }

And in swagger-ui.js, an "else" clause was added so that the swagger configuration in code could specify a relative path that would be used in the UI:

    var basePath = obj.basePath;
    if(obj.basePath.indexOf('http://') === 0) {
        var p = obj.basePath.substring('http://'.length);
        var pos = p.indexOf('/');
        if(pos > 0) {
             swagger.host = p.substring(0, pos);
             swagger.basePath = p.substring(pos);
        }
        else{
            swagger.host = p;
            swagger.basePath = '/';
        }
    }
    /* OGW CHANGE - Added this case so the swagger config could specify a relative path (other than '/') */
    else {
        swagger.basePath = basePath;
    }

If the OGW microservice is changed to use the same REST API implementation as other microservices, these changes can be reverted.

# Incorporating Files from BOA Project

This section describes the rough steps taken to add the files and code that implement the core for the BOA API.  If the BOA project introduces changes to the API that need to be pulled into the OGW, these steps should help with that process, though may need to be adapted depending on what the changes are, and whether OGW has made some additions to the API which we would not want to lose. 

The BOA project can be found at https://wwwin-svn-sjc.cisco.com/vvp/conductor/trunk/vsos/Projects/Conductor_Services/BillingAdaptor/ (adjust for a specific branch as needed).

## XSDs

The XSD for the entities in the REST API is `conf/schemas/boa.xsd`.  Only the required types should copied into a new XSD, 'ogw.xsd', with the namespace `http://protocols.cisco.com/spvss/veaas/ogw`.   There are some types that may only be used for internal testing methods (such as CreateDevice) that aren't intended for public use.   These types are placed at the bottom of the ogw.xsd and left out of the published XSD  (attempts to isolate these provide types to a separate XSD that included the "public" one were problematic for the schema validation code being used)

Note that the new namespace means that the packages for the generated Java classes will be different than the BOA project.  When including Java source files from BOA, imports will need to be corrected.

There are additional schemas in BOA that are for parts of the REST API, but only for methods that we are not implementing in OGW at this time.  They are not copied from BOA to keep the amount of dead code in OGW to a minimum.

## Java Code

Source files imported from the BOA project were kept in there original packages to reinforce the fact that they represent external code.  While they can be modified as needed for OGW, the fact that we may want to pull in updated versions from BOA in the future will limit the types of changes we'll be willing to make in order to make updating the code easier.


**com.cisco.conductorapp.billingadaptor.common.util.JAXBStaticWrapper.java**

Logger changed to be SLF4j native.  No other changes made.

**com.cisco.conductorapp.billingadaptor.common.util.XSDValidator**

Changed class to set the value of `DEFAULT_SCHEMA_FILENAME` to "ogw.xsd" (which eliminates dependency on BAConstants.java), and changed loading to be done via the classpath (so it would work in an IDE or packaged in a jar).  Logger updated to use SLF4j.  

**com.cisco.conductorapp.billingadaptor.service.BillingAdaptorServiceInterface.java**

Unused methods should be commented out.  This will primarily be methods that refer to missing data types (either because some schemas were not taken from BOA, or some types from boa.xsd were not included in ogw.xsd).  Essentially, the only methods that should remain are those that map to the list of support API calls. 

Then after organizing imports, all errors should be gone.

**com.cisco.conductorapp.billingadaptor.web.rest.BillingAdaptorLocator.java**

Can be imported with no changes (apart from updating logging to use SLF4j), though the implementation will need to be changed to be functional.  Since the role of this class is to return the back-end implementation, it's unlikely we'll need to pull in changes from BOA later one.

**com.cisco.conductorapp.billingadaptor.web.rest.BillingAdaptorREST**

Due to the lack of many types, all methods corresponding to unsupported API calls should be commented out and imports organized to clean up references to invalid/changed packages.  

Logging was also be changed to use SLF4j.


# Deploying OGW

## System Requirements

* Java runtime 1.8+ installed, with "java" on the path.
* Consul agent installed locally and running

## Deployment/Networking Requirements

OGW provides an HTTP-based API that must be reachable by and exposed to the operator, including a hostname that resolves from the operator's network over the site-to-site VPN connection.

OGW should be deployed in an HA setup with two instances running the OGW service behind a load balancer.

The OGW web server implements both SSL and authentication directly.  It listens on port 8443 by default, though this can be changed via configuration if desired.  No other ports need to be made accessible.

## Installation

OGW is packaged an an RPM (`ogw-microservice.x86_64`) which installs the service to /opt/cisco/vci/service/ogw-microservice/

By default, the service will not start at boot time.  This should be enabled using chkconfig (i.e. `chkconfig --add ogw-microservice`)  It is critical that the local Consul agent be started and fully running before the OGW service starts.


## Configuration

OGW can read deployment-specific information from a local config file and from key/value pairs in Consul, with values in Consul taking precedence over the local config file.  Not all things can be configured in Consul though due to an ability to define complex value types such as a list, so the local config file option is recommended.

The local config file is a YAML formatted file that must be created at /opt/cisco/vci/service/ogw-microservice/config/application.yml.  The following sections define values that likely need to be specified in any deployment.


### SSL Setup

OGW includes a default, self-signed certificate for SSL, but it's expected that a custom deployment would use a different certificate which must be configured.

OGW uses Tomcat as the embedded web server, so the certificate should be imported into a Tomcat-compatible keystore using the method of your choice.  The keystore file can then be placed in the "config" directory of the service (alongside the application.yml), and the application.yml file updated/created, with the following key/value pairs:

    server:
      ssl:
        key-store: config/mykeystore.jks
        key-store-password: changeit
        key-password: changeit
    

Tomcat provides general instructions on creating a keystore for a CA-signed certificate at https://tomcat.apache.org/tomcat-6.0-doc/ssl-howto.html#Installing_a_Certificate_from_a_Certificate_Authority (steps about modifying the Tomcat configuration are not relevant for OGW).  Note that despite what some Tomcat documentation may say, different passwords can be used for the keystore and the key itself.

### Authentication Setup

OGW implements role-based authentication. The user definition should be configured in application.yml file.

OGW expects atleast one user with role "USER" and one `systeminfo` user to start the application successfully.

In this version, the user/role for access to the `/systeminfo` endpoint is a special case and configured separately (using the same method as available in CGW microservices).

For API and management access, the available roles are:

* USER - This role has access to the REST APIs and Swagger UI.
* ADMIN - This role has access to the application management endpoint.

The user definition in application.yml is as follows:

    authentication:
      users:
        - 
          username: changeit
          password: changeit
          roles: [ADMIN]
        -
          username: changeit
          password: changeit
          roles: [USER]

Value for username and password will be deployment specific. A single user can be assigned both roles if desired. Note that this username **MUST BE** unique, and cannot be shared between users in this release.

For access to the systeminfo endpoint, the user definition should be as below:

    systemInfoController:
      username: changeit
      password: changeit

Note that system info username **MUST BE** unique, and cannot be shared with an ADMIN or USER role user in this release.

### VMS API Endpoint

A URL and credentials for the VMS endpoint OGW must be specified in order for the application to start.  The URL should not contain a path component (e.g. http://vms-us.projectvci.com/), and the username/password must be a valid VMS account that has web service privileges. (It's recommended that a separate VMS user be created rather than reuse the existing web service user account created by default in VMS since changing those credentials can break some functionality)

Additionally, some timeouts related to the VMS can be set if desired.  The following is the YAML snippet for VMS settings (including the default timeouts)

    vms:
      url: 
      username:
      password:
      connect-timeout: 10000
      read-timeout: 30000
      
### Vod-Regions

Vod regions supported in VMS. Value for vod regions should be comma seperated string.

The following is the YAML snippet for vod regions.

    vod-regions: region1, region2
    
region1, region2 are names of the vod region defined in VMS. These are the valid values OGW will accept for Vod region when creating or updating household.
    
### Channel-lineups

Channel lineups supported in VMS. Value for channel lineups should be comma seperated string.

The following is the YAML snippet for channel lineups.

    channel-lineups: lineup1, lineup2
    
lineup1, lineup2 are names of the channel lineup defined in VMS. These are the valid values OGW will accept for Channel lineup when creating or updating household.
    
### Available-services

Services supported in VMS. Value for Available-services should be comma seperated string.

The following is the YAML snippet for Available services.

    Available-services: Linear, SVOD
    
These are the valid values OGW will accept for available services when creating or updating household.
      
## Health Checks

Check scripts for Sensu are not part of the RPM, and must be pulled from the repo in `src/main/sensu/plugins/`.  Scripts for the desired commit should be deployed as desired.  Currently there's a single check script that can be used to ensure the webserver is running and responding correctly.

Example client json and check configuration files are provided in `src/main/sensu/conf.d/`.  The check requires credentials, which need to be stored in the client.json file to avoid exposing the values as clear text.   The client.json fields should be updated as appropriate, and the username/password should be that for a user with the USER role (e.g. the same credentials that are used by the operator to access the API).

If not using a properly signed certificate, a "-k" or "--insecure" option can be added to the command to disable certificate verification.

The check can also be used as a consul check.
      
# Operation

## Starting and Stopping the Service

The RPM will install an init script to control the service (`/etc/init.d/ogw-microservice`) with standard start/stop/restart/status commands.  It must be run as the root user.

A local Consul agent must be running in order for the service to start.

## Logging

Application logs are written to `/opt/cisco/vci/service/ogw-microservice/log/`.  Two separate logs files are written:

* `ogw-microservice.log` The main application log, intended primarily for use by humans
* `ogw-microservice_logstash.log` Constains the same data as above, but formatted specifically for ingestion into Logstash.

Both log files are automatically rolled (appending a number - ogw-microservice.log.1, ogw-microservice.log.2, etc.) and the oldest file will be deleted when the maximum window size is reached.  The actual logging configuration is specified in `/opt/cisco/vci/service/ogw-microservice/conf/logback.xml`

