#!/bin/sh
WORKSPACE=$(pwd)/rinha-de-backend-2023-q3/stress-test/user-files

if [ ! -d "rinha-de-backend-2023-q3" ]; then
    echo "rinha-de-backend-2023-q3 not found. Downloading..."
    git clone --depth=1 --branch=main --quiet https://github.com/zanfranceschi/rinha-de-backend-2023-q3
else
    echo "rinha-de-backend-2023-q3 found. Updating..."
    cd rinha-de-backend-2023-q3
    git pull --quiet
    cd ..
fi

cd rinha-de-backend-2023-q3

# check if directory gatling-charts-highcharts-bundle exists. if it doesnt, then download and unzip
if [ ! -d "gatling-charts-highcharts-bundle-3.9.5" ]; then
    echo "gatling-charts-highcharts-bundle-3.9.5 not found. Downloading..."
    wget --no-verbose https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.9.5/gatling-charts-highcharts-bundle-3.9.5-bundle.zip
    unzip gatling-charts-highcharts-bundle-3.9.5-bundle.zip
fi

cd gatling-charts-highcharts-bundle-3.9.5
./bin/gatling.sh -rm local -s RinhaBackendSimulation -rd "DESCRICAO" -rf $WORKSPACE/results -sf $WORKSPACE/simulations -rsf $WORKSPACE/resources
echo GATLING_OUTPUT_FOLDER=$(ls $WORKSPACE/results | sort | head -n 1)

cd ..
