# ia-case-documents-api

Immigration &amp; Asylum case documents API

## Purpose

Immigration &amp; Asylum case documents API is a Spring Boot based application to generate documents for Immigration & Asylum Appellants and Legal Representatives.

### Prerequisites

To run the project you will need to have the following installed:

* Java 17
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

The following environment variables are required when running the api without its dependencies mocked. This includes running the functional tests locally. The examples (the values below are not real):

| Environment Variable | *Example values*  |
|----------------------|----------|
| DOCMOSIS_ACCESS_KEY | some-docmosis-access-key |
| DOCMOSIS_ENDPOINT | some-docmosis-url |
| IA_GOV_NOTIFY_KEY | some-gov-notify-key |
| IA_BAIL_GOV_NOTIFY_KEY | some-gov-notify-key |
| IA_IDAM_CLIENT_ID  |  some-idam-client-id |
| IA_IDAM_SECRET  |  some-idam-secret |
| IA_IDAM_REDIRECT_URI  |  http://localhost:3451/oauth2redirect |
| IA_S2S_SECRET  |  some-s2s-secret |
| IA_S2S_MICROSERVICE  |  some-s2s-gateway |
| IA_ADMIN_NEWPORT_EMAIL | some-email |
| IA_ADMIN_TAYLOR_HOUSE_EMAIL | some-email |
| IA_ADMIN_HATTON_CROSS_EMAIL | some-email |
| IA_ADMIN_MANCHESTER_EMAIL | some-email |
| IA_ADMIN_GLASGOW_EMAIL | some-email |
| IA_ADMIN_BRADFORD_EMAIL | some-email |
| IA_ADMIN_BIRMINGHAM_EMAIL | some-email |
| IA_HEARING_CENTRE_BRADFORD_EMAIL |  some-email |
| IA_HEARING_CENTRE_MANCHESTER_EMAIL |  some-email |
| IA_HEARING_CENTRE_NEWPORT_EMAIL |  some-email |
| IA_HEARING_CENTRE_TAYLOR_HOUSE_EMAIL |  some-email |
| IA_HEARING_CENTRE_NORTH_SHIELDS_EMAIL |  some-email |
| IA_HEARING_CENTRE_BIRMINGHAM_EMAIL |  some-email |
| IA_HEARING_CENTRE_HATTON_CROSS_EMAIL |  some-email |
| IA_HEARING_CENTRE_GLASGOW_EMAIL |  some-email |
| IA_HOME_OFFICE_BRADFORD_EMAIL |  some-email |
| IA_HOME_OFFICE_MANCHESTER_EMAIL |  some-email |
| IA_HOME_OFFICE_NEWPORT_EMAIL |  some-email |
| IA_HOME_OFFICE_TAYLOR_HOUSE_EMAIL |  some-email |
| IA_HOME_OFFICE_NORTH_SHIELDS_EMAIL |  some-email |
| IA_HOME_OFFICE_BIRMINGHAM_EMAIL |  some-email |
| IA_HOME_OFFICE_HATTON_CROSS_EMAIL |  some-email |
| IA_HOME_OFFICE_GLASGOW_EMAIL |  some-email |
| IA_FTPA_HOME_OFFICE_BRADFORD_EMAIL |  some-email |
| IA_FTPA_HOME_OFFICE_TAYLOR_HOUSE_EMAIL |  some-email |
| IA_FTPA_HOME_OFFICE_NEWCASTLE_EMAIL |  some-email |
| IA_FTPA_HOME_OFFICE_HATTON_CROSS_EMAIL |  some-email |
| IA_RESPONDENT_NON_STANDARD_DIRECTION_UNTIL_LISTING_EMAIL |  some-email |
| IA_RESPONDENT_EVIDENCE_DIRECTION_EMAIL |  some-email |
| IA_RESPONDENT_REVIEW_DIRECTION_EMAIL |  some-email |
| IA_HOME_OFFICE_GOV_NOTIFY_ENABLED |  true/false |
| IA_DET_PRISON_EMAIL_MAPPINGS |  *See Prison Email Mappings section below* |

### Prison Email Mappings

For local development, you need to set the `IA_DET_PRISON_EMAIL_MAPPINGS` environment variable with prison email mappings in JSON format:

