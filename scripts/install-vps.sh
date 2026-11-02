#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/hsokmt}"
SERVICE_NAME="${SERVICE_NAME:-hsokmt}"
REPO_URL="${REPO_URL:-}"
BRANCH="${BRANCH:-main}"

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Run as root: sudo bash scripts/install-vps.sh"
  exit 1
fi
if [[ -z "$REPO_URL" && ! -d "$APP_DIR/.git" ]]; then
  echo "Set REPO_URL for the first installation."
  exit 1
fi

apt-get update
apt-get install -y git openjdk-17-jdk maven mysql-server

if [[ ! -d "$APP_DIR/.git" ]]; then
  git clone --branch "$BRANCH" "$REPO_URL" "$APP_DIR"
else
  git -C "$APP_DIR" fetch origin "$BRANCH"
  git -C "$APP_DIR" checkout "$BRANCH"
  git -C "$APP_DIR" pull --ff-only origin "$BRANCH"
fi

id -u hsokmt >/dev/null 2>&1 || useradd --system --home-dir "$APP_DIR" --shell /usr/sbin/nologin hsokmt
install -d -o hsokmt -g hsokmt /etc/hsokmt "$APP_DIR/log" "$APP_DIR/log_server"
install -m 0644 "$APP_DIR/deploy/hsokmt.service.example" "/etc/systemd/system/$SERVICE_NAME.service"

if [[ ! -f /etc/hsokmt/hsokmt.env ]]; then
  cat > /etc/hsokmt/hsokmt.env <<'EOF'
JAVA_OPTS=-Xms256m\ -Xmx1024m
EOF
  chmod 0600 /etc/hsokmt/hsokmt.env
fi

chown -R hsokmt:hsokmt "$APP_DIR"
"$APP_DIR/scripts/deploy-vps.sh"
systemctl enable "$SERVICE_NAME"
echo "Installed. Check with: systemctl status $SERVICE_NAME"
