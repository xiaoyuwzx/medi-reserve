# =============================================
# MediReserve 多阶段构建 Dockerfile
# 使用 Maven 构建，然后运行 JAR 包
# =============================================

# ========== 第一阶段：构建阶段 ==========
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY medi-common/pom.xml medi-common/
COPY medi-patient/pom.xml medi-patient/
COPY medi-doctor/pom.xml medi-doctor/
COPY medi-admin/pom.xml medi-admin/
COPY medi-websocket/pom.xml medi-websocket/

RUN mvn dependency:go-offline -B

COPY medi-common/src medi-common/src/
COPY medi-patient/src medi-patient/src/
COPY medi-doctor/src medi-doctor/src/
COPY medi-admin/src medi-admin/src/
COPY medi-websocket/src medi-websocket/src/

RUN mvn clean package -DskipTests -B

# ========== 第二阶段：运行阶段 ==========
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/medi-patient/target/medi-patient-*.jar /app/medi-patient.jar
COPY --from=builder /app/medi-doctor/target/medi-doctor-*.jar /app/medi-doctor.jar
COPY --from=builder /app/medi-admin/target/medi-admin-*.jar /app/medi-admin.jar
COPY --from=builder /app/medi-websocket/target/medi-websocket-*.jar /app/medi-websocket.jar

COPY <<EOF /app/start.sh
#!/bin/sh
set -e

case "\$SERVICE" in
  patient)
    exec java \$JAVA_OPTS -jar /app/medi-patient.jar
    ;;
  doctor)
    exec java \$JAVA_OPTS -jar /app/medi-doctor.jar
    ;;
  admin)
    exec java \$JAVA_OPTS -jar /app/medi-admin.jar
    ;;
  websocket)
    exec java \$JAVA_OPTS -jar /app/medi-websocket.jar
    ;;
  *)
    echo "Unknown service: \$SERVICE"
    echo "Available: patient, doctor, admin, websocket"
    exit 1
    ;;
esac
EOF

RUN chmod +x /app/start.sh

ENV JAVA_OPTS="-Xmx256m -Xms128m"

EXPOSE 8081 8082 8083 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8081}/actuator/health || exit 1

ENTRYPOINT ["/app/start.sh"]