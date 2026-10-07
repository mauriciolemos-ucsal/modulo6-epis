# Sobe o backend Spring Boot em http://localhost:8080 (o MySQL precisa estar rodando).
$ErrorActionPreference = 'Stop'
$mvn = Join-Path $env:USERPROFILE 'tools\apache-maven-3.9.16\bin\mvn.cmd'
if (-not (Test-Path $mvn)) { $mvn = 'mvn' }
Set-Location (Join-Path $PSScriptRoot '..\backend')
& $mvn spring-boot:run
