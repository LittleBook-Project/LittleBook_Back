#!/usr/bin/env bash
# Script d'installation des Git hooks
# Usage: ./setup-hooks.sh (à exécuter une seule fois après le clone)

set -e

echo "🔧 Configuration des Git hooks..."
git config core.hooksPath .githooks

# Rendre les hooks exécutables (important sur Linux/Mac)
chmod +x .githooks/*

echo "✅ Hooks Git activés dans .githooks/"
echo "   Le pre-commit bloquera maintenant les secrets Firebase/GCP"
