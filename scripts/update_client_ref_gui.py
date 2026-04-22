#!/usr/bin/env python3
"""
HSO KMT Client Ref Sync GUI Tool
================================
- Đồng bộ tài nguyên (icon, part_char, msg, msg_eff, map) từ client.jar
- Extract TẤT CẢ ảnh trong jar (bất kể đường dẫn)
- Giao diện đồ họa dark-mode hiện đại
- Hỗ trợ preview ảnh, progress bar, log real-time

Yêu cầu:
    pip install customtkinter Pillow
"""

from __future__ import annotations

import html
import io
import os
import queue
import re
import shutil
import sys
import tempfile
import threading
import urllib.error
import urllib.request
import zipfile
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Iterable, Optional, List, Tuple
from urllib.parse import urljoin, urlparse

# ─── Kiểm tra dependencies ───────────────────────────────────────────────────
try:
    import customtkinter as ctk
    from customtkinter import CTkImage
except ImportError:
    print("Thiếu thư viện! Chạy lệnh:\n  pip install customtkinter Pillow")
    sys.exit(1)

try:
    from PIL import Image, ImageTk, ImageDraw
    HAS_PIL = True
except ImportError:
    HAS_PIL = False
    print("[WARN] Pillow không có - tính năng xem ảnh bị tắt. pip install Pillow")

import tkinter as tk
from tkinter import filedialog, messagebox, ttk

# ─── Constants ────────────────────────────────────────────────────────────────
USER_AGENT      = "hsokmt-client-ref-sync/2.0-gui"
ZOOM_LEVELS     = {"x1", "x2", "x3", "x4"}
CATEGORY_ORDER  = ("msg_eff", "part_char", "icon", "msg", "map")
ALL_IMAGE_EXTS  = (".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp")
IGNORED_SUFFIXES = (".class", ".java", ".kt", ".scala", ".groovy", ".mf", ".rsa", ".sf")
SCAN_SKIP_SUFFIXES = (".png", ".jpg", ".jpeg", ".gif", ".webp", ".mp3", ".ogg", ".wav", ".dex", ".so", ".ttf", ".otf")
CONFIG_PATTERNS = {
    "indexRes":     re.compile(rb"indexRes\s*[:=]\s*(-?\d+)"),
    "indexCharPar": re.compile(rb"indexCharPar\s*[:=]\s*(-?\d+)"),
}

# ─── Palette màu ─────────────────────────────────────────────────────────────
CLR = {
    "bg":          "#0f1117",
    "panel":       "#1a1d27",
    "card":        "#20243a",
    "border":      "#2a2f4e",
    "accent":      "#7c6af5",
    "accent2":     "#5e9eff",
    "success":     "#2dd4bf",
    "warn":        "#f59e0b",
    "err":         "#f87171",
    "text":        "#e2e8f0",
    "text_dim":    "#64748b",
    "gold":        "#fbbf24",
}

# ─── Core data classes ────────────────────────────────────────────────────────
@dataclass(frozen=True)
class MatchEntry:
    category: str
    relative_path: str
    zip_name: str

    @property
    def destination_key(self) -> str:
        return f"{self.category}/{self.relative_path}"


@dataclass
class ImageEntry:
    """Đại diện cho một ảnh bất kỳ trong jar."""
    zip_name: str
    ext: str
    size_bytes: int
    thumb: Optional[object] = None   # CTkImage sau khi load


# ─── Utility functions ────────────────────────────────────────────────────────
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

def normalize_jar_path(path: str) -> tuple[list[str], list[str]]:
    clean = path.replace("\\", "/").strip("/")
    parts = [p for p in clean.split("/") if p]
    return parts, [p.lower() for p in parts]

def ensure_zip(jar_path: Path) -> None:
    if not jar_path.is_file():
        raise FileNotFoundError(f"Jar không tồn tại: {jar_path}")
    if not zipfile.is_zipfile(jar_path):
        raise ValueError(f"File không phải jar/zip hợp lệ: {jar_path}")

def backup_file(source: Path, backup_root: Path, relative_path: Path) -> Path:
    target = backup_root / relative_path
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)
    return target

def write_file_if_changed(target: Path, content: bytes, *, dry_run: bool,
                           backup_root: Optional[Path], backup_relative_path: Optional[Path]) -> str:
    if target.exists():
        if target.read_bytes() == content:
            return "unchanged"
        if not dry_run and backup_root is not None:
            if backup_relative_path is None:
                raise ValueError("backup_relative_path bắt buộc khi backup_root được bật")
            backup_file(target, backup_root, backup_relative_path)
        if not dry_run:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(content)
        return "updated"
    if not dry_run:
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
    return "new"

def classify_entry(zip_name: str) -> Optional[MatchEntry]:
    parts, lower_parts = normalize_jar_path(zip_name)
    if not parts or lower_parts[0] == "meta-inf":
        return None
    if parts[-1].lower().endswith(IGNORED_SUFFIXES):
        return None
    for category in CATEGORY_ORDER:
        if category not in lower_parts:
            continue
        index = lower_parts.index(category)
        rel_parts = parts[index + 1:]
        rel_lower_parts = lower_parts[index + 1:]
        if not rel_parts or any(p in {".", ".."} for p in rel_parts):
            return None
        if category == "icon":
            if rel_lower_parts[0] not in ZOOM_LEVELS or not rel_parts[-1].lower().endswith(".png"):
                return None
        elif category == "part_char":
            if rel_lower_parts[0] not in ZOOM_LEVELS:
                return None
        return MatchEntry(category=category, relative_path="/".join(rel_parts), zip_name=zip_name)
    return None

