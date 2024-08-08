echo "INTEGRATION TESTS"

mvn --batch-mode --no-transfer-progress package -DspecificModule=prepare
mvn --batch-mode --no-transfer-progress dependency:tree clean verify --fail-at-end -Dmaven.test.redirectTestOutputToFile=true
