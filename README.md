./mvnw clean install ./mvnw spring-boot:run
"# BackEnd_ChuyenDeThucTap" 
# Database
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/badminton_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Backend / Frontend URL
app.backend.url=http://localhost:8080
app.frontend.url=http://localhost:3000

# JWT
JWT_SECRET=Q+DqnJezHFRtBkDNE9G/2YqTx5sigT/M1qBR9S/58aE=
JWT_EXPIRATION=86400000