def collect_matches(zip_file: zipfile.ZipFile) -> tuple[dict, dict]:
    matches: dict[str, MatchEntry] = {}
    duplicates: dict[str, list[str]] = defaultdict(list)
    for info in zip_file.infolist():
        if info.is_dir():
            continue
        m = classify_entry(info.filename)
        if m is None:
            continue
        duplicates[m.destination_key].append(m.zip_name)
        matches[m.destination_key] = m
    dup_targets = {k: v for k, v in duplicates.items() if len(v) > 1}
    return matches, dup_targets

def collect_all_images(zip_file: zipfile.ZipFile) -> List[ImageEntry]:
    """Lấy TẤT CẢ ảnh trong jar (không lọc theo category)."""
    results: List[ImageEntry] = []
    for info in zip_file.infolist():
        if info.is_dir():
            continue
        lower = info.filename.lower()
        if lower.startswith("meta-inf/"):
            continue
        for ext in ALL_IMAGE_EXTS:
            if lower.endswith(ext):
                results.append(ImageEntry(zip_name=info.filename, ext=ext, size_bytes=info.file_size))
                break
    return sorted(results, key=lambda e: e.zip_name)

def extract_matches(zip_file: zipfile.ZipFile, matches: Iterable[MatchEntry],
                    data_dir: Path, *, dry_run: bool, backup_root: Optional[Path],
                    progress_cb=None) -> tuple[Counter, Counter]:
    status_counts: Counter = Counter()
    category_counts: Counter = Counter()
    items = list(matches)
    total = len(items)
    for idx, m in enumerate(items):
        target = data_dir / m.category / Path(m.relative_path)
        content = zip_file.read(m.zip_name)
        status = write_file_if_changed(target, content, dry_run=dry_run,
                                        backup_root=backup_root,
                                        backup_relative_path=Path(m.category) / Path(m.relative_path))
        status_counts[status] += 1
        category_counts[m.category] += 1
        if progress_cb:
            progress_cb(idx + 1, total, m.zip_name)
    return status_counts, category_counts

def extract_all_images_to_dir(zip_file: zipfile.ZipFile, images: List[ImageEntry],
                               out_dir: Path, *, progress_cb=None) -> Counter:
    """Ghi tất cả ảnh ra thư mục đích, giữ nguyên cấu trúc thư mục trong jar."""
    status_counts: Counter = Counter()
    total = len(images)
    for idx, img in enumerate(images):
        target = out_dir / img.zip_name.replace("\\", "/")
        content = zip_file.read(img.zip_name)
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists() or target.read_bytes() != content:
            target.write_bytes(content)
            status_counts["new" if not target.exists() else "updated"] += 1
        else:
            status_counts["unchanged"] += 1
        if progress_cb:
            progress_cb(idx + 1, total, img.zip_name)
    return status_counts

def scan_config_values(zip_file: zipfile.ZipFile) -> tuple[dict, dict]:
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
            m = pattern.search(data)
            if m:
                found_values[key] = int(m.group(1))
                found_sources[key] = info.filename
        if len(found_values) == len(CONFIG_PATTERNS):
            break
    return found_values, found_sources

def update_config_file(config_path: Path, values: dict, *, dry_run: bool,
                        backup_root: Optional[Path]) -> dict:
    if not values:
        return {}
    if not config_path.is_file():
        raise FileNotFoundError(f"Config không tìm thấy: {config_path}")
    text = config_path.read_text(encoding="utf-8", errors="ignore")
    updated_text = text
    changes: dict[str, tuple] = {}
    for key, new_value in values.items():
        pat = re.compile(rf"^(\s*{re.escape(key)}\s*:\s*)(-?\d+)(\s*)$", flags=re.MULTILINE)
        m = pat.search(updated_text)
        if m:
            old_value = int(m.group(2))
            if old_value == new_value:
                continue
            updated_text = pat.sub(rf"\g<1>{new_value}\g<3>", updated_text, count=1)
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

def load_template_jar_url(template_path: Path) -> str:
    if not template_path.is_file():
        raise FileNotFoundError(f"Template không tìm thấy: {template_path}")
    text = template_path.read_text(encoding="utf-8", errors="ignore")
    matches = re.findall(r"href\s*=\s*['\"]([^'\"]+\.jar[^'\"]*)['\"]]", text, flags=re.IGNORECASE)
    if not matches:
        raise ValueError(f"Không tìm thấy link .jar trong template: {template_path}")
    return html.unescape(matches[0])

def fetch_text(url: str, timeout: int) -> str:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.read().decode("utf-8", errors="ignore")

def resolve_download_url(url: str, timeout: int) -> str:
    parsed = urlparse(url)
    if parsed.scheme not in {"http", "https"}:
        raise ValueError(f"Scheme không hỗ trợ: {url}")
    if "mediafire.com" in parsed.netloc.lower():
        html_text = fetch_text(url, timeout)
        for pat in (
            r'id=["\']]downloadButton["\']][^>]*href=["\']]([^"\']+)["\']]',
            r'href=["\']]([^"\']+download[^"\']+\.jar[^"\']*)["\']]',
        ):
            m = re.search(pat, html_text, flags=re.IGNORECASE)
            if m:
                cand = html.unescape(m.group(1))
                return ("https:" + cand) if cand.startswith("//") else urljoin(url, cand)
        raise ValueError(f"Không thể lấy link download từ MediaFire: {url}")
    if parsed.path.lower().endswith(".jar"):
        return url
    html_text = fetch_text(url, timeout)
    m = re.search(r'href=["\']]([^"\']+\.jar[^"\']*)["\']]', html_text, flags=re.IGNORECASE)
    if not m:
        raise ValueError(f"Không tìm thấy link .jar trong trang: {url}")
    cand = html.unescape(m.group(1))
    return ("https:" + cand) if cand.startswith("//") else urljoin(url, cand)

