FROM eclipse-temurin:23-jdk-alpine AS build

WORKDIR /app

# Copia arquivos Maven
COPY pom.xml .
COPY src ./src

# Instala Maven e compila o projeto
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar

# Estágio final
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Cria diretório para dados processados
RUN mkdir -p /app/data/processed

# Copia o JAR do estágio de build
COPY --from=build /app/app.jar .

# Variáveis de ambiente
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
