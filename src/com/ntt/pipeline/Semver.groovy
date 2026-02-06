package com.ntt.pipeline

class Semver implements Serializable {
  def steps
  Semver(steps) { this.steps = steps }

  String gitSha() {
    return steps.sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
  }

  // Se tiver tag semver (vX.Y.Z) usa, senão usa sha
  String versionTagOrSha() {
    def tag = steps.sh(script: "git describe --tags --abbrev=0 2>/dev/null || true", returnStdout: true).trim()
    if (tag ==~ /^v\\d+\\.\\d+\\.\\d+$/) return tag
    return gitSha()
  }
}
