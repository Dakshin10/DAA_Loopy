# 🎮 Slitherlink: Human vs CPU

<div align="center">

**A competitive puzzle game powered by Graph Theory and Greedy Algorithms**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/GUI-Swing-blue.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![DAA](https://img.shields.io/badge/Course-Design%20%26%20Analysis%20of%20Algorithms-purple.svg)](.)

*Connect the dots, close the loop, outsmart the CPU!*

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Game Rules](#-game-rules)
- [Technical Architecture](#-technical-architecture)
- [Algorithms Used](#-algorithms-used)
- [Installation](#-installation)
- [Usage](#-usage)
- [Project Structure](#-project-structure)
- [Implementation Details](#-implementation-details)
- [Screenshots](#-screenshots)
- [Contributing](#-contributing)

---

## 🎯 Overview

**Slitherlink** is a logic puzzle game where players compete against an intelligent CPU to create a single closed loop on a grid of dots. This implementation showcases advanced algorithmic concepts including:

- **Graph Theory** (Planar Graphs, Adjacency Lists, Connectivity)
- **Greedy Algorithms** (Constraint Satisfaction, Local Optimization)
- **Disjoint Set Union** (Union-Find for Loop Validation)
- **Geometric Algorithms** (Point-to-Segment Distance Calculation)
- **Heuristic Search** (Prioritized Edge Selection)

The game features a beautiful dark-themed UI with smooth animations, color-coded player moves, and real-time validation feedback.

---

## ✨ Features

### 🎨 **Premium UI/UX**
- **Dark Theme Interface** with vibrant yellow (Human) and electric blue (CPU) color coding
- **Smooth Animations** with glow effects on active edges
- **Perfect Click Detection** using geometric distance algorithms (no dead zones!)
- **Real-time Visual Feedback** for rule violations and satisfied clues
- **Hover Effects** showing which edge you're about to interact with

### 🤖 **Intelligent CPU Opponent**
- **Greedy Logic Deduction** for instant forced moves
- **Constraint Propagation** across cells and nodes
- **Heuristic Guessing** when logical deductions are exhausted
- **Fail-Fast Validation** to avoid illegal states

### 🎲 **Procedural Puzzle Generation**
- **Random Walk Algorithm** creates guaranteed solvable puzzles
- **Configurable Difficulty** through clue obfuscation
- **Always Valid** - every generated puzzle has exactly one solution

### 🔍 **Advanced Validation**
- **Degree Checking** ensures no T-junctions (nodes must have degree 0 or 2)
- **Cell Constraint Validation** prevents over-filling cells
- **Single Loop Detection** using Disjoint Set Union algorithm
- **Partial State Validation** during gameplay

### 🎮 **Gameplay Features**
- **Turn-Based Competition** between Human and CPU
- **Undo Support** (right-click to mark edges as inactive)
- **CPU Help Mode** - let the CPU make a move for you
- **Real-time Graph State Logging** for debugging and analysis

---

## 📐 Game Rules

### Objective
Create a **single closed loop** by connecting dots with line segments.

### Constraints
1. **Clue Numbers (0-3)**: Each cell with a number must have exactly that many edges as part of the loop
2. **No T-Junctions**: Each dot can have 0 or 2 connected edges (never 1 or 3+)
3. **Single Loop**: All edges must form exactly one connected loop (no separate islands)
4. **No Crossings**: Edges cannot cross each other (planar graph property)

### Controls
- **Left Click**: Toggle edge ACTIVE (add to loop)
- **Right Click**: Toggle edge INACTIVE (mark as excluded)
- **Click Again**: Reset edge to UNKNOWN

### Winning
The game is won when:
- ✅ All clues are satisfied
- ✅ All nodes have degree 0 or 2
- ✅ All active edges form a single connected loop

---

## 🏗️ Technical Architecture

### Graph Representation

The game board is modeled as a **Planar Graph**:

```
Nodes (Vertices)  →  Dot intersections at grid points (i, j)
Edges             →  Horizontal/Vertical segments connecting dots
Cells (Faces)     →  Square regions containing clue numbers
```

### Data Structures

| Class | Purpose | Key Data Structure |
|-------|---------|-------------------|
| `Node.java` | Represents grid intersections | Adjacency List (`List<Edge>`) |
| `Edge.java` | Represents line segments | Endpoints (`Node a, b`), Status, Owner |
| `Cell.java` | Represents clue regions | Array of 4 surrounding edges |
| `Board.java` | Manages game state | 2D arrays of nodes, edges, cells |

### Edge Status States

```java
UNKNOWN  → Not yet decided (gray, invisible)
ACTIVE   → Part of the loop (yellow/blue, glowing)
INACTIVE → Excluded from loop (white X mark)
```

---

## 🧮 Algorithms Used

### 1. **Greedy Constraint Satisfaction** (`Greedy.java`)

**Cell-Clue Logic:**
- **Saturation**: If active edges = clue, mark remaining as INACTIVE
- **Requirement**: If active + unknown = clue, mark all unknown as ACTIVE

**Node-Degree Logic:**
- **Extension**: If node has 1 active + 1 unknown, activate the unknown
- **Termination**: If node has 2 active, mark all unknown as INACTIVE

**Complexity:** O(C + V) per pass, where C = cells, V = vertices

### 2. **Disjoint Set Union (DSU)** (`Validator.java`)

Used for **single loop validation**:

```pseudo
function validateFinal():
    dsu = new DisjointSetUnion(totalNodes)
    
    for each activeEdge:
        dsu.union(edge.nodeA, edge.nodeB)
    
    rootSets = countDistinctRoots(activeNodes)
    
    return rootSets.size() == 1  // Exactly one connected component
```

**Complexity:** O(E · α(V)) ≈ O(E) with path compression

### 3. **Geometric Hit Detection** (`SlitherlinkApp.java`)

**Point-to-Segment Distance:**

```pseudo
function findEdgeAt(mouseX, mouseY):
    closestEdge = null
    minDist = 45.0  // Snap threshold
    
    for each edge:
        dist = distanceToSegment(mouse, edge)
        if dist < minDist:
            minDist = dist
            closestEdge = edge
    
    return closestEdge
```

**Complexity:** O(E) linear search with early termination

### 4. **Procedural Puzzle Generation** (`PuzzleGenerator.java`)

**Random Walk Algorithm:**

```pseudo
function generate(rows, cols):
    loop = []
    currentNode = randomStartNode()
    
    while !loopClosed:
        nextEdge = randomUnvisitedNeighbor(currentNode)
        loop.add(nextEdge)
        currentNode = nextEdge.otherNode(currentNode)
    
    // Derive clues from generated loop
    for each cell:
        cell.clue = countEdgesInLoop(cell)
    
    // Obfuscate by removing some clues
    removeRandomClues(30%)
```

### 5. **CPU Heuristic Search** (`GameController.java`)

**Decision Making:**

```pseudo
function executeCpuMove():
    // 1. Try logical deductions
    if Greedy.applyAllDeductions():
        return true
    
    // 2. Heuristic guessing
    for each unknownEdge:
        // Hypothesis: what if this is ACTIVE?
        tempActivate(edge)
        
        if Validator.isPartiallyValid():
            edge.setStatus(ACTIVE, CPU)
            return true
        else:
            // Contradiction → must be INACTIVE
            edge.setStatus(INACTIVE, CPU)
            return true
```

---

## 🚀 Installation

### Prerequisites

- **Java Development Kit (JDK) 17+**
- **Any Java IDE** (IntelliJ IDEA, Eclipse, VS Code) or command line

### Steps

1. **Clone or Download** the repository:
   ```bash
   git clone <repository-url>
   cd DAA_Loopy_Phase1
   ```

2. **Compile** all Java files:
   ```bash
   javac *.java
   ```

3. **Run** the application:
   ```bash
   java SlitherlinkApp
   ```

---

## 🎮 Usage

### Starting a Game

1. Launch the application
2. A 5×5 puzzle is automatically generated
3. **Your turn** (yellow indicator) - click edges to add them to the loop
4. **CPU turn** (blue indicator) - CPU automatically responds after your move

### Controls

| Action | Control |
|--------|---------|
| Add edge to loop | Left-click on edge |
| Mark edge as excluded | Right-click on edge |
| Reset edge | Click again on active/inactive edge |
| Get CPU help | Click "CPU HELP" button |
| Start new puzzle | Click "NEW GAME" button |

### Visual Feedback

| Color | Meaning |
|-------|---------|
| 🟡 **Yellow** | Your moves (Human player) |
| 🔵 **Blue** | CPU moves |
| 🟢 **Green** | Clue satisfied correctly |
| 🔴 **Red** | Clue violated (too many edges) |
| ⚪ **Gray** | Clue not yet satisfied |

---

## 📁 Project Structure

```
DAA_Loopy_Phase1/
│
├── SlitherlinkApp.java      # Main GUI application (Swing)
├── GameController.java      # Game logic orchestrator
├── Board.java               # Board state management
├── Node.java                # Graph vertex representation
├── Edge.java                # Graph edge representation
├── Cell.java                # Clue cell representation
├── Greedy.java              # Greedy constraint satisfaction
├── Validator.java           # Win condition validation (DSU)
├── PuzzleGenerator.java     # Procedural puzzle creation
├── EdgeSorter.java          # CPU edge prioritization
├── LoopReporter.java        # Victory reporting
├── PlayerType.java          # Enum: HUMAN, CPU
├── EdgeStatus.java          # Enum: ACTIVE, INACTIVE, UNKNOWN
├── DisjointSet.class        # Union-Find data structure
├── project_workflow.txt     # Technical documentation
└── README.md                # This file
```

---

## 🔬 Implementation Details

### Perfect Click Detection

The game uses **geometric distance calculation** to find the closest edge to any mouse click:

- **Horizontal edges**: Distance from point to horizontal line segment
- **Vertical edges**: Distance from point to vertical line segment
- **Snap threshold**: 45 pixels (eliminates dead zones between cells)

This ensures players can click anywhere near an edge and the correct edge will be selected.

### CPU Intelligence

The CPU uses a **two-phase approach**:

1. **Logical Deduction Phase**: Apply greedy rules until no forced moves remain
2. **Heuristic Guessing Phase**: Test hypotheses and use contradiction detection

This makes the CPU feel intelligent while maintaining fast response times.

### Graph State Logging

The application logs detailed graph state after each move:

- **Edge Status**: All edges with their current state
- **Node Topology**: Degree of each node
- **Validation Errors**: Any rule violations detected

This is invaluable for debugging and understanding the algorithm behavior.

---

## 📸 Screenshots

### Main Game Interface
*Dark-themed UI with 5×5 puzzle grid, color-coded player moves, and real-time clue validation*

### CPU Thinking
*Electric blue indicator shows CPU is calculating its next move using greedy algorithms*

### Victory State
*Green "LOOP COMPLETE" message when the puzzle is solved with a single connected loop*

---

## 🤝 Contributing

Contributions are welcome! Here are some ideas:

### Potential Enhancements
- [ ] Add difficulty levels (Easy, Medium, Hard)
- [ ] Implement larger board sizes (7×7, 10×10)
- [ ] Add move history and replay functionality
- [ ] Create a hint system showing next logical move
- [ ] Add sound effects and animations
- [ ] Implement save/load game state
- [ ] Add multiplayer mode (Human vs Human)
- [ ] Create a puzzle solver with step-by-step explanation

### How to Contribute
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📚 References

### Algorithms & Data Structures
- **Greedy Algorithms**: Introduction to Algorithms (CLRS), Chapter 16
- **Disjoint Set Union**: Competitive Programming 3, Section 2.4.2
- **Graph Theory**: Graph Theory with Applications (Bondy & Murty)

### Slitherlink Puzzle
- [Nikoli Puzzles - Slitherlink](https://www.nikoli.co.jp/en/puzzles/slitherlink/)
- [Wikipedia - Slitherlink](https://en.wikipedia.org/wiki/Slitherlink)

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👨‍💻 Author

**DAA Project - Phase 1**  
*Design & Analysis of Algorithms Course*  
Semester 4, CSE

---

## 🙏 Acknowledgments

- **Nikoli** for creating the original Slitherlink puzzle
- **Java Swing** for the GUI framework
- **Graph Theory** for providing the mathematical foundation
- **Greedy Algorithms** for making the CPU intelligent and fast

---

<div align="center">

**Made with ❤️ and lots of ☕**

*If you found this project helpful, please consider giving it a ⭐!*

</div>
