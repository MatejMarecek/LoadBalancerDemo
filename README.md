# Demo Load Balancer app

This is an application that demonstrates behavior and usage of Load Balancer.
It consists of a React front-end and Kotlin/Spring Boot back-end.

## Dependencies

In order to run/debug the application, you need the usual dependencies for React/Kotlin/Java apps.
The main dependencies are:

- [Java (JDK](https://adoptopenjdk.net/)) 11 or newer 
- [NodeJS](https://nodejs.org/en/download/)

## How to run it

The two easiest way how to run the application and access the front-end
in a web browser on address http://localhost:8080/ are using IntelliJ IDEA or Terminal + Gradle.

### IntelliJ IDEA

1. Download and install [IntelliJ IDEA](https://www.jetbrains.com/idea/)
2. Use the IDE to open the project
    - make sure the Gradle configuration is imported
3. In the IDE, run the `bootRun` task

### Terminal

1. Navigate to the project root folder
2. Run `$ .\gradlew bootRun` or `$ .\gradlew.bat bootRun` (Windows)

### The bootRun task

This task compiles the React front-end and copies it in the spring back-end "static" directory.
That way, the files needed for the web browser are available to the Spring Boot application.

When the front-end is handled, then the Kotlin/Java code is compiled as a standard Spring Boot
and executed.

## How to develop

In the development mode, it is advised to do:

1. Comment `dependsOn("copyFrontend")` in the `build.gradle.kts`
    - This speeds up Java compilation because the front-end is ignored
2. In `AppConfig.ts`, change the `isProduction` variable to `false`
3. Run `npm start` in the front-end root folder `{projectRoot}\src\main\frontend`
    - The front-end is available on http://localhost:3000/ and is automatically reloaded
    - HTTP requests get redirected to port 8080
