param(
  [switch]$SkipDocker
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
  param([string]$Message)
  $script:failures.Add($Message)
}

function Require-File {
  param([string]$RelativePath)
  $path = Join-Path $root $RelativePath
  if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
    Add-Failure "Missing file: $RelativePath"
  }
}

function Invoke-Checked {
  param(
    [string]$Label,
    [scriptblock]$Command
  )
  & $Command
  $exitCode = $LASTEXITCODE
  if ($exitCode -ne 0) {
    Add-Failure "$Label failed with exit code $exitCode"
  }
  $global:LASTEXITCODE = 0
}

$requiredFiles = @(
  "README.md",
  "project.yaml",
  "REFERENCES.md",
  "AGENTS.md",
  "sdd/spec.md",
  "sdd/benchmark-plan.md",
  "sdd/architecture-decision.md",
  "sdd/technical-decision.md",
  "sdd/agent-handoff.md",
  "sdd/reuse-improvement-review.md",
  "compose.yaml",
  "gradlew",
  "gradlew.bat",
  "gradle/wrapper/gradle-wrapper.jar",
  "gradle/wrapper/gradle-wrapper.properties",
  "benchmarks/results/cache-strategies-v2.json",
  ".portfolio/contracts/benchmark-result-v2.schema.json"
)
foreach ($file in $requiredFiles) { Require-File $file }

$reuseReviewPath = Join-Path $root "sdd/reuse-improvement-review.md"
if (Test-Path -LiteralPath $reuseReviewPath -PathType Leaf) {
  $reuseReview = Get-Content -Raw -LiteralPath $reuseReviewPath
  if ($reuseReview -match "<id>|<project-name>") {
    Add-Failure "Reuse improvement review still contains template placeholders"
  }
  if ($reuseReview.Contains('|  | `patch_now|backlog|reject` |')) {
    Add-Failure "Reuse improvement review still contains the blank template finding row"
  }
  $requiredFinalGatePatterns = @(
    "(?m)^- \[x\] Reusable improvements were patched or recorded\.\r?$",
    "(?m)^- \[x\] Project-specific implementation was not moved into the kit\.\r?$",
    "(?m)^- \[x\] Validation reflects .+\.\r?$"
  )
  foreach ($pattern in $requiredFinalGatePatterns) {
    if ($reuseReview -notmatch $pattern) {
      Add-Failure "Reuse improvement review final gate is incomplete: $pattern"
    }
  }
}

$benchmarkFiles = @()
$benchmarkDir = Join-Path $root "benchmarks/results"
if (Test-Path -LiteralPath $benchmarkDir -PathType Container) {
  $benchmarkFiles = @(Get-ChildItem -LiteralPath $benchmarkDir -Filter *.json -File)
}
if ($benchmarkFiles.Count -eq 0) {
  Add-Failure "Missing benchmark JSON under benchmarks/results"
}

