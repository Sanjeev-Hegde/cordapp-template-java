FROM openjdk:8u171-jdk-alpine
COPY  . /home/gradle/src
WORKDIR /home/gradle/src
RUN apk add bash iputils tar rsync && \
    rm -rf /var/cache/apk/* && \
    chmod +x ./gradlew && \
    ./gradlew :clients:build --stacktrace && \
    mkdir /app && \
    cp /home/gradle/src/clients/build/libs/*.jar /app/spring-boot-application.jar && \
    addgroup springboot && \
    adduser -G springboot -D -s /bin/bash springboot && \
    chmod -R u+x /app && \
    chgrp -R 0 /app && \
    chmod -R g=u /app /etc/passwd
EXPOSE 8080
USER springboot
#COPY --from=build /home/gradle/src/clients/build/libs/*.jar /app/spring-boot-application.jar
WORKDIR /app
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","spring-boot-application.jar"]