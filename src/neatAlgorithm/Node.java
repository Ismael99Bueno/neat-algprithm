package neatAlgorithm;

import processing.core.*;
import java.util.Random;

public class Node {

	public enum TYPE {
		INPUT,
		HIDDEN, 
		OUTPUT
	}
	
	public enum AF {
		SIGMOID,
		RELU,
		TANH,
		BSTEP,
		SOFTPLUS,
		LINEAR
	}
	
	public AF af;
	public TYPE type;
	
	public int label;
	public float value, bias;
	
	public PVector pos;
	public boolean isReady;
	
	public Node(TYPE type_, AF af_, int label_, float bias_) {
		
		type = type_;
		label = label_;
		bias = bias_;
		af = af_;
		
		value = 0;
	}
	
	public Node copy() { //devuelve una copia de este nodo
		
		return new Node(type, af, label, bias);
	}
	
	public static AF getRandomAF() {
		
		AF af[] = {Node.AF.SIGMOID, Node.AF.RELU, Node.AF.TANH, Node.AF.BSTEP, Node.AF.SOFTPLUS};
		float rand = new Random().nextFloat();
		
		for (int i = 0; i < af.length; i++) {
			
			rand -= 1.0f / af.length;
			if (rand <= 0)
				return af[i];
		}
		
		throw new RuntimeException("Could not find an Activation FUnction...");
	}
	
	public void applyActFunc() { //aplica una funcion de activacion en funcion de los booleans sigmoid, reLU etc
		
		if (af == Node.AF.SIGMOID) {
			value = 1.0f / (1.0f + (float) Math.exp( - value));
			return;
		}
		
		if (af == Node.AF.RELU) {
			
			if (value < 0)
				value = 0;
			return;
		}
		
		if (af == Node.AF.TANH) {
			value = (float) Math.tanh(value);
			return;
		}
		
		if (af == Node.AF.BSTEP) {
			
			if (value > 0)
				value = 1.0f;
			else value = 0.0f;
			return;
		}
			
		if (af == Node.AF.SOFTPLUS) {
			value = (float) Math.log(1f + (float) Math.exp(value));
			return;
		}
	}
	
	public static AF getOutputAF() {
		
		if (NeatGenome.sigmoid)
			return Node.AF.SIGMOID;
		
		if (NeatGenome.reLU)
			return Node.AF.RELU;
		
		if (NeatGenome.tanh)
			return Node.AF.TANH;
		
		if (NeatGenome.BStep)
			return Node.AF.BSTEP;
					
		if (NeatGenome.softPlus)
			return Node.AF.SOFTPLUS;
		
		return Node.AF.LINEAR;
	}
}
