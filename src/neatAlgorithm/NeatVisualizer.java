package neatAlgorithm;

import java.lang.Math;
import java.util.Map;
import java.util.Random;
import processing.core.*;

public class NeatVisualizer {

	private NeatGenome partner;
	private PApplet parent;
	
	private Random rand = new Random();
	
	public boolean isSet, hasNode;
	public Node selected;
	
	private int centerLeft, centerRight, right, radius;
	
	public NeatVisualizer(NeatGenome partner_) {
		
		partner = partner_;
		parent = NeatGenome.parent;
		
		isSet = false;
		
		centerLeft = Math.round((float) parent.width / 4);
		
		centerRight = Math.round(3 * (float) parent.width / 4);
		right = parent.width;
		
		radius = 10;
		
		hasNode = false;
		selected = null;
	}
	
	public void setDraw() {
		
		isSet = true;
				
		for (Map.Entry<Integer, Node> entryNode : partner.nodeGenes.entrySet()) {
			
			Node node = entryNode.getValue();
			
			if (node.type == Node.TYPE.INPUT)
				node.pos = new PVector(rand.nextInt(centerLeft), rand.nextInt(parent.height));
			else if (node.type == Node.TYPE.HIDDEN)
				node.pos = new PVector(centerLeft + rand.nextInt(centerRight - centerLeft), rand.nextInt(parent.height));
			else
				node.pos = new PVector(centerRight + rand.nextInt(right - centerRight), rand.nextInt(parent.height));
		}
	}
	
	public void update(Node node) {
		
		if (node.type == Node.TYPE.INPUT)
			node.pos = new PVector(rand.nextInt(centerLeft), rand.nextInt(parent.height));
		else if (node.type == Node.TYPE.HIDDEN)
			node.pos = new PVector(centerLeft + rand.nextInt(centerRight - centerLeft), rand.nextInt(parent.height));
		else
			node.pos = new PVector(centerRight + rand.nextInt(right - centerRight), rand.nextInt(parent.height));
	}
	
	public void selectNode(float x, float y) {
		
		if (isSet) {
			if (!hasNode) {
			
				for (Map.Entry<Integer, Node> entryNode : partner.nodeGenes.entrySet()) {
					
					Node sel = entryNode.getValue();
					
					if (PApplet.dist(x, y, sel.pos.x, sel.pos.y) < radius) {
						hasNode = true;
						selected = sel;
						break;
					}
				}
			} else 
				hasNode = false;
		} else
			throw new RuntimeException("Drawing for this genome has not been set. Call the function display() or setDraw() first");
	}
	
	public void visualize() {
		
		PVector dir;
		
		if (hasNode) {
			
			selected.pos.set(parent.mouseX, parent.mouseY);
			
			parent.push();
			
			parent.textSize(20);
			parent.text(selected.af.toString(), selected.pos.x - 2 * radius, selected.pos.y + 2 * radius);
			
			parent.pop();
		}
				
		for (Map.Entry<Integer, Connection> entryCon : partner.connectionGenes.entrySet()) {
			
			Connection con = entryCon.getValue();
			if (con.expressed) {
				
				Node in = partner.nodeGenes.get(con.inNode);
				Node out = partner.nodeGenes.get(con.outNode);
				
				dir = PVector.sub(out.pos, in.pos).div(in.pos.dist(out.pos));
				
				parent.line(in.pos.x, in.pos.y, out.pos.x, out.pos.y);
				
				parent.push();
				
				parent.translate((in.pos.x + out.pos.x) / 2.0f, (in.pos.y + out.pos.y) / 2.0f);
				parent.rotate(dir.heading());
				
				parent.textSize(20);
				parent.text("--->", 0, 0);
				
				parent.pop();
				parent.push();
				
				parent.textSize(20);
				parent.text(con.innovation + 1, (in.pos.x + out.pos.x) / 2.0f - dir.x * 20f, (in.pos.y + out.pos.y) / 2.0f - dir.y * 20f);
				
				parent.pop();
			}
		}
		
		for (Map.Entry<Integer, Node> entryNode : partner.nodeGenes.entrySet()) {
			
			Node node = entryNode.getValue();
			
			parent.push();
			parent.noStroke();
			
			if (node.type == Node.TYPE.INPUT)
				parent.fill(0, 0, 255);
			else if (node.type == Node.TYPE.OUTPUT)
				parent.fill(255, 0, 0);
			else
				parent.fill(255, 255, 0);
			
			parent.ellipse(node.pos.x, node.pos.y, 2 * radius, 2 * radius);			
			
			parent.textSize(20);
			parent.text(node.label + 1, node.pos.x + radius, node.pos.y - radius);
			
			parent.pop();
		}
		
	}
}
