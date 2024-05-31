#!/bin/bash
echo "start sedFiles"

echo "BEA_VERSION_TIMESTAMP"

BASEDIR="<REPLACE>"
DESIREDTAG="MBA_<REPLACE>"
SERVER_TITLE="<REPLACE>"

USER_ID="2002"
GROUP_ID="2002"
ENVIRON="EXTERNAL"
BEV_PORT="8080"
MBA_PORT="8181"

MBA_URL="<REPLACE>:${BEV_PORT}"
BEV_URL="<REPLACE>:${MBA_PORT}"

MBA_IMAGETXT="mdabcb/mba_image"
MBA_DESIREDTAG=$DESIREDTAG
MBATCH_IMAGETXT="mdabcb/mbatch_image"
MBATCH_DESIREDTAG=$DESIREDTAG
BEV_IMAGETXT="mdabcb/bev_image"
BEV_DESIREDTAG=$DESIREDTAG
PROP_DIR=${BASEDIR}/ext/PROPS
JOB_OUTPUT_DIR=${BASEDIR}/ext/OUTPUT
WEBSITE_DIR=${BASEDIR}/ext/WEBSITE
UTIL_DIR=${BASEDIR}/ext/UTIL

echo "PROPS/mba.properties_template"
if [ -e ${PROP_DIR}/mba.properties ]; then
	rm ${PROP_DIR}/mba.properties
fi
sed -e "s|<SERVER_TITLE>|${SERVER_TITLE}|g" -e "s|<MBA_URL>|${MBA_URL}|g" -e "s|<BEV_URL>|${BEV_URL}|g" ${PROP_DIR}/mba.properties_template > ${PROP_DIR}/mba.properties

echo "mba-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/mba-stack/mba-image/Dockerfile ]; then
	rm ${BASEDIR}/build/mba-stack/mba-image/Dockerfile
fi
sed -e "s|<DOCKER_UID>|${USER_ID}|g" ${BASEDIR}/build/mba-stack/mba-image/Dockerfile_template > ${BASEDIR}/build/mba-stack/mba-image/Dockerfile

echo "bev-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/mba-stack/bev-image/Dockerfile ]; then
	rm ${BASEDIR}/build/mba-stack/bev-image/Dockerfile
fi
sed -e "s|<USERID>|${USER_ID}|g" -e "s|<GROUPID>|${GROUP_ID}|g" ${BASEDIR}/build/mba-stack/bev-image/Dockerfile_template > ${BASEDIR}/build/mba-stack/bev-image/Dockerfile

echo "mbatch-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/mba-stack/mbatch-image/Dockerfile ]; then
	rm ${BASEDIR}/build/mba-stack/mbatch-image/Dockerfile
fi
sed -e "s|<USERID>|${USER_ID}|g" -e "s|<GROUPID>|${GROUP_ID}|g" -e "s|<GROUPID>|${GROUP_ID}|g" ${BASEDIR}/build/mba-stack/mbatch-image/Dockerfile_template > ${BASEDIR}/build/mba-stack/mbatch-image/Dockerfile

echo "docker-compose_template.yml"
if [ -e ${BASEDIR}/build/mba-stack/docker-compose.yml ]; then
	 rm ${BASEDIR}/build/mba-stack/docker-compose.yml
fi
sed -e "s|<BEV-PORT>|${BEV_PORT}|g" -e "s|<MBA-PORT>|${MBA_PORT}|g" -e "s|<ENVIRON>|${ENVIRON}|g" -e "s|<MBA-IMAGETXT>|${MBA_IMAGETXT}|g" -e "s|<MBA-DESIREDTAG>|${MBA_DESIREDTAG}|g" -e "s|<GDC-DESIREDTAG>|${GDC_DESIREDTAG}|g" -e "s|<MBATCH-IMAGETXT>|${MBATCH_IMAGETXT}|g" -e "s|<MBATCH-DESIREDTAG>|${MBATCH_DESIREDTAG}|g" -e "s|<BEV-IMAGETXT>|${BEV_IMAGETXT}|g" -e "s|<BEV-DESIREDTAG>|${BEV_DESIREDTAG}|g" -e "s|<PROP-DIR>|${PROP_DIR}|g" -e "s|<JOB-OUTPUT-DIR>|${JOB_OUTPUT_DIR}|g" -e "s|<WEBSITE-DIR>|${WEBSITE_DIR}|g" -e "s|<UTIL-DIR>|${UTIL_DIR}|g" ${BASEDIR}/build/mba-stack/docker-compose_template.yml > ${BASEDIR}/build/mba-stack/docker-compose.yml

echo "finish sedFiles"
