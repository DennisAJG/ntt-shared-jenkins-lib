def call(Map cfg = [:]) {
  // defaults
  String appName = cfg.get('appName', 'bank-analytics-api')
  String apiDir  = cfg.get('apiDir', 'api')
  String composeFile = cfg.get('dockerComposeFile', 'infra/docker-compose.yaml')
  Integer coverageMin = (cfg.get('coverageMin', 70) as Integer)
  Boolean runIntegration = (cfg.get('runIntegration', false) as Boolean)

  pipeline {
    agent any
    options {
      ansiColor('xterm')
      timestamps()
      disableConcurrentBuilds()
    }

    environment {
      PYTHONUNBUFFERED = "1"
      // Ajuste se quiser apontar integração para container/compose depois
      INTEGRATION_BASE_URL = cfg.get('integrationBaseUrl', 'http://localhost:8001')
    }

    stages {
      stage('Checkout') {
        steps {
          checkout scm
        }
      }

      stage('Lint / Quality') {
        steps {
          dir(apiDir) {
            sh '''
              set -e
              docker run --rm \
                -v "\$WORKSPACE/${apiDir}":/work -w /work \
                python:3.11-slim bash -lc "
                  python -V
                  apt-get update && apt-get install -y --no-install-recommends curl git && rm -rf /var/lib/apt/lists/*
                  curl -sSL https://install.python-poetry.org | python3 -
                  export PATH=\\"/root/.local/bin:$PATH\\"
                  poetry --version
                  poetry install --no-interaction --no-ansi
                  poetry run ruff format --check .
                  poetry run ruff check .
                "
            '''
          }
        }
      }


      stage('Test') {
        steps {
          dir(apiDir) {
            sh """
              set -e
              docker run --rm \
                -v "\$WORKSPACE/${apiDir}":/work -w /work \
                python:3.11-slim bash -lc "
                  python -V
                  apt-get update && apt-get install -y --no-install-recommends curl git && rm -rf /var/lib/apt/lists/*
                  curl -sSL https://install.python-poetry.org | python3 -
                  export PATH=\\\"/root/.local/bin:\$PATH\\\"
                  poetry install --no-interaction --no-ansi
                  poetry run pytest -m 'not integration' \
                    --cov=bank_api \
                    --cov-report=term-missing \
                    --cov-report=xml:coverage.xml \
                    --cov-fail-under=${coverageMin} \
                    --junitxml=junit.xml
                "
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
            set -e
            docker compose -f ${composeFile} build
          """
        }
      }

      stage('Integration (optional)') {
        when { expression { return runIntegration } }
        steps {
          sh """
            set -e
            docker compose -f ${composeFile} up -d
          """
          dir(apiDir) {
            sh '''
              set -e
              INTEGRATION_BASE_URL=${INTEGRATION_BASE_URL} poetry run pytest -m integration --junitxml=junit-integration.xml
            '''
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
