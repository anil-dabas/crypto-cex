# Getting Started

## How to Run the Project 

### Infra development 

- Make sure that you have Docker desktop installed in the system 
- Please go to the root application folder on the terminal
  - Build the project using ```gradle build```
  - Run below commands to deploy MySQL, Redis, Kafka on your docker desktop
  ```docker compose -f docker-compose.dependencies.yml up -d --build --remove-orphans```

### To Run the matching-engine and order-service

Option 1 - Run below command 
  ```docker compose -f docker-compose.apps.yml up -d --build```
Option 2 - You can individually start all the 3 applications 
  - Go to the application folder from root like 
    - ```cd matching-engine```
    - ```./gradlew bootRun```
    - ```cd order-service```
    - ```./gradlew bootRun```
    - ```cd driver```
    - ```./gradlew bootRun```


