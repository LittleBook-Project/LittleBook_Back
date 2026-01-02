#!/usr/bin/env python3
"""
Generate a global code coverage report from JaCoCo XML files.
Parses coverage data for all services and outputs to GitHub Step Summary.
"""

import xml.etree.ElementTree as ET
import os
import sys
from pathlib import Path

# List of services to check
SERVICES = [
    "admin-service",
    "auth-service",
    "user-service",
    "review-service",
    "book-service",
]

def get_coverage_percentage(jacoco_xml_path):
    """
    Extract line coverage percentage from JaCoCo XML file.
    
    Args:
        jacoco_xml_path: Path to jacoco.xml file
        
    Returns:
        Tuple of (coverage_pct, covered_lines, total_lines) or None if file not found
    """
    if not os.path.exists(jacoco_xml_path):
        return None
    
    try:
        tree = ET.parse(jacoco_xml_path)
        root = tree.getroot()
        
        # Get counters directly under <report> tag (global stats)
        # Not from packages or classes
        for counter in root.findall('./counter[@type="LINE"]'):
            covered = int(counter.get('covered', 0))
            missed = int(counter.get('missed', 0))
            total = covered + missed
            
            if total > 0:
                coverage_pct = round((covered / total) * 100, 1)
                print(f"DEBUG: Parsing {jacoco_xml_path} - covered={covered}, missed={missed}, total={total}")
                return (coverage_pct, covered, total)
        
        return None
    except Exception as e:
        print(f"Error parsing {jacoco_xml_path}: {e}", file=sys.stderr)
        return None


def get_status_emoji(coverage_pct):
    """
    Get status emoji based on coverage percentage.
    
    Args:
        coverage_pct: Coverage percentage (0-100)
        
    Returns:
        Tuple of (emoji, status_text)
    """
    if coverage_pct >= 80:
        return ("✅", "Excellent")
    elif coverage_pct >= 60:
        return ("⚠️", "Good")
    elif coverage_pct >= 40:
        return ("⚠️", "Fair")
    else:
        return ("❌", "Low")


def generate_report():
    """Generate and write the coverage report to GitHub Step Summary."""
    
    summary_file = os.environ.get('GITHUB_STEP_SUMMARY', '/dev/null')
    
    with open(summary_file, 'a') as f:
        f.write("# 📊 Code Coverage Summary - All Services\n\n")
        f.write("| Service | Line Coverage | Status |\n")
        f.write("|---------|---|--------|\n")
        
        total_coverage = 0
        valid_services = 0
        
        for service in SERVICES:
            jacoco_xml = Path(service) / "target" / "site" / "jacoco" / "jacoco.xml"
            
            result = get_coverage_percentage(str(jacoco_xml))
            
            if result:
                coverage_pct, covered, total = result
                emoji, status_text = get_status_emoji(coverage_pct)
                
                f.write(f"| {service} | **{coverage_pct}%** ({covered}/{total}) | {emoji} {status_text} |\n")
                print(f"✅ {service}: {coverage_pct}% coverage ({covered}/{total} lines)")
                
                total_coverage += coverage_pct
                valid_services += 1
            else:
                f.write(f"| {service} | N/A | ⏭️ Skipped |\n")
                print(f"⏭️ {service}: No coverage data found")
        
        # Add average coverage
        if valid_services > 0:
            avg_coverage = total_coverage // valid_services
            f.write(f"\n**Average Coverage:** `{avg_coverage}%`\n")
            print(f"\n📊 Average coverage across {valid_services} services: {avg_coverage}%")
        
        f.write("\n---\n")


if __name__ == "__main__":
    generate_report()
    sys.exit(0)
