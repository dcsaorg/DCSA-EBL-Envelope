FROM eclipse-temurin:17-jre-alpine

EXPOSE 8443
ENV db_hostname ec_registry
COPY test-certificates/two-platforms/springboot-https-platform1-pub.cer $JAVA_HOME/lib/security/springboot-https-platform1-pub.cer
COPY test-certificates/two-platforms/springboot-https-platform2-pub.cer $JAVA_HOME/lib/security/springboot-https-platform2-pub.cer
RUN keytool -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias springboot.https.platform1 -file $JAVA_HOME/lib/security/springboot-https-platform1-pub.cer
RUN keytool -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias springboot.https.platform2 -file $JAVA_HOME/lib/security/springboot-https-platform2-pub.cer
COPY run-in-container.sh /run.sh
RUN chmod +x /run.sh
COPY ec-service/src/main/resources/application.yml .
COPY ec-service/target/dcsa-ec-service.jar .
CMD ["/run.sh"]
