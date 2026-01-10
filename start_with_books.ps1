# Start script: fetch books from OpenLibrary, then start docker-compose
# Usage: Run in PowerShell from this folder
#   .\start_with_books.ps1

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "Working dir: $root"

# 1) Run the Python fetch script
$script = Join-Path $root 'scripts\fetch_openlibrary_top100.py'
if (-Not (Test-Path $script)) {
    Write-Error "Fetch script not found: $script"
    exit 1
}

# Ensure output dir exists
$outdir = Join-Path $root 'dev-data\book-service'
if (-Not (Test-Path $outdir)) {
    New-Item -ItemType Directory -Path $outdir | Out-Null
}

Write-Host "Running OpenLibrary fetch script..."
try {
    & python $script
} catch {
    Write-Warning "Python script failed: $_"
    Write-Host "If you don't have Python installed, install it or run the fetch script manually. Aborting."
    exit 1
}

# 2) Start docker-compose stack
Write-Host "Starting docker-compose (build + up)..."
Push-Location $root
try {
    docker-compose up --build -d
} catch {
    Write-Error "docker-compose failed: $_"
    Pop-Location
    exit 1
}

Write-Host "Waiting a few seconds for containers to initialize..."
Start-Sleep -Seconds 5

Write-Host "Tailing book-service logs (press Ctrl+C to stop)..."
try {
    docker-compose logs -f book-service
} finally {
    Pop-Location
}
