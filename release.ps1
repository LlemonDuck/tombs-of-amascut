# assumes you have a project named plugin-hub in the parent directory

Set-Location -Path $PSScriptRoot

$PROJECT = Split-Path -Path $(Get-Location) -Leaf
$REPO = ((git remote get-url origin) -split ":")[1]
if ($PROJECT -eq "easy-teleports") {
    $PROJECT = "easy-pharaoh-sceptre"
}

$NEW_REVISION = git rev-parse HEAD

$VERSION_LINE = Get-Content build.gradle | Select-String -CaseSensitive "version = "
$VERSION = ($VERSION_LINE -split " ")[2].Replace("'", "")

Push-Location

Set-Location -Path "../plugin-hub"

Write-Host "Updating local master branch"
git fetch upstream
git checkout --force master
git reset --hard upstream/master

Write-Host "Updating local $PROJECT branch"
git checkout $PROJECT
git reset --hard master

Write-Host "Looks like I'm ready to make a release for $NEW_REVISION. Should I commit and push? [y/n]"
$confirm = Read-Host
if ($confirm -eq "y") {
    $outfile = "plugins/$PROJECT"
    
    "repository=https://github.com/$REPO" | Out-File -Encoding ASCII -FilePath $outfile
    "commit=$NEW_REVISION" | Out-File -Encoding ASCII -FilePath $outfile -Append
    
    git add $outfile
    git commit -m "update $PROJECT to v$VERSION"
    git push --force
    
    Start-Process "https://github.com/runelite/plugin-hub/compare/master...LlemonDuck:$PROJECT"
}

Pop-Location