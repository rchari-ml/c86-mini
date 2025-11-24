FROM eclipse-temurin:21-jdk
## FROM openjdk:21-slim

# Install some prerequisites that will help debug network issues
# Plus, copy the application JAR file into the container

# Install curl and grpcurl dependencies
RUN apt-get update && \
    apt-get install -y curl ca-certificates gnupg && \
    rm -rf /var/lib/apt/lists/*

# Install grpcurl
RUN curl -L https://github.com/fullstorydev/grpcurl/releases/download/v1.8.9/grpcurl_1.8.9_linux_x86_64.tar.gz \
    | tar -xz -C /usr/local/bin


# Set default workdir
WORKDIR /app

# make sure the target jar file exists. Run mvn build, if required
# mvn clean package -DskipTests
COPY target/toolkit-0.0.1-SNAPSHOT.jar /app/toolkit-app.jar

ENTRYPOINT ["java","-jar","/app/toolkit-app.jar"]
