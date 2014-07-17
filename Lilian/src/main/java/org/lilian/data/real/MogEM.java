package org.lilian.data.real;

import java.util.List;

public class MogEM
{
	private static final double PERTURB_VAR = 0.03;
	
	private List<Point> data;
	private MOG model;
	
	public MogEM(List<Point> data, int numComponents)
	{	
		this.data = data;
		List<List<Double>> codes = MOG.initial(data.size(), numComponents);
		model = MOG.maximization(codes, data);	
	}
	
	public void iterate()
	{
		List<List<Double>> codes = model.expectation(data);
		model = MOG.maximization(codes, data);
	}
	
	public MOG model()
	{
		return model;
	}
}
