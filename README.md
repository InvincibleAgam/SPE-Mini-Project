# Calculator DevOps — CI/CD Pipeline for a Calculator REST API

CSE 816 Software Production Engineering — Mini-Project

A small Spring Boot calculator API wrapped in a fully automated DevOps pipeline:
a push to GitHub triggers Jenkins, which builds, tests, containerizes, publishes
the image to Docker Hub, and deploys the stack with Docker Compose.

**Stack:** Java 21 · Spring Boot 3.5 · Maven · JUnit 5 · Git/GitHub · Jenkins · Docker · Docker Compose · nginx
