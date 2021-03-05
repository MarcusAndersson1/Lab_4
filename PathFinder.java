
import java.util.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * This is a class that can find paths in a given graph.
 * 
 * There are several methods for finding paths, 
 * and they all return a PathFinder.Result object.
 */
public class PathFinder<Node> {

    private DirectedGraph<Node> graph;
    private long startTimeMillis;

    /**
     * Creates a new pathfinder for the given graph.
     * @param graph  the graph to search
     */
    public PathFinder(DirectedGraph<Node> graph) {
        this.graph = graph;
    }

    /**
     * The main search method, taking the search algorithm as input.
     * @param  algorithm  "random", "ucs" or "astar"
     * @param  start  the start node
     * @param  goal   the goal node
     */
    public Result search(String algorithm, Node start, Node goal) {
        startTimeMillis = System.currentTimeMillis();
        switch (algorithm) {
        case "random": return searchRandom(start, goal);
        case "ucs":    return searchUCS(start, goal);
        case "astar":  return searchAstar(start, goal);
        }
        throw new IllegalArgumentException("Unknown search algorithm: " + algorithm);
    }

    /**
     * Perform a random walk in the graph, hoping to reach the goal.
     * Warning: this method will give up of the random walk
     * reaches a dead end or after one million iterations.
     * So a negative result does not mean there is no path.
     * @param start  the start node
     * @param goal   the goal node
     */
    public Result searchRandom(Node start, Node goal) {
        int iterations = 0;
        LinkedList<Node> path = new LinkedList<>();
        double cost = 0;
        Random random = new Random();

        Node current = start;
        while (iterations < 1e6) {
            iterations++;
            path.add(current);
            if (current.equals(goal))
                return new Result(true, start, current, cost, path, iterations);

            List<DirectedEdge<Node>> neighbours = graph.outgoingEdges(start);
            if (neighbours.size() == 0)
                break;

            DirectedEdge<Node> edge = neighbours.get(random.nextInt(neighbours.size()));
            cost += edge.weight();
            current = edge.to();
        }
        return new Result(false, start, goal, -1, null, iterations);
    }

    /**
     * Run the Uniform Cost Search algorithm for finding the shortest path.
     * @param start  the start node
     * @param goal   the goal node
     */
    public Result searchUCS(Node start, Node goal) {
        int iterations = 0;
        Queue<PQEntry> pqueue = new PriorityQueue<>(Comparator.comparingDouble(entry -> entry.costToHere));
        Set<Node> visitedNodes = new HashSet<>();
        /******************************
         * TODO: Task 1a+c            *
         * Change below this comment  *
         ******************************/
        pqueue.add(new PQEntry(start, 0 , null));
        while (!pqueue.isEmpty()){
            iterations++;
            PQEntry entry = pqueue.remove();
            if(!visitedNodes.contains(entry.node)){
            if(entry.node.equals(goal)){
                return new Result(true, start, goal, entry.costToHere, extractPath(entry), iterations);
            }
            for(DirectedEdge<Node> edge: graph.outgoingEdges(entry.node)){

                    visitedNodes.add(edge.from());
                    double costToNext = entry.costToHere + edge.weight();
                    pqueue.add(new PQEntry(edge.to(), costToNext, entry));
                }
            visitedNodes.add(entry.node);
            }
        }
        return new Result(false, start, goal, -1, null, iterations);
    }
    
