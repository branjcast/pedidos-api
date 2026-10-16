package com.pedidos.infrastructure;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PedidoApiTest {

    static String token;

    @BeforeAll
    static void login() {
        token = given().contentType(ContentType.JSON)
                .body(Map.of("correo", "admin@pedidos.local", "password", "Admin123!"))
                .post("/api/auth/login")
                .then().statusCode(200).body("token", notNullValue())
                .extract().path("token");
    }

    private static Map<String, Object> pedido(String sku, String nombre, String correo) {
        return Map.of(
                "canal", "APP",
                "cliente", Map.of("nombre", nombre, "apellidoPaterno", "Pérez", "apellidoMaterno", "Soto", "correo", correo),
                "direccionEnvio", "Calle 1, CDMX",
                "codigoProducto", sku,
                "cantidad", 3,
                "precio", 10.50);
    }

    @Test
    void sinTokenDevuelve401() {
        given().get("/api/pedidos").then().statusCode(401);
    }

    @Test
    void loginIncorrectoDevuelve401() {
        given().contentType(ContentType.JSON)
                .body(Map.of("correo", "admin@pedidos.local", "password", "mala"))
                .post("/api/auth/login")
                .then().statusCode(401).body("codigo", equalTo("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void crudCompletoDePedido() {
        String id = given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(pedido("CRUD-1", "Ana", "ana@correo.com"))
                .post("/api/pedidos")
                .then().statusCode(201).header("Location", containsString("/api/pedidos/"))
                .body("estatus", equalTo("PENDIENTE")).body("total", equalTo(31.5f))
                .extract().path("id");

        given().auth().oauth2(token).get("/api/pedidos/" + id).then().statusCode(200).body("codigoProducto", equalTo("CRUD-1"));

        given().auth().oauth2(token).contentType(ContentType.JSON).body(Map.of("estatus", "ENVIADO"))
                .patch("/api/pedidos/" + id + "/estatus").then().statusCode(200).body("estatus", equalTo("ENVIADO"));

        given().auth().oauth2(token).delete("/api/pedidos/" + id).then().statusCode(204);
        given().auth().oauth2(token).get("/api/pedidos/" + id).then().statusCode(404);
    }

    @Test
    void validacionDevuelve400() {
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("canal", "WEB")).post("/api/pedidos").then().statusCode(400);
    }

    @Test
    void typeaheadSugiereYAvisaCuandoNoHayResultados() {
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(pedido("TYPE-777", "Beatriz", "bea@correo.com")).post("/api/pedidos").then().statusCode(201);

        given().auth().oauth2(token).queryParam("q", "type-7")
                .get("/api/pedidos/typeahead")
                .then().statusCode(200)
                .body("sugerencias.valor", hasItem("TYPE-777"))
                .body("mensaje", org.hamcrest.Matchers.nullValue());

        given().auth().oauth2(token).queryParam("q", "zzz-inexistente")
                .get("/api/pedidos/typeahead")
                .then().statusCode(200)
                .body("sugerencias", empty())
                .body("mensaje", equalTo("No se tienen resultados que coincidan con los criterios de búsqueda"));

        given().auth().oauth2(token).queryParam("q", "zzz-inexistente")
                .get("/api/pedidos")
                .then().statusCode(200)
                .body("total", equalTo(0))
                .body("mensaje", containsString("No se tienen resultados"));

        given().auth().oauth2(token).queryParam("q", "beatriz")
                .get("/api/pedidos")
                .then().statusCode(200).body("items.codigoProducto", hasItem("TYPE-777")).body("mensaje", org.hamcrest.Matchers.nullValue());
    }

    @Test
    void crudDeUsuariosSoloAdmin() {
        String correo = "usuario.test@correo.com";
        String id = given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("nombre", "Test", "apellidoPaterno", "Uno", "correo", correo, "password", "Secreta123!", "rol", "USER"))
                .post("/api/usuarios").then().statusCode(201).body("rol", equalTo("USER")).extract().path("id");

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("nombre", "Test", "apellidoPaterno", "Uno", "correo", correo, "password", "Secreta123!"))
                .post("/api/usuarios").then().statusCode(409);

        String tokenUser = given().contentType(ContentType.JSON)
                .body(Map.of("correo", correo, "password", "Secreta123!")).post("/api/auth/login")
                .then().statusCode(200).extract().path("token");

        given().auth().oauth2(tokenUser).get("/api/usuarios").then().statusCode(403);
        given().auth().oauth2(tokenUser).get("/api/pedidos").then().statusCode(200);

        given().auth().oauth2(token).delete("/api/usuarios/" + id).then().statusCode(204);
    }
}
