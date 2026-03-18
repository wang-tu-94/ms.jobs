# --- Étape 1 : Build ---
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

# 1. Copie des fichiers de configuration Gradle pour mettre les dépendances en cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# On télécharge les dépendances (cette étape sera mise en cache si les fichiers .gradle ne changent pas)
RUN ./gradlew build -x test --no-daemon > /dev/null 2>&1 || true

# 2. Copie du code source et build réel
COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

# 3. Extraction des couches (Layered JAR) pour Spring Boot 4
# Note : Gradle génère le jar dans build/libs/
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

# --- Étape 2 : Runtime ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Sécurité : Utilisateur non-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copie des couches depuis le builder
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Port d'écoute (8087 selon ton application.yml)
EXPOSE 8087

# Lancement via le JarLauncher de Spring Boot
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]