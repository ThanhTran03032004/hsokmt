#!/usr/bin/env python3
from __future__ import annotations

import argparse
import html
import os
import re
import shutil
import sys
import tempfile
import urllib.error
import urllib.request
import zipfile
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Iterable
from urllib.parse import urljoin, urlparse


USER_AGENT = "hsokmt-client-ref-sync/1.0"
ZOOM_LEVELS = {"x1", "x2", "x3", "x4"}
CATEGORY_ORDER = ("msg_eff", "part_char", "icon", "msg", "map")
IGNORED_SUFFIXES = (
    ".class",
    ".java",
    ".kt",
    ".scala",
    ".groovy",
    ".mf",
    ".rsa",
    ".sf",
)
CONFIG_PATTERNS = {
    "indexRes": re.compile(rb"indexRes\s*[:=]\s*(-?\d+)"),
    "indexCharPar": re.compile(rb"indexCharPar\s*[:=]\s*(-?\d+)"),
}
SCAN_SKIP_SUFFIXES = (
    ".png",
    ".jpg",
    ".jpeg",
    ".gif",
    ".webp",
    ".mp3",
    ".ogg",
    ".wav",
    ".dex",
    ".so",
    ".ttf",
    ".otf",
)


@dataclass(frozen=True)
class MatchEntry:
    category: str
    relative_path: str
    zip_name: str

    @property
    def destination_key(self) -> str:
        return f"{self.category}/{self.relative_path}"


def repo_root() -> Path:
    return Path(__file__).resolve().parent.parent


def default_template_path() -> Path:
    return repo_root() / "src" / "main" / "resources" / "templates" / "index.html"


def default_data_dir() -> Path:
    return repo_root() / "data"


def default_config_path() -> Path:
    return repo_root() / "hso.conf"


def default_backup_dir() -> Path:
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    return Path(tempfile.gettempdir()) / "hsokmt-client-ref-backups" / stamp


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Pull refs from a client jar and update server data/"
            " (icon, part_char, msg, msg_eff, map)."
        )
    )
    source_group = parser.add_mutually_exclusive_group()
    source_group.add_argument("--jar", type=Path, help="Local path to the client jar.")
    source_group.add_argument("--url", help="Direct jar URL or MediaFire/file page URL.")
    parser.add_argument(
        "--template",
        type=Path,
        default=default_template_path(),
        help="Template HTML used to auto-detect the jar link when --jar/--url is not set.",
    )
    parser.add_argument(
        "--data-dir",
        type=Path,
        default=default_data_dir(),
        help="Target server data directory.",
    )
    parser.add_argument(
        "--config",
        type=Path,
        default=default_config_path(),
        help="Path to hso.conf for indexRes/indexCharPar updates.",
    )
    parser.add_argument(
        "--backup-dir",
        type=Path,
        default=None,
        help="Directory used to back up overwritten files. Defaults to a temp folder.",
    )
    parser.add_argument(
        "--no-backup",
        action="store_true",
        help="Do not back up overwritten files before writing.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Scan the jar and print what would change without writing files.",
    )
    parser.add_argument(
        "--no-config-update",
        action="store_true",
        help="Skip scanning the jar for indexRes/indexCharPar and do not touch hso.conf.",
    )
    parser.add_argument(
        "--timeout",
        type=int,
        default=30,
        help="Network timeout in seconds for URL downloads.",
    )
    return parser.parse_args()


def load_template_jar_url(template_path: Path) -> str:
    if not template_path.is_file():
        raise FileNotFoundError(f"Template file not found: {template_path}")
    text = template_path.read_text(encoding="utf-8", errors="ignore")
    matches = re.findall(r"href\s*=\s*['\"]([^'\"]+\.jar[^'\"]*)['\"]", text, flags=re.IGNORECASE)
    if not matches:
        raise ValueError(f"Could not find a .jar link in template: {template_path}")
    return html.unescape(matches[0])


