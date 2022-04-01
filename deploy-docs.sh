#!/bin/bash

# This script builds the documentation and pushes it to github

GH_REPO_REF="github.com/$TRAVIS_REPO_SLUG"

set -e

JAVADOC_DIR=$(pwd)/target/site/apidocs

# Build the javadocs
mvn clean javadoc:aggregate

# Activate virtualenv
python3 -m venv venv3
. venv3/bin/activate

cd docs
rm -rf ./_build

# Install dependencies
pip install -r requirements.txt

# Build docs
make html

cd _build

# Clone docs branch
git clone -b gh-pages https://git@$GH_REPO_REF docs-upload
cd docs-upload

# Configure git
git config push.default simple
git config user.name "Travis CI"
git config user.email "travis@travis-ci.org"

# Remove existing docs
rm -rf *

# Copy docs from build to repo
cp -r ../html/* ./

# Copy javadocs
rm -rf ./javadocs
mv "$JAVADOC_DIR" ./javadocs

echo "" > .nojekyll

if [[ -f "index.html" ]]; then
    # Commit
    git add --all
    git commit -m "Deploy code docs to GitHub Pages Travis build"

    export GH_REPO_TOKEN="trinopoty@${GH_TOKEN}"
    git push --force "https://${GH_REPO_TOKEN}@${GH_REPO_REF}" > /dev/null 2>&1
else
    echo 'ERROR: index.html not found'
    exit 1
fi
