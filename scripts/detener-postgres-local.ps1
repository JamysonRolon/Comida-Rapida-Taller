param([string]$PgBin = 'C:\Program Files\PostgreSQL\18\bin')
$raizProyecto = Split-Path $PSScriptRoot -Parent
$datosPg = Join-Path $raizProyecto '.work/postgres-local'
if (-not (Test-Path -LiteralPath (Join-Path $datosPg 'PG_VERSION'))) {
    throw 'No existe el PostgreSQL local de este proyecto.'
}
& (Join-Path $PgBin 'pg_ctl.exe') -D $datosPg -m fast -w stop
exit $LASTEXITCODE
