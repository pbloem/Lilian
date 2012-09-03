package org.lilian.data.real.classification;

import java.util.*;

import org.lilian.data.real.Density;
import org.lilian.data.real.Point;


public class DensityClassifier<T extends Density> 
	extends AbstractClassifier {
	
	protected List<T> models = new ArrayList<T>(5);
	protected List<Double> priors = new ArrayList<Double>(5);
	protected double priorSum = 0.0;
	
	protected DensityClassifier(int dimension, int numClasses)
	{
		super(dimension, numClasses);
	}

	public DensityClassifier(T model, double prior) {
		super(model.dimension(), 0);
		
		add(model, prior);
	}
	
	public void add(T model, double prior)
	{
		models.add(model);
		priors.add(prior);
		priorSum += prior;
		
		numClasses++;
	}
	
	public List<Double> probabilities(Point point) 
	{
		List<Double> probs = new ArrayList<Double>(numClasses);
		
		for(int i = 0; i < numClasses; i++)
			probs.add(prior(i) * models.get(i).density(point));
		
		return probs;
	}

	public double prior(int i)
	{
		return priors.get(i)/priorSum;
	}
	
	public List<T> models()
	{
		return Collections.unmodifiableList(models);
	}
	
	public String toString()
	{
		return "[priors: " + priors + "("+priorSum+"), models: " + models + " ]";
	}

	public String modelRep() {
		return "[" + priors + ", " + models + " ]";		
	}
}
