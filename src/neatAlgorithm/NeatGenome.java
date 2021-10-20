package neatAlgorithm;

import processing.core.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class NeatGenome implements Comparable<NeatGenome> {
	
	private static Random rand = new Random();
	public static PApplet parent;
	
	public static int inputs, outputs;
	public static Speciator specieManager;
	
	private static List<Integer> tempList1 = new ArrayList<Integer>();
	private static List<Integer> tempList2 = new ArrayList<Integer>();
	
	public static boolean sigmoid, reLU, tanh, BStep, softPlus, applyToAll;
		
	public int localLabel;
	
	public Map<Integer, Connection> connectionGenes;
	public Map<Integer, Node> nodeGenes;
	
	private NeatVisualizer visualizer;
	public boolean hasVisualizer;
	
	public float fitness;
		
	public NeatGenome(boolean initializer) { //initializer determina si se inicializa el visualizador
		
		localLabel = 0;
		fitness = 0;
		
		connectionGenes = new HashMap<>();
		nodeGenes = new HashMap<>(inputs + outputs);
		
		for (int i = 0; i < inputs; i++) {
			
			int label = generateLabel();
			nodeGenes.put(label, new Node(Node.TYPE.INPUT, Node.AF.LINEAR, label, 0.0f));
		}
		
		for (int i = 0; i < outputs; i++) {
			
			int label = generateLabel();
			
			Node.AF af = Node.getOutputAF(); 
			nodeGenes.put(label, new Node(Node.TYPE.OUTPUT, af, label, 4 * rand.nextFloat() - 2.0f));
		}
		
		hasVisualizer = false;
		if (initializer)
			initializeVisualizer();
	}
	
	public NeatGenome(NeatGenome copied) { //constructor utilizado para copiar una red
		
		localLabel = copied.localLabel;
		fitness = copied.fitness;
		
		connectionGenes = new HashMap<>(copied.connectionGenes.size());
		nodeGenes = new HashMap<>(copied.nodeGenes.size());
		
		for (Map.Entry<Integer, Node> entryNode : copied.nodeGenes.entrySet())
			nodeGenes.put(entryNode.getKey(), entryNode.getValue().copy());
		
		for (Map.Entry<Integer, Connection> entryCon : copied.connectionGenes.entrySet())
			connectionGenes.put(entryCon.getKey(), entryCon.getValue().copy());
		
		hasVisualizer = false;
	}
	
	public NeatGenome copy() {
		
		return new NeatGenome(this);
	}
	
	public void mutateConnection() { //muta una conexion si se dan las condiciones
		
		if (specieManager != null && specieManager.genomes != null) {
		
			int label1 = rand.nextInt(localLabel);
			int label2 = rand.nextInt(localLabel);
			
			while (label1 == label2)
				label1 = rand.nextInt(localLabel);
			
			if (!haveLocalBinding(label1, label2)) {
			
				Node node1 = nodeGenes.get(label1);
				Node node2 = nodeGenes.get(label2);
				
				if (node1.type != node2.type || node1.type == Node.TYPE.HIDDEN) {
			
					boolean reversed = isReversed(node1, node2);
					
					tempList1.clear();
					getFeedingNodes(reversed ? label2 : label1);
					if (!tempList1.contains(reversed ? label1 : label2)) {
						
						int innov = specieManager.generateInnovation(reversed ? label2 : label1, reversed ? label1 : label2);
						
						float weight = rand.nextFloat() * 4 - 2;
						
						connectionGenes.put(innov, new Connection(reversed ? label2 : label1, reversed ? label1 : label2, innov, weight, true));
					}
				}
			}
		} else
			throw new RuntimeException("A Speciator instance has not been linked to the NeatGenome class, or the genomes in Speciator have not been initialized");
	}
	
	public void mutateNode() { //muta un nodo si se dan las condiciones
		
		if (specieManager != null && specieManager.genomes != null) {
			if (connectionGenes.size() > 0) {
			
				List<Integer> innovationNumbers = getListFromSet(connectionGenes.keySet(), tempList2);
				int inn = innovationNumbers.get(rand.nextInt(innovationNumbers.size()));
				
				Connection center = connectionGenes.get(inn);
				if (center.expressed) {
					
					center.expressed = false;
					
					Node.AF af = Node.getRandomAF();
					Node newNode = new Node(Node.TYPE.HIDDEN, af, generateLabel(), rand.nextFloat() * 4 - 2);
					
					int innLeft = specieManager.generateInnovation(center.inNode, newNode.label);
					int innRight = specieManager.generateInnovation(newNode.label, center.outNode);
					
					Connection left = new Connection(center.inNode, newNode.label, innLeft, 1.0f, true);
					Connection right = new Connection(newNode.label, center.outNode, innRight, center.weight, true);
					
					nodeGenes.put(newNode.label, newNode);
					
					connectionGenes.put(innLeft, left);
					connectionGenes.put(innRight, right);
					
					if (hasVisualizer && visualizer.isSet)
						visualizer.update(newNode);
				}
			}
		} else
			throw new RuntimeException("A Speciator instance has not been linked to the NeatGenome class, or the genomes in Speciator have not been initialized");
	}
	
	public void mutateParameters(float rate, float deviation, boolean proportional) { //muta los parametros (weights & bias)
			
		for (Map.Entry<Integer, Connection> entryCon : connectionGenes.entrySet()) {
			
			Connection con = entryCon.getValue();
			if (rand.nextFloat() < rate)
				con.weight += deviation * rand.nextGaussian() * (proportional ? con.weight : 1.0f);
		}
		
		for (Map.Entry<Integer, Node> entryNode : nodeGenes.entrySet()) {
			
			Node node = entryNode.getValue();
			if (rand.nextFloat() < rate && node.type != Node.TYPE.INPUT)
				node.bias += deviation * rand.nextGaussian() * (proportional ? node.bias : 1.0f);
		}
	}
	
	public static NeatGenome crossover(NeatGenome parent1, NeatGenome parent2) { //realiza el crossover entre dos redes
		
		if (parent1.connectionGenes.size() > 0 && parent2.connectionGenes.size() > 0){
			
			int maxInn1 = java.util.Collections.max(parent1.connectionGenes.keySet());
			int maxInn2 = java.util.Collections.max(parent2.connectionGenes.keySet());
			
			boolean reversed = maxInn1 < maxInn2;
			
			NeatGenome first = reversed ? parent2 : parent1;
			NeatGenome second = reversed ? parent1 : parent2;
			
			NeatGenome child = new NeatGenome(false);
			
			float probability;
			if (first.fitness > second.fitness)
				probability = 1.0f;
			else if (first.fitness == second.fitness)
				probability = 0.5f;
			else
				probability = 0.0f;
			
			for (int i = 0; i <= Math.max(maxInn1, maxInn2); i++) {
				
				NeatGenome chosen = null;
				
				if (first.connectionGenes.containsKey(i) && second.connectionGenes.containsKey(i))
					chosen = rand.nextFloat() < 0.5f ? first : second;
				else if (first.connectionGenes.containsKey(i))
					chosen = first;
				else if (second.connectionGenes.containsKey(i))
					chosen = second;
				
				if (chosen != null && (i <= Math.min(maxInn1, maxInn2) || rand.nextFloat() < probability)) {
					Connection parentCon = chosen.connectionGenes.get(i);
					
					NeatGenome.tempList1.clear();
					child.getFeedingNodes(parentCon.inNode);
					if (!child.haveLocalBinding(parentCon.inNode, parentCon.outNode)
							 && !NeatGenome.tempList1.contains(parentCon.outNode)) {
						
						if (!child.nodeGenes.containsKey(parentCon.inNode))
							child.nodeGenes.put(parentCon.inNode, chosen.nodeGenes.get(parentCon.inNode).copy());
						
						if (!child.nodeGenes.containsKey(parentCon.outNode))
							child.nodeGenes.put(parentCon.outNode, chosen.nodeGenes.get(parentCon.outNode).copy());
						
						child.connectionGenes.put(i, parentCon.copy());
					}
				}
			}	
			
			return child;
		} else
			throw new RuntimeException("Cannot crossover if one or both of the parents have no connectionGenes");
	}
	
	public void getFeedingNodes(int nodeLabel) { //obtiene recursivamente una lista de todos los nodos que alimentan al correspondiente a nodeLabel
		
		tempList1.add(nodeLabel);
		
		for (Map.Entry<Integer, Connection> entryCon : connectionGenes.entrySet()) {
			
			Connection con = entryCon.getValue();
			
			if (con.outNode == nodeLabel && !tempList1.contains(con.inNode)) {
				getFeedingNodes(con.inNode);
			}
		}
	}
	
	private void backtrack(Node node) { //algoritmo recursivo utilizado para calcular el output de la red
		
		if (!node.isReady) {
			for (Map.Entry<Integer, Connection> entryCon : connectionGenes.entrySet()) {
				
				Connection con = entryCon.getValue();
				
				Node in = nodeGenes.get(con.inNode);
				Node out = nodeGenes.get(con.outNode);
				if (con.expressed && !con.hasSentInfo && out == node) {
					
					con.hasSentInfo = true;
					backtrack(in);
					node.value += con.weight * in.value;
				}
			}
			
			node.isReady = true;
			if (applyToAll || node.type == Node.TYPE.OUTPUT)
				node.value = applyActFunc(node.value);
			else
				node.applyActFunc();
		}
	}
	
	public float[] computeOutputs(float[] ins) { //function que calcula el otput de la red
		
		float[] outs = new float[outputs];
		
		int i = 0;
		for (Map.Entry<Integer, Node> entryNode : nodeGenes.entrySet()) {
			
			Node node = entryNode.getValue();
			if (node.type == Node.TYPE.INPUT) {
				
				node.value = ins[i++];
				node.bias = 0;
				node.isReady = true;
			}
			else {
				
				node.value = node.bias;
				node.isReady = false;
			}
		}
		
		for (Map.Entry<Integer, Connection> entryCon : connectionGenes.entrySet())
			entryCon.getValue().hasSentInfo = false;
		
		i = 0;
		for (Map.Entry<Integer, Node> entryNode : nodeGenes.entrySet()) {
			
			Node node = entryNode.getValue();
			if (node.type == Node.TYPE.OUTPUT) {
				
				backtrack(node);
				outs[i++] = node.value;
			}
		}
		
		return outs;
	}
	
	public boolean haveLocalBinding(int in, int out) { //determina si existe una conexion, sin importar el sentido
		
		for (Map.Entry<Integer, Connection> entryCon : connectionGenes.entrySet()) {
			
			Connection con = entryCon.getValue();
			if ((in == con.inNode && out == con.outNode) || (out == con.inNode && in == con.outNode))
				return true;
		}
		
		return false;
	}
	
	public boolean haveLocalConnection(int in, int out) { //determina si existe una conexion en el sentido de los inputs
		
		for (Map.Entry<Integer, Connection> entryCon : connectionGenes.entrySet()) {
			
			Connection con = entryCon.getValue();
			if (in == con.inNode && out == con.outNode)
				return true;
		}
		
		return false;
	}
	
	public Connection getLocalConnection(int in, int out) { //devuelve la conexion correspondiente a los inputs en ese sentido (si existe)
		
		for (Map.Entry<Integer, Connection> entryCon : connectionGenes.entrySet()) {
			
			Connection con = entryCon.getValue();
			if (in == con.inNode && out == con.outNode)
				return con;
		}
		
		return null;
	}
	
	public void addConnection(int in_, int out_, boolean isExpressed) { //haz una conexion manualmente
		
		int in = in_ - 1;
		int out = out_ - 1;
		
		if (specieManager != null && specieManager.genomes != null) {
			if (in != out && nodeGenes.containsKey(in) && nodeGenes.containsKey(out)) {
				Node node1 = nodeGenes.get(in);
				Node node2 = nodeGenes.get(out);
				
				if (!isReversed(node1, node2) && !haveLocalBinding(in, out)) {
					
					tempList1.clear();
					getFeedingNodes(node1.label);
					if (!tempList1.contains(node2.label)) {
						
						int inn = specieManager.generateInnovation(in, out);
						connectionGenes.put(inn, new Connection(in, out, inn, 4 * rand.nextFloat() - 2, isExpressed));
					} else
						throw new RuntimeException("Making that connection will result in circular structure (" + in + "->" + out + ")");
				} else
					throw new RuntimeException("The connection you are trying to make already exists or is reversed (" + in + "->" + out + ")");
			} else
				throw new RuntimeException("One of the nodes does not exist, or both nodes are equal (" + in + "->" + out + ")");
		} else 
			throw new RuntimeException("A Speciator instance has not been linked to the NeatGenome class, or the genomes in Speciator have not been initialized (" + in + "->" + out + ")");
	}
	
	public void addNode() { //haz un nodo manualmente
		
		int label = generateLabel();
		
		Node.AF af = Node.getRandomAF();
		Node newNode = new Node(Node.TYPE.HIDDEN, af, label, 4 * rand.nextFloat() - 2);
		
		nodeGenes.put(label, newNode);
		
		if (hasVisualizer && visualizer.isSet)
			visualizer.update(newNode);
	}
	
	private boolean isReversed(Node inNode, Node outNode) { //comprueba si una conexion esta al reves (output > hidden por ejemplo)
		
		return (inNode.type == Node.TYPE.OUTPUT && outNode.type == Node.TYPE.HIDDEN)
				|| (inNode.type == Node.TYPE.OUTPUT && outNode.type == Node.TYPE.INPUT)
				|| (inNode.type == Node.TYPE.HIDDEN && outNode.type == Node.TYPE.INPUT);
	}
	
	private static List<Integer> getListFromSet(Set<Integer> set, List<Integer> list){ //transforma un set en una lista
		
		list.clear();
		list.addAll(set);
		
		return list;
	}
	
	/*
	private static List<Integer> sortList(Set<Integer> set, List<Integer> list) {
		
		list.clear();
		list.addAll(set);
		java.util.Collections.sort(list);
		
		return list;
	}
	*/
	public int generateLabel() { //genera una etiqueta para un nodo en funcion de las etiquetas locales
		
		return localLabel++;
	}
	
	public void initializeVisualizer() { //inicializa el visualizador
		
		visualizer = new NeatVisualizer(this);
		hasVisualizer = true;
	}
	
	public void setDraw() { //prepara la visualizacion
		
		if (hasVisualizer)
			visualizer.setDraw();
		else
			throw new RuntimeException("This genome does not contain a visualizer");
	}
	
	public void display() { //funcion que permite visualizar la red
		
		if (hasVisualizer) {
			if (!visualizer.isSet)
				visualizer.setDraw();
			
			visualizer.visualize();
		} else
			throw new RuntimeException("This genome does not contain a visualizer");
	}
	
	public void select(float x, float y) { //funcion que selecciona un nodo si x e y estan en sus dimensiones
		
		if (hasVisualizer)
			visualizer.selectNode(x, y);
		else
			throw new RuntimeException("This genome does not contain a visualizer");
	}
	
	public int compareTo(NeatGenome gen) {
		if (gen.fitness > fitness)
			return -1;
		else if (gen.fitness == fitness)
			return 0;
		else
			return 1;
	}
	
	private static float applyActFunc(float value) { //aplica una funcion de activacion en funcion de los booleans sigmoid, reLU etc
		
		if (sigmoid)
			return 1.0f / (1.0f + (float) Math.exp( - value));
		
		if (reLU) {
			
			if (value > 0)
				return value;
			else return 0;
		}
		
		if (tanh)
			return (float) Math.tanh(value);
		
		if (BStep) {
			
			if (value > 0)
				return 1.0f;
			else return 0;
		}
			
		if (softPlus)
			return (float) Math.log(1f + (float) Math.exp(value));
		
		return value;
	}
	
	public static void sigmoid() {
		
		resetFunc();
		sigmoid = true;
	}
	
	public static void reLU() {
		
		resetFunc();
		reLU = true;
	}
	
	public static void tanh() {
		
		resetFunc();
		tanh = true;
	}
	
	public static void BStep() {
	
		resetFunc();
		BStep = true;
	}

	public static void softPlus() {
	
		resetFunc();
		softPlus = true;
	}
	
	private static void resetFunc() {
		
		sigmoid = false;
		reLU = false;
		tanh = false;
		BStep = false;
		softPlus = false;
	}
	
	public static void instructions() {
		
		new Reference().instructions();
	}
	
	public static void reference() {
		
		new Reference().reference();
	}
}
