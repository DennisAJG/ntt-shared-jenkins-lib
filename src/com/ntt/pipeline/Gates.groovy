package com.ntt.pipeline

class Gates implements Serializable {
  def steps
  Gates(steps) { this.steps = steps }

  void requireDocker() {
    steps.sh "docker version"
    steps.sh "docker compose version || docker-compose version"
  }

  void requirePoetry() {
    steps.sh "poetry --version"
  }

  void requirePython311() {
    steps.sh "python --version || true"
    steps.sh "python3 --version || true"
  }
}
