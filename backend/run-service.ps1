# ============================================================
# run-service.ps1 — Healthcare Platform Backend Service Runner
# ============================================================
# Usage: .\run-service.ps1 <service-name> [-Clean] [-AppPort <port>]
#
# Services:
#   auth-service         port 8086  — Auth + Admin User Management (/api/v1/admin/users)
#   user-service         port 8082  — User Profiles
#   doctor-service       port 8083  — Doctor schedules, leaves, + Admin Doctor endpoint
#   appointment-service  port 8084  — Appointments + Admin Appointment Governance
#   notification-service port 8085  — Notifications
#   ai-service           port 8087  — AI Chat (optional)
#
# NOTE (Task 5 - Admin Management):
#   There is NO separate admin-service. Admin endpoints are embedded in existing services:
#     - auth-service:        GET/PUT /api/v1/admin/users/*
#     - doctor-service:      GET     /api/v1/admin/doctors
#     - appointment-service: GET/PUT /api/v1/admin/appointments/*
#   Admin account: admin01@healthcare.local / Admin@123  (role=ADMIN, auth-service)
#   All admin endpoints require a valid JWT with role=ADMIN (enforced at controller layer).
# ============================================================
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet('auth-service', 'user-service', 'doctor-service', 'appointment-service', 'notification-service', 'ai-service')]
    [string]$Service,

    [switch]$Clean,

    [string]$AppPort,

    [string]$DbUrl,

    [string]$MavenCommand,

    [string]$JavaHome
)

