# Build / Run

## Test

Unit tests are under [test](../src/test):

```bash
$ sbt test
```

## Integration Test

Unit tests are under [it](../src/it):

```bash
$ sbt dockerComposeUp

$ sbt it:test

$ sbt dockerComposeStop
```

or

```bash
$ sbt dockerComposeTest
```



## Demo

```bash
$ sbt dockerComposeUp

$ sbt "it:runMain com.sky.ott.cassandra.evolutions.Demo"

$ sbt dockerComposeStop
```

Or

```bash
$ docker-compose up

$ sbt "it:runMain com.sky.ott.cassandra.evolutions.Demo"

$ docker-compose down
```

Or

Right-click [docker-compose.yml](src/it/resources/docker-compose.yml) and select **Run**.

```bash
$ sbt "it:runMain com.sky.ott.cassandra.evolutions.Demo"
```







```bash
$ sbt docker:publishLocal
```