FROM openjdk:17-oracle
EXPOSE 9999
VOLUME /tmp
COPY target/*.jar  app.jar
ENTRYPOINT ["java","-jar", "app.jar"]
