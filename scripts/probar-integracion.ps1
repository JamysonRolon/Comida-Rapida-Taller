param(
    [string]$Url = 'jdbc:postgresql://localhost:55432/comidas_pruebas',
    [string]$Usuario = 'comidas_test',
    [string]$ArchivoEntorno = '.work/postgres-test.env'
)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $Url.EndsWith('/comidas_pruebas')) {
    throw 'Utilice exclusivamente una base de pruebas llamada comidas_pruebas.'
}
$env:TEST_DB_URL = $Url
$env:TEST_DB_USER = $Usuario
if (Test-Path -LiteralPath $ArchivoEntorno) {
    $passwordLine = Get-Content -LiteralPath $ArchivoEntorno | Where-Object { $_.StartsWith('POSTGRES_PASSWORD=') }
    $env:TEST_DB_PASSWORD = $passwordLine.Substring('POSTGRES_PASSWORD='.Length)
}
if (-not $env:TEST_DB_PASSWORD) {
    throw 'Defina TEST_DB_PASSWORD o proporcione un archivo de entorno local.'
}
$env:RUN_PG_IT = 'true'
$ErrorActionPreference = 'Continue'
& mvn -B verify
exit $LASTEXITCODE
