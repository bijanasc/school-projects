package datastructures;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import datastructures.IndexMaxPQ;


/**
 * This is the solution to CS310 project 1.
 * http://josephpcohen.com/teaching/cs310/project1/
 * Do not distribute this publicly.
 * 
 * Some sources of PCAP files:
 * https://www.ll.mit.edu/ideval/data/1999/training/week1/index.html
 * http://www.netresec.com/?page=ISTS
 * 
 * @author Joseph Paul Cohen 2015
 *
 */

public class NSA {
	
	
	public static void main(String[] args) throws PcapNativeException, NotOpenException {
		
		//Silence Logger
		Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.toLevel("ERROR"));
		
		
		//open pcap
		//String filename = args[0];
		String filename = "outside.tcpdump-small.pcap";
	    PcapHandle handle = Pcaps.openOffline(filename);
		

	    
	   	Graph graphStream = new SingleGraph("");
		graphStream.addAttribute("ui.stylesheet", "graph {text-mode: normal;}");
		
		//to store edges->count
		Map<String, Integer> edgesSeen = new HashMap<String, Integer>();
		
	    // loop through packets
	    //for (int i = 0; i < 1000; i++)  // uncomment to limit packets
	    while (true)
	    {
	      try {
	        Packet packet = handle.getNextPacketEx();

	        if (packet.contains(IpV4Packet.class)){
	        	
	        	String src = packet.get(IpV4Packet.class).getHeader().getSrcAddr().getHostAddress();
	        	String dst = packet.get(IpV4Packet.class).getHeader().getDstAddr().getHostAddress();
	        	
	        	//add to existing count or insert
	        	Integer num = edgesSeen.get(src + "-" + dst);
	        	if (num != null)
	        		edgesSeen.put(src + "-" + dst, num+1);
	        	else
	        		edgesSeen.put(src + "-" + dst, 1);
	        }

	      } catch (TimeoutException e) {
	      } catch (EOFException e) {
	        System.out.println("Reached end of file.");
	        break;
	      } catch (Exception e){
	    	  System.out.println("Skipping broken packet");
	      }
	    }
	    
	    
	    for (String edge : edgesSeen.keySet()) {

	    	String[] es = edge.split("-");
	    	String src = es[0];
        	String dst = es[1];
        	Integer src2dst = edgesSeen.get(src + "-" + dst);
        	Integer dst2src = edgesSeen.get(dst + "-" + src);
	    	
        	// if there is bidirectional communication add nodes and edges
    		if (src2dst != null && dst2src != null){
    			
	    		if (graphStream.getNode(src) == null){
		    		Node n = graphStream.addNode(src);
		    		n.addAttribute("ui.label", src);
	    		}
	    		
	    		if (graphStream.getNode(dst) == null){
		    		Node n = graphStream.addNode(dst);
		    		n.addAttribute("ui.label", dst);
	    		}
    			
	    		if (!graphStream.getNode(src).hasEdgeBetween(dst)){
	    			Edge e = graphStream.addEdge(src + "-" + dst, src, dst);
	    			e.addAttribute("weight", src2dst + dst2src);
	    			e.addAttribute("ui.label", src2dst + dst2src);
	    		}
    		}
	    }
	    

	    //show the tree
		graphStream.display(true);
		
		
		List<List<Node>> ccs = labelConnectedComponents(graphStream);

