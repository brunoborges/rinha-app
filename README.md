# Rinha App

Projeto que implementa a Rinha de Backend.

## Como rodar?

Use Docker Compose de acordo com o seu sistema operacional. 

Para hardware **Intel**, basta executar o comando abaixo:

```bash
docker-compose up
```

Se for um hardware **Apple M1 ou Linux Arm**, execute o comando abaixo:

```bash
docker-compose -f docker-compose-arm.yml up
```

## Rodar com imagem local
Por padrão, os arquivos do Docker Compose apontam para imagems já construídas e disponíveis no Docker Hub. Caso queira construir as imagens localmente, mude as configurações dos serviços `api1` e `api2` no(s) arquivo(s) `docker-compose.yml` (ou `docker-compose-arm.yml` para M1/Arm) para:

* Intel

```diff
-    image: brunoborges/rinha-brborges-api:latest
+    build:
+      context: .
+      dockerfile: src/main/docker/Dockerfile.jvm
```

* M1/Arm

```diff
-    image: brunoborges/rinha-brborges-api:arm64
+    build:
+      context: .
+      dockerfile: src/main/docker/Dockerfile.arm
```

# Testar aplicação para debug

Inicie o banco de dados:

```bash
docker-compose up -d db-postgresql
```

Inicie a aplicação Quarkus:

```bash
./mvnw quarkus:dev
```

Qualquer dúvida, entre em contato comigo pelo Twitter: [@brunoborges](https://twitter.com/brunoborges)
