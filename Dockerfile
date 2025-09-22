FROM amazoncorretto:25

RUN rm /etc/localtime
RUN ln -s /usr/share/zoneinfo/PST8PDT /etc/localtime

COPY target/watch-*.jar /usr/src/watch.jar
WORKDIR                 /usr/src/

ENV _JAVA_OPTIONS="-XX:+UseShenandoahGC \
-Djdk.xml.maxGeneralEntitySizeLimit=0 \
-Djdk.xml.entityExpansionLimit=0 \
-Djdk.xml.totalEntitySizeLimit=0 \
-Djdk.xml.maxGeneralEntitySizeLimit=0 \
-Xmx2g \
-XX:ActiveProcessorCount=2 \
-XX:+UnlockExperimentalVMOptions \
-XX:MetaspaceSize=25m \
-XX:MinMetaspaceFreeRatio=10 \
-XX:ShenandoahUncommitDelay=1000 \
-XX:ShenandoahGuaranteedGCInterval=10000"

CMD ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-jar", "watch.jar" ]


