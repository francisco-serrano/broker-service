FROM openjdk:10
WORKDIR /app
RUN mkdir -p /app
RUN mkdir -p /app/tmp
ADD target/broker-beta.jar /app/
EXPOSE 8080
CMD ["java", "-jar", "/app/broker-beta.jar", "dockerized"]