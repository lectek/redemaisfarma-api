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

# Copia o artefato gerado
COPY --from=builder /workspace/target/*.jar app.jar

# Configuração de memória e codificação
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

# Ativa logs coloridos e suporte a perfis externos
ENV SPRING_OPTS="--spring.output.ansi.enabled=ALWAYS --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"

# Expondo a porta padrão do app
EXPOSE 8080

# Comando de entrada
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar $SPRING_OPTS"]
