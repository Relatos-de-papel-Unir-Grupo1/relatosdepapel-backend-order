# Relatos de Papel - Microservicio de Órdenes

Este microservicio gestiona todo el ciclo de vida de las órdenes de compra en la aplicación **Relatos de Papel**. Es responsable de crear nuevas órdenes, consultar su estado y gestionar los ítems asociados a cada una.

## ✨ Características Principales

- **Gestión de Órdenes**: Proporciona una API REST para crear y consultar órdenes.
- **Comunicación entre Microservicios**: Se comunica de forma síncrona con el `catalogue-service` para validar la existencia de libros y su stock disponible antes de crear una orden.
- **Transaccionalidad Distribuida (Simplificada)**: Asegura que el stock en el catálogo se descuente como parte del flujo de creación de una orden.
- **Integración con Service Discovery**: Utiliza Eureka para descubrir y comunicarse con el `catalogue-service` de forma dinámica.
- **Carga de Datos de Prueba (Seeding)**: Incluye un mecanismo robusto y configurable para generar órdenes de prueba, que depende de los datos previamente cargados por el servicio de catálogo.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Java 17
- **Framework**: Spring Boot 3
- **Persistencia**: Spring Data JPA, Hibernate, MySQL
- **Comunicación Reactiva**: Spring WebFlux (para `WebClient`)
- **Gestión de Dependencias**: Maven
- **Service Discovery**: Spring Cloud Netflix Eureka Client
- **Utilidades**: Lombok

## 🚀 Cómo Empezar

Sigue estos pasos para configurar y ejecutar el microservicio en tu entorno local.

### Prerrequisitos

- JDK 17 o superior.
- Maven 3.8 o superior.
- Una instancia de MySQL en ejecución.
- Un servidor Eureka en ejecución.
- El microservicio `relatosdepapel-backend-catalogue` debe estar en ejecución y registrado en Eureka.

### Configuración

1.  **Clona el repositorio**:
    ```bash
    git clone <url-del-repositorio>
    cd relatosdepapel-backend-order
    ```

2.  **Configura la base de datos**:
    -   Crea una base de datos en MySQL llamada `orders_db`.
    -   Los esquemas de las tablas `orders` y `order_items` se crearán automáticamente al iniciar la aplicación.

3.  **Configura las propiedades de la aplicación**:
    Ajusta el archivo `src/main/resources/application.properties` con la configuración de tu base de datos y Eureka.

    ```properties
    # Puerto del servidor
    server.port=8081

    # Configuración de la Base de Datos
    spring.datasource.url=jdbc:mysql://localhost:3306/orders_db
    spring.datasource.username=root
    spring.datasource.password=tu_contraseña
    spring.jpa.hibernate.ddl-auto=update

    # Configuración de Eureka
    eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka

    # Configuración de Carga de Datos de Prueba
    app.seed.enabled=true
    app.seed.orders-count=15
    ```

### Ejecución

Puedes ejecutar la aplicación utilizando el siguiente comando de Maven:

```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8081`.

## 🌱 Carga de Datos de Prueba (Seeding)

Este servicio puede generar órdenes de prueba basadas en los libros existentes en el catálogo.

- **Habilitación**: Se activa con `app.seed.enabled=true`.
- **Dependencia**: Para que funcione, el `catalogue-service` debe estar activo y haber cargado sus propios datos de prueba.
- **Resiliencia**: El seeder de órdenes implementa una política de reintentos. Si no puede conectarse al catálogo al arrancar, esperará y reintentará varias veces antes de desistir.
- **Configuración**: Puedes definir cuántas órdenes crear con la propiedad `app.seed.orders-count`.

## 📖 Documentación de la API

La API proporciona los siguientes endpoints para gestionar las órdenes:

#### `POST /api/v1/orders`
- **Descripción**: Crea una nueva orden. El servicio valida el stock de cada libro contra el microservicio de catálogo y descuenta las unidades correspondientes.
- **Cuerpo de la Petición**:
  ```json
  {
    "userId": 1,
    "items": [
      {
        "bookId": 10,
        "quantity": 2
      },
      {
        "bookId": 25,
        "quantity": 1
      }
    ]
  }
  ```
- **Respuesta Exitosa (200 OK)**:
  ```json
  {
    "id": 1,
    "orderNumber": "ORD-A1B2C3D4",
    "orderDate": "2026-05-29T10:30:00",
    "total": 75.50,
    "status": "COMPLETED",
    "userId": 1,
    "items": [
      {
        "id": 1,
        "bookId": 10,
        "quantity": 2,
        "unitPrice": 25.00,
        "subtotal": 50.00
      },
      {
        "id": 2,
        "bookId": 25,
        "quantity": 1,
        "unitPrice": 25.50,
        "subtotal": 25.50
      }
    ]
  }
  ```

#### `GET /api/v1/orders/{id}`
- **Descripción**: Obtiene los detalles de una orden por su ID.

#### `GET /api/v1/orders/user/{userId}`
- **Descripción**: Obtiene todas las órdenes realizadas por un usuario específico.

## 👥 Colaboradores

- **Sebastian Felipe Alvarado Prieto**
- **Ardys Díaz Hurtado**
- **Luis Ferdinand Lugoz Rivas**