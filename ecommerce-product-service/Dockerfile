FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/ecommerce-product-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 18092
ENTRYPOINT ["java","-jar","app.jar"]
