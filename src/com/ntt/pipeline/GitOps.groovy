package com.ntt.pipeline

class GitOps implements Serializable {
  def steps
  GitOps(steps) { this.steps = steps }

  void bumpImageTag(String repoUrl, String branch, String valuesPath, String newTag) {
    steps.echo "TODO: GitOps bump image tag in ${repoUrl} (${branch}) at ${valuesPath} -> ${newTag}"
    // Aqui entra: git clone, yq/sed pra atualizar values.yaml (Helm) ou kustomization.yaml (Kustomize),
    // commit + push, e ArgoCD sincroniza.
  }
}
