# Temporary fix for template API version error on deployment
provider "azurerm" {
  version = "1.21.0"
}

locals {

  preview_app_service_plan     = "${var.product}-${var.component}-${var.env}"
  non_preview_app_service_plan = "${var.product}-${var.env}"
  app_service_plan             = "${var.env == "preview" || var.env == "spreview" ? local.preview_app_service_plan : local.non_preview_app_service_plan}"

  preview_vault_name           = "${var.raw_product}-aat"
  non_preview_vault_name       = "${var.raw_product}-${var.env}"
  key_vault_name               = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"

}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"
  tags     = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
}

data "azurerm_key_vault" "ia_key_vault" {
  name                = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

data "azurerm_key_vault_secret" "test_caseofficer_username" {
  name      = "test-caseofficer-username"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "test_caseofficer_password" {
  name      = "test-caseofficer-password"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "test_law_firm_a_username" {
  name      = "test-law-firm-a-username"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "test_law_firm_a_password" {
  name      = "test-law-firm-a-password"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "system_username" {
  name      = "system-username"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "system_password" {
  name      = "system-password"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_client_id" {
  name      = "idam-client-id"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_secret" {
  name      = "idam-secret"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name      = "s2s-secret"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_microservice" {
  name      = "s2s-microservice"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ia_customer_services_telephone" {
  name      = "customer-services-telephone"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "ia_customer_services_email" {
  name      = "customer-services-email"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "em_stitching_enabled" {
  name      = "em-stitching-enabled"
  vault_uri = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

data "azurerm_lb" "consul_dns" {
  name                = "consul-server_dns"
  resource_group_name = "${var.consul_dns_resource_group_name}"
}

module "ia_case_documents_api" {
  source                          = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product                         = "${var.product}-${var.component}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  enable_ase                      = "${var.enable_ase}"
  ilbIp                           = "${var.ilbIp}"
  resource_group_name             = "${azurerm_resource_group.rg.name}"
  subscription                    = "${var.subscription}"
  capacity                        = "${var.capacity}"
  instance_size                   = "${var.instance_size}"
  common_tags                     = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  asp_name                        = "${local.app_service_plan}"
  asp_rg                          = "${local.app_service_plan}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE  = false

    WEBSITE_DNS_SERVER = "${data.azurerm_lb.consul_dns.private_ip_address}"

    IA_CUSTOMER_SERVICES_EMAIL      = "${data.azurerm_key_vault_secret.ia_customer_services_email.value}"
    IA_CUSTOMER_SERVICES_TELEPHONE  = "${data.azurerm_key_vault_secret.ia_customer_services_telephone.value}"

    IA_SYSTEM_USERNAME   = "${data.azurerm_key_vault_secret.system_username.value}"
    IA_SYSTEM_PASSWORD   = "${data.azurerm_key_vault_secret.system_password.value}"
    IA_IDAM_CLIENT_ID    = "${data.azurerm_key_vault_secret.idam_client_id.value}"
    IA_IDAM_SECRET       = "${data.azurerm_key_vault_secret.idam_secret.value}"
    IA_S2S_SECRET        = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    IA_S2S_MICROSERVICE  = "${data.azurerm_key_vault_secret.s2s_microservice.value}"

    IA_EM_STITCHING_ENABLED  = "${data.azurerm_key_vault_secret.em_stitching_enabled.value}"

    ROOT_LOGGING_LEVEL   = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_IA         = "${var.log_level_ia}"
  }
}
