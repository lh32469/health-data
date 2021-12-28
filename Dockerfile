FROM openjdk:11

RUN rm /etc/localtime
RUN ln -s /usr/share/zoneinfo/PST8PDT /etc/localtime

COPY target/watch-*.jar /usr/src/watch.jar
WORKDIR                 /usr/src/

ENV _JAVA_OPTIONS="-Xmx512m"

ENV PORT 5000
EXPOSE $PORT

CMD ["java", "-jar", "-Dserver.port=${PORT}", "watch.jar" ]


