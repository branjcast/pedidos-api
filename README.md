# API de Gestión de Pedidos

Backend en **Quarkus 3 (Java 21) + MongoDB** para gestionar pedidos hechos desde la **app** y el **sitio web**.
Arquitectura hexagonal, autenticación con JWT, documentación OpenAPI/Swagger y listo para desplegar en **OpenShift**.

## Arquitectura hexagonal

```
src/main/java/com/pedidos
├── domain/                      ← núcleo: modelo y reglas (Java puro)
│   ├── model/                   Pedido, Cliente, Usuario, EstatusPedido, CanalPedido, Rol, Pagina, Sugerencia
│   └── exception/
├── application/                 ← casos de uso (Java puro, sin frameworks)
│   ├── port/in/                 PedidoUseCase, UsuarioUseCase, AuthUseCase  (lo que ofrece la app)
│   ├── port/out/                PedidoRepositoryPort, UsuarioRepositoryPort, PasswordHasherPort, TokenGeneratorPort
│   └── service/                 PedidoService, UsuarioService, AuthService
└── infrastructure/              ← adaptadores y wiring con Quarkus
    ├── adapter/in/rest/         Resources JAX-RS, DTOs, validaciones, OpenAPI, mapeo de errores
    ├── adapter/out/mongo/       PedidoMongoAdapter, UsuarioMongoAdapter
    ├── adapter/out/security/    BcryptPasswordHasher, JwtTokenGenerator
    └── config/                  BeanConfig (ensambla servicios), AdminBootstrap
```

`domain` y `application` no importan Quarkus, Mongo ni Jakarta: se prueban con JUnit simple. Las dependencias apuntan siempre hacia adentro.

## Modelo

| Pedido | Campo |
|---|---|
| Cliente | `nombre`, `apellidoPaterno`, `apellidoMaterno` (opcional), `correo` |
| Entrega | `direccionEnvio` |
| Pedido | `codigoProducto`, `cantidad`, `precio` (unitario), `estatus` (`PENDIENTE`, `CONFIRMADO`, `EN_PREPARACION`, `ENVIADO`, `ENTREGADO`, `CANCELADO`) |
| Extra | `canal` (`APP` / `WEB`), `total` (precio × cantidad), `creadoEn`, `actualizadoEn` |

## Endpoints

| Método | Ruta | Descripción | Rol |
|---|---|---|---|
| POST | `/api/auth/login` | Devuelve JWT | público |
| POST/GET | `/api/pedidos` | Crear / listar (`q`, `estatus`, `canal`, `page`, `size`) | ADMIN, USER |
| GET | `/api/pedidos/typeahead?q=` | Sugerencias mientras se escribe | ADMIN, USER |
| GET/PUT | `/api/pedidos/{id}` | Consultar / actualizar | ADMIN, USER |
| PATCH | `/api/pedidos/{id}/estatus` | Cambiar estatus | ADMIN, USER |
| DELETE | `/api/pedidos/{id}` | Eliminar | ADMIN |
| POST/GET | `/api/usuarios` | Crear / listar usuarios | ADMIN |
| GET/PUT/DELETE | `/api/usuarios/{id}` | Consultar / actualizar / eliminar | ADMIN |

Documentación: **Swagger UI** `/swagger-ui` · **OpenAPI** `/openapi` · Salud `/q/health`.

### Typeahead
- `GET /api/pedidos/typeahead?q=sku` devuelve opciones (`codigoProducto`, `cliente`, `correo`) que **contienen** el texto, sin distinguir mayúsculas, las que empiezan con el texto primero.
- Si no hay coincidencias: `sugerencias: []` y `mensaje: "No se tienen resultados que coincidan con los criterios de búsqueda"`.
- El valor elegido se usa como filtro: `GET /api/pedidos?q=<valor>`. Si la lista queda vacía, también trae ese `mensaje`.
- Hay una página demo en `/` (login + lista + campo typeahead con debounce y teclado).

