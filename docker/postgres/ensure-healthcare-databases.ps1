[CmdletBinding()]
param(
    [string]$AdminUser = 'postgres',
    [string]$AdminPassword = 'root',
    [string]$DbHost = 'localhost',
    [int]$Port = 5432,
    [string]$AppUser = 'healthcare',
    [string]$AppPassword = 'healthcare'
)

$ErrorActionPreference = 'Stop'

function Escape-SqlLiteral {
    param([string]$Value)
    return $Value.Replace("'", "''")
}

$psql = Get-Command psql -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $psql) {
    throw "psql was not found in PATH. Install PostgreSQL client tools or run the equivalent SQL inside the postgres container."
}

$databases = @(
    'auth_db',
    'user_db',
    'doctor_db',
    'appointment_db',
    'notification_db'
)

$escapedAppUser = Escape-SqlLiteral -Value $AppUser
$escapedAppPassword = Escape-SqlLiteral -Value $AppPassword
$databaseValues = ($databases | ForEach-Object { "    ('$_')" }) -join ",`r`n"

$bootstrapSql = @"
DO `$do`$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$escapedAppUser') THEN
        EXECUTE format('CREATE ROLE %I LOGIN PASSWORD %L', '$escapedAppUser', '$escapedAppPassword');
    ELSE
        EXECUTE format('ALTER ROLE %I WITH LOGIN PASSWORD %L', '$escapedAppUser', '$escapedAppPassword');
    END IF;
END
`$do`$;

SELECT format('CREATE DATABASE %I OWNER %I', db_name, '$escapedAppUser')
FROM (VALUES
$databaseValues
) AS required(db_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = db_name
)
\gexec

SELECT format('ALTER DATABASE %I OWNER TO %I', db_name, '$escapedAppUser')
FROM (VALUES
$databaseValues
) AS required(db_name)
WHERE EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = db_name
)
\gexec
"@

$sqlFile = Join-Path $env:TEMP "ensure-healthcare-databases-$PID.sql"
[System.IO.File]::WriteAllText($sqlFile, $bootstrapSql, [System.Text.UTF8Encoding]::new($false))

$previousPassword = $env:PGPASSWORD
try {
    $env:PGPASSWORD = $AdminPassword

    & $psql.Source -v ON_ERROR_STOP=1 -h $DbHost -p $Port -U $AdminUser -d postgres -f $sqlFile
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create or update the healthcare role and databases."
    }

    foreach ($database in $databases) {
        & $psql.Source -v ON_ERROR_STOP=1 -h $DbHost -p $Port -U $AdminUser -d $database -c "ALTER SCHEMA public OWNER TO $AppUser; GRANT ALL ON SCHEMA public TO $AppUser;"
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to update schema ownership in database '$database'."
        }
    }
}
finally {
    $env:PGPASSWORD = $previousPassword
    Remove-Item $sqlFile -ErrorAction SilentlyContinue
}

Write-Host "Ensured role '$AppUser' and the service databases exist on ${DbHost}:$Port."
