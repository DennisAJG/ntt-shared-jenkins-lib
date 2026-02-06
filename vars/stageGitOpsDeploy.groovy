import com.ntt.pipeline.GitOps

def call(Map cfg = [:]) {
  if (!cfg.runGitOps) return
  stage("Deploy (GitOps)") {
    def go = new GitOps(this)
    go.bumpImageTag(cfg.gitopsRepoUrl, cfg.gitopsBranch, cfg.gitopsValuesPath, cfg.imageTag)
  }
}