```bash
export IA_DET_PRISON_EMAIL_MAPPINGS='{
  "prisonEmailMappings": {
    "Addiewell": "det-prison-addiewell@example.com",
    "Albany": "det-prison-albany@example.com",
    "Altcourse": "det-prison-altcourse@example.com",
    "Ashfield": "det-prison-ashfield@example.com",
    "Ashwell": "det-prison-ashwell@example.com",
    "Askham Grange": "det-prison-askham-grange@example.com",
    "Aylesbury": "det-prison-aylesbury@example.com",
    "Barlinnie": "det-prison-barlinnie@example.com",
    "Bedford": "det-prison-bedford@example.com",
    "Bella Centre": "det-prison-bella-centre@example.com",
    "Belmarsh": "det-prison-belmarsh@example.com",
    "Berwyn": "det-prison-berwyn@example.com",
    "Birmingham": "det-prison-birmingham@example.com",
    "Blakenhurst": "det-prison-blakenhurst@example.com",
    "Blantyre House": "det-prison-blantyre-house@example.com",
    "Blundeston": "det-prison-blundeston@example.com",
    "Brinsford": "det-prison-brinsford@example.com",
    "Bristol": "det-prison-bristol@example.com",
    "Brixton": "det-prison-brixton@example.com",
    "Brockhill": "det-prison-brockhill@example.com",
    "Bronzefield": "det-prison-bronzefield@example.com",
    "Buckley Hall": "det-prison-buckley-hall@example.com",
    "Bullingdon": "det-prison-bullingdon@example.com",
    "Bullwood Hall": "det-prison-bullwood-hall@example.com",
    "Bure": "det-prison-bure@example.com",
    "Camp Hill": "det-prison-camp-hill@example.com",
    "Canterbury": "det-prison-canterbury@example.com",
    "Cardiff": "det-prison-cardiff@example.com",
    "Castington": "det-prison-castington@example.com",
    "Castle Huntly": "det-prison-castle-huntly@example.com",
    "Channings Wood": "det-prison-channings-wood@example.com",
    "Chelmsford": "det-prison-chelmsford@example.com",
    "Coldingley": "det-prison-coldingley@example.com",
    "Cookham Wood": "det-prison-cookham-wood@example.com",
    "Cornton Vale": "det-prison-cornton-vale@example.com",
    "Dartmoor": "det-prison-dartmoor@example.com",
    "Deerbolt": "det-prison-deerbolt@example.com",
    "Doncaster": "det-prison-doncaster@example.com",
    "Dorchester": "det-prison-dorchester@example.com",
    "Dovegate": "det-prison-dovegate@example.com",
    "Downview": "det-prison-downview@example.com",
    "Drake Hall": "det-prison-drake-hall@example.com",
    "Dumfries": "det-prison-dumfries@example.com",
    "Durham": "det-prison-durham@example.com",
    "East Sutton Park": "det-prison-east-sutton-park@example.com",
    "Eastwood Park": "det-prison-eastwood-park@example.com",
    "Edinburgh, Saughton": "det-prison-edinburgh-saughton@example.com",
    "Edmunds Hill": "det-prison-edmunds-hill@example.com",
    "Elmley": "det-prison-elmley@example.com",
    "Erlestoke": "det-prison-erlestoke@example.com",
    "Everthorpe": "det-prison-everthorpe@example.com",
    "Exeter": "det-prison-exeter@example.com",
    "Featherstone": "det-prison-featherstone@example.com",
    "Feltham": "det-prison-feltham@example.com",
    "Five Wells": "det-prison-five-wells@example.com",
    "Ford": "det-prison-ford@example.com",
    "Forest Bank": "det-prison-forest-bank@example.com",
    "Foston Hall": "det-prison-foston-hall@example.com",
    "Fosse Way": "det-prison-fosse-way@example.com",
    "Frankland": "det-prison-frankland@example.com",
    "Full Sutton": "det-prison-full-sutton@example.com",
    "Garth": "det-prison-garth@example.com",
    "Gartree": "det-prison-gartree@example.com",
    "Glen Parva": "det-prison-glen-parva@example.com",
    "Glenochil": "det-prison-glenochil@example.com",
    "Gloucester": "det-prison-gloucester@example.com",
    "Grampian": "det-prison-grampian@example.com",
    "Greenock": "det-prison-greenock@example.com",
    "Grendon/Spring Hill": "det-prison-grendon-spring-hill@example.com",
    "Guys Marsh": "det-prison-guys-marsh@example.com",
    "Hatfield (Main site)": "det-prison-hatfield-main@example.com",
    "Hatfield (Lakes site)": "det-prison-hatfield-lakes@example.com",
    "Haverigg": "det-prison-haverigg@example.com",
    "Hewell Grange": "det-prison-hewell-grange@example.com",
    "High Down": "det-prison-high-down@example.com",
    "Highpoint": "det-prison-highpoint@example.com",
    "Hindley": "det-prison-hindley@example.com",
    "Hollesley Bay/Hmyoi Warren Hill": "det-prison-hollesley-bay-warren-hill@example.com",
    "Holloway": "det-prison-holloway@example.com",
    "Holme House": "det-prison-holme-house@example.com",
    "Hull": "det-prison-hull@example.com",
    "Humber": "det-prison-humber@example.com",
    "Huntercombe": "det-prison-huntercombe@example.com",
    "Hydebank Wood": "det-prison-hydebank-wood@example.com",
    "Inverness": "det-prison-inverness@example.com",
    "Isis": "det-prison-isis@example.com",
    "Isle of Wight": "det-prison-isle-of-wight@example.com",
    "Kennet": "det-prison-kennet@example.com",
    "Kilmarnock": "det-prison-kilmarnock@example.com",
    "Kingston": "det-prison-kingston@example.com",
    "Kirkham": "det-prison-kirkham@example.com",
    "Kirklevington Grange": "det-prison-kirklevington-grange@example.com",
    "Lancaster": "det-prison-lancaster@example.com",
    "Lancaster Farms": "det-prison-lancaster-farms@example.com",
    "Latchmere House": "det-prison-latchmere-house@example.com",
    "Leeds": "det-prison-leeds@example.com",
    "Leicester": "det-prison-leicester@example.com",
    "Lewes": "det-prison-lewes@example.com",
    "Leyhill": "det-prison-leyhill@example.com",
    "Lilias Centre": "det-prison-lilias-centre@example.com",
    "Lincoln": "det-prison-lincoln@example.com",
    "Lindholme": "det-prison-lindholme@example.com",
    "Littlehey": "det-prison-littlehey@example.com",
    "Liverpool": "det-prison-liverpool@example.com",
    "Long Lartin": "det-prison-long-lartin@example.com",
    "Low Moss": "det-prison-low-moss@example.com",
    "Low Newton": "det-prison-low-newton@example.com",
    "Lowdham Grange": "det-prison-lowdham-grange@example.com",
    "Magilligan": "det-prison-magilligan@example.com",
    "Maghaberry": "det-prison-maghaberry@example.com",
    "Maidstone": "det-prison-maidstone@example.com",
    "Manchester": "det-prison-manchester@example.com",
    "Moorland Closed": "det-prison-moorland-closed@example.com",
    "Moorland Open": "det-prison-moorland-open@example.com",
    "Morton Hall": "det-prison-morton-hall@example.com",
    "New Hall": "det-prison-new-hall@example.com",
    "North Sea Camp": "det-prison-north-sea-camp@example.com",
    "Northallerton": "det-prison-northallerton@example.com",
    "Northumberland": "det-prison-northumberland@example.com",
    "Norwich": "det-prison-norwich@example.com",
    "Nottingham": "det-prison-nottingham@example.com",
    "Oakwood": "det-prison-oakwood@example.com",
    "Onley": "det-prison-onley@example.com",
    "Parc": "det-prison-parc@example.com",
    "Parkhurst": "det-prison-parkhurst@example.com",
    "Pentonville": "det-prison-pentonville@example.com",
    "Perth": "det-prison-perth@example.com",
    "Peterborough": "det-prison-peterborough@example.com",
    "Polmont": "det-prison-polmont@example.com",
    "Portland": "det-prison-portland@example.com",
    "Prescoed": "det-prison-prescoed@example.com",
    "Preston": "det-prison-preston@example.com",
    "Ranby": "det-prison-ranby@example.com",
    "Reading": "det-prison-reading@example.com",
    "Risley": "det-prison-risley@example.com",
    "Rochester": "det-prison-rochester@example.com",
    "Rye Hill": "det-prison-rye-hill@example.com",
    "Send": "det-prison-send@example.com",
    "Shepton Mallet": "det-prison-shepton-mallet@example.com",
    "Shotts": "det-prison-shotts@example.com",
    "Shrewsbury": "det-prison-shrewsbury@example.com",
    "Spring Hill": "det-prison-spring-hill@example.com",
    "Stafford": "det-prison-stafford@example.com",
    "Standford Hill": "det-prison-standford-hill@example.com",
    "Stocken": "det-prison-stocken@example.com",
    "Stoke Heath": "det-prison-stoke-heath@example.com",
    "Stirling": "det-prison-stirling@example.com",
    "Styal": "det-prison-styal@example.com",
    "Sudbury": "det-prison-sudbury@example.com",
    "Swaleside": "det-prison-swaleside@example.com",
    "Swansea": "det-prison-swansea@example.com",
    "Swinfen Hall": "det-prison-swinfen-hall@example.com",
    "Thameside": "det-prison-thameside@example.com",
    "The Mount": "det-prison-the-mount@example.com",
    "The Verne": "det-prison-the-verne@example.com",
    "The Weare": "det-prison-the-weare@example.com",
    "Thorn Cross": "det-prison-thorn-cross@example.com",
    "Usk": "det-prison-usk@example.com",
    "Wakefield": "det-prison-wakefield@example.com",
    "Wandsworth": "det-prison-wandsworth@example.com",
    "Warren Hill": "det-prison-warren-hill@example.com",
    "Wayland": "det-prison-wayland@example.com",
    "Wealstun": "det-prison-wealstun@example.com",
    "Wellingborough": "det-prison-wellingborough@example.com",
    "Werrington": "det-prison-werrington@example.com",
    "Wetherby": "det-prison-wetherby@example.com",
    "Whatton": "det-prison-whatton@example.com",
    "Whitemoor": "det-prison-whitemoor@example.com",
    "Winchester": "det-prison-winchester@example.com",
    "Wolds": "det-prison-wolds@example.com",
    "Woodhill": "det-prison-woodhill@example.com",
    "Wormwood Scrubs": "det-prison-wormwood-scrubs@example.com",
    "Wymott": "det-prison-wymott@example.com"
  }
}'
```

### Running the application

To run the API quickly use the docker helper script as follows: (make sure to have set the required environment variables as above)

```
./bin/run-in-docker.sh --clean --install
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

