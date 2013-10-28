package org.yarquen.trust;

import java.util.Collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.neo4j.graphdb.index.IndexHits;

/**
 * 
 * @author Oscar Cornejo Olivares
 * @date 23/10/2013
 * 
 */

public class Trust {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Trust.class);
	
	RestAPI api = new RestAPIFacade("http://localhost:7474/db/data/");
	Index<Node> people;
	RelationshipIndex trustRel;
	
	public Trust(){
		createIndexes();
	}
	
	public void createIndexes(){

		IndexManager index = api.index();
		people = index.forNodes("people");
		trustRel = index.forRelationships("trust");

	}
	
	public void createUser(String accountID) {
		// TODO Auto-generated method stub
		LOGGER.info("registering account {} in Neo4j DB", accountID);
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("accountID", accountID);
		Node node = api.createNode(props);
		System.out.println(node.getProperty("accountID"));

		people.add(node,"accountID",node.getProperty("accountID"));
	}
	
	public Node getNode(String accountID){
		
		IndexHits<Node> hit1 = people.get("accountID",accountID);
		Node node = hit1.getSingle();
		return node;
	}
	
	public boolean checkAdjacency(Node start, Node end){  
		
		RelationshipType rel = DynamicRelationshipType.withName("TRUSTS");
		
		if(rel==null) return false;
		
		for(Relationship r :start.getRelationships(Direction.OUTGOING,rel)) {
			LOGGER.info("search of node {}",start);
			if(r.getOtherNode(start).equals(end)){
				LOGGER.info("founding friends called {}",end);
				return true;
			}
		}
		return false;
	}
	
	public int getAdjacencyTrust(Node start, Node end){
		RelationshipType rel = DynamicRelationshipType.withName("TRUSTS");
		
		for(Relationship r :start.getRelationships(Direction.OUTGOING,rel)) {
			if(r.getOtherNode(start).equals(end)) return (int) r.getProperty("value");
		}
		return 0;
	}
	
	public double getTrust(Node source, Node sink)
	{
		double total = 0;

		if(checkAdjacency(source,sink))
			total = (double) getAdjacencyTrust(source,sink);
		else
			total = tidalTrust(source,sink);
		return total;
	}
	
	public double tidalTrust(Node source, Node sink){

		Queue<Node> qe=new LinkedList<Node>();
		Queue<Node> tempQe=new LinkedList<Node>();
		HashMap<Node, Integer> color = new HashMap<Node, Integer>();

		int depth = 1;
		int max=1;
		//double maxdepth = Double.POSITIVE_INFINITY;
		int maxdepth = 4;
		Node currentNode;
		MultiMap ddepth = new MultiMap();
		ChildrenMap children = new ChildrenMap();
		HashMap<String, Double> cachedRating = new HashMap<String, Double>();

		qe.add(source);

		while(!qe.isEmpty() && depth <= maxdepth){

			currentNode = qe.poll();

			ddepth.put(depth, currentNode);

			if(checkAdjacency(currentNode, sink)){

				cachedRating.put(currentNode.getProperty("accountID")+"_"+sink.getProperty("accountID"), (double) getAdjacencyTrust(currentNode,sink));
				//maxdepth = depth;
				//se agrega a sink como adyacente al currentNode
				children.put(currentNode, sink);
			}
			else{

				QueryEngine engine=new RestCypherQueryEngine(api);  
				QueryResult<Map<String,Object>> result= engine.query("START n=node:people('accountID:"+currentNode.getProperty("accountID")+"') match n-[TRUSTS]->m RETURN m", Collections.EMPTY_MAP);
				Iterator<Map<String, Object>> iterator=result.iterator(); 

				while(iterator.hasNext()) {  
					Map<String,Object> row= iterator.next();  

					if(color.get(row.get("m"))==null){
						color.put((Node) row.get("m"), 1);
						tempQe.add((Node) row.get("m"));
						children.put(currentNode, (Node) row.get("m"));
					}
					else if(tempQe.contains((Node) row.get("m"))){
						children.put(currentNode, (Node) row.get("m"));
					}
				}
			}
			if(qe.isEmpty()){

				qe = copyQueue(tempQe);
				depth+=1;
				tempQe.clear();
			}
		}

		depth -= 1;
		double ratingTemp;
		while(depth>0){
			while(!ddepth.get(depth).isEmpty()){
				double numerator = 0;
				double denominator = 0;
				currentNode = ddepth.get(depth).remove(0);
				
				if(children.get(currentNode) != null){
					
					Iterator<Node> iterator = children.get(currentNode).iterator();

					while (iterator.hasNext()) { //foreach children de currentNode
						Node childNode= iterator.next();

						if(cachedRating.get(childNode.getProperty("accountID")+"_"+sink.getProperty("accountID"))!=null){

							ratingTemp = cachedRating.get(childNode.getProperty("accountID")+"_"+sink.getProperty("accountID"));

						}
						else
							ratingTemp = -9999;


						if(getAdjacencyTrust(currentNode,childNode)>=max && ratingTemp > -9999){

							numerator =numerator + getAdjacencyTrust(currentNode,childNode) * ratingTemp;
							denominator= denominator + getAdjacencyTrust(currentNode,childNode);
						}
					}
				}
				if(denominator > 0){
					cachedRating.put(currentNode.getProperty("accountID")+"_"+sink.getProperty("accountID"), numerator/denominator);
				}
				else 
					if(cachedRating.get(currentNode.getProperty("accountID")+"_"+sink.getProperty("accountID"))!=null
					&& !checkAdjacency(currentNode,sink))
					{

						cachedRating.put(currentNode.getProperty("accountID")+"_"+sink.getProperty("accountID"), (double) -9999);
						System.out.println(currentNode.getProperty("accountID")+"_"+sink.getProperty("accountID")+99);

					}
			}
			depth-= 1;
		}

		//		      for(Map.Entry e : children.entrySet()) {
		//			      System.out.println(e.getKey() + " -> " + e.getValue());
		//			  }
		//		      for(Map.Entry e : cachedRating.entrySet()) {
		//			      System.out.println(e.getKey() + " -> " + e.getValue());
		//			  }
		return cachedRating.get(source.getProperty("accountID")+"_"+sink.getProperty("accountID")) != null ? 
				cachedRating.get(source.getProperty("accountID")+"_"+sink.getProperty("accountID")) : 0;
	}
	
	private Queue<Node> copyQueue(Queue<Node> tempQe) {

		Queue<Node> qe = new LinkedList<Node>();
		Iterator<Node> it = tempQe.iterator(); 

		while(it.hasNext())
		{
			Node temp = it.next();
			qe.add(temp);
		}
		return qe;
	}


}
