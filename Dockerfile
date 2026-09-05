FROM eclipse-temurin:25-jdk AS build
RUN apt-get update && apt-get install -y make && rm -rf /var/lib/apt/lists/*
WORKDIR /build
COPY src ./src
COPY Makefile .
RUN make build

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/out ./out

COPY scripts/start-server.sh ./minecraft-server/start-server.sh
RUN chmod +x ./minecraft-server/start-server.sh
COPY minecraft-server ./minecraft-server

RUN echo "eula=true" > ./minecraft-server/eula.txt

EXPOSE 30000

ENTRYPOINT ["java", "--enable-preview", "-cp", "out", "Main", \
            "30000", "localhost", "25565", \
            "/app/minecraft-server/start-server.sh", \
            "/app/minecraft-server", \
            "/app/minecraft-server/server.logs"]