


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