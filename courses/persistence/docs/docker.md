# Docker

First "doc" file refers to **docker**? Well, we use docker to run required services such as Cassandra and Postgres.

Within the [root](..) of this module:

```bash
$ docker-compose up
```
or in Intellij **right-click** the file and choose **run** - this will run the configured services.

To connect to the running service **Cassandra**:

```bash
$ cqlsh
Connected to OUR_DOCKERIZED_CASSANDRA_SINGLE_NODE_CLUSTER at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.11.4 | CQL spec 3.4.4 | Native protocol v4]
Use HELP for help.
cqlsh>
```

To connect to the running service **Postgres**:

```bash
$ psql -h localhost -U admin
Password for user admin:
psql (11.3)
Type "help" for help.

admin=#
```