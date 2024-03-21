# Copy In Repro

This is a small repro for [this issue](https://github.com/pgjdbc/r2dbc-postgresql/discussions/638).

To run this repro:

1. Make sure you have Docker installed, or another way to start a Postgres server
2. Run `docker compose up -d greenplum` to start a Postgres server
3. Run `./gradlew clean build update` to build the Java code and run Liquibase migrations
4. Run `./run-app.sh` to run the application
5. Observe the issue, the application hangs.
