FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/ecommerce-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 18090
ENTRYPOINT ["java","-jar","app.jar"]
