FROM python:3.11-slim

# ferramentas básicas (precisa ser root aqui — ok pq é build da imagem)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl git \
  && rm -rf /var/lib/apt/lists/*

# instala poetry (fixando versão)
ENV POETRY_VERSION=2.3.2
RUN curl -sSL https://install.python-poetry.org | python3 - \
  && /root/.local/bin/poetry --version

ENV PATH="/root/.local/bin:${PATH}"
ENV POETRY_CACHE_DIR=/tmp/pypoetry_cache
ENV RUFF_CACHE_DIR=/tmp/ruff_cache

WORKDIR /work
