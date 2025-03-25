FROM openjdk:8
EXPOSE 8080
LABEL authors="slience_me"

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar", "--spring.profiles.active=prod"]
