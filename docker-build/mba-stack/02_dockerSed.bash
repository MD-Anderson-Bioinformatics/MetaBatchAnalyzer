#!/bin/bash

echo "START GitLabCi mba-stack 02_dockerSed"

echo "BEA_VERSION_TIMESTAMP"

set -e

BASE_DIR=$1

# release version, such as BEA_VERSION_TIMESTAMP
RELEASE=${2}
# user id, such as 2002
USER_ID=${3}

# outside port for MBA Tomcat, such as 8080
MBA_PORT=${4}
# outside port for BEV Tomcat, such as 8080
BEV_PORT=${5}
# not used - formerly used for sub-net
NOTUSED=${6}
# environment, usually dvlp, stag, or prod
ENVIRON=${7}
START_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/10p_startPROD.bash
STOP_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/20p_stopPROD.bash
UPCHECK_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/30p_checkPROD.bash
if [ "${ENVIRON}" == "stag" ]; then
	START_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/10s_startSTAG.bash
	STOP_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/20s_stopSTAG.bash
	UPCHECK_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/30s_checkSTAG.bash
fi
if [ "${ENVIRON}" == "dvlp" ]; then
	START_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/10d_startDVLP.bash
	STOP_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/20d_stopDVLP.bash
	UPCHECK_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/30d_checkDVLP.bash
fi
if [ "${ENVIRON}" == "hub" ]; then
	START_SCRIPT=-
	STOP_SCRIPT=-
	UPCHECK_SCRIPT=-
fi

# local paths point to local setup for DVLP, STAG, or PROD
LOCAL_PATH_ENV=${8}
### file with properties and job list for this installation
PROP_DIR=${LOCAL_PATH_ENV}/MBA/PROPS
### directory for output from MBA and MBatch Results
JOB_OUTPUT_DIR=${LOCAL_PATH_ENV}/MBA/OUTPUT
### directory for the Batch Effects Website
WEBSITE_DIR=${LOCAL_PATH_ENV}/MBA/WEBSITE
### read-only directory for util files
UTIL_DIR=${LOCAL_PATH_ENV}/MBA/UTILS

# URL and tag to use as image name for MBA image, such as mdabcb/mba_image:DAP_BEA_VERSION_TIMESTAMP
MBA_IMAGEURL=${9}
# URL and tag to use as image name for MBatch image, such as mdabcb/mbatch_image:DAP_BEA_VERSION_TIMESTAMP
MBATCH_IMAGEURL=${10}
# URL and tag to use as image name for BEV image, such as mdabcb/bev_image:DAP_BEA_VERSION_TIMESTAMP
BEV_IMAGEURL=${11}

OUTSIDE_CONFIGPATH=${12}
ZIPTMPPATH=${13}

# path for log files
LOGPATH=${14}

# group id, such as 2002
GROUP_ID=${15}

# path for Shaidy server setup
SHAIDY_DIR=${16}

# Dockerfile extension (used to create internally used _local dockerfile)
DF_EXTENSION=${17}
LOCAL_UID=${18}
DOCKERFILE_URL_MBA=${19}
DOCKERFILE_URL_MBT=${20}
DOCKERFILE_URL_BEV=${21}

echo "MBA arguments"
echo "BASE_DIR=${BASE_DIR}"
echo "RELEASE=${RELEASE}"
echo "USER_ID=${USER_ID}"
echo "GROUP_ID=${GROUP_ID}"
echo "MBA_PORT=${MBA_PORT}"
echo "BEV_PORT=${BEV_PORT}"
echo "ENVIRON=${ENVIRON}"
echo "LOCAL_PATH_ENV=${LOCAL_PATH_ENV}"
echo "MBA_IMAGEURL=${MBA_IMAGEURL}"
echo "MBATCH_IMAGEURL=${MBATCH_IMAGEURL}"
echo "BEV_IMAGEURL=${BEV_IMAGEURL}"
echo "OUTSIDE_CONFIGPATH=${OUTSIDE_CONFIGPATH}"
echo "ZIPTMPPATH=${ZIPTMPPATH}"
echo "LOGPATH=${LOGPATH}"
echo "GROUP_ID=${GROUP_ID}"
echo "SHAIDY_DIR=${SHAIDY_DIR}"

echo "create Dockerfile from Dockerfile_template"

# #- <SHAIDY-DIR>:/MBA/SHAIDY

