# Running the reference implementation locally
Running the reference implementation locally can be done in various ways, but the easiest is via Docker Compose.

## Docker Compose
In order to run the reference implementation locally via Docker Compose using the test certificates.
1. clone this repository
2. build the Java application
3. Build and run the docker containers

clone the repository:
```shell
git clone git@github.com:dcsaorg/DCSA-EBL-Envelope.git
cd DCSA-EBL-EBL
```
Build the Java application:
```shell
mvn package
```
Build and run the docker containers
```shell
docker-compose -f docker-compose.yml up -d -V --build
```

## Running via an IDE
ToDo

## Generating and using your own key material
ToDo
