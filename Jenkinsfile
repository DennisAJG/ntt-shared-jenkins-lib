@Library('ntt-shared-jenkins-lib@main') _
pipelineBankApp(
  appName: 'bank-analytics-api',
  apiDir: 'api',
  dockerComposeFile: 'infra/docker-compose.yaml',
  dockerfile: 'docker/api/Dockerfile',
  imageName: 'bank-analytics-api',
  apiPort: '8001',
  coverageMin: 70,
  runIntegration: true,
  runSecurity: false,
  runGitOps: false
)
