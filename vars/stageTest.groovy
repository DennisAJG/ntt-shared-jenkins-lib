def call(Map cfg = [:]) {
  stage("Test (Unit + Coverage Gate)") {
    sh """
      set -euo pipefail
      cd ${cfg.apiDir}
      poetry run pytest -m "not integration" \
        --cov=bank_api \
        --cov-report=term-missing \
        --cov-report=xml:coverage.xml \
        --cov-fail-under=${cfg.coverageMin} \
        --junitxml=junit.xml
    """
    junit "${cfg.apiDir}/junit.xml"
    archiveArtifacts artifacts: "${cfg.apiDir}/coverage.xml", fingerprint: true
  }

  if (cfg.runIntegration) {
    stage("Integration Tests") {
      sh """
        set -euo pipefail
        docker compose -f ${cfg.dockerComposeFile} up -d --build
        cd ${cfg.apiDir}
        INTEGRATION_BASE_URL=http://localhost:${cfg.apiPort} poetry run pytest -m integration --junitxml=junit-integration.xml
      """
      junit "${cfg.apiDir}/junit-integration.xml"
      sh "docker compose -f ${cfg.dockerComposeFile} down -v || true"
    }
  }
}
