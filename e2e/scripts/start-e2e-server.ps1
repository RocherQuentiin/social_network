# Start Spring Boot with in-memory H2 and seeded E2E users (port 8081 by default).
$ErrorActionPreference = "Stop"
$env:SPRING_PROFILES_ACTIVE = "e2e"
$env:E2E_SERVER_PORT = if ($env:E2E_SERVER_PORT) { $env:E2E_SERVER_PORT } else { "8081" }

Push-Location (Join-Path $PSScriptRoot "..\..\socialnetwork")
try {
  mvn -q spring-boot:run "-Dspring-boot.run.profiles=e2e"
} finally {
  Pop-Location
}
