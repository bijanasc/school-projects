import java.io.EOFException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

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

public class Main {

	static HashSet <Node> toBeAdded = new HashSet<Node>();
	static HashSet <Node> subGraphed = new HashSet<Node>();

	
	public static void main(String[] args) throws PcapNativeException, NotOpenException {
		
		//silence Logger
				
		     Graph graph = new SingleGraph("");

				//open pcap
			    PcapHandle handle = Pcaps.openOffline("outside.tcpdump-small.pcap");
			    
			    // loop through 10000 packets
			    while (true) {
			      try {
			        Packet packet = handle.getNextPacketEx();

			        // If packet has IP addresses print it
			        if (packet.contains(IpV4Packet.class)){
			        	String src = packet.get(IpV4Packet.class).getHeader().getSrcAddr().getHostAddress();
			        	String dst = packet.get(IpV4Packet.class).getHeader().getDstAddr().getHostAddress();
			        	
			        	//int ttl = packet.get(IpV4Packet.class).getHeader().getTtlAsInt();
			        	//String proto = packet.get(IpV4Packet.class).getHeader().getProtocol().name();
			        	//int port = packet.get(IpV4Packet.class).getHeader().getProtocol().value();
			        	
			        	//System.out.println(src + "->" + dst + ", TTL:" + ttl + ", Prot:" + proto + ", Port:" + port);
			        	
			        	//check if src already exists in graph, if not, create it
			        	if (graph.getNode(src) == null) {
			        		
			        		graph.addNode(src).addAttribute("ui.label", src);;
			        		
			        	}
			        	//check if dest already exists in graph, if not, create it
			        	if (graph.getNode(dst) == null) {
			        		
			        		graph.addNode(dst).addAttribute("ui.label", dst);
			        		
			        	}
			        	//check if edge exists in graph (src-dest, or, dest-src), if not create it, set weight to 0, set direction to one-way
			        	Edge e = graph.getEdge(src + "-" + dst);
			        	
			        	if (e == null ) {
			        		
			        		e = graph.getEdge(dst + "-" + src);
			        		//check if src of packet is dest of edge, if so, set direction to both
			        		if (e != null) { e.setAttribute("isOneWay", 0); }
			        	}
			        	
			        	if (e == null) {
			        		
			        		e = graph.addEdge(src + "-" + dst, src, dst);
			        		e.addAttribute("weight", 0);
			        		e.addAttribute("isOneWay", 1);		        		
			        	}
		        				    		
			        	//increment edge
			        	double weight = e.getNumber("weight");
			        	weight++;
			    		e.setAttribute("weight", weight);
			        
			        }

			      } catch (TimeoutException e) {
			      } catch (EOFException e) {
			        break;
			      }
			    }
		       
			  boolean notFinishedRemoving = false;
			  //loop through graph edges and delete edges that are one-way
			  do {
				  
				notFinishedRemoving = false; 
			    for(Edge e: graph.getEachEdge()){
			    	if((e.getSourceNode().getId().equals("172.16.112.20") &&  e.getTargetNode().getId().equals("196.37.75.158")) || (e.getTargetNode().getId().equals("172.16.112.20") &&  e.getSourceNode().getId().equals("196.37.75.158")))
					{ e = e;  }
			    	double weight = e.getNumber("weight");
			    	e.setAttribute("ui.label", weight);
			    	
			    	if (e.getNumber("isOneWay") == 1){
			    		
			    		graph.removeEdge(e);
			    		notFinishedRemoving = true;
			    		break;
			    	}			    	
			    }
			  } while (notFinishedRemoving);
	    		//loop though graph nodes and delete nodes that have no edges
			    for (Node n: graph.getEachNode()) {
			    	
			    	if (n.getDegree() == 0) {
			    		
			    		graph.removeNode(n);			    		
			    	}			    	
			    }
			    
			    //graph.display(true);			    
				ArrayList <Graph> graphList = new ArrayList<Graph>();
      
			    
			   //Part 2
				int i = 0;
				
				while (subGraphed.size() != graph.getNodeCount()) {
					
					//pick subGraphed node based on subGraphed list
					Node start = null;
					
					for (Node n: graph.getEachNode()){
						
						if (!subGraphed.contains(n)){
							
							start = n;
							break;
						}												
					}					
					
					//run recursive method on start
					connectionCheck(start); 
					
					//
					//create new graph based on toBeAdded nodes
					Graph newGraph = new SingleGraph(i + "");
					
					//System.out.println(toBeAdded.size());
					
					for (Node n: toBeAdded) {
						
						newGraph.addNode(n.getId()).addAttribute("ui.label", n.getId());;
						
					}					
					
					for (Node n: toBeAdded) {
						
						for (Edge e: n.getEachLeavingEdge()) {
							
							if (newGraph.getEdge(e.getId()) == null) {
							Edge newEdge = newGraph.addEdge(e.getId(), e.getSourceNode().getId(), e.getTargetNode().getId());
							double weight = e.getNumber("weight");
							newEdge.addAttribute("weight", weight);
							}
						}
						
					}
					
					graphList.add(newGraph);
				    //newGraph.display(false);

					//create empty toBeAdded list
					toBeAdded.clear();
					i++;
				}
				
				//part 3
				//iterate over graphList
				//System.out.println(graphList.size());
				int c = 0;
				for (Graph g: graphList) {
					System.out.println("CC: " + c + ", " + g.getNodeCount() + " Nodes Total");
					System.out.println("-Without Maximum Spanning Tree");					
										
										
					
				    //add nodes in g into maxPQ
					
					    PriorityQueue<Node> maxPQ = new PriorityQueue<Node>(10, new NodeDegreeComparator());
					    
						maxPQ.addAll(g.getNodeSet());
														
						
					//part 4
					//dequeue top 3 from maxPQ and print to screen
					for (int j = 0; j < 3; j++){
						
						Node n = maxPQ.poll();
						System.out.println("  " + n.getId() + "," + n.getDegree());
						
					}
					
					
					
										
				
					//call maxtree method using passing in graph from graphList
					Graph tree = maxTree(g);
					//tree.display(true);
					
					
					double weight = 0;
					for (Edge e: tree.getEdgeSet()){
						
						weight += e.getNumber("weight");
											
					}
					System.out.println("-With Maximum Spanning Tree" + " (Weight:" + (int) weight + "):");					

					
				    //add nodes in maxtree into maxPQ
					
					    maxPQ = new PriorityQueue<Node>(10, new NodeDegreeComparator());
						maxPQ.addAll(tree.getNodeSet());														
						
					//part 4
					//dequeue top 3 from maxPQ and print to screen
					for (int j = 0; j < 3; j++){
						
						Node n = maxPQ.poll();
						System.out.println("  " + n.getId() + "," + n.getDegree());	
					}
					//connected component counter
					c++;
				}			    
	}

