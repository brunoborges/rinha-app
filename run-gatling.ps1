$WORKSPACE = "$(Get-Location)\rinha-de-backend-2023-q3\stress-test\user-files"

if (!(Test-Path "rinha-de-backend-2023-q3")) {
    Write-Host "rinha-de-backend-2023-q3 not found. Downloading..."
    git clone --single-branch --quiet https://github.com/zanfranceschi/rinha-de-backend-2023-q3
} else {
    Write-Host "rinha-de-backend-2023-q3 found. Updating..."
    cd rinha-de-backend-2023-q3
    git pull --quiet
    cd ..
}

cd rinha-de-backend-2023-q3

# check if directory gatling-charts-highcharts-bundle exists. if it doesnt, then download and unzip
if (!(Test-Path "gatling-charts-highcharts-bundle-3.9.5")) {
    Write-Host "gatling-charts-highcharts-bundle-3.9.5 not found. Downloading..."
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.9.5/gatling-charts-highcharts-bundle-3.9.5-bundle.zip" -OutFile "gatling-charts-highcharts-bundle-3.9.5-bundle.zip"
    Expand-Archive -Path "gatling-charts-highcharts-bundle-3.9.5-bundle.zip"
}

cd gatling-charts-highcharts-bundle-3.9.5
.\bin\gatling.ps1 -rm local -s RinhaBackendSimulation -rd "DESCRICAO" -rf $WORKSPACE\results -sf $WORKSPACE\simulations -rsf $WORKSPACE\resources
Write-Host "GATLING_OUTPUT_FOLDER=$(Get-ChildItem $WORKSPACE\results | Sort-Object | Select-Object -First 1)"

cd ..
