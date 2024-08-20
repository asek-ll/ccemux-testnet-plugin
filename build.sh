export GITHUB_TOKEN=$(secstore get gh-artifactory-read) 
./gradlew jar
