# Project-local Maven bootstrap; verifies the official archive before extraction.
$ErrorActionPreference = 'Stop'
$version = '3.9.11'
$toolsDir = Join-Path $PSScriptRoot '.tools'
$mavenCmd = Join-Path $toolsDir "apache-maven-$version/bin/mvn.cmd"
if (-not (Test-Path -LiteralPath $mavenCmd)) {
    New-Item -ItemType Directory -Force $toolsDir | Out-Null
    $archive = Join-Path $toolsDir "apache-maven-$version-bin.zip"
    $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip"
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -UseBasicParsing $url -OutFile $archive
    $expected = ((Invoke-WebRequest -UseBasicParsing "$url.sha512").Content.Trim() -split '\s+')[0]
    if ((Get-FileHash -LiteralPath $archive -Algorithm SHA512).Hash -ne $expected) { throw 'Maven checksum mismatch.' }
    Expand-Archive -LiteralPath $archive -DestinationPath $toolsDir -Force
}
$mavenHome = Join-Path $toolsDir "apache-maven-$version"
$launcher = (Get-ChildItem -LiteralPath (Join-Path $mavenHome 'boot') -Filter 'plexus-classworlds-*.jar' | Select-Object -First 1).FullName
$javaCmd = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
# Invoke Java directly to preserve Vietnamese paths on Windows PowerShell/cmd.
Push-Location $PSScriptRoot
$previousEncoding = [Console]::OutputEncoding
try {
    [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
    $relativeHome = ".tools/apache-maven-$version"
    $relativeLauncher = "$relativeHome/boot/" + [IO.Path]::GetFileName($launcher)
    & $javaCmd '-Dfile.encoding=UTF-8' "-Dmaven.home=$relativeHome" "-Dclassworlds.conf=$relativeHome/bin/m2.conf" '-Dmaven.multiModuleProjectDirectory=.' '-Dmaven.repo.local=.tools/repository' -classpath $relativeLauncher org.codehaus.plexus.classworlds.launcher.Launcher @args
    $result = $LASTEXITCODE
} finally { [Console]::OutputEncoding = $previousEncoding; Pop-Location }
exit $result
