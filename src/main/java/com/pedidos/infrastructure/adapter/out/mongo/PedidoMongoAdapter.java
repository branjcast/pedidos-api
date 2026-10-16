package com.pedidos.infrastructure.adapter.out.mongo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.regex;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import com.pedidos.application.port.in.FiltroPedidos;
import com.pedidos.application.port.out.PedidoRepositoryPort;
import com.pedidos.domain.model.CanalPedido;
import com.pedidos.domain.model.Cliente;
import com.pedidos.domain.model.EstatusPedido;
import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Pedido;
import com.pedidos.domain.model.Sugerencia;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PedidoMongoAdapter implements PedidoRepositoryPort {

    static final String COLECCION = "pedidos";

    private static final String[][] CAMPOS_BUSQUEDA = {
        {"codigoProducto", "codigoProducto"},
        {"cliente.nombreCompleto", "cliente"},
        {"cliente.correo", "correo"}
    };

    private final MongoCollection<Document> coleccion;

    public PedidoMongoAdapter(MongoClient cliente, @ConfigProperty(name = "quarkus.mongodb.database") String baseDatos) {
        this.coleccion = cliente.getDatabase(baseDatos).getCollection(COLECCION);
    }

    void crearIndices(@Observes StartupEvent evento) {
        coleccion.createIndex(Indexes.descending("creadoEn"));
        coleccion.createIndex(Indexes.ascending("estatus"));
        coleccion.createIndex(Indexes.ascending("canal"));
        coleccion.createIndex(Indexes.ascending("codigoProducto"), new IndexOptions().name("idx_codigoProducto"));
        coleccion.createIndex(Indexes.ascending("cliente.correo"));
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        coleccion.replaceOne(eq("_id", pedido.id()), aDocumento(pedido), new ReplaceOptions().upsert(true));
        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPorId(String id) {
        return Optional.ofNullable(coleccion.find(eq("_id", id)).first()).map(PedidoMongoAdapter::aDominio);
    }

    @Override
    public Pagina<Pedido> buscar(FiltroPedidos filtro) {
        List<Bson> condiciones = new ArrayList<>();
        if (filtro.termino() != null) {
            Pattern patron = patron(filtro.termino());
            List<Bson> alternativas = new ArrayList<>();
            for (String[] campo : CAMPOS_BUSQUEDA) {
                alternativas.add(regex(campo[0], patron));
            }
            condiciones.add(or(alternativas));
        }
        if (filtro.estatus() != null) {
            condiciones.add(eq("estatus", filtro.estatus().name()));
        }
        if (filtro.canal() != null) {
            condiciones.add(eq("canal", filtro.canal().name()));
        }
        Bson criterio = condiciones.isEmpty() ? new Document() : and(condiciones);

        long total = coleccion.countDocuments(criterio);
        List<Pedido> items = new ArrayList<>();
        coleccion.find(criterio)
                .sort(Sorts.descending("creadoEn"))
                .skip(filtro.page() * filtro.size())
                .limit(filtro.size())
                .forEach(doc -> items.add(aDominio(doc)));
        return new Pagina<>(items, filtro.page(), filtro.size(), total);
    }

    @Override
    public boolean eliminar(String id) {
        return coleccion.deleteOne(eq("_id", id)).getDeletedCount() > 0;
    }

    @Override
    public List<Sugerencia> sugerir(String termino, int limite) {
        Pattern patron = patron(termino);
        String minusculas = termino.toLowerCase(Locale.ROOT);
        List<Sugerencia> candidatas = new ArrayList<>();
        for (String[] campo : CAMPOS_BUSQUEDA) {
            coleccion.aggregate(List.of(
                            Aggregates.match(regex(campo[0], patron)),
                            Aggregates.group("$" + campo[0]),
                            Aggregates.sort(Sorts.ascending("_id")),
                            Aggregates.limit(limite)))
                    .forEach(doc -> candidatas.add(new Sugerencia(campo[1], String.valueOf(doc.get("_id")))));
        }

        return candidatas.stream()
                .sorted(Comparator
                        .comparing((Sugerencia s) -> !s.valor().toLowerCase(Locale.ROOT).startsWith(minusculas))
                        .thenComparing(s -> s.valor().toLowerCase(Locale.ROOT)))
                .limit(limite)
                .toList();
    }

    private static Pattern patron(String termino) {
        return Pattern.compile(Pattern.quote(termino), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    static Document aDocumento(Pedido p) {
        Cliente c = p.cliente();
        Document cliente = new Document("nombre", c.nombre())
                .append("apellidoPaterno", c.apellidoPaterno())
                .append("apellidoMaterno", c.apellidoMaterno())
                .append("correo", c.correo())
                .append("nombreCompleto", c.nombreCompleto());
        return new Document("_id", p.id())
                .append("canal", p.canal().name())
                .append("cliente", cliente)
                .append("direccionEnvio", p.direccionEnvio())
                .append("codigoProducto", p.codigoProducto())
                .append("cantidad", p.cantidad())
                .append("precio", new Decimal128(p.precio()))
                .append("estatus", p.estatus().name())
                .append("creadoEn", Date.from(p.creadoEn()))
                .append("actualizadoEn", Date.from(p.actualizadoEn()));
    }

    static Pedido aDominio(Document d) {
        Document c = d.get("cliente", Document.class);
        Cliente cliente = new Cliente(
                c.getString("nombre"), c.getString("apellidoPaterno"), c.getString("apellidoMaterno"), c.getString("correo"));
        return new Pedido(
                d.getString("_id"),
                CanalPedido.valueOf(d.getString("canal")),
                cliente,
                d.getString("direccionEnvio"),
                d.getString("codigoProducto"),
                d.getInteger("cantidad"),
                d.get("precio", Decimal128.class).bigDecimalValue(),
                EstatusPedido.valueOf(d.getString("estatus")),
                d.getDate("creadoEn").toInstant(),
                d.getDate("actualizadoEn").toInstant());
    }
}