## Ejecutar en local

Requisitos: JDK 21, Maven 3.9+ y Docker/Podman (Quarkus Dev Services levanta MongoDB solo).

```bash
mvn quarkus:dev
```

- Demo: http://localhost:8080/ · Swagger: http://localhost:8080/swagger-ui
- Usuario de desarrollo: `admin@pedidos.local` / `Admin123!`

Con un MongoDB propio en lugar de Dev Services: `mvn quarkus:dev -Dquarkus.mongodb.connection-string=mongodb://localhost:27017`.

```bash
# Login y uso del token
TOKEN=$(curl -s localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"correo":"admin@pedidos.local","password":"Admin123!"}' | jq -r .token)

curl -s localhost:8080/api/pedidos -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{
  "canal":"APP",
  "cliente":{"nombre":"María","apellidoPaterno":"González","apellidoMaterno":"Pérez","correo":"maria@correo.com"},
  "direccionEnvio":"Av. Reforma 222, CDMX",
  "codigoProducto":"SKU-1001","cantidad":2,"precio":199.90}'

curl -s "localhost:8080/api/pedidos/typeahead?q=mar" -H "Authorization: Bearer $TOKEN"
```

## Pruebas

```bash
mvn verify
```
- `PedidoServiceTest`: casos de uso con repositorio en memoria (no requiere Docker).
- `PedidoApiTest`: integración de la API con MongoDB real vía Dev Services (requiere Docker/Podman).

## Despliegue en OpenShift

Configuración por variables de entorno:

| Variable | Descripción |
|---|---|
| `MONGODB_URI` | **Obligatoria en prod.** Cadena de conexión de MongoDB |
| `MONGODB_DATABASE` | Base de datos (default `pedidos`) |
| `JWT_PRIVATE_KEY_PATH` / `JWT_PUBLIC_KEY_PATH` | Rutas de las llaves RSA (el Deployment las monta en `/etc/jwt`) |
| `JWT_ISSUER`, `JWT_EXPIRATION_MINUTES` | Emisor y vigencia del token |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Crean el admin inicial si no existe (opcional) |
| `CORS_ORIGINS` | Orígenes permitidos (default `*`) |

Pasos:

```bash
oc new-project pedidos

# 1) Llaves JWT propias (NO uses las de dev-keys/ en producción)
mkdir -p secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/privateKey.pem
openssl rsa -pubout -in secrets/privateKey.pem -out secrets/publicKey.pem
oc create secret generic pedidos-api-jwt-keys \
  --from-file=privateKey.pem=secrets/privateKey.pem --from-file=publicKey.pem=secrets/publicKey.pem

# 2) Secretos de la app
oc create secret generic pedidos-api-secrets \
  --from-literal=MONGODB_URI='mongodb://usuario:clave@host:27017/pedidos?authSource=admin' \
  --from-literal=ADMIN_EMAIL='jcastillo.brandon@gmail.com' \
  --from-literal=ADMIN_PASSWORD='una-clave-segura'

# 3) Edita openshift/buildconfig.yaml (URL de tu repo Git) y aplica todo
oc apply -k openshift/

# 4) Construye la imagen; el Deployment se actualiza solo al terminar
oc start-build pedidos-api --follow

# 5) URL pública
oc get route pedidos-api -o jsonpath='https://{.spec.host}{"\n"}'
```

El Deployment usa 2 réplicas, rolling update sin caída, probes en `/q/health/*`, límites de recursos y contexto de seguridad
compatible con el SCC `restricted` (usuario no root).

## Notas de seguridad
- Las llaves de `src/main/resources/dev-keys/` son **solo para desarrollo** y están en el repo; en producción se reemplazan por las del Secret.
- Contraseñas con bcrypt; los usuarios inactivos no pueden iniciar sesión.
- `quarkus.security.jaxrs.deny-unannotated-endpoints=true`: un endpoint nuevo sin `@RolesAllowed`/`@PermitAll` queda denegado.