    /**
     * Run the A* algorithm for finding the shortest path.
     * @param start  the start node
     * @param goal   the goal node
     */
    public Result searchAstar(Node start, Node goal) {
        int iterations = 0;
        Queue<PQEntry> pqueue = new PriorityQueue<>(Comparator.comparingDouble(entry -> entry.costToHere));
        Set<Node> visitedNodes = new HashSet<>();

        Map<Node,Node> cameFrom = new HashMap();

        pqueue.add(new PQEntry(start, 0 , null));

        Map<Node, Double> gScore =  new HashMap<>();
        gScore.put(start, 0.0);

        Map<Node, Double> fScore =  new HashMap<>();
        fScore.put(start, graph.guessCost(start, goal));


        while (!pqueue.isEmpty()){
            iterations++;
            PQEntry entry = pqueue.peek();

            if(entry.node.equals(goal)){
                return new Result(true, start, goal, entry.costToHere, extractPath(entry), iterations);
            }

            pqueue.remove();

           if(!visitedNodes.contains(entry.node)){
                for(DirectedEdge<Node> edge: graph.outgoingEdges(entry.node)){
                    visitedNodes.add(edge.from());
                    gScore.put(edge.to(), entry.costToHere);
                   double tentativeGScore = gScore.get(entry.node) + edge.weight();
                //   System.out.print(tentativeGScore + " + ");
                 //   System.out.print(gScore.get(edge.to()));
                //    System.out.println();
                    double costToNext = entry.costToHere + edge.weight();
                  if(tentativeGScore < gScore.get(edge.to())) {
                      cameFrom.put(edge.to(), entry.node);
                      System.out.println("hej");
                      gScore.put(edge.to(), tentativeGScore);
                      fScore.put(edge.to(), gScore.get(edge.to()) + graph.guessCost(entry.node, edge.to()));
                      if (!visitedNodes.contains(edge.to())){
                          visitedNodes.add(edge.to());
                          pqueue.add(new PQEntry(edge.to(), costToNext, entry));
                      }
                  }else {
                      pqueue.add(new PQEntry(edge.to(), costToNext, entry));
                  }

                }
                visitedNodes.add(entry.node);
            }
        }
        return new Result(false, start, goal, -1, null, iterations);
    }

    /**
     * Extract the final path from start to goal, from the final priority queue entry.
     * @param entry  the final priority queue entry
     * @return the path from start to goal as a list of nodes
     */
    private List<Node> extractPath(PQEntry entry) {
        LinkedList<Node> path = new LinkedList<>();
        /******************************
         * TODO: Task 1b              *
         * Change below this comment  *
         ******************************/
        Stack<PQEntry> s = new Stack();
        s.push(entry);
        while(entry.backPointer!=null) {
            s.push(entry.backPointer);
            entry = entry.backPointer;
        }
        while (!s.isEmpty()){
            path.add(s.pop().node);
        }
        return path;
    }

    /**
     * Entries to put in the priority queues in {@code searchUCS} and {@code searchAstar}.
     */
    private class PQEntry {
        public final Node node;
        public final double costToHere;
        public final PQEntry backPointer;
        /***********************************
         * TODO: Task 3                    *
         * Change below this comment,      *
         * for example, to add new fields. *
         **********************************/

        PQEntry(Node n, double c, PQEntry bp) {
            node = n;
            costToHere = c;
            backPointer = bp;
        }
    }

    /**
     * The internal class for search results.
     */
    public class Result {
        public final boolean success;
        public final Node start;
        public final Node goal;
        public final double cost;
        public final List<Node> path;
        public final int iterations;
        public final double elapsedTime;

        public Result(boolean success, Node start, Node goal, double cost, List<Node> path, int iterations) {
            this.success = success;
            this.start = start;
            this.goal = goal;
            this.cost = cost;
            this.path = path;
            this.iterations = iterations;
            this.elapsedTime = (System.currentTimeMillis() - startTimeMillis) / 1000.0;
        }

        @Override
        public String toString() {
            StringWriter buffer = new StringWriter();
            PrintWriter w = new PrintWriter(buffer);
            if (iterations <= 0)
                w.println("ERROR: You have to iterate over at least the starting node!");
            w.println("Loop iterations: " + iterations);
            w.println("Elapsed time: " + elapsedTime);
            if (success) {
                w.println("Total cost from " + start + " to " + goal + ": " + cost);
                int len = path.size();
                if (len == 0)
                    w.println("WARNING: you have not implemented extractPath!");
                else {
                    w.println("Total path length: " + (len - 1));
                    Function<List<Node>, String> joinPath = path ->
                        path.stream().map(Node::toString).collect(Collectors.joining(" -> "));
                    if (len < 10)
                        w.println("Path: " + joinPath.apply(path));
                    else
                        w.println("Path: " + joinPath.apply(path.subList(0, 5)) + " -> ... -> " + joinPath.apply(path.subList(len-5, len)));
                }
            } else
                w.println("No path found from " + start + " to " + goal);
            return buffer.toString();
        }
    }

}
