
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src/ ./src/
COPY web/ ./web/

RUN mkdir -p out && \
    javac --add-modules jdk.httpserver \
    -encoding UTF-8 \
    -d out \
    src/app/*.java \
    src/model/*.java \
    src/service/*.java \
    src/repository/*.java

EXPOSE 8080

CMD ["java", "--add-modules", "jdk.httpserver", "-cp", "out", "app.Main"]