def resolve_download_url(url: str, timeout: int) -> str:
    parsed = urlparse(url)
    if parsed.scheme not in {"http", "https"}:
        raise ValueError(f"Unsupported URL scheme: {url}")
    if "mediafire.com" in parsed.netloc.lower():
        return resolve_mediafire_page(url, timeout)
    if parsed.path.lower().endswith(".jar"):
        return url
    return resolve_generic_download_page(url, timeout)


def resolve_mediafire_page(url: str, timeout: int) -> str:
    html_text = fetch_text(url, timeout)
    patterns = (
        r'id=["\']downloadButton["\'][^>]*href=["\']([^"\']+)["\']',
        r'href=["\']([^"\']+download[^"\']+\.jar[^"\']*)["\']',
        r'window\.location\.href\s*=\s*["\']([^"\']+)["\']',
    )
    for pattern in patterns:
        match = re.search(pattern, html_text, flags=re.IGNORECASE)
        if match:
            return absolutize_url(url, html.unescape(match.group(1)))
    raise ValueError(f"Could not resolve MediaFire download URL from: {url}")


def resolve_generic_download_page(url: str, timeout: int) -> str:
    html_text = fetch_text(url, timeout)
    match = re.search(r'href=["\']([^"\']+\.jar[^"\']*)["\']', html_text, flags=re.IGNORECASE)
    if not match:
        raise ValueError(f"Could not find a .jar download link in page: {url}")
    return absolutize_url(url, html.unescape(match.group(1)))


def absolutize_url(base_url: str, candidate: str) -> str:
    if candidate.startswith("//"):
        return "https:" + candidate
    return urljoin(base_url, candidate)


def fetch_text(url: str, timeout: int) -> str:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=timeout) as response:
        data = response.read()
    return data.decode("utf-8", errors="ignore")


def download_to_temp(url: str, timeout: int) -> Path:
    fd, temp_name = tempfile.mkstemp(prefix="hsokmt_client_", suffix=".jar")
    os.close(fd)
    target = Path(temp_name)
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=timeout) as response, target.open("wb") as output:
        shutil.copyfileobj(response, output)
    return target


def normalize_jar_path(path: str) -> tuple[list[str], list[str]]:
    clean = path.replace("\\", "/").strip("/")
    parts = [part for part in clean.split("/") if part]
    lower_parts = [part.lower() for part in parts]
    return parts, lower_parts


def classify_entry(zip_name: str) -> MatchEntry | None:
    parts, lower_parts = normalize_jar_path(zip_name)
    if not parts:
        return None
    if lower_parts[0] == "meta-inf":
        return None
    if parts[-1].lower().endswith(IGNORED_SUFFIXES):
        return None

    for category in CATEGORY_ORDER:
        if category not in lower_parts:
            continue
        index = lower_parts.index(category)
        rel_parts = parts[index + 1 :]
        rel_lower_parts = lower_parts[index + 1 :]
        if not rel_parts or any(part in {".", ".."} for part in rel_parts):
            return None

        if category == "icon":
            if rel_lower_parts[0] not in ZOOM_LEVELS or not rel_parts[-1].lower().endswith(".png"):
                return None
        elif category == "part_char":
            if rel_lower_parts[0] not in ZOOM_LEVELS:
                return None

        return MatchEntry(category=category, relative_path="/".join(rel_parts), zip_name=zip_name)
    return None


def collect_matches(zip_file: zipfile.ZipFile) -> tuple[dict[str, MatchEntry], dict[str, list[str]]]:
    matches: dict[str, MatchEntry] = {}
    duplicates: dict[str, list[str]] = defaultdict(list)
    for info in zip_file.infolist():
        if info.is_dir():
            continue
        match = classify_entry(info.filename)
        if match is None:
            continue
        duplicates[match.destination_key].append(match.zip_name)
        matches[match.destination_key] = match
    duplicate_targets = {key: value for key, value in duplicates.items() if len(value) > 1}
    return matches, duplicate_targets


def ensure_zip(jar_path: Path) -> None:
    if not jar_path.is_file():
        raise FileNotFoundError(f"Jar file not found: {jar_path}")
    if not zipfile.is_zipfile(jar_path):
        raise ValueError(f"File is not a valid jar/zip: {jar_path}")