def download_to_temp(url: str, timeout: int, progress_cb=None) -> Path:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    fd, tmp = tempfile.mkstemp(prefix="hsokmt_client_", suffix=".jar")
    os.close(fd)
    target = Path(tmp)
    with urllib.request.urlopen(req, timeout=timeout) as resp, target.open("wb") as out:
        total = int(resp.headers.get("Content-Length", 0))
        downloaded = 0
        while True:
            chunk = resp.read(65536)
            if not chunk:
                break
            out.write(chunk)
            downloaded += len(chunk)
            if progress_cb and total:
                progress_cb(downloaded, total)
    return target


# ─────────────────────────────────────────────────────────────────────────────
#  GUI APPLICATION
# ─────────────────────────────────────────────────────────────────────────────

ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")


class LogWidget(ctk.CTkTextbox):
    """Textbox hiện log có màu theo tag."""
    COLORS = {
        "INFO":    CLR["text"],
        "OK":      CLR["success"],
        "WARN":    CLR["warn"],
        "ERR":     CLR["err"],
        "HEAD":    CLR["accent"],
        "GOLD":    CLR["gold"],
        "DIM":     CLR["text_dim"],
    }

    def __init__(self, master, **kw):
        super().__init__(master, font=("Consolas", 11), state="disabled",
                         wrap="word", **kw)
        for tag, color in self.COLORS.items():
            self._textbox.tag_configure(tag, foreground=color)

    def log(self, msg: str, level: str = "INFO"):
        self.configure(state="normal")
        ts = datetime.now().strftime("%H:%M:%S")
        self._textbox.insert("end", f"[{ts}] {msg}\n", level)
        self._textbox.see("end")
        self.configure(state="disabled")

    def clear(self):
        self.configure(state="normal")
        self.delete("1.0", "end")
        self.configure(state="disabled")


