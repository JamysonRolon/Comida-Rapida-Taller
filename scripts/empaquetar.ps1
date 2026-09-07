$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path $PSScriptRoot -Parent
Set-Location $raizProyecto
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$salida = Join-Path $raizProyecto 'entregables'
New-Item -ItemType Directory -Force -Path $salida | Out-Null
$zipPath = Join-Path $salida 'comidas-rapidas.zip'
$archivos = @(
    Get-Item -LiteralPath 'pom.xml','README.md','.gitignore'
    Get-ChildItem -LiteralPath 'src','docs','scripts' -File -Recurse
)
$stream = [IO.File]::Open($zipPath, [IO.FileMode]::Create)
$zip = New-Object IO.Compression.ZipArchive($stream, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($archivo in $archivos) {
        $relativa = $archivo.FullName.Substring($raizProyecto.Length + 1).Replace('\','/')
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $archivo.FullName,
            ('comidas-rapidas/' + $relativa), [IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally {
    $zip.Dispose()
    $stream.Dispose()
}
$tamano = (Get-Item -LiteralPath $zipPath).Length
if ($tamano -gt 15000000) { throw ('El ZIP supera 15 MB: ' + $tamano + ' bytes.') }
Write-Output ('ZIP creado: ' + $zipPath + ' (' + $tamano + ' bytes)')
