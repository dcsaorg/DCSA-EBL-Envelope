# Running the reference implementation locally
Running the reference implementation locally can be done in various ways, but the easiest is via Docker Compose.

## Prerequisites
In order to run the reference implementation the following is needed:
* JDK 17
* Maven 3
* Docker
* Docker Compose
* Github PAT to access DCSA artifacts

### Access to Github DCSA artifacts

To use Github Packages, Github Authentication must be set up, even if the packages are public (This seems to be limitation on GitHubs end)
This is done like this:

To do this, follow these steps:

#### Creating a Github Personal Access Token
Go to https://github.com/settings/tokens and click "Generate new token"
Write a note for the token, for example "MAVEN_PACKAGES_PAT"
Select read:packages, delete:packages, and write:packages.
Click Generate Token
Copy the token, we will need it soon. If you leave this page without copying the token, you will need to generate a new one
Note: Treat this token as you would treat any password.

#### Setting up settings.xml

1. Go to your maven directory. There are two locations where a settings.xml file may live:

   The Maven install: ${maven.home}/conf/settings.xml

   A user's install: ${user.home}/.m2/settings.xml.

   If you don't have this directory, and you do have Maven installed create this directory.
2. Create a file called "settings.xml"
3. Paste the following into the file:
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>github</id>
          <name>GitHub dcsa Apache Maven Packages</name>
          <url>https://maven.pkg.github.com/dcsaorg/DCSA-Core</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>

        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_NAME</username>
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

    4. Replace YOUR_GITHUB_NAME with your Github name, and replace YOUR_PERSONAL_ACCESS_TOKEN with the token generated earlier
    5. Save the file. You should now be able to use dcsa Github packages in your projects

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

## Running via an IDE or commandline
In order to run the via an IDE or via the command line a standalone database is needed for the application to store and retrieve data. The setup of the database is described [here](test-certificates/README.md).
The hostname and port to the database need to be provided via the environment variable: DB_HOSTNAME
for example:
```shell
export DB_HOSTNAME=localhost:5432
```

The application is using Java and Spring Boot and contains multiple Spring profiles and groups of profiles. For development the 'dev' profile should be used.
Setting the Spring profile can also be done via an environment variable:
```shell
export SPRING_PROFILES_ACTIVE=dev
```

Like the application running via Docker the application by default is using the key material available in the 'test-certificates' directory.
If needed this can be changed via the properties in [application.yml](ec-service/src/main/resources/application.yml)

Due to the Spring-Boot maven plugin the application can be run from the commandline using Maven:
```shell
mvn spring-boot:run
```

## Generating and using your own key material
The required key material for running the EBL-Envelope reference implementation is described [here](test-certificates/README.md).
This directory also includes a set of test keys, certificates and keystores. However, it is certainly possible to use your own key material.
When using/runninng the reference implementation other than for local development it is highly recommended using your own key material.

### Generating keystore for TLS
The TLS connection is setup to require a PKCS12 keystore containing a private key and public certificate.
To generate a self-signed certificate:
```shell
keytool -genkeypair -alias springboot-https -keyalg RSA -storetype PKCS12 -keystore springboot-https.p12 -storepass your_key-store_password
```
To use an existing TLS private key and certificate chain in PEM format:
```shell
openssl pkcs12 -export -name "springboot-https" -out springboot-https.p12 -in fullchain.pem -inkey privkey.pem
```

### Generating keystore for signing
Generating the signature requires a JKS. The alias is configured in [application.yml](ec-service/src/main/resources/application.yml) in the property spring.security.jws.key-id

To generate the keystore :
```shell
keytool -genkeypair -alias dcsa-kid -keyalg RSA -keystore dcsa-jwk.jks -storepass dcsa-pass
```

### Generating keystore for verifying signatures
Verifying the signatures of either incoming transferblocks or verifying the signature in the response of an outgoing transferblock requires a JKS.
This JKS can contain multiple public certificates of each platform connected. The reference implementations uses the **_'CN'_** in the certificate to determine which public key to use for verification.
For incoming transferblocks the CN is matched with the 'platformHost' field in the transaction.
For verifying responses of outgoing transferblocks the CN is matched to the URL the transferblock is being sent to.
So when generating the key material make sure to take note of the CN used.

To generate the keystore:
```shell
keytool -genkeypair -alias dcsa-kid \
-keyalg RSA -keysize 2048 \
-keystore dcsa-jwk-verify.jks -storepass keystore-pass \
-dname "CN=server.mycompany.com,OU=My Company Dev Team,O=My Company,L=State,S=City,C=Country" \
-validity 365;
```

To import an existing public certificate(chain) in PEM format in the keystore:
```shell
keytool -importcert -alias my_public_cert -file public_cert.cer -keystore dcsa-jwk-verify.jks -storepass keystore-pass -noprompt
```
