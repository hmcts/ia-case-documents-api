output "microserviceName" {
  value = "${var.component}"
}

output "resourceGroup" {
  value = "${azurerm_resource_group.rg.name}"
}

output "appServicePlan" {
  value = "${local.app_service_plan}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.ia_key_vault.vault_uri}"
}

output "docmosisVaultUri" {
  value = "${local.docmosis_key_vault_uri}"
}

output "docmosisEndpoint" {
  value = "${data.azurerm_key_vault_secret.docmosis_endpoint.value}"
}
