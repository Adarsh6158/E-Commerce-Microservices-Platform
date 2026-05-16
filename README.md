<h1 align="center">🛒 ShopFlux: E-Commerce Microservices Platform</h1>

<p align="center">
  A highly scalable, production-oriented E-Commerce platform built using Spring Boot microservices, Kafka, Redis, and React Micro-Frontends.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Architecture-Microservices-blue" />
  <img src="https://img.shields.io/badge/Frontend-Micro--Frontends-purple" />
  <img src="https://img.shields.io/badge/Event%20Driven-Kafka-red" />
  <img src="https://img.shields.io/badge/Backend-SpringBoot-green" />
  <img src="https://img.shields.io/badge/Database-PostgreSQL%20%7C%20MongoDB-orange" />
</p>


## Core Features

* **Micro-Frontend Architecture:** Decoupled React frontends (Shell, Admin, Cart, Product, Search, Order).
* **Distributed Microservices:** 10 independent Spring Boot services (Auth, Catalog, Inventory, Payment, etc.).
* **Event-Driven Workflows:** Asynchronous communication and notifications using Apache Kafka.
* **High-Performance Caching:** Distributed caching and session management using Redis.
* **Polyglot Persistence:** Utilizing PostgreSQL (relational) and MongoDB (document) based on service domain needs.
* **Advanced Search:** Integrated Elasticsearch for rapid, robust product querying.


## Architecture

### System Overview
<img width="1500" height="1000" alt="Architecture_design" src="https://github.com/user-attachments/assets/605c4b66-3d19-441f-9123-5bca145d8c23" />



### Database Design
<img width="840" height="470" alt="DB Design" src="https://github.com/user-attachments/assets/9f4c227f-b796-4e34-9af2-db8e1fe5e311" />



## Tech Stack

<table>
<tr>
<td>

<b>Backend</b><br/> 
<img src="https://www.vectorlogo.zone/logos/java/java-icon.svg" width="28"/> Java &nbsp; 
<img src="https://www.vectorlogo.zone/logos/springio/springio-icon.svg" width="28"/> Spring Boot &nbsp; 
<img src="https://www.vectorlogo.zone/logos/apache_kafka/apache_kafka-icon.svg" width="28"/> Kafka

</td>

<td>

<b>Databases & Cache</b><br/> 
<img src="https://www.vectorlogo.zone/logos/postgresql/postgresql-icon.svg" width="28"/> PostgreSQL &nbsp; 
<img src="https://www.vectorlogo.zone/logos/mongodb/mongodb-icon.svg" width="28"/> MongoDB &nbsp; 
<img src="https://www.vectorlogo.zone/logos/redis/redis-icon.svg" width="28"/> Redis

</td>
</tr>

<tr>
<td>

<b>Frontend</b><br/> 
<img src="https://www.vectorlogo.zone/logos/reactjs/reactjs-icon.svg" width="28"/> React &nbsp; 
<img src="https://www.vectorlogo.zone/logos/tailwindcss/tailwindcss-icon.svg" width="28"/> TailwindCSS &nbsp; 
<img src="https://vitejs.dev/logo.svg" width="28"/> Vite

</td>

<td>

<b>Search & DevOps</b><br/> 
<img src="https://www.vectorlogo.zone/logos/elastic/elastic-icon.svg" width="28"/> Elasticsearch &nbsp; 
<img src="https://www.vectorlogo.zone/logos/docker/docker-icon.svg" width="28"/> Docker

</td>
</tr>
</table>


## Design Highlights

* **Database per Service Pattern:** Each microservice owns its own database to ensure loose coupling.
* **Event-Driven Sagas:** Kafka enables distributed transactions and asynchronous orchestration.
* **Module Federation:** Frontend applications are independently deployable and loaded dynamically at runtime.


## Getting Started 

I have built a suite of automated terminal scripts within the `/Scripts` directory to make running this complex architecture incredibly simple.

### 1. Start the Infrastructure (Databases, Kafka, Redis)
Spins up all necessary background services via Docker Compose.
```bash
cd Scripts
./start-infra.sh
```

### 2. Seed the Databases with Sample Data
Populates your databases with sample products, pricing rules, and inventory.
```bash
./seed-data.sh
```

### 3. Start the Backend Microservices
Boots all 10 Spring Boot services in the background.
```bash
./start-backend.sh
```

### 4. Start the Frontend Application
Starts the Micro-Frontend shell and all individual sub-applications.
```bash
./start-frontend.sh
```

## Stopping the Platform

Use our teardown scripts to cleanly turn everything off. They gracefully stop the processes and keep your computer clean.

```bash
cd Scripts
./stop-frontend.sh
./stop-backend.sh
./stop-infra.sh
```
*(Note: To completely wipe the database data, run `docker compose down -v` inside the `Infra/` folder).*


## Why this project

Built to understand how real distributed e-commerce systems are designed, how polyglot persistence strategies are applied, how to manage distributed state using Kafka, and how Micro-Frontend architectures scale in production environments.
