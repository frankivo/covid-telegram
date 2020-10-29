FROM oosterhuisf/sbt:1.4.1 as builder
WORKDIR /home
COPY build.sbt .
COPY project/build.properties project/plugins.sbt ./project/
COPY src/ src/
RUN sbt assembly

FROM openjdk:8-alpine
RUN apk add --no-cache ttf-dejavu
CMD java -jar /home/*jar
COPY --from=builder /home/target/scala-2.13/*jar /home
