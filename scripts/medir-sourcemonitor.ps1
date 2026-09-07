param(
    [Parameter(Mandatory = $true)][string]$Ejecutable,
    [string]$Checkpoint = '02-verificacion-final',
    [string]$Prefijo = '02'
)
$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path $PSScriptRoot -Parent
Set-Location $raizProyecto
$ejecutableSm = (Resolve-Path -LiteralPath $Ejecutable).Path
$salida = Join-Path $raizProyecto 'docs/metricas'
New-Item -ItemType Directory -Force -Path $salida | Out-Null
New-Item -ItemType Directory -Force -Path '.work' | Out-Null
$fuentes = [Security.SecurityElement]::Escape((Join-Path $raizProyecto 'src/main/java'))
$proyecto = [Security.SecurityElement]::Escape((Join-Path $salida 'comidas-rapidas.smproj'))
$nombre = [Security.SecurityElement]::Escape($Checkpoint)
foreach ($tipo in @(1, 2)) {
    $sufijo = if ($tipo -eq 1) { 'resumen' } else { 'detalle' }
    $exportacion = [Security.SecurityElement]::Escape((Join-Path $salida ($Prefijo + '-' + $sufijo + '.xml')))
    $comandos = @"
<?xml version="1.0" encoding="UTF-8"?>
<sourcemonitor_commands>
  <write_log>true</write_log>
  <command>
    <project_file>$proyecto</project_file>
    <project_language>Java</project_language>
    <source_directory>$fuentes</source_directory>
    <file_extensions>*.java</file_extensions>
    <include_subdirectories>true</include_subdirectories>
    <parse_utf8_files>true</parse_utf8_files>
    <checkpoint_name>$nombre</checkpoint_name>
    <export>
      <export_file>$exportacion</export_file>
      <export_type>$tipo</export_type>
      <export_option>3</export_option>
    </export>
  </command>
</sourcemonitor_commands>
"@
    $archivo = Join-Path $raizProyecto ('.work/sm-' + $sufijo + '.xml')
    [IO.File]::WriteAllText($archivo, $comandos, (New-Object System.Text.UTF8Encoding($false)))
    $procesoSm = Start-Process -FilePath $ejecutableSm -ArgumentList @('/C', ('"' + $archivo + '"')) -WindowStyle Hidden -Wait -PassThru
    if ($procesoSm.ExitCode -ne 0) { throw ('SourceMonitor devolvió ' + $procesoSm.ExitCode) }
    $archivoSalida = Join-Path $salida ($Prefijo + '-' + $sufijo + '.xml')
    if (-not (Test-Path -LiteralPath $archivoSalida)) { throw 'SourceMonitor no generó la exportación esperada.' }
}
Write-Output ('Checkpoint y exportaciones generados en ' + $salida)
