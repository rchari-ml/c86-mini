FROM openjdk:21-jdk-slim
# make sure the target jar file exists. run mvn clean package before building this Docker image
COPY target/toolkit-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
