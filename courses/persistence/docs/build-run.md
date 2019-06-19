# Build / Run

## Test

Unit tests are under [test](../src/test):

```bash
$ sbt test
```

## Demo

```bash
$ sbt "; project persistence; run"

Multiple main classes detected, select one to run:

 [1] com.backwards.persistence.ex1.PersistentActors
 [2] com.backwards.persistence.ex1.PersistentActorsExercise
 ...

Enter number:
```

Or specify a specific "app" to run:

```bash
$ sbt "; project persistence; runMain com.backwards.persistence.ex1.PersistentActorsExercise"
```