if [ "${LOCAL_UID}" != "" ]; then
	echo "do local Dockerfile"
	rm -f ${BASE_DIR}/mba-image/Dockerfile_newuser
	sed -e "s|<IMAGEURL>|${DOCKERFILE_URL_MBA}|g" \
	    -e "s|<USERID>|${USER_ID}|g" \
	    -e "s|<LOCALUID>|${LOCAL_UID}|g" \
	    ${BASE_DIR}/mba-image/Dockerfile_newuser_template > ${BASE_DIR}/mba-image/Dockerfile_newuser

	rm -f ${BASE_DIR}/bev-image/Dockerfile_newuser
	sed -e "s|<IMAGEURL>|${DOCKERFILE_URL_BEV}|g" \
	    -e "s|<USERID>|${USER_ID}|g" \
	    -e "s|<LOCALUID>|${LOCAL_UID}|g" \
	    ${BASE_DIR}/bev-image/Dockerfile_newuser_template > ${BASE_DIR}/bev-image/Dockerfile_newuser

	rm -f ${BASE_DIR}/mbatch-image/Dockerfile_newuser
	sed -e "s|<IMAGEURL>|${DOCKERFILE_URL_MBT}|g" \
	    -e "s|<USERID>|${USER_ID}|g" \
	    -e "s|<LOCALUID>|${LOCAL_UID}|g" \
	    ${BASE_DIR}/mbatch-image/Dockerfile_newuser_template > ${BASE_DIR}/mbatch-image/Dockerfile_newuser
else
	echo "do regular Dockerfile"
	rm -f ${BASE_DIR}/mba-image/Dockerfile
	sed -e "s|<RELEASE_VERSION>|${RELEASE}|g" \
	    -e "s|<USERID>|${USER_ID}|g" \
	    -e "s|<GROUPID>|${GROUP_ID}|g" \
	    -e "s|<LOG_DIR>|${JOB_OUTPUT_DIR}|g" \
	    -e "s|<START_SCRIPT>|${START_SCRIPT}|g" \
	    -e "s|<STOP_SCRIPT>|${STOP_SCRIPT}|g" \
	    -e "s|<UPCHECK_SCRIPT>|${UPCHECK_SCRIPT}|g" \
	    ${BASE_DIR}/mba-image/Dockerfile_template > ${BASE_DIR}/mba-image/Dockerfile

	rm -f ${BASE_DIR}/bev-image/Dockerfile
	sed -e "s|<RELEASE_VERSION>|${RELEASE}|g" \
	    -e "s|<USERID>|${USER_ID}|g" \
	    -e "s|<GROUPID>|${GROUP_ID}|g" \
	    -e "s|<LOG_DIR>|${JOB_OUTPUT_DIR}|g" \
	    -e "s|<START_SCRIPT>|${START_SCRIPT}|g" \
	    -e "s|<STOP_SCRIPT>|${STOP_SCRIPT}|g" \
	    -e "s|<UPCHECK_SCRIPT>|${UPCHECK_SCRIPT}|g" \
	    ${BASE_DIR}/bev-image/Dockerfile_template > ${BASE_DIR}/bev-image/Dockerfile

	rm -f ${BASE_DIR}/mbatch-image/Dockerfile
	sed -e "s|<RELEASE_VERSION>|${RELEASE}|g" \
	    -e "s|<USERID>|${USER_ID}|g" \
	    -e "s|<GROUPID>|${GROUP_ID}|g" \
	    -e "s|<LOG_DIR>|${JOB_OUTPUT_DIR}|g" \
	    -e "s|<START_SCRIPT>|${START_SCRIPT}|g" \
	    -e "s|<STOP_SCRIPT>|${STOP_SCRIPT}|g" \
	    -e "s|<UPCHECK_SCRIPT>|${UPCHECK_SCRIPT}|g" \
	    ${BASE_DIR}/mbatch-image/Dockerfile_template > ${BASE_DIR}/mbatch-image/Dockerfile
fi

echo "create docker-compose.yml from docker-compose_template.yml"

rm -f ${BASE_DIR}/docker-compose.yml
sed -e "s|<ENVIRON>|${ENVIRON}|g" \
    -e "s|<MBA-IMAGEURL>|${MBA_IMAGEURL}|g" \
    -e "s|<MBA-PORT>|${MBA_PORT}|g" \
    -e "s|<MBATCH-IMAGEURL>|${MBATCH_IMAGEURL}|g" \
    -e "s|<BEV-IMAGEURL>|${BEV_IMAGEURL}|g" \
    -e "s|<BEV-PORT>|${BEV_PORT}|g" \
    -e "s|<PROP-DIR>|${PROP_DIR}|g" \
    -e "s|<JOB-OUTPUT-DIR>|${JOB_OUTPUT_DIR}|g" \
    -e "s|<WEBSITE-DIR>|${WEBSITE_DIR}|g" \
    -e "s|<UTIL-DIR>|${UTIL_DIR}|g" \
    -e "s|<IMAGEURL>|${IMAGE_URL}|g" \
    -e "s|<CONFIGPATH>|${OUTSIDE_CONFIGPATH}|g" \
    -e "s|<ZIPTMPPATH>|${ZIPTMPPATH}|g" \
    -e "s|<LOGPATH>|${LOGPATH}|g" \
    -e "s|#- <SHAIDY-DIR>|- ${SHAIDY_DIR}|g" \
    -e "s|<DC_DF_DVLP>|${DF_EXTENSION}|g" \
    ${BASE_DIR}/docker-compose_template.yml > ${BASE_DIR}/docker-compose.yml

# then build with docker compose -f docker-compose.yml build --force-rm --no-cache

echo "FINISH 02_dockerSed"

