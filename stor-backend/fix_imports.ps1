$files = @(
    "d:\ANTIGRAVITY\Stor\stor-backend\src\main\kotlin\com\stor\dashboard\DashboardRoutes.kt",
    "d:\ANTIGRAVITY\Stor\stor-backend\src\main\kotlin\com\stor\expenses\ExpenseRoutes.kt",
    "d:\ANTIGRAVITY\Stor\stor-backend\src\main\kotlin\com\stor\income\IncomeRoutes.kt",
    "d:\ANTIGRAVITY\Stor\stor-backend\src\main\kotlin\com\stor\loans\LoanRoutes.kt",
    "d:\ANTIGRAVITY\Stor\stor-backend\src\main\kotlin\com\stor\repayments\RepaymentRoutes.kt",
    "d:\ANTIGRAVITY\Stor\stor-backend\src\main\kotlin\com\stor\reports\ReportRoutes.kt",
    "d:\ANTIGRAVITY\Stor\stor-backend\src\main\kotlin\com\stor\search\SearchRoutes.kt"
)

foreach ($file in $files) {
    $content = Get-Content $file -Raw
    if ($content -notmatch "import io.ktor.server.application.\*") {
        # Insert import io.ktor.server.application.* after the first package declaration
        $newContent = $content -replace "(package [^\r\n]+[\r\n]+)", "`$1`nimport io.ktor.server.application.*`n"
        Set-Content -Path $file -Value $newContent -NoNewline
        Write-Host "Updated $file"
    }
}
