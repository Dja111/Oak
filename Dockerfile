FROM openjdk:20-jdk-slim

RUN mkdir /app
WORKDIR /app

ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY build/libs/Oak-1.0-SNAPSHOT.jar oakApp.jar

ENTRYPOINT ["sh", "-c", "java -jar oakApp.jar"]

USER nobody

EXPOSE 8081






