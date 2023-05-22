#!/bin/bash

echo "START 01_copyInstalls"

set -e

BASE_DIR=$1

echo "Copy MetaBatchAnalyzer WAR"
cp ${BASE_DIR}/apps/MetaBatchAnalyzer/target/*.war ${BASE_DIR}/docker-build/mba-stack/mba-image/installations/MBA#MBA.war

echo "Copy Batch Effects Viewer WAR"
cp ${BASE_DIR}/../DataAPI/apps/MetabatchOmicBrowser/target/*.war ${BASE_DIR}/docker-build/mba-stack/bev-image/installations/MBA#BEV.war

echo "List MBA Installations"
ls -lh ${BASE_DIR}/docker-build/mba-stack/mba-image/installations/

echo "List BEV Installations"
ls -lh ${BASE_DIR}/docker-build/mba-stack/bev-image/installations/

echo "FINISH 01_copyInstalls"

