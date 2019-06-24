FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-2.0.1

# Mandatory!
ENV APP ia-case-notifications-api.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 75

# Optional
ENV JAVA_OPTS ""

COPY build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=12 CMD http_proxy="" wget -q --spider http://localhost:8093/health || exit 1

EXPOSE 8093

CMD [ "ia-case-notifications-api.jar" ]
