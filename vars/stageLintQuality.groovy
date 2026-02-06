def call(Map cfg = [:]) {
  stage("Lint / Quality") {
    sh """
      set -euo pipefail
      cd ${cfg.apiDir}
      poetry run ruff check .
      poetry run ruff format --check .
    """
  }
}
