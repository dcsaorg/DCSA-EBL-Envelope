FROM eclipse-temurin:17-jre-alpine

EXPOSE 8443
ENV db_hostname dcsa_db
COPY ec-service/target/dcsa-ec-service.jar .
CMD java -jar dcsa-ec-service.jar
