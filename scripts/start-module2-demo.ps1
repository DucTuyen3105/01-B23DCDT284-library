# Compatibility launcher: use real login; automatic demo sessions have been removed.
$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot '../mvnw.ps1') test-compile exec:java '-Dhttp.port=8080'
exit $LASTEXITCODE
