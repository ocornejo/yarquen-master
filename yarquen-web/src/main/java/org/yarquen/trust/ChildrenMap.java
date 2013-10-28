package org.yarquen.trust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.neo4j.graphdb.Node;

public class ChildrenMap extends HashMap<Node, List<Node>> {
    
	public void put(Node key, Node node) {
        List<Node> current = get(key);
        if (current == null) {
            current = new ArrayList<Node>();
            super.put(key, current);
        }
        current.add(node);
    }
	
}