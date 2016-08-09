FROM java:7
COPY target/*.jar application.jar
ENTRYPOINT java $JVM_OPTS -jar application.jar
EXPOSE 8080
