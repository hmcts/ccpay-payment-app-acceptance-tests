FROM openjdk:8-jre

RUN curl -L http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock/1.58/wiremock-1.58-standalone.jar -o /app.jar

COPY mappings /mappings

COPY entrypoint.sh /

EXPOSE 8080

ENTRYPOINT [ "/entrypoint.sh" ]
