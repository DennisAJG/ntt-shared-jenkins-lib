import com.ntt.pipeline.Config
import com.ntt.pipeline.Gates
import com.ntt.pipeline.Semver
import com.ntt.pipeline.Registry

def call(Map userCfg = [:]) {
  def cfg = Config.from(userCfg)
  def gates = new Gates(this)
  def semver = new Semver(this)
  def registry = new Registry(this)

  pipeline {
    agent any
    options {
      timestamps()
      ansiColor('xterm')
      disableConcurrentBuilds()
      buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    stages {
      stage("Toolchain") {
        steps {
          script {
            gates.requireDocker()
            gates.requirePoetry()
            gates.requirePython311()
          }
        }
      }

      stage("Checkout") {
        steps { checkout scm }
      }

      stage("Versioning") {
        steps {
          script {
            cfg.imageTag = semver.versionTagOrSha()
            echo "Image tag resolved: ${cfg.imageTag}"
          }
        }
      }

      stage("Build (Docker)") {
        steps {
          script {
            registry.buildImage(cfg.imageName, cfg.dockerfile, cfg.imageTag)
          }
        }
      }

      stage("CI") {
        steps {
          script {
            // lint + tests
            stageLintQuality(apiDir: cfg.apiDir)
            stageTest(
              apiDir: cfg.apiDir,
              coverageMin: cfg.coverageMin,
              dockerComposeFile: cfg.dockerComposeFile,
              apiPort: cfg.apiPort,
              runIntegration: cfg.runIntegration
            )
            // security (placeholder por enquanto)
            stageSecurity(
              runSecurity: cfg.runSecurity,
              imageName: cfg.imageName,
              imageTag: cfg.imageTag
            )
          }
        }
      }

      stage("CD") {
        when { branch "main" }
        steps {
          script {
            stageGitOpsDeploy(
              runGitOps: cfg.runGitOps,
              gitopsRepoUrl: cfg.gitopsRepoUrl,
              gitopsBranch: cfg.gitopsBranch,
              gitopsValuesPath: cfg.gitopsValuesPath,
              imageTag: cfg.imageTag
            )
          }
        }
      }
    }

    post {
      always {
        echo "Pipeline finished."
      }
    }
  }
}