def backup_file(source: Path, backup_root: Path, relative_path: Path) -> Path:
    target = backup_root / relative_path
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)
    return target


def write_file_if_changed(
    target: Path,
    content: bytes,
    *,
    dry_run: bool,
    backup_root: Path | None,
    backup_relative_path: Path | None,
) -> str:
    if target.exists():
        existing = target.read_bytes()
        if existing == content:
            return "unchanged"
        if not dry_run and backup_root is not None:
            if backup_relative_path is None:
                raise ValueError("backup_relative_path is required when backup_root is enabled")
            backup_file(target, backup_root, backup_relative_path)
        if not dry_run:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(content)
        return "updated"

    if not dry_run:
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
    return "new"


def extract_matches(
    zip_file: zipfile.ZipFile,
    matches: Iterable[MatchEntry],
    data_dir: Path,
    *,
    dry_run: bool,
    backup_root: Path | None,
) -> tuple[Counter, Counter]:
    status_counts: Counter = Counter()
    category_counts: Counter = Counter()
    for match in matches:
        target = data_dir / match.category / Path(match.relative_path)
        content = zip_file.read(match.zip_name)
        status = write_file_if_changed(
            target,
            content,
            dry_run=dry_run,
            backup_root=backup_root,
            backup_relative_path=Path(match.category) / Path(match.relative_path),
        )
        status_counts[status] += 1
        category_counts[match.category] += 1
    return status_counts, category_counts


def scan_config_values(zip_file: zipfile.ZipFile) -> tuple[dict[str, int], dict[str, str]]:
    found_values: dict[str, int] = {}
    found_sources: dict[str, str] = {}
    for info in zip_file.infolist():
        if info.is_dir() or info.file_size <= 0 or info.file_size > 2_000_000:
            continue
        if info.filename.lower().endswith(SCAN_SKIP_SUFFIXES):
            continue
        data = zip_file.read(info.filename)
        for key, pattern in CONFIG_PATTERNS.items():
            if key in found_values:
                continue
            match = pattern.search(data)
            if match:
                found_values[key] = int(match.group(1))
                found_sources[key] = info.filename
        if len(found_values) == len(CONFIG_PATTERNS):
            break
    return found_values, found_sources


def update_config_file(
    config_path: Path,
    values: dict[str, int],
    *,
    dry_run: bool,
    backup_root: Path | None,
) -> dict[str, tuple[int | None, int]]:
    if not values:
        return {}
    if not config_path.is_file():
        raise FileNotFoundError(f"Config file not found: {config_path}")

    text = config_path.read_text(encoding="utf-8", errors="ignore")
    updated_text = text
    changes: dict[str, tuple[int | None, int]] = {}

    for key, new_value in values.items():
        pattern = re.compile(rf"^(\s*{re.escape(key)}\s*:\s*)(-?\d+)(\s*)$", flags=re.MULTILINE)
        match = pattern.search(updated_text)
        if match:
            old_value = int(match.group(2))
            if old_value == new_value:
                continue
            updated_text = pattern.sub(rf"\g<1>{new_value}\g<3>", updated_text, count=1)
            changes[key] = (old_value, new_value)
        else:
            if updated_text and not updated_text.endswith("\n"):
                updated_text += "\n"
            updated_text += f"{key}: {new_value}\n"
            changes[key] = (None, new_value)

    if changes and not dry_run:
        if backup_root is not None:
            backup_file(config_path, backup_root, Path("config") / config_path.name)
        config_path.write_text(updated_text, encoding="utf-8")

    return changes


def describe_source(args: argparse.Namespace, resolved_url: str | None, jar_path: Path) -> str:
    if args.jar:
        return f"local jar: {jar_path}"
    if args.url:
        return f"url: {args.url} -> {resolved_url or jar_path}"
    return f"template jar: {args.template} -> {resolved_url or jar_path}"


