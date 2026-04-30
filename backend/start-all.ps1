<#
.SYNOPSIS
    Start the local Healthcare Platform stack.

.DESCRIPTION
    Starts Docker infrastructure (PostgreSQL, Redis, RabbitMQ), ensures the
    service databases exist, then launches all 6 Spring Boot services in
    separate PowerShell windows. Flyway creates tables and seed data during
    each service startup.

.EXAMPLE
    .\start-all.ps1
    .\start-all.ps1 -Clean
    .\start-all.ps1 -SkipInfra
    .\start-all.ps1 -InfraOnly
#>

[CmdletBinding()]
param(
    [switch]$Clean,
    [switch]$SkipInfra,
    [switch]$InfraOnly,
    [string]$MavenCommand,
    [string]$JavaHome,
    [int]$InfraTimeoutSeconds = 120
)

$ErrorActionPreference = 'Stop'
if (Get-Variable PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$scriptDir = $PSScriptRoot
$repoRoot = Split-Path $scriptDir -Parent
$dockerDir = Join-Path $repoRoot 'docker'
$composeFile = Join-Path $dockerDir 'compose.yml'
$runService = Join-Path $scriptDir 'run-service.ps1'

$services = @(
    @{ Name = 'auth-service';         Port = '8086'; Db = 'auth_db' },
    @{ Name = 'user-service';         Port = '8082'; Db = 'user_db' },
    @{ Name = 'doctor-service';       Port = '8083'; Db = 'doctor_db' },
    @{ Name = 'appointment-service';  Port = '8084'; Db = 'appointment_db' },
    @{ Name = 'notification-service'; Port = '8085'; Db = 'notification_db' },
    @{ Name = 'ai-service';           Port = '8087'; Db = 'ai_db' }
)

$infraServices = @('postgres', 'redis', 'rabbitmq')

function Write-Step {
    param([string]$Message)
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Write-Ok {
    param([string]$Message)
    Write-Host "    OK  $Message" -ForegroundColor Green
}

function Assert-CommandExists {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "'$Name' was not found in PATH."
    }
}

function Invoke-DockerCompose {
    param([string[]]$Arguments)

    & docker compose -f $composeFile @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose failed: $($Arguments -join ' ')"
    }
}

function Invoke-Docker {
    param([string[]]$Arguments)

    & docker @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker failed: $($Arguments -join ' ')"
    }
}

function Get-ComposeContainerId {
    param([string]$ServiceName)

    $containerId = & docker compose -f $composeFile ps -q $ServiceName
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($containerId)) {
        throw "Could not resolve container for compose service '$ServiceName'."
    }

    return ($containerId | Select-Object -First 1).Trim()
}

function Get-ContainerHealth {
    param([string]$ContainerId)

    $health = & docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $ContainerId
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($health)) {
        return 'unknown'
    }

    return ($health | Select-Object -First 1).Trim()
}

function Wait-InfraHealthy {
    param([string[]]$ServiceNames, [int]$TimeoutSeconds)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $pending = @()
        foreach ($serviceName in $ServiceNames) {
            $containerId = Get-ComposeContainerId -ServiceName $serviceName
            $health = Get-ContainerHealth -ContainerId $containerId
            if ($health -notin @('healthy', 'running')) {
                $pending += "$serviceName=$health"
            }
        }

        if ($pending.Count -eq 0) {
            return
        }

        Write-Host "    Waiting for infra: $($pending -join ', ')" -ForegroundColor DarkGray
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)

    throw "Timed out waiting for Docker infrastructure to become healthy."
}

function Invoke-PostgresSql {
    param(
        [string]$Sql,
        [string]$Database = 'postgres',
        [switch]$Quiet
    )

    $containerId = Get-ComposeContainerId -ServiceName 'postgres'
    $output = & docker exec $containerId psql -U healthcare -d $Database -v ON_ERROR_STOP=1 -c $Sql
    if ($LASTEXITCODE -ne 0) {
        throw "PostgreSQL bootstrap failed."
    }

    if (-not $Quiet) {
        return $output
    }
}

