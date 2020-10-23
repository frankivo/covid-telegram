FROM mozilla/sbt as builder
WORKDIR /root
COPY build.sbt .
COPY project/build.properties project/plugins.sbt ./project/
RUN sbt compile

FROM builder as compiler
WORKDIR /root
COPY build.sbt .
COPY src src
RUN sbt assembly

FROM openjdk:8-alpine
COPY --from=compiler /root/target/scala-2.13/*jar /root
RUN java -jar /root/*jar
