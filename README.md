# IA Case Payments API

### Background
`ia-case-payments-api` allows Immigration and Asylum workflow to check fees and make a payment for an appeal based on chosen type of hearing
during appeal submission. There are exceptions based on Business logic when payment can be done after appeal is submitted. All interactions with Fees and Payment system are encapsulated within this service.

### Prerequisites

To run the project you will need to have the following installed:

* Java 11
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

### Running application

`ia-home-office-integration-api` is common Spring Boot application. Command to run:
```
./gradlew clean bootRun
```

### Testing application
Unit tests and code style checks:
```
./gradlew clean build
```

Integration tests use Wiremock and Spring MockMvc framework:
```
./gradlew integration
```

Functional tests use started application instance:
```
./gradlew functional
```

In order for these tests to run successfully you will need its dependencies to be running.

There is a need to run ia-docker service stack to provide OpenId authentication and Wiremock recorded responses for Professional Ref Data, Fees service and Payment service.

To successfully interact with the above dependencies a few environment variables need to be set as below.

| Environment Variable                  | *Example values*  |
|----------------------                 |----------|
| TEST_LAW_FIRM_A_USERNAME              |  ia-law-firm-a@example.com            |
| TEST_LAW_FIRM_A_PASSWORD              |  password                             |
| TEST_LAW_FIRM_ORG_SUCCESS_USERNAME    |  ia-law-firm-success@fake.hmcts.net   |
| TEST_LAW_FIRM_ORG_SUCCESS_PASSWORD    |  password                             |
| TEST_LAW_FIRM_ORG_DELETED_USERNAME    |  ia-law-firm-deleted@fake.hmcts.net   |
| TEST_LAW_FIRM_ORG_DELETED_PASSWORD    |  password                             |
| IA_IDAM_CLIENT_ID                     |  some-idam-client-id                  |
| IA_IDAM_SECRET                        |  some-idam-secret                     |
| IA_IDAM_REDIRECT_URI                  |  http://localhost:3451/oauth2redirect |
| IA_S2S_SECRET                         |  some-s2s-secret                      |
| IA_S2S_MICROSERVICE                   |  some-s2s-gateway                     |
| FEES_REGISTER_API_URL                 |  http://localhost:8991                |
| PAYMENT_API_URL                       |  http://localhost:8991                |
| PROF_REF_DATA_URL                     |  http://localhost:8991                |

If you want to run a specific scenario use this command:

```
./gradlew functional --tests CcdScenarioRunnerTest --info -Dscenario=RIA-3271
```

### Running smoke tests:

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *smoke tests* as follows:

```
./gradlew smoke
```

### Using the application

To understand if the application is working, you can call it's health endpoint:

```
curl http://localhost:8096/health
```

If the API is running, you should see this response:

```
{"status":"UP"}
```

### Usage
API details about usages and error statuses are placed in [Swagger UI](http://ia-case-payments-api-aat.service.core-compute-aat.internal/swagger-ui.html)

### Implementation

`ia-case-payments-api` has 2 endpoints which are equivalent for CCD callbacks phases. Payload is a standard CCD record. Based on the event request is dispatched to proper handlers and Business logic is applied.

Authentication is defined as any other Reform application with Idam `Authorization` token and S2S `ServiceAuthorization` token.

Business logic and validation have to be implemented in dedicated handler. `ia-case-payments-api` has three main handlers which are responsible for integration with downstream services.

Downstream services are listed below:
- Fees service
- Professional Ref Data
- Payment service


## Adding Git Conventions

### Include the git conventions.
* Make sure your git version is at least 2.9 using the `git --version` command
* Run the following command:
```
git config --local core.hooksPath .git-config/hooks
```
Once the above is done, you will be required to follow specific conventions for your commit messages and branch names.

If you violate a convention, the git error message will report clearly the convention you should follow and provide
additional information where necessary.

*Optional:*
* Install this plugin in Chrome: https://github.com/refined-github/refined-github

  It will automatically set the title for new PRs according to the first commit message, so you won't have to change it manually.

  Note that it will also alter other behaviours in GitHub. Hopefully these will also be improvements to you.

*In case of problems*

1. Get in touch with your Technical Lead and inform them, so they can adjust the git hooks accordingly
2. Instruct IntelliJ not to use Git Hooks for that commit or use git's `--no-verify` option if you are using the command-line
3. If the rare eventuality that the above is not possible, you can disable enforcement of conventions using the following command

   `git config --local --unset core.hooksPath`

   Still, you shouldn't be doing it so make sure you get in touch with a Technical Lead soon afterwards.


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
