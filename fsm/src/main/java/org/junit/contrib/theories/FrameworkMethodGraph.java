package org.junit.contrib.theories;

import static org.junit.contrib.theories.GraphTheories.TheoryVertex;
import static org.junit.contrib.theories.GraphTheories.TheoryEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.contrib.theories.util.DiscreteChooser;
import org.junit.runners.model.FrameworkMethod;

/**
 * @author Gabriel Ki <gabeki@apple.com>
 */
public class FrameworkMethodGraph {

    private DefaultDirectedWeightedGraph<FrameworkMethod, FrameworkMethodWeightedEdge> fsm;

    /**
     * Implement WeightedEdge.
     */
    private static class FrameworkMethodWeightedEdge extends DefaultWeightedEdge {

        public static final long serialVersionUID = 42L;

        private double weight = 0.0d;

        public FrameworkMethodWeightedEdge(double weight) {
            this.weight = weight;
        }

        public double weight() {
            return weight;
        }
    }

    private FrameworkMethodGraph() {
        fsm = new DefaultDirectedWeightedGraph<FrameworkMethod, FrameworkMethodWeightedEdge>(FrameworkMethodWeightedEdge.class);
    }

    public static FrameworkMethodGraph parseFrameworkMethodGraph(final List<FrameworkMethod> fms) {
        FrameworkMethodGraph graph = new FrameworkMethodGraph();
        for (FrameworkMethod fm : fms) {
            graph.add(fm);
        }
        return graph;
    }

    /**
     * Parse a FrameworkMethod and add it to the graph.
     *
     * @param newMethod
     */
    public void add(FrameworkMethod newMethod) {
        fsm.addVertex(newMethod);

        TheoryVertex vertexInfo = getVertexInfo(newMethod);
        if (vertexInfo.loopToSelf()) {
            fsm.addEdge(newMethod, newMethod, new FrameworkMethodWeightedEdge(vertexInfo.weight()));
        }

        for (FrameworkMethod fm : fsm.vertexSet()) {
            if (fm.equals(newMethod)) {
                continue;
            }
            fsm.addEdge(newMethod, fm, new FrameworkMethodWeightedEdge(getWeight(newMethod, fm)));
            fsm.addEdge(fm, newMethod, new FrameworkMethodWeightedEdge(getWeight(fm, newMethod)));
        }
    }

    private TheoryVertex getVertexInfo(FrameworkMethod fm) {
        TheoryVertex vertexInfo = fm.getAnnotation(TheoryVertex.class);
        return (vertexInfo == null) ? TheoryVertex.DEFAULTS : vertexInfo;
    }

    private double getWeight(final FrameworkMethod from, final FrameworkMethod to) {
        TheoryVertex vertexInfo = getVertexInfo(from);
        if (vertexInfo.connectTo().length == 0) {
            return TheoryEdge.DEFAULTS.weight();
        }

        for (TheoryEdge edgeInfo : vertexInfo.connectTo()) {
            if (edgeInfo.name().equals(to.getName())) {
                return edgeInfo.weight();
            }
        }
        return 0D;
    }

    /**
     * @return a list of vertexes that are indicated could be the start vertex.  If none
     * of the Theory is labeled as start vertex, then a random start will be picked.
     */
    private List<FrameworkMethod> possibleStartingVertexes() {
        List<FrameworkMethod> startingVertexes = new ArrayList<>();
        for (FrameworkMethod fm : fsm.vertexSet()) {
            TheoryVertex ann = fm.getAnnotation(TheoryVertex.class);
            if ((ann == null) ? TheoryVertex.DEFAULTS.isStart() : ann.isStart()) {
                startingVertexes.add(fm);
            }
        }

        if (startingVertexes.isEmpty()) {
            startingVertexes.addAll(fsm.vertexSet());
        }

        return startingVertexes;
    }


    /**
     * @return true if there is no vertex in the graph.  false otherwise.
     */
    public boolean isEmpty() {
        return fsm.vertexSet().isEmpty();
    }

    /**
     * This is really only to provide an iterator for simple for-loop style graph traversal.
     *
     * @param length set the length of the list.  If its less than or equal to zero, it will
     *               be in an infinite loop or traverse to one of the end node of a graph.
     * @return
     */
    public List<FrameworkMethod> asList(final int length) {
        List<FrameworkMethod> theList = new ArrayList<FrameworkMethod>(fsm.vertexSet()) {

            @Override
            public Iterator<FrameworkMethod> iterator() {
                return listIterator();
            }

            @Override
            public ListIterator<FrameworkMethod> listIterator() {
                return new ListIterator<FrameworkMethod>() {
                    private FrameworkMethod previous = null;
                    private HashMap<FrameworkMethod, DiscreteChooser<FrameworkMethodWeightedEdge>> edgeChoosers
                            = new HashMap<>();
                    private int size = 0;

                    @Override
                    public boolean hasNext() {
                        return !isEmpty() && (length <= 0 || size < length);
                    }

                    @Override
                    public FrameworkMethod next() {
                        if (previous == null) {
                            final List<FrameworkMethod> startingVertexes = possibleStartingVertexes();
                            previous = startingVertexes.get(new Random().nextInt(startingVertexes.size()));

                        } else {
                            if (!edgeChoosers.containsKey(previous)) {
                                DiscreteChooser<FrameworkMethodWeightedEdge> chooser = new DiscreteChooser<>();
                                for (FrameworkMethodWeightedEdge edge : fsm.outgoingEdgesOf(previous)) {
                                    chooser.add(edge, fsm.getEdgeWeight(edge));
                                }
                                edgeChoosers.put(previous, chooser);
                            }

                            previous = fsm.getEdgeTarget(edgeChoosers.get(previous).next());
                        }

                        size++;
                        return previous;
                    }

                    @Override
                    public boolean hasPrevious() {
                        return (previous != null);
                    }

                    @Override
                    public FrameworkMethod previous() {
                        return previous;
                    }

                    @Override
                    public int nextIndex() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public int previousIndex() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void set(FrameworkMethod frameworkMethod) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void add(FrameworkMethod frameworkMethod) {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };

        return theList;
    }

    public List<FrameworkMethod> asList() {
        return asList(0);
    }

    @Override
    public String toString() {
        return fsm.toString();
    }
}

