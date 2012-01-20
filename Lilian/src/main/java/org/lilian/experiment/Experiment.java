package org.lilian.experiment;

/**
 * An experiment represents a piece of code that is run to achieve certain 
 * results.
 * 
 * A class that is annoted as such can be run automatically with a selection of
 * parameters so that everything affecting the environment is catalogued and 
 * stored together with the basic parameters, information about the run, and 
 * state information required to restart the run if it is interrupted
 * 
 * The experiment can also be run with a selection of values for each parameter,
 * the running code will run the experiment for each possible combination and 
 * collate and plot the results.
 * 
 * @author Peter
 *
 */
public @interface Experiment
{

}