def print_summary(
    *,
    source_description: str,
    jar_path: Path,
    matches: dict[str, MatchEntry],
    duplicate_targets: dict[str, list[str]],
    category_counts: Counter,
    status_counts: Counter,
    missing_categories: list[str],
    backup_root: Path | None,
    config_scan_sources: dict[str, str],
    config_changes: dict[str, tuple[int | None, int]],
    dry_run: bool,
) -> None:
    print(f"Source: {source_description}")
    print(f"Jar: {jar_path}")
    print(f"Matched entries: {len(matches)}")
    if category_counts:
        print(
            "By category: "
            + ", ".join(f"{category}={category_counts[category]}" for category in CATEGORY_ORDER if category_counts[category])
        )
    if missing_categories:
        print("Missing categories: " + ", ".join(missing_categories))
    if duplicate_targets:
        print(f"Duplicate destinations inside jar: {len(duplicate_targets)} (last match was used)")
    if status_counts:
        print(
            "Write result: "
            + ", ".join(f"{status}={status_counts[status]}" for status in ("new", "updated", "unchanged") if status_counts[status])
        )
    if dry_run:
        print("Dry-run only, no files were written.")
    elif backup_root is not None and (status_counts["updated"] or config_changes):
        print(f"Backup: {backup_root}")

    if config_scan_sources:
        config_found = ", ".join(f"{key} from {config_scan_sources[key]}" for key in sorted(config_scan_sources))
        print(f"Config scan: {config_found}")
    else:
        print("Config scan: no indexRes/indexCharPar found in jar")

    if config_changes:
        details = []
        for key, (old_value, new_value) in sorted(config_changes.items()):
            if old_value is None:
                details.append(f"{key}=added:{new_value}")
            else:
                details.append(f"{key}={old_value}->{new_value}")
        print("Config update: " + ", ".join(details))


def main() -> int:
    args = parse_args()
    jar_path: Path | None = None
    downloaded = False
    resolved_url: str | None = None
    backup_root = None if args.no_backup else (args.backup_dir or default_backup_dir())

    try:
        if args.jar:
            jar_path = args.jar.expanduser().resolve()
        else:
            source_url = args.url or load_template_jar_url(args.template)
            try:
                resolved_url = resolve_download_url(source_url, args.timeout)
                jar_path = download_to_temp(resolved_url, args.timeout)
                downloaded = True
            except (ValueError, urllib.error.URLError) as exc:
                source_label = f"--url {source_url}" if args.url else f"template jar link in {args.template}"
                raise ValueError(
                    f"Could not download client jar from {source_label}: {exc}"
                ) from exc

        ensure_zip(jar_path)
        data_dir = args.data_dir.expanduser().resolve()
        config_path = args.config.expanduser().resolve()

        with zipfile.ZipFile(jar_path) as zip_file:
            matches, duplicate_targets = collect_matches(zip_file)
            if not matches:
                raise ValueError("No supported refs were found in the client jar.")

            ordered_matches = [matches[key] for key in sorted(matches)]
            status_counts, category_counts = extract_matches(
                zip_file,
                ordered_matches,
                data_dir,
                dry_run=args.dry_run,
                backup_root=backup_root,
            )

            config_scan_sources: dict[str, str] = {}
            config_changes: dict[str, tuple[int | None, int]] = {}
            if not args.no_config_update:
                config_scan_values, config_scan_sources = scan_config_values(zip_file)
                config_changes = update_config_file(
                    config_path,
                    config_scan_values,
                    dry_run=args.dry_run,
                    backup_root=backup_root,
                )

        missing_categories = [category for category in CATEGORY_ORDER if category_counts[category] == 0]
        source_description = describe_source(args, resolved_url, jar_path)
        print_summary(
            source_description=source_description,
            jar_path=jar_path,
            matches=matches,
            duplicate_targets=duplicate_targets,
            category_counts=category_counts,
            status_counts=status_counts,
            missing_categories=missing_categories,
            backup_root=backup_root,
            config_scan_sources=config_scan_sources,
            config_changes=config_changes,
            dry_run=args.dry_run,
        )
        return 0
    except (FileNotFoundError, ValueError, urllib.error.URLError, zipfile.BadZipFile) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1
    finally:
        if downloaded and jar_path is not None and jar_path.exists():
            jar_path.unlink()


if __name__ == "__main__":
    raise SystemExit(main())
