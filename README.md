# 🍎 eCommerce API

A robust and scalable Spring Boot RESTful API for a full-featured eCommerce platform. This backend service manages user accounts, product catalogs, orders, addresses, and shopping carts, providing secure authentication and authorization along with email notifications.

---

## ✨ Features

* ✅ **User Management**: Registration, login, and role-based access control (`USER`, `SELLER`)
* 📦 **Product Catalog**: Add, update, delete, and retrieve products
* 🛍️ **Shopping Cart & Orders**: Cart management and order placement
* 💳 **Payment Methods**: Support for multiple payment types (e.g., Cash on Delivery, Stripe)
* 📬 **Address Management**: Add and manage billing and shipping addresses
* 📧 **Email Notifications**: Order confirmations and refund confirmations.
* 🔐 **Authentication**: JWT-based login with refresh token support, , OAuth 2.0 login with Google
* 🔒 **Security**: BCrypt password hashing, Spring Security
* 🧹 **Validation & Error Handling**: Robust input validation and exception management
* 📄 **Swagger Docs**: Interactive API documentation via Swagger UI
* ☁️ **Cloudinary Integration**: Upload and manage user profile images

---

## 🛠 Technologies Used

* **Java 17**
* **Spring Boot**
* **Spring Security (JWT)**
* **Hibernate / JPA**
* **OAuth 2.0**
* **MySQL**
* **MongoDB**
* **Lombok**
* **Cloudinary**
* **Swagger / OpenAPI**
* **JavaMailSender**

---

## 📂 Folder Structure

```
EcommerceRestAPI
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.ecommerce.ecomapi
│   │   └── resources
│   │       └── application.properties (not committed)
├── pom.xml
├── .gitignore
└── README.md
```

---

## 🚀 How to Run This Project Locally

### ✅ Prerequisites

Make sure you have the following installed:

* [Java 17+](https://adoptopenjdk.net/)
* [Maven](https://maven.apache.org/download.cgi)
* [MySQL](https://dev.mysql.com/downloads/mysql/)
* [MongoDB](https://www.mongodb.com/try/download/community)
* Internet access for API integrations (Cloudinary, Stripe, Gmail, etc.)

---

### 📂 Setup `application.properties`

Create a file `src/main/resources/application.properties` and fill it with your configuration details:

```properties
spring.application.name=EcommerceRestAPI
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce?serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Cloudinary
cloudinary.cloud.name=your_cloud_name
cloudinary.api.key=your_api_key
cloudinary.api.secret=your_api_secret

# Stripe
stripe.secret.key=your_stripe_secret
stripe.api.publishable.key=your_stripe_publishable
stripe.currency=brl

# OAuth (Google)
spring.security.oauth2.client.registration.google.client-id=your_google_client_id
spring.security.oauth2.client.registration.google.client-secret=your_google_client_secret

# Mail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> ⚠️ **Important**: Never commit your `application.properties` file to GitHub. Add it to `.gitignore`.

---

### 📅 Create Databases

#### 🐬 MySQL

Run the following command in MySQL to create the database:

```sql
CREATE DATABASE ecommerce;
```

#### 🌿 MongoDB

No need to manually create. The database `ecommerce` will be auto-created if it does not exist.

Make sure your MongoDB server is running:

```bash
mongod
```

---

### 🚪 Build and Run the Application

Using Maven Wrapper:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Or with Maven directly:

```bash
mvn clean install
mvn spring-boot:run
```

---

### 📚 API Documentation

Visit Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

You can explore and test all available API endpoints from there.

---

## ✍️ Author

**Yash Batavle**

GitHub: [@Yash7028](https://github.com/Yash7028)

LinkedIn: https://www.linkedin.com/in/yash-batavle-b4b870298

---

