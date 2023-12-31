# Rinha App com Quarkus

Essa implementação usa o seguinte stack:

- Quarkus 3
- OpenJDK 17
- PostgreSQL (latest)
- Nginx (latest)

## Como o sistema está ajustado

O foco dessa implementação é o método [K.I.S.S](https://www.baeldung.com/cs/kiss-software-design-principle#:~:text=KISS%20stands%20for%20Keep%20It,complexity%20as%20much%20as%20possible.).

Por exemplo, não tem cache (e.g., Redis), e o número de conexões com o banco está configurado para 20, por instância de API (40 total). Outro ajuste importante foi o heap da JVM para `80%`. Fora isso, o banco (PostgreSQL) e o load balancer (Nginx) tem algums ajustes:

### PostgreSQL

1. max_connections=200

Número máximo de conexões. Com certeza não precisamos mais do que 200. [Mais informações](https://postgresqlco.nf/doc/en/param/max_connections/).

2. shared_buffers=256MB

Aumenta o tamanho do buffer em memória para evitar escrita em disco. O padrão é 128MB. [Mais informações](https://postgresqlco.nf/doc/en/param/shared_buffers/).

3. synchronous_commit=off

Executa inserts mais rápido, retornando a conexão imediatamente para o pool. Com este parâmetro, atingimos "consistência eventual" no sistema. [Mais informações](https://postgresqlco.nf/doc/en/param/synchronous_commit/).

4. fsync=off

Diminui o número de chamadas para o disco já que não precisamos garantir o estado do banco em caso de crash. [Mais informações](https://postgresqlco.nf/doc/en/param/fsync/).

5. full_page_writes=off

Desabilita escritas do estado do banco após completar diversas atividades do banco. Por ser um benchmark, também não precisamos disso. [Mais informações](https://postgresqlco.nf/doc/en/param/full_page_writes/).

### Nginx

1. least_conn

Este modo de load balancer é similar ao round-robin, mas manda o requeste para a instância com menos conexões ativas. [Mais informações](https://nginx.org/en/docs/http/ngx_http_upstream_module.html#least_conn).

## Como rodar?

Use Docker Compose de acordo com o seu sistema operacional. 

Para hardware **x64**, basta executar o comando abaixo:

```bash
docker compose up
```

Se for um hardware **Apple M1 ou Linux Arm**, execute o comando abaixo:

```bash
docker compose -f docker-compose-arm.yml up
```

## Rodar com imagem local

Utilize os arquivos `docker-compose-local.yml` (x64) ou `docker-compose-local-arm.yml` (M1/Arm):

x64:
```bash
docker compose -f docker-compose-local.yml up
```

M1/Arm:
```bash
docker compose -f docker-compose-local-arm.yml up
```

## Testar aplicação para debug

Inicie o banco de dados:

x64: 
```bash
docker compose up -d db-postgresql
```

M1/Arm
```bash
docker compose -f docker-compose-arm.yml -d db-postgresql
```

Inicie a aplicação Quarkus:

```bash
./mvnw quarkus:dev
```

Accesse por [http://localhost:8080/contagem-pessoas](http://localhost:8080/contagem-pessoas).

## Código fonte

O código está no GitHub, no repositório [brunoborges/rinha-app](https://github.com/brunoborges/rinha-app).

## Resultados

Até o momento, o melhor resultado foi no meu MacBook M1:

<img width="1047" alt="image" src="https://github.com/brunoborges/rinha-app/assets/129743/d588df9b-5c96-4024-8600-b362dcf137ac">

Outros resultados executados via GitHub Actions:

- https://brunoborges.github.io/rinha-app/rinhabackendsimulation-20230821063351093/
  https://brunoborges.github.io/rinha-app/rinhabackendsimulation-20230821064423827/

## Autor

Bruno Borges ([@brunoborges](https://twitter.com/brunoborges)).

Qualquer dúvida, pergunta no Twitter.

## Licença

MIT
