# Sabor Gourmet — EVA 2

**Asignatura:** Desarrollo de Software Web II  
**Estudiante:** Andre Secall  
**Profesor:** Cristopher Morales

Aplicación web de reservas creada con Spring Boot 3.5.6, Spring MVC, Spring Data JPA y Thymeleaf. Incluye consulta de disponibilidad, creación, modificación y cancelación de reservas, además de un panel para ver reservas y configurar mesas. El diseño funciona en pantallas de escritorio y móviles.

## Ejecutar el proyecto

Se necesita **Java 17 o superior** y **Maven 3.6.3 o superior**. En la carpeta del proyecto:

```bash
mvn spring-boot:run
```

Abrir `http://localhost:8080`. La configuración inicial usa una base H2 persistente en `data/sabor-gourmet.mv.db`, por lo que no exige instalar un servidor de base de datos para revisar la evaluación. Al arrancar por primera vez se crean seis mesas de ejemplo. Los datos de reservas sobreviven a un reinicio.

Para usar **PostgreSQL**, crear una base `sabor_gourmet` y un usuario con permisos, luego ejecutar:

```bash
DB_URL=jdbc:postgresql://localhost:5432/sabor_gourmet DB_USER=sabor DB_PASSWORD=sabor mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Se pueden cambiar los valores de las variables según la instalación. `application-postgres.properties` configura este perfil. Hibernate crea/actualiza las tablas con `ddl-auto=update`, adecuado para esta demostración académica.

Para generar el ejecutable:

```bash
mvn clean package
java -jar target/sabor-gourmet-1.0.0.jar
```

## Cómo usarlo

1. En **Disponibilidad**, indicar fecha, hora y número de personas. Se muestran solo mesas activas con capacidad suficiente y sin una reserva que se cruce con el intervalo solicitado.
2. Seleccionar **Reservar mesa**, completar nombre, correo y teléfono, y guardar. El sistema comprueba otra vez la disponibilidad justo antes de registrar la reserva.
3. En **Mis reservas**, buscar por correo. Desde allí se puede modificar fecha, hora, mesa y datos de contacto, o cancelar la reserva.
4. En **Administración**, ver las reservas registradas, agregar mesas, cambiar su capacidad y activar o desactivar mesas.

Las reservas duran **dos horas** y pueden comenzar desde las 12:00 hasta las 20:00. Solo se admiten fechas futuras y grupos de 1 a 20 personas. Una cancelación deja la reserva registrada como `CANCELADA` para conservar el historial, pero libera la mesa.

## Organización del código y función de cada archivo

### Configuración y arranque

| Archivo | Función |
|---|---|
| `pom.xml` | Define Java 17, Spring Boot y dependencias web, Thymeleaf, JPA, validación, PostgreSQL, H2 y pruebas. |
| `SaborGourmetApplication.java` | Arranca Spring Boot y detecta componentes de los paquetes subordinados. |
| `application.properties` | Configura H2 persistente, JPA y Thymeleaf para la ejecución directa. |
| `application-postgres.properties` | Permite conectar a PostgreSQL mediante variables de entorno. |
| `DatosIniciales.java` | Crea seis mesas al arrancar si no hay ninguna; evita duplicarlas en arranques posteriores. |

### Modelo de datos (`model`)

| Archivo | Función |
|---|---|
| `Cliente.java` | Entidad JPA con nombre, correo único y teléfono. Un cliente puede tener varias reservas. |
| `Mesa.java` | Entidad JPA con número único, capacidad y estado activo/inactivo. |
| `Reserva.java` | Entidad JPA que relaciona cliente y mesa; guarda inicio, fin, personas y estado. |
| `EstadoReserva.java` | Define los estados `ACTIVA` y `CANCELADA`. |

### Persistencia (`repository`)

| Archivo | Función |
|---|---|
| `ClienteRepository.java` | Busca clientes por correo ignorando mayúsculas. |
| `MesaRepository.java` | Busca mesas activas por capacidad, lista mesas y obtiene una mesa con bloqueo de escritura durante la reserva. |
| `ReservaRepository.java` | Busca reservas por correo, lista todas y cuenta cruces de horario de una mesa. |

La consulta de conflictos considera dos intervalos superpuestos cuando `inicioExistente < finSolicitado` y `finExistente > inicioSolicitado`. Las reservas canceladas no cuentan. El bloqueo pesimista de la mesa evita que dos operaciones simultáneas confirmen el mismo espacio y horario después de una consulta de disponibilidad.

### Lógica de negocio (`service`)

`ReservaService.java` concentra las reglas de reserva. `disponibles` filtra por fecha, capacidad, estado y cruces; `crear` y `modificar` validan y guardan dentro de una transacción; `cancelar` cambia el estado; `buscar`, `porCorreo` y `todas` entregan datos a los controladores. La lógica se ubica aquí para mantener los controladores enfocados en solicitudes y respuestas HTTP. Al modificar, el identificador de la reserva actual se excluye del control de cruces.

### Web (`web`)

| Archivo | Función |
|---|---|
| `ReservaForm.java` | Recibe y valida campos del formulario, sin exponer directamente la entidad JPA. |
| `MesaForm.java` | Valida número y capacidad de la mesa nueva. |
| `ReservaController.java` | Atiende inicio, disponibilidad, creación, búsqueda, edición y cancelación; muestra errores y mensajes de éxito. |
| `AdminController.java` | Muestra el panel, agrega mesas y actualiza capacidad/estado. |

Los controladores preparan datos del `Model` y devuelven nombres de plantillas Thymeleaf. Los formularios `POST` cambian datos; las rutas `GET` muestran páginas o resultados de consulta. Tras guardar se redirige para evitar un duplicado al recargar.

### Vistas y estilos

| Archivo | Función |
|---|---|
| `templates/inicio.html` | Portada y acceso a las funciones. |
| `templates/disponibilidad.html` | Búsqueda y lista de mesas libres. |
| `templates/form-reserva.html` | Formulario compartido para crear y modificar. |
| `templates/mis-reservas.html` | Consulta por correo, edición y cancelación. |
| `templates/admin.html` | Lista de reservas y configuración de mesas. |
| `templates/error.html` | Mensaje cuando una reserva o solicitud no existe. |
| `static/css/estilos.css` | Colores, tipografía, grillas, formularios y reglas `@media` para móviles/tabletas. |

Thymeleaf emplea `th:text` para mostrar valores escapados y `th:field` para enlazar y validar campos. Las vistas incluyen la etiqueta `viewport`; la grilla se reduce de tres columnas a una en pantallas pequeñas, y las tablas administrativas permiten desplazamiento horizontal.

## Estructura de la base de datos

```text
Cliente (id, nombre, email, telefono)
Mesa (id, numero, capacidad, activa)
Reserva (id, cliente_id, mesa_id, inicio, fin, personas, estado)
```

Relaciones: cada reserva pertenece a un cliente y a una mesa; un cliente y una mesa pueden tener muchas reservas. El correo y el número de mesa son únicos.

## Prueba incluida

`src/test/java/cl/ipss/saborgourmet/FlujoReservasManual.java` ejecuta un flujo de integración sobre H2 en memoria: disponibilidad inicial, creación, rechazo de doble reserva, modificación y cancelación. Puede ejecutarse desde un IDE como clase Java. `mvn test` también compila los archivos del proyecto, aunque esta clase es un ejecutable manual y no un caso JUnit.

## Alcance

El enunciado pide un panel de administración, pero no define autenticación. Por eso `/admin` es una interfaz funcional de demostración sin control de acceso. Del mismo modo, la búsqueda de reservas por correo funciona para la evaluación, pero una puesta en producción necesitaría verificación de identidad, autenticación de personal, protección CSRF y migraciones de base de datos versionadas.

## Entrega

- Código completo en esta carpeta.
- Informe en `EVU2_GRUPO_SECALL_ANDRE.docx` con portada, índice, introducción, desarrollo, conclusión y bibliografía.
- Reemplazar en la portada el texto **[INSERTAR LOGO IPSS]** por el logo institucional antes de enviar.

Fuentes técnicas: [Spring Boot](https://docs.spring.io/spring-boot/3.5/), [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/), [Thymeleaf](https://www.thymeleaf.org/documentation.html).
