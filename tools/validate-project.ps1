param(
  [switch]$SkipDocker,
  [switch]$SkipBuild,
  [switch]$SkipReadmeMetric,
  [string]$BenchmarkResultPath = "benchmarks/results/cache-strategies-v2.json"
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
Require-File $BenchmarkResultPath

$selectedBenchmarkPath = [System.IO.Path]::GetFullPath((Join-Path $root $BenchmarkResultPath))

$primaryMetricName = ""
$manifestPath = Join-Path $root "project.yaml"
if (Test-Path -LiteralPath $manifestPath -PathType Leaf) {
  $manifestText = Get-Content -Raw -LiteralPath $manifestPath
  $primaryMetricMatch = [regex]::Match($manifestText, "(?m)^\s+primary_metric:\s*([^\r\n#]+)")
  if ($primaryMetricMatch.Success) {
    $primaryMetricName = $primaryMetricMatch.Groups[1].Value.Trim().Trim('"').Trim("'")
  } else {
    Add-Failure "project.yaml is missing benchmark.primary_metric"
  }
}

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
    try {
      $null = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json
    } catch {
      Add-Failure "Invalid benchmark JSON $($file.Name): $($_.Exception.Message)"
      continue
    }
    if ([System.IO.Path]::GetFullPath($file.FullName) -eq $selectedBenchmarkPath) {
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
        $primaryMetric = @($result.metrics | Where-Object { $_.name -eq $primaryMetricName })
        if ($primaryMetric.Count -ne 1) {
          Add-Failure "V2 benchmark must contain primary metric: $primaryMetricName"
        } else {
          $valueText = [Convert]::ToString($primaryMetric[0].value, [System.Globalization.CultureInfo]::InvariantCulture)
          $roundedText = ([double]$primaryMetric[0].value).ToString("0.000", [System.Globalization.CultureInfo]::InvariantCulture)
          $readmeOpening = ((Get-Content -LiteralPath (Join-Path $root "README.md") -TotalCount 8) -join "`n")
          if (-not $SkipReadmeMetric -and -not $readmeOpening.Contains($valueText) -and -not $readmeOpening.Contains($roundedText)) {
            Add-Failure "README opening must include primary metric value: $valueText"
          }
        }
      } catch {
        Add-Failure "Cannot validate V2 benchmark: $($_.Exception.Message)"
      }
    }
  }

  if (-not $SkipBuild -and (Test-Path -LiteralPath (Join-Path $root "build.gradle.kts") -PathType Leaf)) {
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
