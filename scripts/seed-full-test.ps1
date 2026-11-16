# Reapply local test fixtures; deliberately resets only admin/tuyen passwords to 1234.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    docker compose cp ./database/06-full-test.sql db:/tmp/libman-full-test.sql
    if ($LASTEXITCODE -ne 0) { throw 'Cannot copy the full test seed to MySQL.' }
    docker compose exec -T db sh -c 'MYSQL_PWD=$MYSQL_PASSWORD mysql --default-character-set=utf8mb4 -u$MYSQL_USER $MYSQL_DATABASE < /tmp/libman-full-test.sql'
    if ($LASTEXITCODE -ne 0) { throw 'MySQL full test seed failed.' }
    Write-Output 'Full test data is ready. Accounts: admin / 1234 and tuyen / 1234.'
} finally { Pop-Location }
