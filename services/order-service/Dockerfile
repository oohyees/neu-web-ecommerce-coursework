FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/ecommerce-order-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 18093
ENTRYPOINT ["java","-jar","app.jar"]
