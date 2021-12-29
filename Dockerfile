FROM amazoncorretto:11

RUN rm /etc/localtime
RUN ln -s /usr/share/zoneinfo/PST8PDT /etc/localtime

COPY target/watch-*.jar /usr/src/watch.jar
WORKDIR                 /usr/src/

ENV _JAVA_OPTIONS="-XX:+UseShenandoahGC \
-Xmx1g \
-XX:+UnlockExperimentalVMOptions \
-XX:MetaspaceSize=25m \
-XX:MinMetaspaceFreeRatio=10 \
-XX:ShenandoahUncommitDelay=1000 \
-XX:ShenandoahGuaranteedGCInterval=10000"

ENV PORT 5000
EXPOSE $PORT

CMD ["java", "-jar", "-Dserver.port=${PORT}", "watch.jar" ]


