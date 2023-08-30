package org.acme;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.acme.model.tables.Pessoas;
import org.acme.model.tables.records.PessoasRecord;
import org.jooq.DSLContext;
import org.jooq.InsertReturningStep;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BatchInsert {

    @Inject
    DSLContext dsl;

    ExecutorService executor = Executors.newFixedThreadPool(4);

    private static final int BATCH_SIZE = 2000;

    private final ConcurrentLinkedQueue<InsertReturningStep<PessoasRecord>> queue = new ConcurrentLinkedQueue<>();

    public void queueInsert(Pessoa p) {
        UUID id = UUID.randomUUID();
        p.setId(id.toString());

        PessoasRecord record = new PessoasRecord();
        record.setId(id);
        record.setApelido(p.getApelido());
        record.setNome(p.getNome());
        record.setNascimento(p.getNascimento());

        String stack = convertToDatabaseColumn(p.getStack());
        record.setStack(stack);

        var insert = dsl.insertInto(org.acme.model.tables.Pessoas.PESSOAS).set(record)
                .onConflict(Pessoas.PESSOAS.APELIDO)
                .doNothing();

        queue.offer(insert);
    }

    List smallBatchBuffer = new ArrayList(500);
    List largeBatchBuffer = new ArrayList(BATCH_SIZE);

    // Large batches
    public void execute() {
        while (!queue.isEmpty()) {
            queue.stream().limit(BATCH_SIZE).forEach((o) -> {
                largeBatchBuffer.add(o);
                queue.remove(o);
            });
            dsl.batch(largeBatchBuffer).executeAsync(executor);
        }
    }

    // Small batches
    @Scheduled(every = "5s")
    public void executeScheduled() {
        queue.stream().limit(500).forEach((o) -> {
            smallBatchBuffer.add(o);
            queue.remove(o);
        });
        dsl.batch(smallBatchBuffer).executeAsync(executor);
    }

    private String convertToDatabaseColumn(List<String> stringList) {
        return stringList != null ? String.join(";", stringList) : "";
    }

}
