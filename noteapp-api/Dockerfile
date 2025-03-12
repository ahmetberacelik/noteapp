FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Maven Wrapper yerine doğrudan Maven kullanma
COPY pom.xml .
COPY src src

# Maven'ı Docker içinde kullan
RUN apk add --no-cache maven && \
    mvn package -DskipTests && \
    mkdir -p target/dependency && \
    (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.noteapp.api.NoteAppApiApplication"]