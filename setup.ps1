# Setup script for Housing Utilities System
Write-Host "=== Housing Utilities System Setup ===" -ForegroundColor Cyan

# Check if .env exists in HousingUtilitiesSystemAdmin
$adminEnvPath = ".\HousingUtilitiesSystemAdmin\.env"
$rootEnvPath = ".\.env"

if (Test-Path $adminEnvPath) {
    Write-Host "Found .env file in HousingUtilitiesSystemAdmin directory" -ForegroundColor Green
    Write-Host "Copying .env file to root directory..." -ForegroundColor Yellow
    
    Copy-Item $adminEnvPath $rootEnvPath -Force
    Write-Host "✓ .env file copied successfully" -ForegroundColor Green
} elseif (Test-Path $rootEnvPath) {
    Write-Host "✓ .env file already exists in root directory" -ForegroundColor Green
} else {
    Write-Host "⚠ No .env file found!" -ForegroundColor Red
    Write-Host "Creating .env from .env.example..." -ForegroundColor Yellow
    
    if (Test-Path ".\.env.example") {
        Copy-Item ".\.env.example" $rootEnvPath
        Write-Host "✓ Created .env from .env.example" -ForegroundColor Green
        Write-Host "⚠ Please edit .env file and fill in your configuration values!" -ForegroundColor Yellow
    } else {
        Write-Host "✗ .env.example not found. Please create .env manually." -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n=== Building project ===" -ForegroundColor Cyan
mvn clean install

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✓ Setup completed successfully!" -ForegroundColor Green
    Write-Host "`nTo run the applications:" -ForegroundColor Cyan
    Write-Host "  Admin:    cd HousingUtilitiesSystemAdmin && mvn spring-boot:run -Dspring-boot.run.profiles=dev" -ForegroundColor White
    Write-Host "  Chairman: cd HousingUtilitiesSystemChairman && mvn spring-boot:run" -ForegroundColor White
} else {
    Write-Host "`n✗ Build failed. Please check the errors above." -ForegroundColor Red
    exit 1
}
