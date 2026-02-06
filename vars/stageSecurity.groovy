import com.ntt.pipeline.Security

def call(Map cfg = [:]) {
  if (!cfg.runSecurity) return

  stage("Security") {
    def sec = new Security(this)
    sec.runAll("${cfg.imageName}:${cfg.imageTag}")
  }
}