		// sort CCs by size
		Collections.sort(ccs, new Comparator<List<Node>>() {
			
			@Override
			public int compare(List<Node> o1, List<Node> o2) {

				return ((Integer)o2.size()).compareTo(o1.size());
			}
		});
		
		
		// uncomment this to remove all but the large CC
/*		for (int i = 1; i < ccs.size(); i++){
			List<Node> cc = ccs.get(i);
			for (Node n : cc)
				graphStream.removeNode(n);
		}
*/
		
		
		// main loop for printing out all CCs
		for (int i = 0; i < ccs.size(); i++){
			
			List<Node> cc = ccs.get(i);
			
			System.out.println("CC:" + i + ", " + cc.size() + " Nodes Total");
			System.out.println("=Without Maximum Spanning Tree:");
			
			// sort nodes by degree
			Collections.sort(cc, new Comparator<Node>() {
			
				@Override
				public int compare(Node o1, Node o2) {

					return ((Integer)o2.getDegree()).compareTo(o1.getDegree());
				}
			});
			
			for (int j = 0; j < Math.min(cc.size(), 3); j++){
				
				System.out.println("  " + cc.get(j) + "," + cc.get(j).getDegree());
			}
			
			Set<Edge> mstEdges = computeMaximumSpanningTree(graphStream, cc);
			
			int weight = 0;
			for(Edge e : mstEdges){
				weight += (Integer)e.getAttribute("weight");
			}
			
			System.out.println("=With Maximum Spanning Tree (Weight:" + weight + "):");
			
			// sort nodes by degree in MST
			Collections.sort(cc, new Comparator<Node>() {
			
				@Override
				public int compare(Node o1, Node o2) {

					return getDegreeInMST(o2,mstEdges).compareTo(getDegreeInMST(o1,mstEdges));
				}
			});
			
			for (int j = 0; j < Math.min(cc.size(), 3); j++){
				
				System.out.println("  " + cc.get(j) + "," + getDegreeInMST(cc.get(j),mstEdges));
			
			}
		}
	}
	
	private static Integer getDegreeInMST(Node o, Set<Edge> mstEdges){
		
		int count = 0;
		for(Edge e : o.getEachEdge())
			if (mstEdges.contains(e))
				count++;
		
		return count;
	}
	
	/**
	 * Perform a DFS to find all connected components in graph
	 * 
	 * @param graphStream
	 * @return
	 */
	private static List<List<Node>> labelConnectedComponents(Graph graphStream) {
		
		ArrayList<List<Node>> ccs = new ArrayList<List<Node>>();
		
		Set<Node> s = new HashSet<Node>(graphStream.getNodeSet());
		
		while (!s.isEmpty()){
			Set<Node> currentCC = new HashSet<Node>();
			
			recurse(s.iterator().next(), currentCC);
			
			ccs.add(new ArrayList<Node>(currentCC));
			s.removeAll(currentCC);
		}
		return ccs;
	}
	
	//if not already seen, loop through each edge
	private static void recurse(Node n, Set<Node> currentCC) {
		
		if (currentCC.contains(n)) return;
		
		currentCC.add(n);
		
		for (Edge e : n.getEachEdge()){
			
			recurse(e.getOpposite(n), currentCC);
		}
	}

	static class MSTNode implements Comparable<MSTNode>{
		
		Integer cost;
		Edge edge;
		Node node;
		
		public MSTNode(Integer key, Edge edge, Node node) {
			super();
			this.cost = key;
			this.edge = edge;
			this.node = node;
		}
		
		@Override
		public boolean equals(Object obj) {

			if (obj instanceof MSTNode)
				return node.equals(((MSTNode)obj).node);
			else 
				return false;
		}

		@Override
		public int compareTo(MSTNode o) {

			return cost.compareTo(o.cost);
		}
	}
	
	
	private static Set<Edge> computeMaximumSpanningTree(Graph graphStream, List<Node> cc) {
		
		Set<Edge> mstEdges = new HashSet<Edge>();
		Set<Node> S = new HashSet<Node>();
		IndexMaxPQ<MSTNode> pq = new IndexMaxPQ<MSTNode>(cc.size());
		Map<Node,Integer> pqLookup = new HashMap<Node,Integer>();
		
		// create lookup for nodes
		for (int i  = 0; i < cc.size(); i++)
			pqLookup.put(cc.get(i), i);
		
		Node s = cc.get(0);
		pq.insert(0, new MSTNode(0, null, s));
		
		for (int i  = 1 ; i < cc.size(); i++)
			pq.insert(i,new MSTNode(Integer.MIN_VALUE, null, cc.get(i)));
			
		
		while (pq.size() > 0){
			
			MSTNode mstn = pq.maxKey();
			pq.delMax();
			mstEdges.add(mstn.edge);
			S.add(mstn.node);
			
			// color edges and nodes in graph
			if (mstn.edge != null) mstn.edge.setAttribute("ui.style", "size:5px; fill-color: rgb(0,100,255);");
			mstn.node.setAttribute("ui.style", "fill-color: rgb(0,100,255);");
			
			for (Edge e : mstn.node.getEachEdge()){
				
				Node othernode = e.getOpposite(mstn.node);
				
				// if the edge goes outside of the set s
				if (!S.contains(othernode)){
					
					Integer existingCost = pq.keyOf(pqLookup.get(othernode)).cost;
					Integer cost = e.getAttribute("weight");
					
					if (existingCost < cost){

						MSTNode mstn2 = new MSTNode(cost, e, othernode);
						pq.changeKey(pqLookup.get(othernode), mstn2);
					}
				}
			}
		}
		
		// patch because we added a null element for the first node
		mstEdges.remove(null);
		return mstEdges;
	}
}