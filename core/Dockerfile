FROM openjdk:12

ADD build/libs/cloud-backend-1.0.0.jar cloud-backend.jar

EXPOSE 8080

VOLUME ["/Cloud"]

ENTRYPOINT ["java", "-jar", "cloud-backend.jar"]
