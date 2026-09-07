param(
    [string]$PgBin = 'C:\Program Files\PostgreSQL\18\bin',
    [int]$Puerto = 55434
)
$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path $PSScriptRoot -Parent
Set-Location $raizProyecto
$trabajo = Join-Path $raizProyecto '.work'
$datosPg = Join-Path $trabajo 'postgres-local'
$entorno = Join-Path $trabajo 'db-local.env'
$utf8 = New-Object System.Text.UTF8Encoding($false)
if (-not (Test-Path -LiteralPath (Join-Path $PgBin 'initdb.exe'))) {
    throw 'No se encontró PostgreSQL. Indique -PgBin con la ruta a sus binarios.'
}
New-Item -ItemType Directory -Force -Path $trabajo | Out-Null
if (-not (Test-Path -LiteralPath $entorno)) {
    $claveLocal = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
    $claveDaniel = 'Daniel-' + [guid]::NewGuid().ToString('N').Substring(0, 16)
    $claveJuan = 'Juan-' + [guid]::NewGuid().ToString('N').Substring(0, 16)
    [IO.File]::WriteAllLines($entorno, @(
        ('DB_URL=jdbc:postgresql://127.0.0.1:' + $Puerto + '/comidas_rapidas'),
        'DB_USER=comidas_app',
        ('DB_PASSWORD=' + $claveLocal),
        'DEMO_DATA=true',
        ('DEMO_DANIEL_PASSWORD=' + $claveDaniel),
        ('DEMO_JUAN_PASSWORD=' + $claveJuan)
    ), $utf8)
}
$lineasEntorno = Get-Content -LiteralPath $entorno
if (-not ($lineasEntorno | Where-Object { $_ -eq 'DEMO_DATA=true' })) {
    [IO.File]::AppendAllText($entorno, [Environment]::NewLine + 'DEMO_DATA=true', $utf8)
}
foreach ($cuenta in @('DANIEL', 'JUAN')) {
    $nombreVariable = 'DEMO_' + $cuenta + '_PASSWORD'
    if (-not ($lineasEntorno | Where-Object { $_.StartsWith($nombreVariable + '=') })) {
        $claveDemo = $cuenta.Substring(0, 1) + $cuenta.Substring(1).ToLowerInvariant() + '-' +
                [guid]::NewGuid().ToString('N').Substring(0, 16)
        [IO.File]::AppendAllText($entorno,
                [Environment]::NewLine + $nombreVariable + '=' + $claveDemo, $utf8)
    }
}
$valores = @{}
Get-Content -LiteralPath $entorno | ForEach-Object {
    if ([string]::IsNullOrWhiteSpace($_) -or $_.TrimStart().StartsWith('#')) {
        return
    }
    $partes = $_ -split '=', 2
    if ($partes.Length -eq 2 -and -not [string]::IsNullOrWhiteSpace($partes[0])) {
        $valores[$partes[0]] = $partes[1]
    }
}
$urlGuardada = [uri]$valores.DB_URL.Substring(5)
$Puerto = $urlGuardada.Port
if (-not (Test-Path -LiteralPath (Join-Path $datosPg 'PG_VERSION'))) {
    $archivoClave = Join-Path $trabajo 'initdb-password.tmp'
    [IO.File]::WriteAllText($archivoClave, $valores.DB_PASSWORD, $utf8)
    & (Join-Path $PgBin 'initdb.exe') -D $datosPg -U comidas_app --encoding=UTF8 --locale=C --auth=scram-sha-256 --pwfile=$archivoClave
    $codigoInicializacion = $LASTEXITCODE
    Remove-Item -LiteralPath $archivoClave
    if ($codigoInicializacion -ne 0) { throw 'Falló initdb. Revise su salida.' }
}
& (Join-Path $PgBin 'pg_ctl.exe') -D $datosPg status *> $null
if ($LASTEXITCODE -ne 0) {
    $argumentos = '-D "' + $datosPg + '" -l "' + (Join-Path $trabajo 'postgres-local.log') + '" -o "-p ' + $Puerto + ' -h 127.0.0.1" -w start'
    $arranque = Start-Process -FilePath (Join-Path $PgBin 'pg_ctl.exe') -ArgumentList $argumentos -WindowStyle Hidden -PassThru
    if (-not $arranque.WaitForExit(20000)) { throw 'Se agotó el tiempo de arranque de PostgreSQL.' }
    if ($arranque.ExitCode -ne 0) { throw 'PostgreSQL no arrancó. Revise .work/postgres-local.log.' }
}
$claveAnterior = $env:PGPASSWORD
try {
    $env:PGPASSWORD = $valores.DB_PASSWORD
    $existe = & (Join-Path $PgBin 'psql.exe') -h 127.0.0.1 -p $Puerto -U comidas_app -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='comidas_rapidas'"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo conectar al PostgreSQL local.' }
    if ($existe -ne '1') {
        & (Join-Path $PgBin 'createdb.exe') -h 127.0.0.1 -p $Puerto -U comidas_app comidas_rapidas
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear la base comidas_rapidas.' }
    }
} finally {
    $env:PGPASSWORD = $claveAnterior
}
Write-Output ('PostgreSQL del proyecto disponible en 127.0.0.1:' + $Puerto + '. Configuración en .work/db-local.env.')
