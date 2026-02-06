def call(Map cfg = [:]) {
  // defaults
  String appName         = cfg.get('appName', 'bank-analytics-api')
  String apiDir          = cfg.get('apiDir', 'api')
  String composeFile     = cfg.get('dockerComposeFile', 'infra/docker-compose.yaml')
  Integer coverageMin    = (cfg.get('coverageMin', 70) as Integer)
  Boolean runIntegration = (cfg.get('runIntegration', false) as Boolean)

  // opcional
  String integrationBaseUrl = cfg.get('integrationBaseUrl', 'http://localhost:8001')

  pipeline {
    agent any

    options {
      ansiColor('xterm')
      timestamps()
      disableConcurrentBuilds()
    }

    environment {
      PYTHONUNBUFFERED = "1"
      INTEGRATION_BASE_URL = "${integrationBaseUrl}"
    }

    stages {
      stage('Checkout') {
        steps {
          checkout scm
        }
      }

      stage('Lint / Quality') {
        steps {
          script {
            sh """
              set -euo pipefail
              echo "WORKSPACE=$WORKSPACE"
              ls -la "$WORKSPACE/${apiDir}"
              test -f "$WORKSPACE/${apiDir}/pyproject.toml"

              docker run --rm \
                --user \$(id -u):\$(id -g) \
                -e RUFF_CACHE_DIR=/tmp/ruff_cache \
                -e POETRY_CACHE_DIR=/tmp/pypoetry_cache \
                -v "$WORKSPACE/${apiDir}":/work -w /work \
                python:3.11-slim bash -lc '
                  set -euo pipefail
                  python -V
                  apt-get update && apt-get install -y --no-install-recommends curl git && rm -rf /var/lib/apt/lists/*
                  curl -sSL https://install.python-poetry.org | python3 -
                  export PATH="/root/.local/bin:\$PATH"
                  poetry --version
                  poetry install --no-interaction --no-ansi
                  poetry run ruff format --check .
                  poetry run ruff check .
                '
            """
          }
        }
      }

      stage('Test') {
        steps {
          script {
            sh """
              set -euo pipefail
              test -f "$WORKSPACE/${apiDir}/pyproject.toml"

              docker run --rm \
                --user \$(id -u):\$(id -g) \
                -e RUFF_CACHE_DIR=/tmp/ruff_cache \
                -e POETRY_CACHE_DIR=/tmp/pypoetry_cache \
                -v "$WORKSPACE/${apiDir}":/work -w /work \
                python:3.11-slim bash -lc '
                  set -euo pipefail
                  python -V
                  apt-get update && apt-get install -y --no-install-recommends curl git && rm -rf /var/lib/apt/lists/*
                  curl -sSL https://install.python-poetry.org | python3 -
                  export PATH="/root/.local/bin:\$PATH"
                  poetry install --no-interaction --no-ansi
                  poetry run pytest -m "not integration" \
                    --cov=bank_api \
                    --cov-report=term-missing \
                    --cov-report=xml:coverage.xml \
                    --cov-fail-under=${coverageMin} \
                    --junitxml=junit.xml
                '
            """
          }
        }
        post {
          always {
            dir(apiDir) {
              junit allowEmptyResults: true, testResults: 'junit.xml'
            }
          }
        }
      }

      stage('Build (Docker)') {
        steps {
          sh """
            set -euo pipefail

            # Se sobrou .ruff_cache de execuções antigas (root-owned), NÃO tenta apagar como jenkins.
            # O correto é não gerar mais. Mas se quiser limpar, faça 1x via docker exec -u root.

            docker compose -f ${composeFile} build
          """
        }
      }

      stage('Integration (optional)') {
        when { expression { return runIntegration } }
        steps {
          sh """
            set -euo pipefail
            docker compose -f ${composeFile} up -d
          """

          dir(apiDir) {
            script {
              sh """
                set -euo pipefail

                docker run --rm \
                  --user \$(id -u):\$(id -g) \
                  -e INTEGRATION_BASE_URL="${integrationBaseUrl}" \
                  -e RUFF_CACHE_DIR=/tmp/ruff_cache \
                  -e POETRY_CACHE_DIR=/tmp/pypoetry_cache \
                  -v "$WORKSPACE/${apiDir}":/work -w /work \
                  python:3.11-slim bash -lc '
                    set -euo pipefail
                    python -V
                    apt-get update && apt-get install -y --no-install-recommends curl git && rm -rf /var/lib/apt/lists/*
                    curl -sSL https://install.python-poetry.org | python3 -
                    export PATH="/root/.local/bin:\$PATH"
                    poetry install --no-interaction --no-ansi
                    poetry run pytest -m integration --junitxml=junit-integration.xml
                  '
              """
            }
          }
        }
        post {
          always {
            dir(apiDir) {
              junit allowEmptyResults: true, testResults: 'junit-integration.xml'
            }
            sh "docker compose -f ${composeFile} down -v || true"
          }
        }
      }
    }
  }
}
