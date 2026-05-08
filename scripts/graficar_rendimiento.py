


# ── Argumentos ───────────────────────────────────────────────
if len(sys.argv) >= 3:
    csv_path   = sys.argv[1]
    output_dir = sys.argv[2]
else:
    base       = os.path.dirname(os.path.abspath(__file__))
    csv_path   = os.path.join(base, "resultados", "resultados.csv")
    output_dir = os.path.join(base, "resultados")

if not os.path.exists(csv_path):
    print(f"ERROR: No se encontro el archivo CSV:\n  {csv_path}")
    sys.exit(1)

os.makedirs(output_dir, exist_ok=True)

# ── Leer CSV (compatible con formato antiguo y nuevo) ────────
rows = []
with open(csv_path, newline="", encoding="utf-8") as f:
    reader = csv.DictReader(f)
    headers = reader.fieldnames or []
    for row in reader:
        rows.append(row)

has_bst = "bst_ins_ms" in headers

n_vals = [int(r["n"]) for r in rows]

# Columnas AVL
if "avl_ins_ms" in headers:
    avl_ins  = [float(r["avl_ins_ms"])  for r in rows]
    avl_bus  = [float(r["avl_bus_ms"])  for r in rows]
    avl_elim = [float(r["avl_elim_ms"]) for r in rows]
    avl_h    = [float(r["avl_altura"])  for r in rows]
else:
    avl_ins  = [float(r["insercion_ms"])   for r in rows]
    avl_bus  = [float(r["busqueda_ms"])    for r in rows]
    avl_elim = [float(r["eliminacion_ms"]) for r in rows]
    avl_h    = [float(r["altura_real"])    for r in rows]

# Columnas BST (opcionales)
if has_bst:
    bst_ins  = [float(r["bst_ins_ms"])  for r in rows]
    bst_bus  = [float(r["bst_bus_ms"])  for r in rows]
    bst_elim = [float(r["bst_elim_ms"]) for r in rows]
    bst_h    = [float(r["bst_altura"])  for r in rows]

h_log2 = [math.log2(n) for n in n_vals]

print(f"CSV cargado: {len(n_vals)} tamanio(s) -> {[f'{n:,}' for n in n_vals]}")
print(f"Columnas BST presentes: {has_bst}")

# ── Paleta ───────────────────────────────────────────────────
C_AVL_INS  = "#1a73e8"
C_AVL_BUS  = "#34a853"
C_AVL_ELIM = "#ea4335"
C_BST_INS  = "#4fc3f7"
C_BST_BUS  = "#a5d6a7"
C_BST_ELIM = "#ef9a9a"
C_AVL_H    = "#8b5cf6"
C_BST_H    = "#f59e0b"
C_TEOR     = "#9e9e9e"

TITULO = "Estructuras de Datos 2016699 · UNAL 2026"

def label_n(n):
    if n >= 1_000_000: return f"{n//1_000_000}M"
    if n >= 1_000:     return f"{n//1_000}K"
    return str(n)

x_labels = [label_n(n) for n in n_vals]
x_idx    = list(range(len(n_vals)))

def estilo_ax(ax):
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.grid(axis="y", alpha=0.3, linestyle="--")

# ─────────────────────────────────────────────────────────────
# GRAFICA 1 — Tiempos AVL: 3 subplots
# ─────────────────────────────────────────────────────────────
fig1, axes = plt.subplots(1, 3, figsize=(16, 5.5))
fig1.suptitle(f"Rendimiento del Arbol AVL\n{TITULO}",
              fontsize=13, fontweight="bold", y=1.01)

for ax, (nombre, datos, color) in zip(axes, [
    ("Insercion",   avl_ins,  C_AVL_INS),
    ("Busqueda",    avl_bus,  C_AVL_BUS),
    ("Eliminacion", avl_elim, C_AVL_ELIM),
]):
    ax.bar(x_idx, datos, color=color, alpha=0.15, width=0.5, zorder=1)
    ax.plot(x_idx, datos, "o-", color=color, linewidth=2.5, markersize=10,
            markerfacecolor="white", markeredgewidth=2.5, zorder=3)
    for xi, yi in zip(x_idx, datos):
        ax.annotate(f"{int(yi):,} ms", xy=(xi, yi), xytext=(0, 14),
                    textcoords="offset points", ha="center",
                    fontsize=10, fontweight="bold", color=color)
    ax.set_title(nombre, fontsize=12, fontweight="bold", pad=10)
    ax.set_xlabel("n (estudiantes)", fontsize=10)
    ax.set_ylabel("Tiempo (ms)", fontsize=10)
    ax.set_xticks(x_idx); ax.set_xticklabels(x_labels, fontsize=11)
    ax.set_ylim(bottom=0, top=max(datos) * 1.35)
    ax.margins(x=0.25)
    ax.text(0.97, 0.05, "O(n log n)", transform=ax.transAxes,
            ha="right", fontsize=10, color=color,
            bbox=dict(boxstyle="round,pad=0.3", fc="white", ec=color, alpha=0.8))
    estilo_ax(ax)

