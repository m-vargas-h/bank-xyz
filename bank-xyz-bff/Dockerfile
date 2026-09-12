FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/bank-xyz-bff-0.0.1-SNAPSHOT.jar app.jar
RUN jar xf app.jar BOOT-INF/classes/keystore.p12 && \
    mv BOOT-INF/classes/keystore.p12 /app/keystore.p12 && \
    rm -rf BOOT-INF META-INF && \
    rm -f /root/.keystore
ENTRYPOINT ["java", "-jar", "app.jar"]