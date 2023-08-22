package org.acme;

import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.acme.model.tables.records.PessoasRecord;
import org.jooq.DSLContext;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import static org.acme.model.tables.Pessoas.PESSOAS;

@Path("/")
public class PessoaResource {

    @Inject
    DSLContext dsl;

    @Inject
    LocalCache cache;

    @GET
    @Path("/pessoas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findTop50(@QueryParam("t") String termo) {
        if (termo == null || termo.isBlank()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        List<Pessoa> results = dsl.selectFrom(PESSOAS)
                .where(PESSOAS.APELIDO.like("%" + termo + "%")
                        .or(PESSOAS.NOME.like("%" + termo + "%"))
                        .or(PESSOAS.STACK.like("%" + termo + "%")))
                .limit(50)
                .fetch()
                .map(record -> {
                    Pessoa pessoa = new Pessoa();
                    pessoa.setId(record.getId());
                    pessoa.setApelido(record.getApelido());
                    pessoa.setNome(record.getNome());
                    pessoa.setNascimento(record.getNascimento());
                    pessoa.setStack(convertToEntityAttribute(record.getStack()));
                    return pessoa;
                });

        return Response.ok(results).build();
    }

    @POST
    @Path("/pessoas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Pessoa pessoa) {
        String apelido = pessoa.getApelido();
        String nome = pessoa.getNome();

        if (apelido == null || apelido.isBlank() || apelido.length() > 32
                || nome == null || nome.isBlank() || nome.length() > 100) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (pessoaByApelidoExists(pessoa.getApelido())) {
            return Response.status(422).build();
        }

        pessoa.setId(UUID.randomUUID());

        PessoasRecord record = new PessoasRecord();
        record.setId(pessoa.getId());
        record.setApelido(pessoa.getApelido());
        record.setNome(pessoa.getNome());
        record.setNascimento(pessoa.getNascimento());
        record.setStack(convertToDatabaseColumn(pessoa.getStack()));

        dsl.insertInto(org.acme.model.tables.Pessoas.PESSOAS)
                .set(record)
                .execute();

        cache.addApelido(pessoa.getApelido());

        return Response.status(Status.CREATED).entity(pessoa)
                .header("Location", "/pessoa/" + pessoa.getId().toString()).build();
    }

    @GET
    @Path("/pessoa/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(String id) {
        UUID uuid = null;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Response.status(Status.NOT_FOUND).build();
        }

        PessoasRecord record = dsl.selectFrom(org.acme.model.tables.Pessoas.PESSOAS)
                .where(PESSOAS.ID.eq(uuid))
                .fetchOne();

        if (record == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        Pessoa pessoa = new Pessoa();
        pessoa.setId(record.getId());
        pessoa.setApelido(record.getApelido());
        pessoa.setNome(record.getNome());
        pessoa.setNascimento(record.getNascimento());
        pessoa.setStack(convertToEntityAttribute(record.getStack()));

        return Response.ok(pessoa).build();
    }

    @GET
    @Path("/contagem-pessoas")
    public int count() {
        return dsl.fetchCount(PESSOAS);
    }

    private boolean pessoaByApelidoExists(String apelido) {
        if (cache.containsApelido(apelido)) {
            return true;
        }

        return dsl.fetchExists(dsl.selectFrom(PESSOAS)
                .where(PESSOAS.APELIDO.eq(apelido)));
    }

    public String convertToDatabaseColumn(List<String> stringList) {
        return stringList != null ? String.join(";", stringList) : null;
    }

    public List<String> convertToEntityAttribute(String string) {
        return string != null ? Arrays.asList(string.split(";")) : emptyList();
    }

}
