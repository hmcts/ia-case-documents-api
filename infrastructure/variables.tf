variable "product" {}

variable "component" {}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}


variable "common_tags" {
  type = map(string)
}

variable "appinsights_instrumentation_key" {
  default = ""
}
