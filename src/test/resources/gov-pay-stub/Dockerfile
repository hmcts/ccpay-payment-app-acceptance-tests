FROM openjdk:8-jre

RUN curl -L http://central.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/2.5.1/wiremock-standalone-2.5.1.jar -o /app.jar

COPY entrypoint.sh /

EXPOSE 8080

ENTRYPOINT [ "/entrypoint.sh" ]
