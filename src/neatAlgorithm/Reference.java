package neatAlgorithm;

public class Reference {
	
	public Reference() {
		
	}

	public void instructions() {
				
		System.out.println("How to setup properly");
		
		System.out.println("------------");
		
		System.out.println("Set number of inputs & outputs: ");
		
		System.out.println("NeatGenome.inputs = # of inputs");
		System.out.println("NeatGenome.outputs = # of outputs");
		
		System.out.println("------");
		
		System.out.println("Set the sketch parent: ");
		
		System.out.println("NeatGenome.parent = reference to the sketch");
		System.out.println("This is primarily used to visualize the network");
		
		System.out.println("------");
		
		System.out.println("Set activation functions: ");
		
		System.out.println("NeatGenome.{ActivationFunction}()");
		System.out.println("NeatGenome.applyToAll = true / false");
		System.out.println("If enabled, all nodes will have the specified Activation");
		System.out.println("Otherwise, only the output nodes will have such activation. The algorithm will try to find the best activation for each of the other nodes");
		
		System.out.println("------");
		
		System.out.println("Initialize and link Speciator to the NeatGenome class: ");
		
		System.out.println("Speciator sp = new Speciator(1, 1, 1, 1);");
		System.out.println("NeatGenome.specieManager = sp");
		System.out.println("Use the function add() to add genomes to the speciator and use speciate() once to initialize the species");
		System.out.println("Over the course of the generations, use generateNewOffspring() to get the new set of genomes and then mutate them. Then, speciate the population");
	}
	
	public void reference() {
		
		System.out.println("Reference");
		
		System.out.println("------------------");
		
		System.out.println("NEATGENOME CLASS");
		System.out.println("NeatGenome(boolean initialize): If set to true, a visualizer will be added");
		
		System.out.println("------------");
		
		System.out.println("Methods");
		
		System.out.println("------");
		
		System.out.println("Dyn void mutateNode(): Mutates randomly a node if possible");
		System.out.println("Dyn void mutateConnection(): Mutates randomly a connection if possible");
		System.out.println("Dyn void mutateParameters(): Mutates randomly wieghts & bias");
		System.out.println("Dyn NeatGenome copy(): Returns a copy of the genome");
		System.out.println("Stat NeatGenome crossover(NeatGenome parent1, NeatGenome parent2): Returns a child genome");
		System.out.println("Dyn float[] computeOutputs(): Return the outputs of the network");
		System.out.println("Dyn void addConnection(int in, int out, boolean expressed): Adds a connection if possible");
		System.out.println("Dyn void addNode(): Adds a node if possible");
		System.out.println("Dyn void initializeVisualizer(): Adds a visualizer");
		System.out.println("Dyn void display(): Displays the network");
		System.out.println("Dyn void select(float x, float y): If a node is within x, y, select that node so it follows the mouse");
		
		System.out.println("------");
		
		System.out.println("Fields");
		
		System.out.println("------");
		
		System.out.println("Map<Integer, Connection> connectionGenes: A map storing the connections");
		System.out.println("Map<Integer, Node> nodeGenes: A map storing the nodes");
		System.out.println("You can access the weights or values withe the connections/nodes in those lists");
		
		System.out.println("------");
		
		System.out.println("------------");
		
		System.out.println("SPECIATOR CLASS");
		System.out.println("Speciator(float deltaT, float c1, float c2, float c3): Creates own genomes list. You will have to add the genomes explicitly");
		System.out.println("Speciator(List<NeatGenome> genomes, float deltaT, float c1, float c2, float c3): Adds the genomes list manually");
		
		System.out.println("------------");
		
		System.out.println("Dyn void generateNewOffspring(): Generates a new genome offspring from the previous one, modifying the genomes list");
		System.out.println("Dyn void mutateNodes(float rate): Mutates the nodes of the genomes with the specified rate");
		System.out.println("Dyn void mutateConnections(float rate): Mutates the connections of the genomes with the specified rate");
		System.out.println("Dyn void mutateParameters(float rate, float deviation, boolean proportional): Mutates the parameters of"
				+ " the genomes with the specified rate & deviation. If proportional, they will be updated proportionally to their value");
		System.out.println("Dyn void speciate(): Separates genomes into species. Do this after calling generateNewOffspring()");
		System.out.println("Dyn int getCount(): Get species count");
		System.out.println("Dyn void add(NeatGenome addition): Adds the specified genome to the genomes list");
	}
}
