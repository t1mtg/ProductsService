FROM openjdk:18
ADD target/marketapi.jar marketapi.jar
ENTRYPOINT ["java", "-jar", "marketapi.jar"]
EXPOSE 80
