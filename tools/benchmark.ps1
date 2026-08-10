param(
  [int]$Repeats = 3,
  [int]$WarmupOperations = 200,
  [int]$MeasuredOperations = 2000
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Push-Location $root
try {
  $status = git status --porcelain
  if ($status) {
    throw "Benchmark provenance requires a clean git worktree. Commit the implementation first."
  }

  $env:SOURCE_COMMIT = (git rev-parse HEAD).Trim()
  $env:IMAGE_REF = "cache-strategies-bench:$($env:SOURCE_COMMIT.Substring(0, 12))"
  $env:BENCHMARK_REPEATS = "$Repeats"
  $env:WARMUP_OPERATIONS = "$WarmupOperations"
  $env:MEASURED_OPERATIONS = "$MeasuredOperations"
  $env:DEPENDENCY_LOCK_DIGEST = "sha256:$((Get-FileHash gradle/libs.versions.toml -Algorithm SHA256).Hash.ToLowerInvariant())"

  docker build -t $env:IMAGE_REF .
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
  $imageId = (docker image inspect $env:IMAGE_REF --format '{{.Id}}').Trim()
  $env:IMAGE_DIGEST = if ($imageId -match '^sha256:[0-9a-f]{64}$') {
    $imageId
  } else {
    "sha256:" + "0" * 64
  }

  docker compose up --no-build --abort-on-container-exit --exit-code-from benchmark benchmark
  $exitCode = $LASTEXITCODE
  docker compose down --volumes --remove-orphans
  exit $exitCode
} finally {
  Pop-Location
}
