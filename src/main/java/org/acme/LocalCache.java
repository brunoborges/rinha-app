package org.acme;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LocalCache {

    private Set<String> apelidos = new HashSet<>(10000);
    private Object lock = new Object();

    public boolean containsApelido(String apelido) {
        return apelidos.contains(apelido);
    }

    public void addApelido(String apelido) {
        synchronized(lock) {
            apelidos.add(apelido);
        }
    }
    
}
