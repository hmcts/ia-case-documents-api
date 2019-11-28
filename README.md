# ia-case-documents-api

Immigration &amp; Asylum case documents API

## Purpose

Immigration &amp; Asylum case documents API is a Spring Boot based application to generate documents for Immigration & Asylum Appellants and Legal Representatives.

### Prerequisites

To run the project you will need to have the following installed:

* Java 8
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

The following environment variables are required when running the api without its dependencies mocked. This includes running the functional tests locally. The examples (the values below are not real):

| Environment Variable | *Example values*  |
|----------------------|----------|
| DOCMOSIS_ACCESS_KEY | some-docmosis-access-key |
| DOCMOSIS_ENDPOINT | some-docmosis-url |
| IA_IDAM_CLIENT_ID  |  some-idam-client-id |
| IA_IDAM_SECRET  |  some-idam-secret |
| IA_IDAM_REDIRECT_URI  |  http://localhost:3451/oauth2redirect |
| IA_S2S_SECRET  |  some-s2s-secret |
| IA_S2S_MICROSERVICE  |  some-s2s-gateway |

### Running the application

To run the API quickly use the docker helper script as follows:

```
./bin/run-in-docker.sh install
```

Alternatively, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```

### Using the application

To understand if the application is working, you can call it's health endpoint:

```
curl http://localhost:8092/health
```

If the API is running, you should see this response:

```
{"status":"UP"}
```

### Running verification tests:

You can run the *unit tests* and *integration tests* as follows:

```
./gradlew check
```

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *functional tests* as follows:

```
./gradlew functional
```

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *smoke tests* as follows:

```
./gradlew smoke
```

If you have some time to spare, you can run the *mutation tests* as follows:

```
./gradlew pitest
```

As the project grows, these tests will take longer and longer to execute but are useful indicators of the quality of the test suite.


More information about mutation testing can be found here:
http://pitest.org/ 