class ImageBrowserFrame(ctk.CTkFrame):
    """Tab duyệt ảnh từ jar với thumbnail grid."""
    THUMB_W = 96
    THUMB_H = 96
    COLS    = 6

    def __init__(self, master, log_fn, **kw):
        super().__init__(master, fg_color=CLR["bg"], **kw)
        self._log = log_fn
        self._images: List[ImageEntry] = []
        self._thumbs: List[CTkImage] = []          # tránh GC
        self._selected: set[int] = set()
        self._jar_path: Optional[Path] = None
        self._filter_var = tk.StringVar()
        self._filter_var.trace_add("write", self._on_filter)
        self._build_ui()

    def _build_ui(self):
        top = ctk.CTkFrame(self, fg_color=CLR["panel"], corner_radius=10)
        top.pack(fill="x", padx=10, pady=(10, 4))

        ctk.CTkLabel(top, text="🔍 Lọc tên file:", text_color=CLR["text_dim"],
                     font=("Inter", 12)).pack(side="left", padx=(12, 4), pady=8)
        ctk.CTkEntry(top, textvariable=self._filter_var, width=260,
                     placeholder_text="Nhập tên để lọc...").pack(side="left", pady=8)

        self._lbl_count = ctk.CTkLabel(top, text="0 ảnh", text_color=CLR["text_dim"],
                                        font=("Inter", 11))
        self._lbl_count.pack(side="left", padx=16)

        ctk.CTkButton(top, text="☑ Chọn tất cả", width=120, command=self._select_all,
                      fg_color=CLR["card"], hover_color=CLR["border"]).pack(side="left", padx=4)
        ctk.CTkButton(top, text="☐ Bỏ chọn", width=100, command=self._deselect_all,
                      fg_color=CLR["card"], hover_color=CLR["border"]).pack(side="left", padx=4)

        self._btn_extract = ctk.CTkButton(top, text="📥 Extract đã chọn", width=160,
                                           command=self._extract_selected,
                                           fg_color=CLR["accent"], hover_color="#5a4acf",
                                           font=("Inter", 12, "bold"))
        self._btn_extract.pack(side="right", padx=12, pady=8)

        self._btn_extract_all = ctk.CTkButton(top, text="📦 Extract ALL", width=130,
                                               command=self._extract_all,
                                               fg_color=CLR["accent2"], hover_color="#3a78cc",
                                               font=("Inter", 12, "bold"))
        self._btn_extract_all.pack(side="right", padx=4, pady=8)

        # Scrollable grid
        scroll_outer = ctk.CTkScrollableFrame(self, fg_color=CLR["bg"], label_text="")
        scroll_outer.pack(fill="both", expand=True, padx=10, pady=(0, 10))
        self._grid_frame = scroll_outer

        self._placeholder = ctk.CTkLabel(self._grid_frame,
                                          text="📂  Chưa tải jar — dùng tab SYNC để chọn file rồi nhấn\n「Load Ảnh」",
                                          font=("Inter", 14), text_color=CLR["text_dim"])
        self._placeholder.pack(expand=True, pady=80)

    # ── Public API ──────────────────────────────────────────────────────────
    def load_from_jar(self, jar_path: Path):
        self._jar_path = jar_path
        self._images.clear()
        self._selected.clear()
        self._thumbs.clear()
        self._clear_grid()
        self._placeholder.configure(text="⏳ Đang đọc jar...")
        self._placeholder.pack(expand=True, pady=80)
        threading.Thread(target=self._do_load, daemon=True).start()

    # ── Internal ─────────────────────────────────────────────────────────────
    def _do_load(self):
        try:
            with zipfile.ZipFile(self._jar_path) as zf:
                imgs = collect_all_images(zf)
                # Đọc thumbnail cho từng ảnh
                for img in imgs:
                    if HAS_PIL:
                        try:
                            data = zf.read(img.zip_name)
                            pil = Image.open(io.BytesIO(data)).convert("RGBA")
                            pil.thumbnail((self.THUMB_W, self.THUMB_H), Image.LANCZOS)
                            img.thumb = CTkImage(light_image=pil, dark_image=pil,
                                                  size=(self.THUMB_W, self.THUMB_H))
                        except Exception:
                            img.thumb = None
                    self._images.append(img)
            self.after(0, lambda: self._render_grid(self._images))
        except Exception as e:
            self.after(0, lambda: self._placeholder.configure(text=f"❌ Lỗi: {e}"))

    def _clear_grid(self):
        for w in self._grid_frame.winfo_children():
            w.destroy()

    def _render_grid(self, images: List[ImageEntry]):
        self._clear_grid()
        if not images:
            lbl = ctk.CTkLabel(self._grid_frame, text="Không có ảnh nào.",
                                text_color=CLR["text_dim"], font=("Inter", 13))
            lbl.pack(pady=40)
            self._lbl_count.configure(text="0 ảnh")
            return

        self._lbl_count.configure(text=f"{len(images)} ảnh")
        container = ctk.CTkFrame(self._grid_frame, fg_color="transparent")
        container.pack(fill="both", padx=4)
        self._thumb_widgets: list = []

        for i, img in enumerate(images):
            col = i % self.COLS
            row = i // self.COLS
            cell = ctk.CTkFrame(container, fg_color=CLR["card"], corner_radius=8,
                                  border_width=2, border_color=CLR["border"],
                                  width=self.THUMB_W + 16, height=self.THUMB_H + 36)
            cell.grid(row=row, column=col, padx=5, pady=5, sticky="nsew")
            cell.grid_propagate(False)

            # Checkbox chọn
            var = tk.BooleanVar()
            cb = ctk.CTkCheckBox(cell, text="", variable=var, width=20,
                                   checkbox_width=16, checkbox_height=16,
                                   command=lambda idx=i, v=var: self._toggle(idx, v))
            cb.place(x=4, y=4)

            # Thumbnail / placeholder
            if img.thumb:
                img_lbl = ctk.CTkLabel(cell, image=img.thumb, text="")
                self._thumbs.append(img.thumb)
            else:
                # Placeholder icon
                icon_text = {"png": "🖼️", "jpg": "📷", "gif": "🎞️"}.get(img.ext.lstrip("."), "🖼️")
                img_lbl = ctk.CTkLabel(cell, text=icon_text, font=("Inter", 32))
            img_lbl.place(relx=0.5, rely=0.45, anchor="center")

            # Tên file (rút gọn)
            name = Path(img.zip_name).name
            short = name if len(name) <= 14 else name[:11] + "…"
            ctk.CTkLabel(cell, text=short, font=("Inter", 9),
                          text_color=CLR["text_dim"]).place(relx=0.5, rely=0.92, anchor="center")

            # Click để xem chi tiết
            for w in (cell, img_lbl):
                w.bind("<Button-1>", lambda e, idx=i: self._preview(idx))

            self._thumb_widgets.append((cell, var))

    def _toggle(self, idx: int, var: tk.BooleanVar):
        if var.get():
            self._selected.add(idx)
        else:
            self._selected.discard(idx)

    def _select_all(self):
        for i, (cell, var) in enumerate(getattr(self, "_thumb_widgets", [])):
            var.set(True)
            self._selected.add(i)

    def _deselect_all(self):
        for i, (cell, var) in enumerate(getattr(self, "_thumb_widgets", [])):
            var.set(False)
        self._selected.clear()

    def _on_filter(self, *_):
        q = self._filter_var.get().lower()
        filtered = [img for img in self._images if q in img.zip_name.lower()]
        self._render_grid(filtered)

    def _preview(self, idx: int):
        if not HAS_PIL or idx >= len(self._images):
            return
        img_entry = self._images[idx]
        try:
            with zipfile.ZipFile(self._jar_path) as zf:
                data = zf.read(img_entry.zip_name)
            pil = Image.open(io.BytesIO(data)).convert("RGBA")
        except Exception as e:
            messagebox.showerror("Lỗi", f"Không đọc được ảnh:\n{e}")
            return
        # Cửa sổ preview
        win = ctk.CTkToplevel(self)
        win.title(f"Preview — {Path(img_entry.zip_name).name}")
        win.configure(fg_color=CLR["bg"])
        win.grab_set()
        max_w, max_h = 700, 600
        pil_disp = pil.copy()
        pil_disp.thumbnail((max_w, max_h), Image.LANCZOS)
        ctk_img = CTkImage(light_image=pil_disp, dark_image=pil_disp,
                            size=(pil_disp.width, pil_disp.height))
        ctk.CTkLabel(win, image=ctk_img, text="").pack(padx=16, pady=12)
        info_txt = (f"📄 {img_entry.zip_name}\n"
                    f"📐 {pil.width}×{pil.height}px  •  {img_entry.size_bytes//1024} KB")
        ctk.CTkLabel(win, text=info_txt, font=("Inter", 11),
                      text_color=CLR["text_dim"]).pack(pady=(0, 12))

    def _extract_selected(self):
        indices = list(self._selected)
        if not indices:
            messagebox.showinfo("Thông báo", "Chưa chọn ảnh nào.")
            return
        selected_imgs = [self._images[i] for i in indices if i < len(self._images)]
        self._do_extract(selected_imgs)

    def _extract_all(self):
        self._do_extract(self._images)

    def _do_extract(self, images: List[ImageEntry]):
        if not images or not self._jar_path:
            return
        out_dir = filedialog.askdirectory(title="Chọn thư mục lưu ảnh")
        if not out_dir:
            return
        out_path = Path(out_dir)
        self._log(f"📥 Export {len(images)} ảnh → {out_path}", "HEAD")

        def run():
            try:
                with zipfile.ZipFile(self._jar_path) as zf:
                    counts = extract_all_images_to_dir(zf, images, out_path)
                self._log(f"✅ Xong! new={counts['new']}  updated={counts['updated']}  unchanged={counts['unchanged']}", "OK")
                messagebox.showinfo("Hoàn tất", f"Đã export {len(images)} ảnh vào:\n{out_path}")
            except Exception as e:
                self._log(f"❌ Lỗi export: {e}", "ERR")
                messagebox.showerror("Lỗi", str(e))

        threading.Thread(target=run, daemon=True).start()


