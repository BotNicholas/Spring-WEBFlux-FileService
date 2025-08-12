# FileService
## About:
This is a service to store, save and retrieve files...

## Useful links
* [ResponseEntity in WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux/controller/ann-methods/responseentity.html)
* [Streaming response in Spring MVC](https://www.baeldung.com/spring-mvc-sse-streams)
* [Spring Security for reactive applications](https://www.baeldung.com/spring-security-5-reactive)
* [JWT Authentication in WebFlux](https://medium.com/@jaidenashmore/jwt-authentication-in-spring-boot-webflux-6880c96247c7)

## How to start?
To start the application you need to create an `.env` file with the following content:
```properties
FILE_SERVER_MAILER_EMAIL=***@gmail.com
FILE_SERVER_MAILER_PASSWORD=***
```

Specifying email you will use to send emails in `FILE_SERVER_MAILER_EMAIL` and **"App Password"** in `FILE_SERVER_MAILER_PASSWORD`