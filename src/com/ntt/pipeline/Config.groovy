package com.ntt.pipeline

class Config implements Serializable {
  String appName
  String apiDir
  String dockerComposeFile
  String dockerfile
  String imageName
  String apiPort
  Integer coverageMin = 70

  // toggles
  boolean runIntegration = true
  boolean runSecurity = true
  boolean runGitOps = false

  // registry/gitops (opcional por enquanto)
  String registryUrl
  String gitopsRepoUrl
  String gitopsBranch = "main"
  String gitopsValuesPath

  static Config from(Map cfg) {
    def c = new Config()
    c.appName = cfg.appName ?: "bank-analytics-api"
    c.apiDir = cfg.apiDir ?: "api"
    c.dockerComposeFile = cfg.dockerComposeFile ?: "infra/docker-compose.yaml"
    c.dockerfile = cfg.dockerfile ?: "docker/api/Dockerfile"
    c.imageName = cfg.imageName ?: "bank-analytics-api"
    c.apiPort = (cfg.apiPort ?: "8001").toString()
    c.coverageMin = (cfg.coverageMin ?: 70) as Integer

    c.runIntegration = (cfg.runIntegration != null) ? cfg.runIntegration as boolean : true
    c.runSecurity = (cfg.runSecurity != null) ? cfg.runSecurity as boolean : true
    c.runGitOps = (cfg.runGitOps != null) ? cfg.runGitOps as boolean : false

    c.registryUrl = cfg.registryUrl
    c.gitopsRepoUrl = cfg.gitopsRepoUrl
    c.gitopsBranch = cfg.gitopsBranch ?: "main"
    c.gitopsValuesPath = cfg.gitopsValuesPath

    c.validate()
    return c
  }

  void validate() {
    if (!appName) throw new IllegalArgumentException("appName is required")
    if (!apiDir) throw new IllegalArgumentException("apiDir is required")
    if (!dockerComposeFile) throw new IllegalArgumentException("dockerComposeFile is required")
    if (!dockerfile) throw new IllegalArgumentException("dockerfile is required")
    if (!imageName) throw new IllegalArgumentException("imageName is required")
    if (!apiPort) throw new IllegalArgumentException("apiPort is required")
  }
}