$ErrorActionPreference = 'Stop'
if (Get-Variable PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$serviceDefaults = @{
    'auth-service' = @{
        AppPort = '8086'
        DbUrl = 'jdbc:postgresql://localhost:5432/auth_db'
    }
    'user-service' = @{
        AppPort = '8082'
        DbUrl = 'jdbc:postgresql://localhost:5432/user_db'
    }
    'doctor-service' = @{
        AppPort = '8083'
        DbUrl = 'jdbc:postgresql://localhost:5432/doctor_db'
    }
    'appointment-service' = @{
        AppPort = '8084'
        DbUrl = 'jdbc:postgresql://localhost:5432/appointment_db'
    }
    'notification-service' = @{
        AppPort = '8085'
        DbUrl = 'jdbc:postgresql://localhost:5432/notification_db'
    }
    'ai-service' = @{
        AppPort = '8087'
        DbUrl = 'jdbc:postgresql://localhost:5432/ai_db'
    }
}

function Test-JavaHome {
    param(
        [string]$Candidate,
        [int]$RequiredMajorVersion = 21
    )

    if ([string]::IsNullOrWhiteSpace($Candidate)) {
        return $false
    }

    $javaExe = Join-Path $Candidate 'bin\java.exe'
    $javacExe = Join-Path $Candidate 'bin\javac.exe'

    if (-not (Test-Path $javaExe) -or -not (Test-Path $javacExe)) {
        return $false
    }

    $versionLine = cmd /c """$javaExe"" -version 2>&1" | Select-Object -First 1
    return $versionLine -match "`"$RequiredMajorVersion(\.|`")"
}

function Get-JavaHomeCandidates {
    param([string]$RequestedHome)

    $candidates = [System.Collections.Generic.List[string]]::new()

    foreach ($candidate in @($RequestedHome, $env:JAVA_HOME)) {
        if (-not [string]::IsNullOrWhiteSpace($candidate)) {
            $candidates.Add($candidate)
        }
    }

    foreach ($pattern in @(
        'C:\Program Files\Eclipse Adoptium\jdk-21*',
        'C:\Program Files\Java\jdk-21*',
        'C:\Program Files\Microsoft\jdk-21*'
    )) {
        Get-ChildItem -Path $pattern -Directory -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            ForEach-Object { $candidates.Add($_.FullName) }
    }

    $javaPaths = cmd /c where java 2>$null
    foreach ($javaPath in $javaPaths) {
        if ([string]::IsNullOrWhiteSpace($javaPath)) {
            continue
        }

        $candidate = Split-Path -Parent (Split-Path -Parent $javaPath)
        if (-not [string]::IsNullOrWhiteSpace($candidate)) {
            $candidates.Add($candidate)
        }
    }

    return $candidates | Select-Object -Unique
}

function Resolve-JavaHome {
    param([string]$RequestedHome)

    foreach ($candidate in Get-JavaHomeCandidates -RequestedHome $RequestedHome) {
        if (Test-JavaHome -Candidate $candidate) {
            return $candidate
        }
    }

    throw "Could not locate a JDK 21 installation. Set JAVA_HOME or pass -JavaHome with a JDK 21 path."
}

function Resolve-MavenCommand {
    param([string]$RequestedCommand)

    if (-not [string]::IsNullOrWhiteSpace($RequestedCommand)) {
        if (Test-Path $RequestedCommand) {
            return (Resolve-Path $RequestedCommand).Path
        }

        $resolved = Get-Command $RequestedCommand -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($resolved) {
            return $resolved.Source
        }

        throw "Could not resolve Maven command '$RequestedCommand'."
    }

    foreach ($candidateName in @('mvn.cmd', 'mvn')) {
        $resolved = Get-Command $candidateName -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($resolved) {
            return $resolved.Source
        }
    }

    foreach ($pattern in @(
        'C:\Program Files\JetBrains\IntelliJ IDEA*\plugins\maven\lib\maven3\bin\mvn.cmd',
        'C:\Program Files\Apache\maven\bin\mvn.cmd',
        'C:\apache-maven-*\bin\mvn.cmd'
    )) {
        $match = Get-Item $pattern -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -First 1

        if ($match) {
            return $match.FullName
        }
    }

    throw "Could not locate Maven. Install Maven, add mvn.cmd to PATH, or pass -MavenCommand."
}

function Assert-PortAvailable {
    param([string]$PortValue)

    $portNumber = 0
    if (-not [int]::TryParse($PortValue, [ref]$portNumber)) {
        return
    }

    $listeners = Get-NetTCPConnection -LocalPort $portNumber -State Listen -ErrorAction SilentlyContinue
    if (-not $listeners) {
        return
    }

    $owningProcess = $listeners | Select-Object -First 1 -ExpandProperty OwningProcess
    $processName = '<unknown>'
    if ($owningProcess) {
        $process = Get-Process -Id $owningProcess -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($process) {
            $processName = $process.ProcessName
        }
    }

    throw "Port $portNumber is already in use by process '$processName' (PID $owningProcess). Pass -AppPort with a free port or stop the process first."
}

function Assert-PostgresReady {
    param(
        [string]$JdbcUrl,
        [string]$Username,
        [string]$Password
    )

    $jdbcPattern = '^jdbc:postgresql:\/\/(?<host>[^:\/]+)(:(?<port>\d+))?\/(?<database>[^?\s]+)'
    $match = [regex]::Match($JdbcUrl, $jdbcPattern)
    if (-not $match.Success) {
        return
    }

    $psql = Get-Command psql -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $psql) {
        return
    }

    $dbHost = $match.Groups['host'].Value
    $port = if ($match.Groups['port'].Success) { $match.Groups['port'].Value } else { '5432' }
    $database = $match.Groups['database'].Value

    $previousPassword = $env:PGPASSWORD
    try {
        $env:PGPASSWORD = $Password
        $probeOutput = & $psql.Source -h $dbHost -p $port -U $Username -d $database -tAc "select 1" 2>&1
        $probeExitCode = $LASTEXITCODE
    }
    finally {
        $env:PGPASSWORD = $previousPassword
    }

    if ($probeExitCode -eq 0) {
        return
    }

    $probeMessage = ($probeOutput | Out-String).Trim()
    $repairScript = Join-Path (Split-Path $PSScriptRoot -Parent) 'docker\postgres\ensure-healthcare-databases.ps1'
    $guidance = "PostgreSQL preflight failed for '$JdbcUrl' with user '$Username'. psql reported: $probeMessage"

    if ($probeMessage -match 'role ".+" does not exist|database ".+" does not exist') {
        $guidance += " If this machine is using an existing PostgreSQL volume from another project, run `powershell -NoProfile -ExecutionPolicy Bypass -File `"$repairScript`"`" to create the healthcare role and service databases."
    }

    throw $guidance
}

$defaultConfig = $serviceDefaults[$Service]

if ([string]::IsNullOrWhiteSpace($AppPort)) {
    $AppPort = $defaultConfig.AppPort
}

if ([string]::IsNullOrWhiteSpace($DbUrl)) {
    $DbUrl = $defaultConfig.DbUrl
}

$resolvedJavaHome = Resolve-JavaHome -RequestedHome $JavaHome
$resolvedMavenCommand = Resolve-MavenCommand -RequestedCommand $MavenCommand

$env:JAVA_HOME = $resolvedJavaHome
$env:APP_PORT = $AppPort
$env:DB_URL = $DbUrl

if (-not $env:DB_USERNAME) { $env:DB_USERNAME = 'healthcare' }
if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = 'healthcare' }
if (-not $env:JWT_SECRET) { $env:JWT_SECRET = 'local-dev-healthcare-jwt-secret-change-before-prod' }
if (-not $env:RABBITMQ_URL) { $env:RABBITMQ_URL = 'amqp://healthcare:healthcare@localhost:5673' }
if (-not $env:REDIS_HOST) { $env:REDIS_HOST = 'localhost' }
if (-not $env:REDIS_PORT) { $env:REDIS_PORT = '6379' }

Assert-PortAvailable -PortValue $env:APP_PORT
Assert-PostgresReady -JdbcUrl $env:DB_URL -Username $env:DB_USERNAME -Password $env:DB_PASSWORD

Write-Host "Service   : $Service"
Write-Host "Port      : $env:APP_PORT"
Write-Host "Database  : $env:DB_URL"
Write-Host "JAVA_HOME : $env:JAVA_HOME"
Write-Host "Maven     : $resolvedMavenCommand"

$localMavenRepo = Join-Path $PSScriptRoot '.m2\repository'
if (-not (Test-Path $localMavenRepo)) {
    New-Item -ItemType Directory -Path $localMavenRepo -Force | Out-Null
}

$mavenArgs = @("-Dmaven.repo.local=$localMavenRepo", '-pl', "services/$Service", '-am')
if ($Clean) {
    $mavenArgs += 'clean'
}
$mavenArgs += 'spring-boot:run'

Push-Location $PSScriptRoot
try {
    & $resolvedMavenCommand @mavenArgs
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
