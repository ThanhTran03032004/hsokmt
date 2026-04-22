#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
DEPLOY_BRANCH="${DEPLOY_BRANCH:-main}"
SERVICE_NAME="${SERVICE_NAME:-hsokmt}"
JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"

run_systemctl() {
  if command -v sudo >/dev/null 2>&1 && [ "$(id -u)" -ne 0 ]; then
    sudo systemctl "$@"
  else
    systemctl "$@"
  fi
}

cd "$APP_DIR"

if [ ! -d .git ]; then
  echo "Repo not found in $APP_DIR"
  exit 1
fi

if [ ! -x ./mvnw ]; then
  chmod +x ./mvnw
fi

export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

git fetch origin "$DEPLOY_BRANCH"
git checkout "$DEPLOY_BRANCH"
git pull --ff-only origin "$DEPLOY_BRANCH"

./mvnw -q -DskipTests package

run_systemctl restart "$SERVICE_NAME"
run_systemctl --no-pager --lines=20 status "$SERVICE_NAME"
