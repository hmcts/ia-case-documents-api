ARG APP_INSIGHTS_AGENT_VERSION=2.5.1-BETA
FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.4

# Mandatory!
ENV APP ia-case-documents-api.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 75

# Change to non-root privilege
USER hmcts

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/

EXPOSE 8092

CMD [ "ia-case-documents-api.jar" ]