class SyncFrame(ctk.CTkFrame):
    """Tab đồng bộ tài nguyên chính (giữ đầy đủ logic gốc)."""

    def __init__(self, master, log_fn, on_jar_loaded, **kw):
        super().__init__(master, fg_color=CLR["bg"], **kw)
        self._log = log_fn
        self._on_jar_loaded = on_jar_loaded
        self._jar_path: Optional[Path] = None
        self._downloaded = False
        self._running = False
        self._q: queue.Queue = queue.Queue()
        self._build_ui()
        self.after(100, self._poll_queue)

    def _build_ui(self):
        # ── Source section ──────────────────────────────────────────────────
        src_card = self._card(self, "📂  Nguồn Jar")
        src_card.pack(fill="x", padx=12, pady=(12, 6))

        self._src_mode = tk.StringVar(value="local")
        modes = [("📁 File cục bộ", "local"), ("🌐 URL / MediaFire", "url"), ("🔗 Auto Template", "template")]
        mf = ctk.CTkFrame(src_card, fg_color="transparent")
        mf.pack(fill="x", padx=12, pady=(0, 4))
        for label, val in modes:
            ctk.CTkRadioButton(mf, text=label, variable=self._src_mode, value=val,
                                command=self._on_mode_change,
                                font=("Inter", 12), text_color=CLR["text"]).pack(side="left", padx=10)

        self._local_frame = ctk.CTkFrame(src_card, fg_color="transparent")
        self._local_frame.pack(fill="x", padx=12, pady=(0, 8))
        self._jar_entry = ctk.CTkEntry(self._local_frame, placeholder_text="Chọn file .jar...",
                                        width=400, font=("Consolas", 11))
        self._jar_entry.pack(side="left", padx=(0, 8), fill="x", expand=True)
        ctk.CTkButton(self._local_frame, text="Browse…", width=90,
                       command=self._browse_jar,
                       fg_color=CLR["card"], hover_color=CLR["border"]).pack(side="left")

        self._url_frame = ctk.CTkFrame(src_card, fg_color="transparent")
        ctk.CTkLabel(self._url_frame, text="URL:", text_color=CLR["text_dim"],
                      font=("Inter", 12)).pack(side="left", padx=(0, 6))
        self._url_entry = ctk.CTkEntry(self._url_frame, placeholder_text="https://...",
                                        width=460, font=("Consolas", 11))
        self._url_entry.pack(side="left", fill="x", expand=True)

        self._template_frame = ctk.CTkFrame(src_card, fg_color="transparent")
        self._tpl_entry = ctk.CTkEntry(self._template_frame,
                                        font=("Consolas", 11), width=400)
        self._tpl_entry.insert(0, str(default_template_path()))
        self._tpl_entry.pack(side="left", fill="x", expand=True, padx=(0, 8))
        ctk.CTkButton(self._template_frame, text="Browse…", width=90,
                       command=self._browse_template,
                       fg_color=CLR["card"], hover_color=CLR["border"]).pack(side="left")

        self._on_mode_change()

        # ── Output section ──────────────────────────────────────────────────
        out_card = self._card(self, "🗂️  Đường Dẫn Đầu Ra")
        out_card.pack(fill="x", padx=12, pady=6)

        row = ctk.CTkFrame(out_card, fg_color="transparent")
        row.pack(fill="x", padx=12, pady=6)
        ctk.CTkLabel(row, text="Data Dir:", width=70, anchor="e",
                      text_color=CLR["text_dim"], font=("Inter", 12)).pack(side="left", padx=(0, 6))
        self.data_entry = ctk.CTkEntry(row, font=("Consolas", 11), width=400)
        self.data_entry.insert(0, str(default_data_dir()))
        self.data_entry.pack(side="left", fill="x", expand=True, padx=(0, 8))
        ctk.CTkButton(row, text="Browse…", width=90, command=self._browse_data,
                       fg_color=CLR["card"], hover_color=CLR["border"]).pack(side="left")

        # ── Options section ─────────────────────────────────────────────────
        opt_card = self._card(self, "⚙️  Tùy Chọn")
        opt_card.pack(fill="x", padx=12, pady=6)
        of = ctk.CTkFrame(opt_card, fg_color="transparent")
        of.pack(fill="x", padx=12, pady=6)

        self._opt_dry    = self._check(of, "🧪 Dry-run (không ghi file)")
        self._opt_backup = self._check(of, "💾 Backup", default=True)
        self._opt_allimg = self._check(of, "🖼️ Extract ALL ảnh")

        of2 = ctk.CTkFrame(opt_card, fg_color="transparent")
        of2.pack(fill="x", padx=12, pady=(0, 8))
        ctk.CTkLabel(of2, text="Timeout (s):", text_color=CLR["text_dim"],
                      font=("Inter", 12)).pack(side="left", padx=(0, 6))
        self._timeout_entry = ctk.CTkEntry(of2, width=60, font=("Consolas", 12))
        self._timeout_entry.insert(0, "30")
        self._timeout_entry.pack(side="left")

        ctk.CTkLabel(of2, text="  Output ảnh (nếu extract all):",
                      text_color=CLR["text_dim"], font=("Inter", 12)).pack(side="left", padx=(20, 6))
        self._imgout_entry = ctk.CTkEntry(of2, width=280, font=("Consolas", 11),
                                           placeholder_text="Để trống = thư mục 'all_images' bên cạnh data/")
        self._imgout_entry.pack(side="left", fill="x", expand=True, padx=(0, 6))
        ctk.CTkButton(of2, text="Browse…", width=90, command=self._browse_imgout,
                       fg_color=CLR["card"], hover_color=CLR["border"]).pack(side="left")

        # ── Progress section ─────────────────────────────────────────────────
        prog_card = self._card(self, "📊  Tiến Trình")
        prog_card.pack(fill="x", padx=12, pady=6)

        pf = ctk.CTkFrame(prog_card, fg_color="transparent")
        pf.pack(fill="x", padx=12, pady=(0, 8))
        self._progress = ctk.CTkProgressBar(pf, height=18, corner_radius=9,
                                              progress_color=CLR["accent"])
        self._progress.pack(fill="x", side="left", expand=True, padx=(0, 12))
        self._progress.set(0)
        self._pct_label = ctk.CTkLabel(pf, text="0%", width=44,
                                        font=("Inter", 12, "bold"), text_color=CLR["accent"])
        self._pct_label.pack(side="left")

        self._status_label = ctk.CTkLabel(prog_card, text="Sẵn sàng.",
                                           font=("Inter", 11), text_color=CLR["text_dim"])
        self._status_label.pack(padx=12, pady=(0, 8), anchor="w")

        # ── Buttons ──────────────────────────────────────────────────────────
        bf = ctk.CTkFrame(self, fg_color="transparent")
        bf.pack(pady=8)

        self._btn_load = ctk.CTkButton(bf, text="🔍 Load Ảnh", width=140, height=42,
                                        font=("Inter", 13, "bold"),
                                        fg_color=CLR["accent2"], hover_color="#3a78cc",
                                        command=self._load_images)
        self._btn_load.pack(side="left", padx=8)

        self._btn_sync = ctk.CTkButton(bf, text="▶  BẮT ĐẦU SYNC", width=200, height=42,
                                        font=("Inter", 14, "bold"),
                                        fg_color=CLR["accent"], hover_color="#5a4acf",
                                        command=self._start_sync)
        self._btn_sync.pack(side="left", padx=8)

        self._btn_stop = ctk.CTkButton(bf, text="⏹ Dừng", width=100, height=42,
                                        font=("Inter", 13),
                                        fg_color="#7f1d1d", hover_color="#991b1b",
                                        command=self._stop, state="disabled")
        self._btn_stop.pack(side="left", padx=8)

    # ── Helpers ──────────────────────────────────────────────────────────────
    def _card(self, parent, title: str) -> ctk.CTkFrame:
        frame = ctk.CTkFrame(parent, fg_color=CLR["panel"], corner_radius=12)
        ctk.CTkLabel(frame, text=title, font=("Inter", 13, "bold"),
                      text_color=CLR["accent"]).pack(anchor="w", padx=14, pady=(10, 4))
        ctk.CTkFrame(frame, fg_color=CLR["border"], height=1).pack(fill="x", padx=12, pady=(0, 6))
        return frame

    def _check(self, parent, text: str, default: bool = False) -> tk.BooleanVar:
        var = tk.BooleanVar(value=default)
        ctk.CTkCheckBox(parent, text=text, variable=var,
                         font=("Inter", 12), text_color=CLR["text"]).pack(side="left", padx=12)
        return var

    def _browse_jar(self):
        p = filedialog.askopenfilename(title="Chọn client jar", filetypes=[("Jar files", "*.jar")])
        if p:
            self._jar_entry.delete(0, "end")
            self._jar_entry.insert(0, p)

    def _browse_template(self):
        p = filedialog.askopenfilename(title="Chọn template HTML", filetypes=[("HTML", "*.html")])
        if p:
            self._tpl_entry.delete(0, "end")
            self._tpl_entry.insert(0, p)

    def _browse_data(self):
        p = filedialog.askdirectory(title="Chọn data dir")
        if p:
            self.data_entry.delete(0, "end")
            self.data_entry.insert(0, p)

    def _browse_imgout(self):
        p = filedialog.askdirectory(title="Chọn thư mục lưu ảnh")
        if p:
            self._imgout_entry.delete(0, "end")
            self._imgout_entry.insert(0, p)

    def _on_mode_change(self):
        mode = self._src_mode.get()
        self._local_frame.pack_forget()
        self._url_frame.pack_forget()
        self._template_frame.pack_forget()
        if mode == "local":
            self._local_frame.pack(fill="x", padx=12, pady=(0, 8))
        elif mode == "url":
            self._url_frame.pack(fill="x", padx=12, pady=(0, 8))
        else:
            self._template_frame.pack(fill="x", padx=12, pady=(0, 8))

    def _set_progress(self, val: float, label: str = ""):
        self._progress.set(val)
        self._pct_label.configure(text=f"{int(val*100)}%")
        if label:
            self._status_label.configure(text=label)

    def _poll_queue(self):
        """Đọc queue từ thread background để update UI (thread-safe)."""
        try:
            while True:
                msg = self._q.get_nowait()
                kind = msg.get("kind")
                if kind == "log":
                    self._log(msg["text"], msg.get("level", "INFO"))
                elif kind == "progress":
                    self._set_progress(msg["val"], msg.get("label", ""))
                elif kind == "done":
                    self._on_done(msg.get("ok", True), msg.get("err", ""))
        except queue.Empty:
            pass
        self.after(100, self._poll_queue)

    def _q_log(self, text: str, level="INFO"):
        self._q.put({"kind": "log", "text": text, "level": level})

    def _q_prog(self, val: float, label=""):
        self._q.put({"kind": "progress", "val": val, "label": label})

    def _q_done(self, ok=True, err=""):
        self._q.put({"kind": "done", "ok": ok, "err": err})

    # ── Load Ảnh ─────────────────────────────────────────────────────────────
    def _load_images(self):
        jar = self._resolve_jar_quick()
        if jar:
            self._on_jar_loaded(jar)
            self._log(f"🔍 Đang load ảnh từ: {jar.name}", "HEAD")

    def _resolve_jar_quick(self) -> Optional[Path]:
        mode = self._src_mode.get()
        if mode == "local":
            p = self._jar_entry.get().strip()
            if not p:
                messagebox.showwarning("Thông báo", "Chọn file jar trước.")
                return None
            jp = Path(p)
            if not jp.is_file():
                messagebox.showerror("Lỗi", f"File không tồn tại:\n{jp}")
                return None
            return jp
        messagebox.showinfo("Thông báo", "Load ảnh chỉ hỗ trợ file cục bộ.\nHãy chọn 'File cục bộ' và chọn jar.")
        return None

    # ── Start Sync ───────────────────────────────────────────────────────────
    def _start_sync(self):
        if self._running:
            return
        self._running = True
        self._btn_sync.configure(state="disabled")
        self._btn_stop.configure(state="normal")
        self._set_progress(0, "Đang khởi động...")
        self._log("═" * 60, "DIM")
        self._log("▶  BẮT ĐẦU SYNC", "HEAD")

        opts = {
            "mode":       self._src_mode.get(),
            "jar_path":   self._jar_entry.get().strip(),
            "url":        self._url_entry.get().strip(),
            "template":   self._tpl_entry.get().strip(),
            "data_dir":   self.data_entry.get().strip(),
            "dry_run":    self._opt_dry.get(),
            "backup":     self._opt_backup.get(),
            "all_images": self._opt_allimg.get(),
            "imgout":     self._imgout_entry.get().strip(),
            "timeout":    int(self._timeout_entry.get() or 30),
        }
        threading.Thread(target=self._run_sync, args=(opts,), daemon=True).start()

    def _stop(self):
        self._running = False
        self._q_log("⏹ Người dùng yêu cầu dừng.", "WARN")

    def _on_done(self, ok: bool, err: str):
        self._running = False
        self._btn_sync.configure(state="normal")
        self._btn_stop.configure(state="disabled")
        if ok:
            self._set_progress(1.0, "✅ Hoàn tất!")
            self._log("✅ SYNC HOÀN TẤT", "OK")
        else:
            self._set_progress(0, "❌ Lỗi")
            self._log(f"❌ Lỗi: {err}", "ERR")

    # ── Background thread ────────────────────────────────────────────────────
    def _run_sync(self, opts: dict):
        jar_path: Optional[Path] = None
        downloaded = False
        backup_root = default_backup_dir() if opts["backup"] else None
        try:
            # ── Resolve jar ─────────────────────────────────────────────────
            if opts["mode"] == "local":
                if not opts["jar_path"]:
                    raise ValueError("Chưa chọn file jar.")
                jar_path = Path(opts["jar_path"]).expanduser().resolve()
            elif opts["mode"] == "url":
                if not opts["url"]:
                    raise ValueError("Chưa nhập URL.")
                self._q_log(f"🌐 Đang resolve URL: {opts['url']}", "INFO")
                self._q_prog(0.02, "Đang tải jar từ URL...")
                real_url = resolve_download_url(opts["url"], opts["timeout"])
                self._q_log(f"→ {real_url}", "DIM")
                self._q_prog(0.05, "Đang download jar...")
                jar_path = download_to_temp(real_url, opts["timeout"],
                                             progress_cb=lambda d, t: self._q_prog(0.05 + 0.3 * d / t,
                                                                                     f"Download: {d//1024}K/{t//1024}K"))
                downloaded = True
            else:
                tpl = Path(opts["template"])
                self._q_log(f"📄 Đọc template: {tpl.name}", "INFO")
                url = load_template_jar_url(tpl)
                self._q_log(f"→ Link: {url}", "DIM")
                self._q_prog(0.03, "Đang download jar từ template...")
                real_url = resolve_download_url(url, opts["timeout"])
                jar_path = download_to_temp(real_url, opts["timeout"],
                                             progress_cb=lambda d, t: self._q_prog(0.03 + 0.3 * d / t,
                                                                                     f"Download: {d//1024}K/{t//1024}K"))
                downloaded = True

            ensure_zip(jar_path)
            self._q_log(f"✅ Jar OK: {jar_path.name}  ({jar_path.stat().st_size//1024} KB)", "OK")
            self._q_prog(0.35, "Đang phân tích jar...")

            data_dir  = Path(opts["data_dir"]).expanduser().resolve()
            dry_run   = opts["dry_run"]

            with zipfile.ZipFile(jar_path) as zf:
                # ── Phase 1: Categorized refs ────────────────────────────────
                self._q_log("━━ Phase 1: Sync tài nguyên theo category", "HEAD")
                matches, dup_targets = collect_matches(zf)
                self._q_log(f"  Tìm thấy {len(matches)} entries  |  Trùng lặp: {len(dup_targets)}", "INFO")
                if matches:
                    ordered = [matches[k] for k in sorted(matches)]
                    total_m = len(ordered)

                    def prog_cb(done, total, name):
                        if not self._running:
                            raise InterruptedError("Dừng bởi người dùng")
                        pct = 0.35 + 0.45 * (done / total)
                        self._q_prog(pct, f"[{done}/{total}] {Path(name).name}")

                    sc, cc = extract_matches(zf, ordered, data_dir, dry_run=dry_run,
                                              backup_root=backup_root, progress_cb=prog_cb)
                    for cat in CATEGORY_ORDER:
                        if cc[cat]:
                            self._q_log(f"  {cat}: {cc[cat]} file  "
                                        f"(mới={sc['new']} cập nhật={sc['updated']} bỏ qua={sc['unchanged']})", "OK")
                else:
                    self._q_log("  ⚠ Không tìm thấy tài nguyên có category.", "WARN")

                # ── Phase 2: All images ──────────────────────────────────────
                if opts["all_images"]:
                    self._q_log("━━ Phase 2: Extract ALL ảnh", "HEAD")
                    all_imgs = collect_all_images(zf)
                    self._q_log(f"  Tổng ảnh trong jar: {len(all_imgs)}", "INFO")
                    if all_imgs:
                        img_out_str = opts["imgout"].strip()
                        if img_out_str:
                            img_out = Path(img_out_str).expanduser().resolve()
                        else:
                            img_out = data_dir.parent / "all_images"
                        self._q_log(f"  → Output: {img_out}", "DIM")
                        total_i = len(all_imgs)

                        def img_prog_cb(done, total, name):
                            if not self._running:
                                raise InterruptedError("Dừng bởi người dùng")
                            pct = 0.80 + 0.15 * (done / total)
                            self._q_prog(pct, f"Ảnh [{done}/{total}] {Path(name).name}")

                        if not dry_run:
                            ic = extract_all_images_to_dir(zf, all_imgs, img_out, progress_cb=img_prog_cb)
                            self._q_log(f"  ✅ {total_i} ảnh — mới={ic['new']} cập nhật={ic['updated']} bỏ qua={ic['unchanged']}", "OK")
                        else:
                            self._q_log(f"  [DRY-RUN] Sẽ extract {total_i} ảnh → {img_out}", "WARN")


            if dry_run:
                self._q_log("⚠  DRY-RUN: Không có file nào được ghi!", "WARN")
            if backup_root and not dry_run:
                self._q_log(f"💾 Backup tại: {backup_root}", "DIM")

            self._q_done(ok=True)

        except InterruptedError:
            self._q_done(ok=False, err="Dừng bởi người dùng")
        except Exception as exc:
            import traceback
            self._q_log(traceback.format_exc(), "ERR")
            self._q_done(ok=False, err=str(exc))
        finally:
            if downloaded and jar_path and jar_path.exists():
                try:
                    jar_path.unlink()
                except Exception:
                    pass


