package com.pedidos.infrastructure.adapter.out.mongo;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import com.pedidos.application.port.out.UsuarioRepositoryPort;
import com.pedidos.domain.exception.ConflictoException;
import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Rol;
import com.pedidos.domain.model.Usuario;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UsuarioMongoAdapter implements UsuarioRepositoryPort {

    static final String COLECCION = "usuarios";

    private final MongoCollection<Document> coleccion;

    public UsuarioMongoAdapter(MongoClient cliente, @ConfigProperty(name = "quarkus.mongodb.database") String baseDatos) {
        this.coleccion = cliente.getDatabase(baseDatos).getCollection(COLECCION);
    }

    void crearIndices(@Observes StartupEvent evento) {
        coleccion.createIndex(Indexes.ascending("correo"), new IndexOptions().unique(true).name("uk_correo"));
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        try {
            coleccion.replaceOne(eq("_id", usuario.id()), aDocumento(usuario), new ReplaceOptions().upsert(true));
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                throw new ConflictoException("Ya existe un usuario con el correo " + usuario.correo());
            }
            throw e;
        }
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(coleccion.find(eq("_id", id)).first()).map(UsuarioMongoAdapter::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return Optional.ofNullable(coleccion.find(eq("correo", correo)).first()).map(UsuarioMongoAdapter::aDominio);
    }

    @Override
    public Pagina<Usuario> listar(int page, int size) {
        List<Usuario> items = new ArrayList<>();
        coleccion.find().sort(Sorts.ascending("apellidoPaterno", "nombre"))
                .skip(page * size).limit(size)
                .forEach(doc -> items.add(aDominio(doc)));
        return new Pagina<>(items, page, size, coleccion.countDocuments());
    }

    @Override
    public boolean eliminar(String id) {
        return coleccion.deleteOne(eq("_id", id)).getDeletedCount() > 0;
    }

    static Document aDocumento(Usuario u) {
        return new Document("_id", u.id())
                .append("nombre", u.nombre())
                .append("apellidoPaterno", u.apellidoPaterno())
                .append("apellidoMaterno", u.apellidoMaterno())
                .append("correo", u.correo())
                .append("passwordHash", u.passwordHash())
                .append("rol", u.rol().name())
                .append("activo", u.activo())
                .append("creadoEn", Date.from(u.creadoEn()));
    }

    static Usuario aDominio(Document d) {
        return new Usuario(
                d.getString("_id"),
                d.getString("nombre"),
                d.getString("apellidoPaterno"),
                d.getString("apellidoMaterno"),
                d.getString("correo"),
                d.getString("passwordHash"),
                Rol.valueOf(d.getString("rol")),
                d.getBoolean("activo", true),
                d.getDate("creadoEn").toInstant());
    }
}