plt.tight_layout()
out1 = os.path.join(output_dir, "grafica_tiempos.png")
fig1.savefig(out1, dpi=150, bbox_inches="tight")
print(f"Guardado: {out1}")
plt.close(fig1)

# ─────────────────────────────────────────────────────────────
# GRAFICA 2 — Comparativa AVL vs BST (solo si hay datos BST)
# ─────────────────────────────────────────────────────────────
if has_bst:
    fig2, axes2 = plt.subplots(1, 3, figsize=(17, 6))
    fig2.suptitle(f"Comparativa AVL vs BST (datos aleatorios, semilla 42)\n{TITULO}",
                  fontsize=13, fontweight="bold", y=1.01)

    pares = [
        ("Insercion",   avl_ins,  C_AVL_INS,  bst_ins,  C_BST_INS),
        ("Busqueda",    avl_bus,  C_AVL_BUS,  bst_bus,  C_BST_BUS),
        ("Eliminacion", avl_elim, C_AVL_ELIM, bst_elim, C_BST_ELIM),
    ]
    ancho = 0.3

    for ax, (nombre, avl_d, c_avl, bst_d, c_bst) in zip(axes2, pares):
        x = np.array(x_idx, dtype=float)
        bars_avl = ax.bar(x - ancho/2, avl_d, ancho, label="AVL", color=c_avl, alpha=0.85, zorder=2)
        bars_bst = ax.bar(x + ancho/2, bst_d, ancho, label="BST", color=c_bst, alpha=0.85, zorder=2)
        for bar, val in zip(bars_avl, avl_d):
            ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + max(avl_d)*0.02,
                    f"{int(val)}", ha="center", va="bottom", fontsize=8.5,
                    fontweight="bold", color=c_avl)
        for bar, val in zip(bars_bst, bst_d):
            ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + max(bst_d)*0.02,
                    f"{int(val)}", ha="center", va="bottom", fontsize=8.5,
                    fontweight="bold", color="#888")
        ax.set_title(nombre, fontsize=12, fontweight="bold", pad=10)
        ax.set_xlabel("n (estudiantes)", fontsize=10)
        ax.set_ylabel("Tiempo (ms)", fontsize=10)
        ax.set_xticks(x_idx); ax.set_xticklabels(x_labels, fontsize=11)
        ax.set_ylim(bottom=0, top=max(max(avl_d), max(bst_d)) * 1.35)
        ax.legend(fontsize=10)
        estilo_ax(ax)

    plt.tight_layout()
    out2 = os.path.join(output_dir, "grafica_comparativa.png")
    fig2.savefig(out2, dpi=150, bbox_inches="tight")
    print(f"Guardado: {out2}")
    plt.close(fig2)

# ─────────────────────────────────────────────────────────────
# GRAFICA 3 — Altura real vs teorica
# ─────────────────────────────────────────────────────────────
fig3, ax3 = plt.subplots(figsize=(10, 5.5))
fig3.suptitle(f"Altura del arbol: Real vs Teorica O(log2 n)\n{TITULO}",
              fontsize=13, fontweight="bold")

ax3.fill_between(x_idx, h_log2, avl_h, alpha=0.12, color=C_AVL_H, label="Diferencia AVL")
ax3.plot(x_idx, avl_h, "s-", color=C_AVL_H, linewidth=2.5, markersize=10,
         markerfacecolor="white", markeredgewidth=2.5, label="Altura AVL real")
if has_bst:
    ax3.plot(x_idx, bst_h, "^-", color=C_BST_H, linewidth=2.5, markersize=10,
             markerfacecolor="white", markeredgewidth=2.5, label="Altura BST real")
ax3.plot(x_idx, h_log2, "^--", color=C_TEOR, linewidth=2, markersize=9,
         label="log2(n) teorico")

for xi, yr, yt in zip(x_idx, avl_h, h_log2):
    ax3.annotate(f"AVL={int(yr)}", xy=(xi, yr), xytext=(0, 14),
                 textcoords="offset points", ha="center",
                 fontsize=10, fontweight="bold", color=C_AVL_H)
    ax3.annotate(f"{yt:.1f}", xy=(xi, yt), xytext=(0, -18),
                 textcoords="offset points", ha="center",
                 fontsize=9, color=C_TEOR)
if has_bst:
    for xi, yb in zip(x_idx, bst_h):
        ax3.annotate(f"BST={int(yb)}", xy=(xi, yb), xytext=(14, 0),
                     textcoords="offset points", ha="left",
                     fontsize=9, color=C_BST_H)

ax3.set_xticks(x_idx); ax3.set_xticklabels(x_labels, fontsize=11)
ax3.set_xlabel("n (estudiantes)", fontsize=11)
ax3.set_ylabel("Altura del arbol", fontsize=11)
ax3.set_ylim(bottom=0)
ax3.margins(x=0.18)
ax3.legend(fontsize=10)
ax3.text(0.97, 0.05,
         "AVL: h <= 1.44*log2(n) — GARANTIZADO\nBST: h aprox 2.5*log2(n) — PROMEDIO (datos aleatorios)",
         transform=ax3.transAxes, ha="right", fontsize=8.5, style="italic", color="#555",
         bbox=dict(boxstyle="round,pad=0.4", fc="#f9f9f9", ec="#ccc"))
