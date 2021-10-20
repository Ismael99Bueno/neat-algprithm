package neatAlgorithm;

public class Connection {

	public int inNode, outNode, innovation;
	public float weight;
	
	public boolean expressed, hasSentInfo;
	
	public Connection(int inNode_, int outNode_, int innovation_, float weight_, boolean expressed_){
		
		inNode = inNode_;
		outNode = outNode_;
		innovation = innovation_;
		weight = weight_;
		expressed = expressed_;
		
	}
	
	public Connection copy() { //devuelve una copia de esta conexion
		
		return new Connection(inNode, outNode, innovation, weight, expressed);
	}
	
}
