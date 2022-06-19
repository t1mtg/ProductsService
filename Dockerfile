FROM openjdk:18-jdk
MAINTAINER Timofey "Real OG" Gurman (timagurman@gmail.com)
RUN apt-get update
RUN apt-get install -y maven
COPY pom.xml /usr/local/service/pom.xml
COPY src /usr/local/service/src
WORKDIR /usr/local/service
RUN mvn package
CMD ["java","-jar","target/marketApi-0.0.1-SNAPSHOT.jar"]