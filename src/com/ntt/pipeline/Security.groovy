package com.ntt.pipeline

class Security implements Serializable {
  def steps
  Security(steps) { this.steps = steps }

  void runAll(String imageWithTag) {
    steps.echo "Security stage placeholder - enable tools later"
    steps.echo "Planned: gitleaks, semgrep, trivy fs, trivy image, checkov, kubeconform, syft sbom"
    steps.echo "Image: ${imageWithTag}"
  }
}
