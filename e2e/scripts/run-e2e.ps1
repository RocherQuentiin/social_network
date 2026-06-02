# Run Nightwatch E2E (Chrome headless by default). Ensure the app is running with profile "e2e".
param(
  [string]$Env = "chromeHeadless"
)

$ErrorActionPreference = "Stop"
if (-not $env:E2E_BASE_URL) {
  $env:E2E_BASE_URL = "http://localhost:8081"
}

Push-Location (Join-Path $PSScriptRoot "..")
try {
  if (-not (Test-Path "node_modules")) {
    npm ci
  }
  npx nightwatch --env $Env
} finally {
  Pop-Location
}
