/**
 * 
 */
package org.lttng.flightbox.automaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


/**
 * Run automaton with a string
 * @author francis
 *
 */
public class StringAutomatonRunner {
	
	public StringAutomatonRunner(){
		
	}
	
	public boolean process(Automaton a, String input){
		boolean res = false;
		String[] seq = input.split(",");
		Set<Symbol> sym = a.getAlphabet();
		HashMap<String, Event> map = new HashMap<String, Event>();
		for (Symbol x: sym){
			Event y = (Event) x;
			map.put(y.getLabel(), y);
		}
		Event eps = (Event) Automaton.getEps();
		HashSet<State> workset1 = new HashSet<State>();
		HashSet<State> workset2 = new HashSet<State>();
		Stack<State> stack = new Stack<State>();
		HashSet<State> visited = new HashSet<State>();
		workset1.add(a.getStart());
		// for each input symbol, try to step automaton, assuming non determinism
		
		if (input.length() == 0){
			stack.addAll(a.getStart().getTransitions(eps));
			while(stack.size() > 0){
				State z = stack.pop();
				workset1.add(z);
				if (!visited.contains(z)){
					stack.addAll(z.getTransitions(eps));
					visited.add(z);
				}
			}
		} else {
			
			int i;
			for (i=0;i<seq.length;i++){
				Event symbol = map.get(seq[i]);
				for (State x : workset1){
					workset2.addAll(x.getTransitions(symbol));
					stack.addAll(x.getTransitions(eps));
					while(stack.size() > 0){
						State z = stack.pop();
						if (!visited.contains(z)){
							stack.addAll(z.getTransitions(eps));
							workset2.addAll(z.getTransitions(symbol));
							visited.add(z);
						}
					}
				}
				visited.clear();
				workset1.clear();
				workset1.addAll(workset2);
				workset2.clear();
				if (workset1.size() == 0){
					break;
				}
			}
		}
		for (State s : workset1){
			stack.push(s);
			while(stack.size() > 0){
				State z = stack.pop();
				if (z.isAccept()){
					res = true;
					break;
				} else {
					stack.addAll(z.getTransitions(eps));
				}
			}
		}
		return res;
	}
}
