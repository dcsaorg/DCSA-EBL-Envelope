FROM eclipse-temurin:17-jre-alpine

EXPOSE 8443
ENV db_hostname ec_registry
COPY run-in-container.sh /run.sh
RUN chmod +x /run.sh
COPY ec-service/src/main/resources/application.yml .
COPY ec-service/target/dcsa-ec-service.jar .
CMD ["/run.sh"]
