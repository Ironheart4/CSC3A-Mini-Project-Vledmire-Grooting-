# CSC3A-Mini-Project-Vledmire-Grooting-
Graph-based desktop app simulating wildfire spread using terrain images. Regions are classified (grassland, forest, water, dry vegetation, barren) and fire costs assigned. Dijkstra's algorithm predicts likely fire paths, visualized interactively to aid disaster response and planning.

## Run (JavaFX)

Source files are under `WildfireSimulation/src` and use package `src`.

1. Compile with JavaFX modules configured in your environment (`javafx.controls`, `javafx.graphics`, `javafx.swing`).
2. Run `src.Main` (or `src.App`) as the main class.

---

## Graph Structure — Region Adjacency Graph (RAG)

### How the RAG is constructed

The project builds a **Region Adjacency Graph (RAG)** from the terrain image:

| RAG concept | This project |
|---|---|
| **Region / node** | One `blockSize x blockSize` pixel block of the image, classified into a `Terrain` type (Grassland, Forest, Water, Dry Vegetation, Barren) by `ImageClassifier.classifyBlock()`. |
| **Adjacency / edge** | A directed `Edge` from block A to block B exists when B is directly up/down/left/right of A in the image grid (4-connected spatial adjacency). |
| **Edge weight** | The fire-spread cost of the *target* block's terrain type. WATER cells receive infinite weight, making them impassable barriers. |

`GraphBuilder.build()` performs this construction in two passes:
1. **Node pass** — create one `Node` per block with its classified `Terrain`.
2. **Edge pass** — link each node to its 4-connected neighbours (the RAG adjacency step).

This is a *regular grid RAG* (uniform block size) rather than a superpixel RAG, but the structural principle is identical: nodes represent image regions and edges encode spatial adjacency.

### Dijkstra on the RAG

`WildfireDijkstra.computeSpreadFrom()` runs Dijkstra's algorithm on the RAG from an ignition cell. It finds the **minimum-cost fire-spread path** from the ignition node to every reachable node. Results are stored in:

- **Spread order** (`ArrayList<Node>`) — nodes sorted by settlement order (fire arrival order).
- **Distance map** (`HashMap<String,Double>`) — Dijkstra distance for each settled node.
- **Predecessor map / SPT** (`HashMap<String,String>`) — for each settled node, the node it was reached from. Drawing these links forms the **Shortest-Path Tree (SPT)** overlay.

### Relation to other graph techniques (per project brief)

| Technique | Relation |
|---|---|
| **RAG** | Core structure — grid-based RAG implemented in `GraphBuilder`. |
| **Pathfinding** | Dijkstra on the RAG, implemented in `WildfireDijkstra`. |
| **k-NN graphs** | Alternative RAG where each block is connected to its k most colour-similar blocks rather than spatial neighbours — a natural extension. |
| **MST** | Could be run on this RAG to find the cheapest spanning fire corridor; Dijkstra gives single-source shortest paths, which is more appropriate for spread simulation. |
| **Spectral graph theory** | The graph Laplacian of this RAG could drive spectral clustering/segmentation — a future extension. |
| **GNNs** | A Graph Neural Network could learn terrain-spread costs from historical fire data, replacing the hand-coded cost table. |

---

## UI Overlays

| Button | Effect |
|---|---|
| **Graph Overlay (RAG)** | Draws each RAG node as a coloured circle and each RAG edge as a line. Node colour: red = danger (Dry Vegetation), yellow = medium (other burnable), blue = impossible (Water). Boundary edges where adjacent terrain types differ are brightened (RAG boundary overlay). |
| **Spread Tree (SPT)** | After simulation, draws the Shortest-Path Tree in cyan-green — each settled node connected to its Dijkstra predecessor. |
| **Clear Overlay** | Removes all overlays (fire spread, ignition, graph, SPT). |

Grid lines (based on block size) are always shown on the classified image to make the RAG block structure visible.

---

## Custom Data Structures

- `ArrayList<T>` — custom resizable array used throughout.
- `HashMap<K,V>` — custom chained hash table (extends `AbstractMap`).
- `PriorityQueue<T>` — custom min-priority queue (linear scan) used as Dijkstra frontier.
