package org.lilian.models;
/**
 * A frequency model defines frequencies over tokens, and uses them to define
 * a probability model.
 * 
 * @param <T> The type of token. T should have a proper equals(), hashcode() and 
 * toString() implementation.
 */
public interface FrequencyModel<T> extends ProbabilityModel<T> {
	
	/**
	 * Returns a frequency for a given model. The frequency need not be integer
	 * (to accommodate smoothed models) but they must be non-negative finite 
	 * numbers. 
	 * 
	 * @param token The element for which to return the frequency.
	 * @return The frequency of the given token according to this model.
	 */
	public double frequency(T token);
	
	/**
	 * Returns the total frequency, the sum of all the frequencies of all the 
	 * tokens. The returned value need not be an integer, but it must be a 
	 * non-negative finite number.
	 * 
	 * @return The total frequency. 
	 */	
	public double total();

	/**
	 * Returns the total number of distinct tokens in the model. The returned 
	 * value need not be an integer, but it must be a non-negative finite 
	 * number. 
	 * 
	 * @return The total number of distinct tokens in the model.
	 */
	public double distinct();
	
	public static class Comparator<T> implements java.util.Comparator<T>
	{
		private FrequencyModel<T> model;
		
		public Comparator(FrequencyModel<T> model)
		{
			this.model = model;
		}
		
		@Override
		public int compare(T first, T second) {
			return Double.compare(model.frequency(first), model.frequency(second));
		}
	}	
}	

