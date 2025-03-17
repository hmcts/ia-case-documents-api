 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.1
# Application image
FROM hmctspublic.azurecr.io/base/java:17-distroless

# Mandatory!
ENV APP ia-case-documents-api.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 75

# Change to non-root privilege
USER hmcts

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/$APP /opt/app/

EXPOSE 8092

CMD [ "ia-case-documents-api.jar" ]
