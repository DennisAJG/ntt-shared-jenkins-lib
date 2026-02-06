FROM jenkins/jenkins:lts-jdk17

USER root

# Dependências básicas + git + curl + ca-certificates
RUN apt-get update && apt-get install -y --no-install-recommends \
    git curl ca-certificates gnupg lsb-release jq \
    python3 python3-venv python3-pip \
  && rm -rf /var/lib/apt/lists/*

# Docker CLI (client) + compose plugin
RUN install -m 0755 -d /etc/apt/keyrings \
 && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.gpg \
 && chmod a+r /etc/apt/keyrings/docker.gpg \
 && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" > /etc/apt/sources.list.d/docker.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends docker-ce-cli docker-compose-plugin \
 && rm -rf /var/lib/apt/lists/*

# pipx (instala apps python isoladas em venv)
RUN apt-get update && apt-get install -y --no-install-recommends pipx \
  && rm -rf /var/lib/apt/lists/*

# Poetry via pipx (isolado, compatível com PEP 668)
ENV PIPX_BIN_DIR=/usr/local/bin
ENV PIPX_HOME=/opt/pipx
RUN pipx install poetry==1.8.5


# Plugins via arquivo
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

# Permissão padrão
USER jenkins
