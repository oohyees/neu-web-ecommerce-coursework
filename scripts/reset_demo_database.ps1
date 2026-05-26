param(
  [string]$HostName = "127.0.0.1",
  [int]$Port = 3306,
  [string]$User = "root",
  [string]$Password = "123456",
  [string]$Database = "ecommerce_minimal",
  [int]$RedisPort = 6380
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$schema = Join-Path $projectRoot "apps\api\src\main\resources\schema.sql"
$data = Join-Path $projectRoot "apps\api\src\main\resources\data.sql"
$schemaForMysql = $schema.Replace("\", "/")
$dataForMysql = $data.Replace("\", "/")

if (!(Test-Path -LiteralPath $schema) -or !(Test-Path -LiteralPath $data)) {
  throw "schema.sql or data.sql was not found."
}

$mysql = (Get-Command mysql -ErrorAction SilentlyContinue).Source
if (!$mysql) {
  throw "mysql command was not found. Install MySQL client or reinitialize the database with docker compose."
}

Write-Host "Resetting database '$Database' on ${HostName}:${Port} ..."
& $mysql `
  "--host=$HostName" `
  "--port=$Port" `
  "--user=$User" `
  "--password=$Password" `
  "--default-character-set=utf8mb4" `
  "--database=$Database" `
  "--execute=SOURCE $schemaForMysql; SOURCE $dataForMysql;"

Write-Host "Demo database reset complete."

$redis = (Get-Command redis-cli -ErrorAction SilentlyContinue).Source
if ($redis) {
  & $redis "-p" "$RedisPort" "FLUSHDB" | Out-Null
  Write-Host "Redis cache/session data cleared."
} else {
  Write-Host "redis-cli not found; skip Redis cache cleanup."
}
