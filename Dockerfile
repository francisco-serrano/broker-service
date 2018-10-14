FROM openjdk:10
WORKDIR /app
RUN mkdir -p /app
ADD target/broker-beta.jar /app/
EXPOSE 8080
CMD ["java", "-jar", "/app/broker-beta.jar", "dockerized"]