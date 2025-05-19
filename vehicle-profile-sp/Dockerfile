FROM eclipseecsp/ecsp-base-java17:1.0.0

RUN \
  apk update && \
  apk upgrade --no-cache && \
  apk add --no-cache postgresql-client bash openssl libgcc libstdc++ ncurses-libs
  
ADD ./target/vehicle-profile-sp-shaded.jar /opt/vehicle-profile/sp/vehicle-profile-sp.jar
ADD src/main/resources/application.properties /opt/vehicle-profile/sp/application.properties
ADD src/scripts/start.sh /opt/vehicle-profile/sp/
ADD src/main/resources/kafka.client.keystore.jks /kafka/ssl/kafka.client.keystore.jks
ADD src/main/resources/kafka.client.truststore.jks /kafka/ssl/kafka.client.truststore.jks
ADD src/main/resources/logback.xml /vehicle-profile-sp/conf/logback.xml

RUN addgroup --system appgroup
RUN adduser --system appuser --ingroup appgroup

RUN chown -R appuser:appgroup /opt/vehicle-profile/sp/
RUN chown -R appuser:appgroup /kafka/ssl/
RUN chown -R appuser:appgroup /vehicle-profile-sp/conf/
RUN chmod 755 /opt/vehicle-profile/sp/start.sh
USER appuser

ENTRYPOINT ["/opt/vehicle-profile/sp/start.sh"]