class App(ctk.CTk):
    """Cửa sổ chính."""

    def __init__(self):
        super().__init__()
        self.title("⚔️  HSO KMT — Client Ref Sync Tool  v2.0")
        self.geometry("1280x820")
        self.minsize(960, 640)
        self.configure(fg_color=CLR["bg"])
        self._build_ui()

    def _build_ui(self):
        # ── Header ──────────────────────────────────────────────────────────
        header = ctk.CTkFrame(self, fg_color=CLR["panel"], corner_radius=0, height=60)
        header.pack(fill="x", side="top")
        header.pack_propagate(False)

        ctk.CTkLabel(header, text="⚔️  HSO KMT", font=("Inter", 20, "bold"),
                      text_color=CLR["gold"]).pack(side="left", padx=20)
        ctk.CTkLabel(header, text="Client Ref Sync Tool", font=("Inter", 14),
                      text_color=CLR["text_dim"]).pack(side="left")
        ctk.CTkLabel(header, text="v2.0  •  icon  •  part_char  •  msg  •  msg_eff  •  map  •  ALL IMAGES",
                      font=("Inter", 10), text_color=CLR["text_dim"]).pack(side="right", padx=20)

        # ── Body (left panel + right log) ───────────────────────────────────
        body = ctk.CTkFrame(self, fg_color="transparent")
        body.pack(fill="both", expand=True, padx=0, pady=0)

        # Tabview
        self._tabs = ctk.CTkTabview(body, fg_color=CLR["bg"],
                                     segmented_button_fg_color=CLR["panel"],
                                     segmented_button_selected_color=CLR["accent"],
                                     segmented_button_selected_hover_color="#5a4acf",
                                     segmented_button_unselected_color=CLR["panel"],
                                     segmented_button_unselected_hover_color=CLR["card"],
                                     text_color=CLR["text"],
                                     text_color_disabled=CLR["text_dim"])
        self._tabs.pack(side="left", fill="both", expand=True)

        t_sync  = self._tabs.add("⚙️  SYNC")
        t_imgs  = self._tabs.add("🖼️  IMAGE BROWSER")
        t_log   = self._tabs.add("📋  LOG")

        # Log widget (shared)
        self._log_widget = LogWidget(t_log, fg_color=CLR["panel"], corner_radius=10)
        self._log_widget.pack(fill="both", expand=True, padx=10, pady=10)
        ctk.CTkButton(t_log, text="🗑 Xóa log", width=100,
                       fg_color=CLR["card"], hover_color=CLR["border"],
                       command=self._log_widget.clear).pack(pady=(0, 8))

        # Sync frame
        sync_scroll = ctk.CTkScrollableFrame(t_sync, fg_color=CLR["bg"], label_text="")
        sync_scroll.pack(fill="both", expand=True)
        self._sync_frame = SyncFrame(sync_scroll, log_fn=self._log,
                                      on_jar_loaded=self._on_jar_loaded)
        self._sync_frame.pack(fill="both", expand=True)

        # Image browser
        self._img_browser = ImageBrowserFrame(t_imgs, log_fn=self._log)
        self._img_browser.pack(fill="both", expand=True)

        # ── Status bar ───────────────────────────────────────────────────────
        status = ctk.CTkFrame(self, fg_color=CLR["panel"], corner_radius=0, height=28)
        status.pack(fill="x", side="bottom")
        status.pack_propagate(False)
        self._status_bar = ctk.CTkLabel(status, text="👾 Sẵn sàng — Chọn file jar và nhấn BẮT ĐẦU SYNC",
                                         font=("Inter", 10), text_color=CLR["text_dim"])
        self._status_bar.pack(side="left", padx=16)
        if not HAS_PIL:
            ctk.CTkLabel(status, text="⚠ Pillow chưa cài — preview ảnh bị tắt",
                          font=("Inter", 10), text_color=CLR["warn"]).pack(side="right", padx=16)

    def _log(self, msg: str, level: str = "INFO"):
        self._log_widget.log(msg, level)
        short = msg[:80] + ("…" if len(msg) > 80 else "")
        self._status_bar.configure(text=short)

    def _on_jar_loaded(self, jar_path: Path):
        self._img_browser.load_from_jar(jar_path)
        self._tabs.set("🖼️  IMAGE BROWSER")


def main():
    app = App()
    app.mainloop()


if __name__ == "__main__":
    main()
