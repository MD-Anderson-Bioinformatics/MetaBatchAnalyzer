# MetaBatch Analyzer

 * Rehan Akbani (owner)
 * John Weinstein
 * Bradley Broom
 * Tod Casasent (developer)

MetaBatch Analyzer is an HTTP GUI interface to a MetaBatch Analyzer (MBA) Docker Compose Stack. MBA includes images for a GUI component, an MBatch processing component, a Viewer and analysis component, and a component for downloading and converting GDC data for use with MBA.

### MetaBatch Analyzer Docker Quick Start

Download the docker-compose.yml file at the root of this repository. This file is setup for use on Linux.

Make the following directories.

 - /MBA/OUTPUT
 - /MBA/PROPS
 - /MBA/UTIL
 - /MBA/WEBSITE
 - /MBA/MW_CONFIG
 - /MBA/MW_ZIP

 1. Copy the contents of inst/ext/MW_CONFIG into /MBA/MW_CONFIG.
 2. Copy the contents of inst/ext/OUTPUT into /MBA/OUTPUT.
 3. Copy the contents of inst/ext/PROPS into /MBA/PROPS.
 4. Copy the contents of inst/ext/UTIL into /MBA/UTIL.
 5. Copy the contents of inst/ext/WEBSITE into /MBA/WEBSITE.

Permissions or ownership of the directories may need to be changed or matched to the Docker image user 2004.

In the directory with the docker-compose.yml file run:

	docker compose -p mbahub -f docker-compose.yml up --no-build -d

You can stop it with:

	docker compose -p mbahub -f docker-compose.yml down

To connect to the MBatch Omic Browser with:

	localhost:8080/MBA/MBA


**For educational and research purposes only.**

**Funding** 
This work was supported in part by U.S. National Cancer Institute (NCI) grant: Weinstein, Broom, Akbani. Computational Tools for Analysis and Visualization of Quality Control Issues in Metabolomic Data, U01CA235510

