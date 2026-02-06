package com.ntt.pipeline

class Registry implements Serializable {
  def steps
  Registry(steps) { this.steps = steps }

  void buildImage(String image, String dockerfile, String tag) {
    steps.sh """
      set -euo pipefail
      docker build -f ${dockerfile} -t ${image}:${tag} .
      docker image ls ${image}:${tag}
    """
  }

  // push fica “stub” até você decidir ECR/Artifact Registry
  void pushImage(String image, String tag) {
    steps.echo "TODO: push ${image}:${tag} to registry (ECR/Artifact Registry)."
  }
}
