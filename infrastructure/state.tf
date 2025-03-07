terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.22.0"
    }
  }
}
