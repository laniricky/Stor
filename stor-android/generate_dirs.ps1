$baseDir = "d:\ANTIGRAVITY\Stor\stor-android\app\src\main\kotlin\com\stor"
$dirs = @(
    "data\remote\api",
    "data\remote\dto",
    "data\repository",
    "domain\model",
    "domain\repository",
    "domain\usecase",
    "presentation\navigation",
    "presentation\theme",
    "presentation\screens\auth",
    "presentation\screens\dashboard",
    "presentation\screens\expenses",
    "presentation\screens\income",
    "presentation\screens\loans",
    "presentation\screens\repayments",
    "presentation\screens\reports",
    "presentation\screens\search",
    "presentation\screens\more"
)

foreach ($dir in $dirs) {
    $fullPath = Join-Path $baseDir $dir
    New-Item -ItemType Directory -Force -Path $fullPath
}
