#!/bin/sh
# Exit on failure
set -e

# This assumes that the 2 following variables are defined:
# - SONAR_HOST_URL => should point to the public URL of the SQ server (e.g. for Nemo: https://nemo.sonarqube.org)
# - SONAR_TOKEN    => token of a user who has the "Execute Analysis" permission on the SQ server

installSonarQubeScanner() {
    export SONAR_SCANNER_HOME=$HOME/.sonar/sonar-scanner-2.6
    rm -rf $SONAR_SCANNER_HOME
    mkdir -p $SONAR_SCANNER_HOME
    curl -sSLo $HOME/.sonar/sonar-scanner.zip http://repo1.maven.org/maven2/org/sonarsource/scanner/cli/sonar-scanner-cli/2.6/sonar-scanner-cli-2.6.zip
    unzip $HOME/.sonar/sonar-scanner.zip -d $HOME/.sonar/
    rm $HOME/.sonar/sonar-scanner.zip
    export PATH=$SONAR_SCANNER_HOME/bin:$PATH
    export SONAR_SCANNER_OPTS="-server -Xmx1G -Xms128m"
}

# Install the SonarQube Scanner
# TODO: Would be nice to have it pre-installed by Travis somehow
installSonarQubeScanner

# And run the analysis
# It assumes that there's a sonar-project.properties file at the root of the repo
if [ "$TRAVIS_BRANCH" = "2.4" ] && [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
    # => This will run a full analysis of the project and push results to the SonarQube server.
    #
    # Analysis is done only on branch "2.4" (which seems to be the main developement branch for the moment) 
    # so that build of branches don't push analyses to the same project and therefore "pollute" the results
    echo "Starting analysis by SonarQube..."
    sonar-scanner \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_TOKEN

elif [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN-}" ]; then
    # => This will analyse the PR and display found issues as comments in the PR, but it won't push results to the SonarQube server
    #
    # For security reasons environment variables are not available on the pull requests
    # coming from outside repositories
    # http://docs.travis-ci.com/user/pull-requests/#Security-Restrictions-when-testing-Pull-Requests
    # That's why the analysis does not need to be executed if the variable GITHUB_TOKEN is not defined.
    echo "Starting Pull Request analysis by SonarQube..."
    sonar-scanner \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_TOKEN \
        -Dsonar.analysis.mode=preview \
        -Dsonar.github.oauth=$GITHUB_TOKEN \
        -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
        -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST
fi
# When neither on master branch nor on a non-external pull request => nothing to do