Push-Location -LiteralPath $root
try {
  foreach ($file in $benchmarkFiles) {
    Invoke-Checked "benchmark JSON validation: $($file.Name)" { python -m json.tool $file.FullName | Out-Null }
    if ($file.Name -eq "cache-strategies-v2.json") {
      try {
        $result = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json
        if ($result.schema_version -ne 2) { Add-Failure "V2 benchmark schema_version must equal 2" }
        foreach ($property in @("run_id", "project", "benchmark_id", "workload", "metrics", "execution", "environment", "provenance", "comparability_key")) {
          if (-not ($result.PSObject.Properties.Name -contains $property)) {
            Add-Failure "V2 benchmark is missing property: $property"
          }
        }
        if ($result.metrics.Count -lt 1) { Add-Failure "V2 benchmark must contain metrics" }
        foreach ($metric in $result.metrics) {
          foreach ($property in @("name", "value", "unit", "direction", "samples", "failures", "summary")) {
            if (-not ($metric.PSObject.Properties.Name -contains $property)) {
              Add-Failure "V2 metric is missing property: $property"
            }
          }
        }
        foreach ($digest in @($result.workload.fixture_digest, $result.workload.config_digest, $result.provenance.image_digest, $result.provenance.dependency_lock_digest, $result.provenance.artifact_digest)) {
          if ([string]$digest -notmatch '^sha256:[0-9a-f]{64}$') {
            Add-Failure "V2 benchmark contains an invalid SHA-256 digest"
          }
        }
        if ([string]$result.provenance.source_commit -notmatch '^[0-9a-f]{40}$') {
          Add-Failure "V2 benchmark source_commit must be a 40-character Git SHA"
        }
      } catch {
        Add-Failure "Cannot validate V2 benchmark: $($_.Exception.Message)"
      }
    }
  }

  if (Test-Path -LiteralPath (Join-Path $root "src") -PathType Container) {
    $previousPythonPath = $env:PYTHONPATH
    $srcPath = Join-Path $root "src"
    if ($previousPythonPath) {
      $env:PYTHONPATH = $srcPath + [System.IO.Path]::PathSeparator + $previousPythonPath
    } else {
      $env:PYTHONPATH = $srcPath
    }
    Invoke-Checked "python compile src" { python -m compileall -q (Join-Path $root "src") }
    if (Test-Path -LiteralPath (Join-Path $root "tests") -PathType Container) {
      Invoke-Checked "python compile tests" { python -m compileall -q (Join-Path $root "tests") }
      Invoke-Checked "python unittest" { python -m unittest discover -s (Join-Path $root "tests") -v }
    }
    $env:PYTHONPATH = $previousPythonPath
  }

  if (Test-Path -LiteralPath (Join-Path $root "build.gradle.kts") -PathType Leaf) {
    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) {
      $wrapper = if ([System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT) {
        Join-Path $root "gradlew.bat"
      } else {
        Join-Path $root "gradlew"
      }
      Invoke-Checked "gradle check" { & $wrapper check --no-daemon }
    } elseif ($SkipDocker) {
      Add-Failure "Java 21 is required when Docker validation is skipped"
    }
  }
} finally {
  Pop-Location
}

$legacy = ("ro" + "che" + "do")
$patterns = @($legacy, ($legacy.Substring(0,1).ToUpper() + $legacy.Substring(1)))
$searchFiles = Get-ChildItem -Path $root -Recurse -File | Where-Object {
  $normalized = $_.FullName -replace "\\", "/"
  $normalized -notmatch "/.git/" -and
  $normalized -notmatch "/data/runtime/" -and
  $_.Extension -in @(".md", ".yaml", ".yml", ".json", ".ps1", ".py", ".js", ".ts", ".tsx", ".go", ".kt", ".java")
}
$forbidden = Select-String -Path $searchFiles.FullName -Pattern $patterns -SimpleMatch -ErrorAction SilentlyContinue
if ($forbidden) {
  Add-Failure "Forbidden legacy project nickname found"
}

if (-not $SkipDocker -and (Test-Path -LiteralPath (Join-Path $root "Dockerfile") -PathType Leaf)) {
  $imageName = (Split-Path -Leaf $root).ToLowerInvariant()
  Invoke-Checked "docker build" { docker build -t $imageName $root | Out-Null }
}

if ($failures.Count -gt 0) {
  # Write-Error is a terminating error while $ErrorActionPreference is "Stop",
  # so emitting the list through it aborts on the first entry and hides every
  # remaining failure. Report the complete list on the success stream instead.
  Write-Host "portfolio project validation failed with $($failures.Count) issue(s):"
  foreach ($failure in $failures) {
    Write-Host "  - $failure"
    if ($env:GITHUB_ACTIONS -eq "true") {
      Write-Host "::error::$failure"
    }
  }
  exit 1
}

Write-Host "portfolio project validation passed"
