# autocomplete

## 1. Conception
La structure de donnée utilisée pour l'implementation de l'autocomplete est la Trie.
Le code est dev en Scala.

## 2. Test
Les test unitaire sont dans le répertoire **test**. pour lancer les test **sbt test** depuis le shell SBT.

## 3. Run
le fichier App.scala contient la fonction main dans le package **com.da.autocomplete**, le program prend en entrer un ficher txt contenant les mots et produit en sortie des autocomplition.

# cbm_data-datawarehouse

|        | current state |
| ------ | ------------- |
| `dev`  | [![Dev Build Status](https://codebuild.eu-west-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiTndVTnV0WmNmcm1vYXVOSEZyaGpsTVVjOSs1cjg2YkFKZWppdEdFSndVUUV4bFIwWm5vMlIwU0FpNjg0QllZRXVkWi9LcjFmMkFWZ2ZNWGtoVE9SbGRZPSIsIml2UGFyYW1ldGVyU3BlYyI6IkQycmVZcnFEOTNPNm9BTGgiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=dev)](https://eu-west-1.console.aws.amazon.com/codebuild/home?region=eu-west-1#/projects/data-datawarehouse-dev/view) |
| `rec`  | [![Rec Build Status](https://codebuild.eu-west-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoicUQ5bFkvaFl1V21qem4xVm8rQW1lVEZRZ3VsRkVWbkFvKzZkTDljQitSaHVJT0tnamg0ZzgyeVpnalFuOGFkUjA3STNaZUZzVlFLd1lFK3JLOFhBT2hRPSIsIml2UGFyYW1ldGVyU3BlYyI6Ik00REkwSDBJTVpNVUdabDYiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=rec)](https://eu-west-1.console.aws.amazon.com/codebuild/home?region=eu-west-1#/projects/data-datawarehouse-rec/view) |
| `prod` | [![Prod Build Status](https://codebuild.eu-west-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoid3RkYjFQV1p5OEE5WFpWdHVnSStnb1pWSmpWdnRHZ0s1c0plZFZBQlZJK2VYQ3czWEdMRnZtdzJYUzRhRkJINEV1SU1Wd3lCbG52NHdSY09BQ3JNTkJnPSIsIml2UGFyYW1ldGVyU3BlYyI6IjFWamthYlBxUFFLNmgrRFMiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=prod)](https://eu-west-1.console.aws.amazon.com/codebuild/home?region=eu-west-1#/projects/data-datawarehouse-prod/view) |

This repository manages the datawarehouse (DWH) with this organization:
- **aws/** : cloud formations for the codebuild pipeline + the slack notifier
- **checks/** : data consistency checks (scala test/specs on existing DWH tables)
- **migrations/** : versioned SQL scripts for continuous migration (flyway)
- **model/** : SQL scripts of latest version of each table (informal) with reference data if any
- **diagrams/** : logical model and diagrams
- **patches/** : patch-*.sh will execute the appropriate SQL scripts (for recurrent patches)

## 1. CodePipeline deployment notes (aws/)

You will need manually to set the branch filter of the Github webook (since not available in cloudformation right now)  
each time you create/update the pipeline stack (aws/cloudformation/cf-datawarehouse-codepipeline.yml)

This can be done via the AWS console or from the AWS cli ([cf. Stackoverflow](https://stackoverflow.com/questions/50857928/how-to-set-branch-filter-option-in-aws-codebuild-cloudformation-template)):

```bash
aws codebuild update-webhook --project-name data-datawarehouse-dev --branch-filter ^dev$
aws codebuild update-webhook --project-name data-datawarehouse-rec --branch-filter ^rec$
aws codebuild update-webhook --project-name data-datawarehouse-prod --branch-filter ^prod$
```

An alternative would be to embed the source in a CodePipeline stage instead of the CodeBuild   
but the build status for each commit and the global badge (here above) would not be available anymore.


**FIXME:** flyway is triggered when merging/commiting via the webhook on CodeBuild.  
However when we click on "Retry build" after a build failure we loose the webhook context and the flyway does not apply.  
This is caused by a condition based on [CODEBUILD_WEBHOOK_TRIGGER](https://docs.aws.amazon.com/fr_fr/codebuild/latest/userguide/build-env-ref-env-vars.html) used to prevent from applying flyway for daily reports (that are not webhooks).  

## 2. How to add some assertions to tables (checks/)

All specs (tests) are located at `checks/src/test/scala/fr/carboatmedia/stats/dwh/specs/`.  
You can create your specs by inheriting from `AbstractDimSpec` or `AbstractFactSpec` (to inherit common checks).

Please have a look to existing specs (eg. `DimAnnonceSpec`, `FctEventPaSpec`) to understand how to parameterize them.

**Important :**

_The Redshift in prod is used by default when you run locally the tests._  
To change the env, just edit the "VM parameters" section of your run configuration (IntelliJ) with `-Ddb-env=dev`.

### 2.a) Generate the table object for new tables

All specs are based on tables.  
The project relies on [scalikejdbc](http://scalikejdbc.org) to add compilation checks to SQL (field and table names).

You have to generate the table object for any new table.  
Your generated classes will be located at `checks/src/main/scala/fr/carboatmedia/stats/dwh/specs/`.

_The Redshift in dev is used to retrieve your table schema_.

- from command line : `sbt "gen-table-object your_table_name"`
- or directly from your sbt shell (IntelliJ) : `gen-table-object your_table_name`


### 2.b) Regenerate the table object for existing tables

If a table has changed in Redshift (dev) you may want to regenerate its table object :  

- delete the file in `checks/src/main/scala/fr/carboatmedia/stats/dwh/specs/YourTableName.scala`
- then simply generate your table object (-> see 2.a)

## 3. Continuous migration of the datawarehouse (migrations/)

Migration scripts are located at:
- `migrations/src/main/java/db/migration/V*__*.java`
- `migrations/src/main/resources/db/migration/V*__*.sql`

Each migration script is written in `.java` or `.sql`  
They share the same `V*` sequence (`V1`, `V2`, `V3`, etc.)

The migration engine is powered by flyway via sbt:  

- from command line : `sbt "flyway migrate"`
- or directly from your sbt shell (IntelliJ) : `flyway migrate`

General documentation is available at https://flywaydb.org/documentation/migrations

**Important :**

_The Redshift in dev is used by default when you run `sbt "flyway <action>"` without `-e`_  

Please run `sbt flyway` to show all available commands.  
A useful command is `sbt "flyway info"` to show the state of migrations.
 
 
## 4. How to execute patches on demand (patches/)

You can execute any patch located at `patches/patch-*.sh`.  

All patches require *SQLWorkbenchJ*, especially `sqlworkbench.jar`.  
Please set the env variable `SQLWORKBENCH_DIR` to the directory that contains `sqlworkbench.jar`.

Each `.sh` patch script executes sequentially the ordered `.sql` scripts on the related directory.  
You can choose your Redshift environment via the connection profile name of your *SQLWorkbenchJ*.

For instance, if your connection profile is "Redshift prod", you may :

- first, test your patch (--dry-run means it is rolled back automatically even on success)

```bash
./patches/patch-client-duplicates.sh --profile="Redshift prod" --dry-run
```

- then, apply your patch (committed on success)

```bash
./patches/patch-client-duplicates.sh --profile="Redshift prod"
```

**Tool to generate a patch that removes duplicates from a given table :**

The command `gen-patch-dupli` generates the `.sql` script contents on the console output.  
So you will have to copy/paste the output in your `.sql` file.

_The Redshift in dev is used by default when you run `sbt "gen-patch-dupli <your_table_name>"` without `-e`_  

- from command line : `sbt "gen-patch-dupli your_table_name"`
- or directly from your sbt shell (IntelliJ) : `gen-patch-dupli your_table_name`