estilo_ax(ax3)

plt.tight_layout()
out3 = os.path.join(output_dir, "grafica_altura.png")
fig3.savefig(out3, dpi=150, bbox_inches="tight")
print(f"Guardado: {out3}")
plt.close(fig3)

# ─────────────────────────────────────────────────────────────
# GRAFICA 4 — Resumen completo + tabla
# ─────────────────────────────────────────────────────────────
fig4 = plt.figure(figsize=(15, 10))
fig4.suptitle(f"Resumen Comparativo AVL vs BST — {TITULO}",
              fontsize=14, fontweight="bold", y=0.98)

gs = gridspec.GridSpec(2, 1, figure=fig4, hspace=0.6, height_ratios=[1.2, 1])
ax_combo = fig4.add_subplot(gs[0])
ax_tabla = fig4.add_subplot(gs[1])

for datos, color, label in [
    (avl_ins,  C_AVL_INS,  "AVL Insercion"),
    (avl_bus,  C_AVL_BUS,  "AVL Busqueda"),
    (avl_elim, C_AVL_ELIM, "AVL Eliminacion"),
]:
    ax_combo.plot(x_idx, datos, "o-", color=color, linewidth=2.3, markersize=9,
                  markerfacecolor="white", markeredgewidth=2, label=label, zorder=3)
    for xi, yi in zip(x_idx, datos):
        ax_combo.annotate(f"{int(yi):,}", xy=(xi, yi), xytext=(0, 9),
                          textcoords="offset points", ha="center", fontsize=8.5, color=color)

if has_bst:
    for datos, color, label in [
        (bst_ins,  C_BST_INS,  "BST Insercion"),
        (bst_bus,  C_BST_BUS,  "BST Busqueda"),
        (bst_elim, C_BST_ELIM, "BST Eliminacion"),
    ]:
        ax_combo.plot(x_idx, datos, "s--", color=color, linewidth=1.8, markersize=8,
                      markerfacecolor="white", markeredgewidth=1.5, label=label, zorder=2)

ax_combo.set_title("Comparacion de operaciones", fontsize=11, fontweight="bold")
ax_combo.set_xticks(x_idx); ax_combo.set_xticklabels(x_labels, fontsize=11)
ax_combo.set_xlabel("n", fontsize=10)
ax_combo.set_ylabel("Tiempo (ms)", fontsize=10)
ax_combo.set_ylim(bottom=0)
ax_combo.margins(x=0.12)
ax_combo.legend(fontsize=8.5, ncol=2)
estilo_ax(ax_combo)

# Tabla
ax_tabla.axis("off")
if has_bst:
    cabecera_t = ["n", "AVL Ins\n(ms)", "AVL Bus\n(ms)", "AVL Elm\n(ms)", "Alt\nAVL",
                  "BST Ins\n(ms)", "BST Bus\n(ms)", "BST Elm\n(ms)", "Alt\nBST", "log2(n)"]
    filas_t = [
        [label_n(n),
         f"{int(ai):,}", f"{int(ab):,}", f"{int(ae):,}", f"{int(ah)}",
         f"{int(bi):,}", f"{int(bb):,}", f"{int(be):,}", f"{int(bh)}",
         f"{lg:.2f}"]
        for n, ai, ab, ae, ah, bi, bb, be, bh, lg
        in zip(n_vals, avl_ins, avl_bus, avl_elim, avl_h,
               bst_ins, bst_bus, bst_elim, bst_h, h_log2)
    ]
else:
    cabecera_t = ["n", "Insercion\n(ms)", "Busqueda\n(ms)", "Eliminacion\n(ms)",
                  "Altura\nreal", "log2(n)\nteorico"]
    filas_t = [
        [label_n(n), f"{int(i):,}", f"{int(b):,}", f"{int(e):,}", f"{int(hr)}", f"{hl:.2f}"]
        for n, i, b, e, hr, hl in zip(n_vals, avl_ins, avl_bus, avl_elim, avl_h, h_log2)
    ]

tbl = ax_tabla.table(cellText=filas_t, colLabels=cabecera_t, cellLoc="center", loc="center")
tbl.auto_set_font_size(False)
tbl.set_fontsize(10)
tbl.scale(1.05, 2.1)

for (row, col), cell in tbl.get_celld().items():
    cell.set_edgecolor("#cccccc")
    if row == 0:
        cell.set_facecolor(C_AVL_INS)
        cell.set_text_props(color="white", fontweight="bold")
    else:
        cell.set_facecolor("#eef2ff" if row % 2 == 0 else "white")

out4 = os.path.join(output_dir, "grafica_tabla.png")
fig4.savefig(out4, dpi=150, bbox_inches="tight")
print(f"Guardado: {out4}")
plt.close(fig4)

print(f"\nTodas las graficas generadas en: {os.path.abspath(output_dir)}")

