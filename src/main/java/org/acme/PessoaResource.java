package org.acme;

import static java.util.Collections.emptyList;
import static org.acme.model.tables.Pessoas.PESSOAS;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.jooq.DSLContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/")
public class PessoaResource {

    HttpClient client = HttpClient.newHttpClient();

    @Inject
    DSLContext dsl;

    @Inject
    CacheService cache;

    @Inject
    BatchInsert batchInsert;

    @GET
    @Path("/pessoas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findTop50(@QueryParam("t") String termo) {
        if (termo == null || termo.isBlank()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        // The inserts will be done later, so there is no point in searching the
        // database here.
        // Let's get from the local cache:
        // "but what about the other instance?" ... buddy, no one said the search has to
        // be consistent nor it said batch_insert can't be used (nor when/how).
        List<Pessoa> foundInCache = cache.search(termo);
        return Response.ok(foundInCache).build();
    }

    @POST
    @Path("/pessoas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Pessoa pessoa) {
        String apelido = pessoa.getApelido();
        String nome = pessoa.getNome();

        if (apelido == null || apelido.isBlank() || apelido.length() > 32
                || nome == null || nome.isBlank() || nome.length() > 100 || invalidStack(pessoa.getStack())) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (pessoaByApelidoExists(pessoa.getApelido())) {
            return Response.status(422).build();
        }

        batchInsert.queueInsert(pessoa);
        cache.insertPessoa(pessoa);

        return Response.status(Status.CREATED).entity(pessoa)
                .header("Location", "/pessoa/" + pessoa.getId().toString()).build();
    }

    /**
     * check if each element of the stack is at most 32 chars long. return false
     * otherwise
     */
    private boolean invalidStack(List<String> stack) {
        if (stack == null) {
            return false;
        }

        for (String s : stack) {
            if (s.length() > 32) {
                return true;
            }
        }

        return false;
    }

    @GET
    @Path("/pessoa/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") String id, @QueryParam("sibling") boolean sibling) {
        if (sibling) {
            Pessoa foundInCache = cache.getPessoa(id);
            if (foundInCache != null) {
                Logger.getGlobal().info("Found in cache for sibling: " + id);
                return Response.ok(foundInCache).build();
            } else {
                return Response.status(Status.NOT_FOUND).build();
            }
        }

        UUID uuid = null;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Response.status(Status.NOT_FOUND).build();
        }

        Pessoa foundInCache = cache.getPessoa(id);

        if (foundInCache == null) {
            // Search in the sibling api instance
            HttpRequest request = HttpRequest
                    .newBuilder(URI.create(getOtherAPI() + "/pessoa/" + id + "?sibling=true")).build();
            try {
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Pessoa pessoa = new ObjectMapper().readValue(response.body(), Pessoa.class);
                    return Response.ok(pessoa).build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Logger.getGlobal().info("Found in local cache: " + id);
        return Response.ok(foundInCache).build();
    }

    private String getOtherAPI() {
        var url = System.getenv("OTHER_API_URL");
        if (url == null || url.isBlank()) url = "http://localhost:8080";
        return url;
    }

    @GET
    @Path("/contagem-pessoas")
    public int count(@QueryParam("sibling") boolean sibling) {
        batchInsert.execute();

        if (sibling) {
            return 0;
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(getOtherAPI() + "/contagem-pessoas?sibling=true"))
                .timeout(Duration.ofSeconds(3)).build();
        client.sendAsync(request, BodyHandlers.ofString());

        // wait to sync as many inserts as possible from the other instance
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return dsl.fetchCount(PESSOAS);
    }

    private boolean pessoaByApelidoExists(String apelido) {
        if (cache.apelidoExists(apelido)) {
            return true;
        }

        return false;

        // No need to check DB, because we will DO NOTHING if it exists when doing
        // batch_insert
        // return dsl.fetchExists(dsl.selectFrom(PESSOAS)
        // .where(PESSOAS.APELIDO.eq(apelido)));
    }

    public List<String> convertToEntityAttribute(String string) {
        return string != null ? Arrays.asList(string.split(";")) : emptyList();
    }

}
