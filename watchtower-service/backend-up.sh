# Launch backend server
java \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseCGroupMemoryLimitForHeap \
  -Dcom.sun.management.jmxremote \
  -noverify \
  -Dmicronaut.config.files=tower.yml \
  ${JAVA_OPTS} \
  -jar /tower/watchtower-service.jar