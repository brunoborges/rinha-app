package org.acme;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Pessoa {
    
    private String id;
    private String apelido;
    private String nome;
    private String nascimento;
    private List<String> stack = Collections.emptyList();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNascimento() {
        return nascimento;
    }

    public void setNascimento(String nascimento) {
        this.nascimento = nascimento;
    }

    public List<String> getStack() {
        return stack;
    }

    public void setStack(List<String> stack) {
        if (stack == null) {
            stack = Collections.emptyList();
        }
        this.stack = stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(id, pessoa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toJSONString() {
        return "{" +
                "\"id\":\"" + id + "\"," +
                "\"apelido\":\"" + apelido + "\"," +
                "\"nome\":\"" + nome + "\"," +
                "\"nascimento\":\"" + nascimento + "\"," +
                "\"stack\":" + stack +
                '}';
    }

    @Override
    public String toString() {
        return "Pessoa{" +
                "id=" + id +
                ", apelido='" + apelido + '\'' +
                ", nome='" + nome + '\'' +
                ", nascimento='" + nascimento + '\'' +
                ", stack=" + stack +
                '}';
    }
}
