# Script d'installation des Git hooks pour Windows
# Usage: .\setup-hooks.ps1 (à exécuter une seule fois après le clone)

Write-Host "🔧 Configuration des Git hooks..." -ForegroundColor Cyan
git config core.hooksPath .githooks

Write-Host "✅ Hooks Git activés dans .githooks/" -ForegroundColor Green
Write-Host "   Le pre-commit bloquera maintenant les secrets Firebase/GCP" -ForegroundColor Yellow
