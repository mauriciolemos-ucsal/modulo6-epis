# Inicializa (na 1ª vez) e sobe o MySQL local portátil. Deixe esta janela aberta; Ctrl+C encerra.
# Uso: powershell -File scripts\mysql-iniciar.ps1
$ErrorActionPreference = 'Stop'
$raiz  = Join-Path $env:USERPROFILE 'tools\mysql-8.4.9-winx64'
$dados = Join-Path $env:USERPROFILE 'tools\mysql-dados-sgf'
$bin   = Join-Path $raiz 'bin'

if (-not (Test-Path (Join-Path $dados 'mysql'))) {
  Write-Host 'Inicializando diretório de dados...'
  & "$bin\mysqld.exe" --initialize-insecure "--datadir=$dados" --console
  # Sobe temporariamente para criar o banco e o usuário da aplicação.
  $p = Start-Process "$bin\mysqld.exe" -ArgumentList "--datadir=$dados", '--bind-address=127.0.0.1' -PassThru -WindowStyle Hidden
  for ($i = 0; $i -lt 60; $i++) {
    & "$bin\mysqladmin.exe" -uroot ping 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { break }
    Start-Sleep 1
  }
  $sql = "CREATE DATABASE IF NOT EXISTS sgf CHARACTER SET utf8mb4; " +
         "CREATE USER IF NOT EXISTS 'sgf'@'localhost' IDENTIFIED BY 'sgf'; " +
         "GRANT ALL ON sgf.* TO 'sgf'@'localhost';"
  & "$bin\mysql.exe" -uroot -e $sql
  & "$bin\mysqladmin.exe" -uroot shutdown
  $p.WaitForExit()
}

Write-Host 'MySQL em 127.0.0.1:3306 (banco sgf, usuário sgf / senha sgf)'
& "$bin\mysqld.exe" "--datadir=$dados" --bind-address=127.0.0.1 --console
