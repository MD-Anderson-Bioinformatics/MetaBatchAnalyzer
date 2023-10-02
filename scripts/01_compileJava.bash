#!/bin/bash

echo "START 01_compileJava"
set -e
BASE_DIR=$1

echo "SamplesValidation (different repo)"
cd ${BASE_DIR}/../StandardizedData/apps/SamplesValidation
mvn clean install dependency:copy-dependencies

echo "compile StdMWUtils (different repo)"
cd ${BASE_DIR}/../StandardizedData/apps/StdMWUtils
mvn clean install dependency:copy-dependencies

echo "compile MetaBatchAnalyzer"
cd ${BASE_DIR}/apps/MetaBatchAnalyzer
mvn clean install dependency:copy-dependencies

echo "compile Batch Effects Viewer (different repo - DataAPI)"
cd ${BASE_DIR}/../DataAPI/apps/MetabatchOmicBrowser
mvn clean install dependency:copy-dependencies

echo "list targets"
ls -lh ${BASE_DIR}/apps/*/target/
ls -lh ${BASE_DIR}/../DataAPI/apps/MetabatchOmicBrowser/target/
ls -lh ${BASE_DIR}/../StandardizedData/apps/*/target/

echo "FINISHED 01_compileJava"

