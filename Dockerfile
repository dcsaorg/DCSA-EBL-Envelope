FROM eclipse-temurin:17-jre-alpine

EXPOSE 9090
ENV db_hostname dcsa_db
COPY ovs-service/target/dcsa-ovs-service.jar .
CMD java -jar dcsa-ovs-service.jar