	public static void connectionCheck(Node n) {
		
		//if node in used list, return
		if (toBeAdded.contains(n)) {
			
			return;
		}
		
		//else, add to toBeAdded listed and subGraphed
		toBeAdded.add(n);
		subGraphed.add(n);
		
		//iterate over adjacencies
		//recursively call method on adjacency
		for (Edge e: n.getEachEdge()) {
			
			if (n == e.getSourceNode()){
				
				String id = e.getTargetNode().getId();
				if(id.equals("172.16.112.20") ||  id.equals("172.16.112.10") || id.equals("192.168.1.20") || id.equals("192.168.1.10") || id.equals("192.168.1.90"))
				{ e = e;  }
				connectionCheck(e.getTargetNode());
				
			}
			else if (n == e.getTargetNode()) {
				String id = e.getSourceNode().getId();
				if(id.equals("172.16.112.20") ||  id.equals("172.16.112.10") || id.equals("192.168.1.20") || id.equals("192.168.1.10") || id.equals("192.168.1.90"))
				{ e = e;  }
				connectionCheck(e.getSourceNode());
				
			}			
		}						
	}
	
	public static Graph maxTree(Graph g){
		
		//this method is almost implemented
		//return g;
		
		Graph result = new SingleGraph("result");
	    
        PriorityQueue<Edge> maxEdge= new PriorityQueue<Edge>(10, new EdgeWeightComparator());		
		
        maxEdge.addAll(g.getEdgeSet());
        
        while (maxEdge.size() > 0){
        Edge curEdge = maxEdge.poll();
        
        Node n1 = curEdge.getTargetNode();
        Node n2 = curEdge.getSourceNode();
        
        if (result.getNode(n1.getId()) == null || result.getNode(n2.getId()) == null) {
        	if(result.getNode(n1.getId()) == null){
        		
        		result.addNode(n1.getId()).addAttribute("ui.label", n1.getId());
        	}
        	
        	if(result.getNode(n2.getId()) == null){
        		
        		result.addNode(n2.getId()).addAttribute("ui.label", n2.getId());
        	}
        		
        	Edge e = result.addEdge(curEdge.getId(), n1.getId(), n2.getId());
        	e.addAttribute("ui.label",curEdge.getNumber("weight"));
        	e.addAttribute("weight", curEdge.getNumber("weight"));        	
        }
       }
		
		return result;
	}	
}


class NodeDegreeComparator implements Comparator<Node> {

	@Override
	public int compare(Node o1, Node o2) {
		// TODO Auto-generated method stub
		if (o2.getDegree() < o1.getDegree()) {
			
			return -1;
		}
		
		if (o1.getDegree() == o2.getDegree()){
			
			return 0;			
		}
		
		return 1;				
	}	
}

class EdgeWeightComparator implements Comparator<Edge> {

	@Override
	public int compare(Edge o1, Edge o2) {
		// TODO Auto-generated method stub
		if (o1.getNumber("weight") < o2.getNumber("weight")) {
			
			return 1;
		}
		if (o1.getNumber("weight") == o2.getNumber("weight")){
			
			return 0;
			
		}
		
		return -1;		
		
	}	
}
