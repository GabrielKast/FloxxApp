# floxxBackendApp


# Words

 - Slot : this is a conference with title, kind, start time, end time, day, room
 - TrackInfo : Is more than slot. In track slot information plus hit information
 - Hit :  unique id of the slot, percentage and date time of when hit has been down

# Deployment

On clever-cloud, 

    CC_JAVA_VERSION="11"
    CC_SBT_TARGET_DIR="httpEngine"
    FLOXX_PORT="8080"
    PORT="8080"
    POSTGRESQL_ADDON_PASSWORD=**to be completed**
    POSTGRESQL_ADDON_USER=**to be completed**
    POSTGRESQL_MAX_POOL_SIZE=**to be completed**
    POSTGRESQL_URI="jdbc:postgresql://**to be completed: Host**/**to be completed USER**"
      
Locally : 

 ## Pre-requisites
 1) you need sbt. See. https://www.scala-sbt.org/download.html
 2) You need node with a version>= v16.14.2 (installing nvm might help your day) and yarn
 3) You need docker and docker-compose. As docker-compose starts a db on port 5432, you must not have a postgres running in this port alreadyhttps://www.scala-sbt.org/download.html.
 
 ## Work with backend

1) from the root directory run docker-compose up (should start postgres DB)
2) sbt runDev
3) from browser : http://localhost:8081

## Work on UI

1) go to front directory
2) Run  'yarn start-dev'
3) from browser : http://localhost:8082 

`Some route are mmocked in webpack.config.js file`


# roll out

1) ensure that your master index is clean and fully pushed.
2) Run `sbt goToProd` 
   1) Process il plugged on remote origin, So take care that remote corresponding to prod target environment
3) and then just follow the process


# Run migration DB
  /!\ update env information with targeted env (Prod)
   1) /!\ Run locally sbt with this command line : sbt -Dflyway.url=jdbc:postgresql://{{HOST}}/{{DB}} -Dflyway.user={{User}} -Dflyway.password=
   2) and db/flywayMigrate
-----


## Todo 

 - Refactoring 
   - Finalize migration model -> Domain
   - Move from doobie to quill
 - Upgrade version on front-end side 
 

