param([switch]$SinInternet)
$raizProyecto = Split-Path $PSScriptRoot -Parent
Set-Location $raizProyecto
$configuracion = Join-Path $raizProyecto '.work/db-local.env'
if (Test-Path -LiteralPath $configuracion) {
    Get-Content -LiteralPath $configuracion | ForEach-Object {
        if ([string]::IsNullOrWhiteSpace($_) -or $_.TrimStart().StartsWith('#')) {
            return
        }
        $partes = $_ -split '=', 2
        if ($partes.Length -eq 2 -and -not [string]::IsNullOrWhiteSpace($partes[0])) {
            [Environment]::SetEnvironmentVariable($partes[0], $partes[1], 'Process')
        }
    }
}
if (-not $env:DB_PASSWORD) {
    throw 'Configure DB_URL, DB_USER y DB_PASSWORD o ejecute scripts/preparar-postgres-local.ps1.'
}
if ($SinInternet) {
    & mvn -o javafx:run
} else {
    & mvn javafx:run
}
exit $LASTEXITCODE
