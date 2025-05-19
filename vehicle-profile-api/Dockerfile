FROM eclipseecsp/ecsp-base-java17:1.0.0

ARG PROJECT_JAR_NAME
ADD src/main/resources/kafka.client.keystore.jks /kafka/ssl/kafka.client.keystore.jks
ADD src/main/resources/kafka.client.truststore.jks /kafka/ssl/kafka.client.truststore.jks
ADD ./target/vehicle-profile-api-exec.jar /opt/vehicle-profile/api/vehicle-profile-api.jar
ADD src/main/resources/logback.xml /vehicle-profile-api/conf/logback.xml 
EXPOSE 8080
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

RUN chown -R appuser:appgroup /kafka/ssl
RUN chown -R appuser:appgroup /opt/vehicle-profile/
RUN chown -R appuser:appgroup /vehicle-profile-api/
USER appuser
ENTRYPOINT java -jar /opt/vehicle-profile/api/vehicle-profile-api.jar