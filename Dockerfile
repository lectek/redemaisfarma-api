# ========================
# 🏗️ FASE DE BUILD
# ========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /workspace
ENV SPRING_PROFILES_ACTIVE=docker

# Copia o POM e o Maven Wrapper (para cache de dependências)
COPY pom.xml mvnw* ./
COPY .mvn .mvn

# Cache das dependências
RUN mvn -B -ntp dependency:go-offline

# Copia o código-fonte
COPY src src

# Compila e empacota o JAR (sem testes)
RUN mvn -B -ntp -DskipTests=true package


# ========================
# 🚀 FASE DE EXECUÇÃO
# ========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# (1) Instala curl e certificados na imagem final (para healthchecks e debug)
USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# (2) Copia o artefato gerado
COPY --from=builder /workspace/target/*.jar /app/app.jar

# (3) Opcional: usuário não-root para rodar o app
#    (se preferir ficar como root, pode remover esta seção)
RUN useradd -r -u 1001 -g root appuser \
 && chown -R appuser:root /app
USER appuser

# (4) Configuração de memória e codificação
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

# (5) Ativa logs coloridos e perfis via env externo (compose .env/.env.local)
ENV SPRING_OPTS="--spring.output.ansi.enabled=ALWAYS --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"

# Expondo a porta padrão do app
EXPOSE 8080

# Comando de entrada (usa 'exec' para repassar sinais ao Java)
ENTRYPOINT ["sh", "-lc", "exec java $JAVA_OPTS -jar /app/app.jar $SPRING_OPTS"]
