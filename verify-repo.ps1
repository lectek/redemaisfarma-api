param(
    [string]$ExpectedRemote = "https://github.com/saudemaisfarma/redemaisfarma-api.git",
    [string]$ExpectedBranch = "atualizacao-local"
)

function Fail($msg) {
    Write-Host "REPO CHECK FAILED: $msg" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path .git)) {
    Fail("Missing .git directory: not a git repository.")
}

$remoteUrl = (git remote get-url origin).Trim()
if ($remoteUrl -ne $ExpectedRemote) {
    Fail("Remote URL mismatch: found $remoteUrl but expected $ExpectedRemote.")
}

$branch = (git rev-parse --abbrev-ref HEAD).Trim()
if ($branch -ne $ExpectedBranch) {
    Fail("Branch mismatch: current branch is $branch but expected $ExpectedBranch.")
}

$status = git status -sb
if ($status -match "^[MADRC]") {
    Fail("Working tree is dirty; clean or commit changes before pushing.`n$status")
}

$head = (git rev-parse HEAD).Trim()
$marker = ""
if (Test-Path DEPLOY_MARKER.md) {
    $marker = (Get-Content DEPLOY_MARKER.md -Raw).Trim()
}

if ($marker -and $marker -notlike "*$head*") {
    Write-Warning "DEPLOY_MARKER.md does not mention current HEAD ($head). Update it with: git rev-parse HEAD and git add DEPLOY_MARKER.md"
}

Write-Host "Repository check passed: remote=$remoteUrl branch=$branch HEAD=$head"
