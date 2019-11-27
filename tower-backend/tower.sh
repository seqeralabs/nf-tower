# Launch backend server
exec java \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseCGroupMemoryLimitForHeap \
  -Dcom.sun.management.jmxremote \
  -noverify \
  -Dmicronaut.config.files=tower.yml \
  ${JAVA_OPTS} \
  -jar /tower/tower-backend.jar
