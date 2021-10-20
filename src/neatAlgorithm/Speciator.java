package neatAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Speciator {

	public List<NeatGenome> genomes;
	
	public List<List<NeatGenome>> species;
	public List<List<NeatGenome>> previousSpecies;
	
	private static Random rand = new Random();
	
	public float deltaT, c1, c2, c3;
	public int innovation;
	
	public Speciator(float deltaT_, float c1_, float c2_, float c3_) {
		
		genomes = new ArrayList<NeatGenome>();
		
		species = new ArrayList<List<NeatGenome>>();
		previousSpecies = new ArrayList<List<NeatGenome>>();
		
		deltaT = deltaT_;
		c1 = c1_;
		c2 = c2_;
		c3 = c3_;
		
		innovation = 0;
	}
	
	public Speciator(ArrayList<NeatGenome> genomes_, float deltaT_, float c1_, float c2_, float c3_) {
		
		genomes = genomes_;
		
		species = new ArrayList<List<NeatGenome>>();
		previousSpecies = new ArrayList<List<NeatGenome>>();
		
		deltaT = deltaT_;
		c1 = c1_;
		c2 = c2_;
		c3 = c3_;
		
		innovation = 0;
	}
	
	public void generateNewOffspring() { //genera una nueva poblacion a partir de la anterior y de su especiamiento
		
		float[] speciesFitness = calcFitness();
		
		int population = genomes.size();
		
		for (List<NeatGenome> specie : species) {
			java.util.Collections.sort(specie);
			
			if (specie.size() > 1) {
				
				int threshold = (int) Math.floor((float) specie.size() / 2.0f);
				for (int i = threshold - 1; i >= 0; i--)
					specie.remove(i);
			}
		}
		
		removeEmpty();
		if (speciesFitness.length != species.size())
			throw new RuntimeException("Fitness array length not equal to number of species. Check removal method");
		
		genomes.clear();
		
		while (genomes.size() < population) {

			List<NeatGenome> specie = pickOneSpecie(speciesFitness);

			if (specie.size() > 1) {
				
				NeatGenome parent1 = pickOneGenome(specie);
				NeatGenome parent2 = pickOneGenome(specie);

				if (parent1 != parent2) {
					NeatGenome child = NeatGenome.crossover(parent1, parent2);
					
					genomes.add(child);
				} else
					genomes.add(parent1.copy());
			} else
				genomes.add(specie.get(0).copy());
			
		}
	}
	
	public void mutateConnections(float rate) { //muta a toda la poblacion
		
		for (NeatGenome gen : genomes) {
			if (rand.nextFloat() < rate)
				gen.mutateConnection();

		}
	}
	
	public void mutateNodes(float rate) {
		
		for (NeatGenome gen : genomes) {
			if (rand.nextFloat() < rate)
				gen.mutateNode();
			
		}
	}
	
	public void mutateParameters(float rate, float deviation, boolean proportional) {
		
		for (NeatGenome gen : genomes)
			gen.mutateParameters(rate, deviation, proportional);
	}
	
	public void speciate() { //divide a la poblacion en especies
		
		for (List<NeatGenome> specie : species)
			specie.clear();
		
		for (NeatGenome suitor : genomes) {
			
			boolean suitable = false;
			for (int i = 0; i < species.size(); i++) {
				
				List<NeatGenome> specie = species.get(i);
				NeatGenome representant;
				
				if (i < previousSpecies.size())
					representant = previousSpecies.get(i).get(0);
				else
					representant = specie.get(0);
								
				if (computeDelta(suitor, representant) < deltaT) {
					specie.add(suitor);
					suitable = true;
					break;
				}
			}
			
			if (!suitable) {
				
				List<NeatGenome> newSpecie = new ArrayList<NeatGenome>();
				newSpecie.add(suitor);
				
				species.add(newSpecie);
			}
		}
		
		removeEmpty();
		previousSpecies.clear();
		for (List<NeatGenome> specie : species) {
			previousSpecies.add(new ArrayList<NeatGenome>());
			
			for (NeatGenome member : specie)
				previousSpecies.get(previousSpecies.size() - 1).add(member.copy());
		}
		
	}
	
	private float computeDelta(NeatGenome gen1, NeatGenome gen2) { //calcula la distancia genomica entre dos individuos
		
		if (gen1.connectionGenes.size() > 0 && gen2.connectionGenes.size() > 0){
			
			int maxInn1 = java.util.Collections.max(gen1.connectionGenes.keySet());
			int maxInn2 = java.util.Collections.max(gen2.connectionGenes.keySet());
			
			float weightDiff = 0;
			
			int matching = 0;
			int disjoint = 0;
			int excess = 0;
			
			for (int i = 0; i < Math.max(maxInn1, maxInn2); i++) {
				
				if (i > Math.min(maxInn1, maxInn2))
					excess++;
				else if (gen1.connectionGenes.containsKey(i) && gen2.connectionGenes.containsKey(i)) {
					
					weightDiff += Math.abs(gen1.connectionGenes.get(i).weight - gen2.connectionGenes.get(i).weight);
					matching++;
				} else if (gen1.connectionGenes.containsKey(i) || gen2.connectionGenes.containsKey(i))
					disjoint++;
			}
			
			if (matching > 0)
				weightDiff /= matching;
			
			int N = (maxInn1 > maxInn2 ? gen1 : gen2).connectionGenes.size();
			return (c1 * excess + c2 * disjoint) / N + weightDiff;
		} else
			throw new RuntimeException("Cannot compute delta if one or both of the genomes have no connectionGenes");
	}
	
	private float[] calcFitness() { //calcula la fitness de las especies
		
		float[] fitness = new float[species.size()];
		float totalFitness = 0;
		
		for (int i = 0; i < species.size(); i++) {
			List<NeatGenome> specie = species.get(i);
			fitness[i] = 0;
			
			for (NeatGenome member : specie)
				fitness[i] += member.fitness;
			
			fitness[i] /= specie.size();
			totalFitness += fitness[i];
		}
		
		for (int i = 0; i < fitness.length; i++)
			fitness[i] /= totalFitness;
		
		return fitness;
	}
	
	private List<NeatGenome> pickOneSpecie(float[] fitness){ //elige una especie en funcion de su fitness
		
		float threshold = rand.nextFloat();
		
		int maxCount = 10;
		int count = 0;
		
		while(threshold > 0 && count < maxCount) {
			for (int i = 0; i < fitness.length; i++) {
				
				threshold -= fitness[i];
				if (threshold <= 0.0f)
					return species.get(i);
			}
		
			count++;
		}
		
		throw new RuntimeException("Could not find a specie...");
	}
	
	private NeatGenome pickOneGenome(List<NeatGenome> specie) { //elije a un genoma en funcion de su fitness
		
		float total = 0;
		for (NeatGenome member : specie)
			total += member.fitness;
		
		float threshold = rand.nextFloat() * total;
		
		int maxCount = 10;
		int count = 0;
		
		while (threshold > 0 && count < maxCount) {
			for (NeatGenome member : specie) {
				
				threshold -= member.fitness;
				if (threshold <= 0.0f)
					return member;
			}
			
			count++;
		}
		throw new RuntimeException("Could not find a genome...");
	}
	
	private void removeEmpty() { //elimina especies vacias
		
		for (int i = species.size() - 1; i >= 0; i--) {
			
			List<NeatGenome> specie = species.get(i);
			if (specie.isEmpty())
				species.remove(i);
			
		}
	}
	
	public int getCount() {
		
		return species.size();
	}
	
	public void add(NeatGenome addition) {
		genomes.add(addition);
	}
	
	public int generateInnovation(int in, int out) { //genera una inovacion en funcion de la poblacion
				
		for (NeatGenome genome : genomes) {
			
			if (genome.haveLocalConnection(in, out))
				return genome.getLocalConnection(in, out).innovation;
		}
		
		return innovation++;
	}
}
