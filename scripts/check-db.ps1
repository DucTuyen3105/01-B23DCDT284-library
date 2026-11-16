# Maven runner uses the same DB_* settings as the application.
$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot '../mvnw.ps1') -B test-compile exec:java '-Dexec.mainClass=dev.CheckDatabase'
exit $LASTEXITCODE