function Ensure-ServiceDatabases {
    param([object[]]$ServiceConfigs)

    foreach ($serviceConfig in $ServiceConfigs) {
        $database = $serviceConfig.Db
        $exists = Invoke-PostgresSql `
            -Sql "SELECT 1 FROM pg_database WHERE datname = '$database';" |
            Select-String -Pattern '1'

        if (-not $exists) {
            Invoke-PostgresSql -Sql "CREATE DATABASE $database OWNER healthcare;" -Quiet | Out-Null
        }

        Invoke-PostgresSql -Sql "ALTER DATABASE $database OWNER TO healthcare;" -Quiet | Out-Null
        Invoke-PostgresSql -Database $database -Sql 'ALTER SCHEMA public OWNER TO healthcare; GRANT ALL ON SCHEMA public TO healthcare;' -Quiet | Out-Null
    }
}

function Quote-ProcessArgument {
    param([string]$Value)

    if ($Value -notmatch '[\s"]') {
        return $Value
    }

    return '"' + $Value.Replace('"', '\"') + '"'
}

function Start-ServiceWindow {
    param([hashtable]$ServiceConfig)

    $arguments = @(
        '-NoExit',
        '-ExecutionPolicy', 'Bypass',
        '-File', $runService,
        '-Service', $ServiceConfig.Name,
        '-AppPort', $ServiceConfig.Port
    )

    if ($Clean) {
        $arguments += '-Clean'
    }
    if (-not [string]::IsNullOrWhiteSpace($MavenCommand)) {
        $arguments += @('-MavenCommand', $MavenCommand)
    }
    if (-not [string]::IsNullOrWhiteSpace($JavaHome)) {
        $arguments += @('-JavaHome', $JavaHome)
    }

    $argumentText = ($arguments | ForEach-Object { Quote-ProcessArgument -Value $_ }) -join ' '
    Start-Process powershell -ArgumentList $argumentText -WorkingDirectory $scriptDir -WindowStyle Normal
}

if (-not (Test-Path $runService)) {
    throw "run-service.ps1 not found at: $runService"
}

if (-not (Test-Path $composeFile)) {
    throw "Docker compose file not found at: $composeFile"
}

Write-Host ''
Write-Host 'Healthcare Platform - local stack startup' -ForegroundColor Cyan
Write-Host ''

if (-not $SkipInfra) {
    Write-Step 'Starting Docker infrastructure'
    Assert-CommandExists -Name 'docker'
    Invoke-DockerCompose -Arguments @('up', '-d', 'postgres', 'redis', 'rabbitmq')
    Wait-InfraHealthy -ServiceNames $infraServices -TimeoutSeconds $InfraTimeoutSeconds
    Write-Ok 'PostgreSQL, Redis, and RabbitMQ are running'

    Write-Step 'Ensuring service databases'
    Ensure-ServiceDatabases -ServiceConfigs $services
    Write-Ok 'auth_db, user_db, doctor_db, appointment_db, notification_db, ai_db are ready'
}
else {
    Write-Step 'Skipping Docker infrastructure startup'
}

if ($InfraOnly) {
    Write-Host ''
    Write-Host 'Infrastructure is ready. Service launch skipped because -InfraOnly was provided.' -ForegroundColor Green
    Write-Host ''
    exit 0
}

Write-Step 'Launching Spring Boot services'
foreach ($service in $services) {
    Write-Host "    Starting $($service.Name) on http://localhost:$($service.Port)" -ForegroundColor Yellow
    Start-ServiceWindow -ServiceConfig $service
    Start-Sleep -Seconds 3
}

Write-Host ''
Write-Host 'All services are launching in separate PowerShell windows.' -ForegroundColor Green
Write-Host 'Flyway will create/update tables and seed data automatically.' -ForegroundColor Green
Write-Host ''
Write-Host 'Service URLs:' -ForegroundColor Cyan
foreach ($service in $services) {
    Write-Host "  $($service.Name.PadRight(24)) http://localhost:$($service.Port)" -ForegroundColor White
}
Write-Host ''
Write-Host 'Infrastructure:' -ForegroundColor Cyan
Write-Host '  PostgreSQL  localhost:5432' -ForegroundColor White
Write-Host '  Redis       localhost:6379' -ForegroundColor White
Write-Host '  RabbitMQ    localhost:5673, management http://localhost:15673' -ForegroundColor White
Write-Host ''
Write-Host 'Demo accounts:' -ForegroundColor Cyan
Write-Host '  Patient  patient01@healthcare.local / Patient@123' -ForegroundColor White
Write-Host '  Doctor   doctor01@healthcare.local  / Doctor@123' -ForegroundColor White
Write-Host ''
Write-Host 'Wait about 30-60 seconds for Maven and Spring Boot startup.' -ForegroundColor DarkGray
Write-Host ''
