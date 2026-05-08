


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