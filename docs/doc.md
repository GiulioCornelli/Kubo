# Appunti 

Clone opensouce di google drive, per aumentare le mi skill di java.


Suddivizione dei Microservizi:

1. API Gateway (Spring Cloud Gateway)
È l'unico punto d'ingresso per il client. Instrada le richieste verso il servizio giusto, verifica il JWT, gestisce CORS e rate limiting. Non ha logica di business e non ha database.

2. Auth service
Gestisce registrazione e login ed emette i JWT. Possiede la tabella users. Gli altri servizi non chiamano questo servizio a ogni richiesta: validano il JWT da soli (Spring Security come resource server) e leggono lo user_id dal claim sub. Se usi Keycloak, questo servizio praticamente sparisce e ti resta al massimo un piccolo servizio per il profilo utente.

3. Metadata service
È il cuore del sistema. Possiede la tabella files (e le cartelle, quando le aggiungi): crea, rinomina, sposta, cestina, elenca il contenuto di una cartella. Non tocca mai i byte dei file, gestisce solo i metadati. A ogni operazione deve chiedersi "questo utente può farla su questo file?".

4. Permission service
Possiede la tabella permissions. Condivide un file con un utente, assegna un ruolo, revoca. Espone soprattutto un endpoint di controllo, tipo GET /permissions/check?fileId=...&userId=...&action=READ, che il File service chiama prima di agire. Qui c'è la parte difficile: l'ereditarietà dei permessi dalle cartelle ai file contenuti.

5. Storage service
È l'unico che conosce MinIO. Genera le presigned URL (anche multipart per i file grandi) e, se vuoi, riceve le notifiche di MinIO quando un upload è terminato. Isolarlo ti permette di cambiare backend (MinIO, SeaweedFS, S3) toccando un solo servizio.


### Scheam ER




### Librerie

API Gateway
- spring-cloud-starter-gateway: il nome dello starter è cambiato nelle release recenti (gateway-server-webflux / gateway-server-webmvc), quindi controlla quello della tua versione
- spring-boot-starter-oauth2-resource-server: valida il JWT all'ingresso
- spring-cloud-starter-circuitbreaker-reactor-resilience4j: opzionale, circuit breaker e fallback
- spring-boot-starter-data-redis-reactive: opzionale, solo se vuoi il rate limiting (RequestRateLimiter)

Niente database, niente JPA. Il gateway è reattivo (WebFlux), quindi non mischiare spring-boot-starter-web.

Auth-Service
- spring-boot-starter-web
- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server: porta con sé Nimbus (NimbusJwtEncoder), che ti serve per firmare i JWT. Firma con chiave RSA e pubblica il JWKS, così gli altri servizi validano senza condividere segreti
- spring-boot-starter-data-jpa
- postgresql (driver)
flyway-core + flyway-database-postgresql
Per l'hash delle password usi BCryptPasswordEncoder, già incluso in Spring Security (per Argon2 serve anche bcprov-jdk18on)
spring-boot-starter-amqp: se pubblichi gli eventi UserRegistered su RabbitMQ (con la tabella outbox)

In alternativa a tutto questo: Keycloak come container e nei servizi solo il resource server. Per un progetto di studio scrivere Auth a mano è più istruttivo. Esiste anche spring-boot-starter-oauth2-authorization-server, ma è più complesso del necessario per il tuo caso.

Meta-Service
spring-boot-starter-web
spring-boot-starter-oauth2-resource-server
spring-boot-starter-data-jpa
postgresql
flyway-core + flyway-database-postgresql
mapstruct + mapstruct-processor: mapping entity ↔ DTO. Se usi anche Lombok, aggiungi lombok-mapstruct-binding
spring-boot-starter-amqp: per consumare gli eventi utente e aggiornare la copia locale di users
Per chiamare Storage e Permis non serve nulla di extra: Spring ha già RestClient e le interfacce dichiarative @HttpExchange. spring-cloud-starter-openfeign è l'alternativa classica
resilience4j-spring-boot3: opzionale, retry e timeout per le chiamate tra servizi
Permis-Service

Lo stack è quasi identico a Meta, visto che condividono il DB:

spring-boot-starter-web
spring-boot-starter-oauth2-resource-server
spring-boot-starter-data-jpa
postgresql
flyway-core + flyway-database-postgresql
mapstruct (+ processor)
spring-boot-starter-amqp: per reagire a FileDeleted e simili

Dato che condividono il database, decidi chi possiede le migrazioni. Se le eseguono entrambi, dai a ciascuno una tabella di storico diversa con spring.flyway.table, altrimenti si pestano i piedi.

Storage-Service
- spring-boot-starter-web
- spring-boot-starter-oauth2-resource-server
- Client S3, a scelta:
software.amazon.awssdk:s3 (AWS SDK v2, con software.amazon.awssdk:bom): include S3Presigner per le presigned URL. È la mia scelta, perché funziona con qualsiasi backend S3-compatibile (MinIO, SeaweedFS, Garage)
