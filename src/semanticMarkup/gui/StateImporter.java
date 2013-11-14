package semanticMarkup.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
public class StateImporter {
	HashSet<State> states = null;
	
	StateImporter(ArrayList<String> states){
		this.states = new HashSet<State>();
		Iterator<String> it = states.iterator();
		while(it.hasNext()){
			this.states.add(new State((String)it.next()));
		}
	}
	
	@SuppressWarnings("unchecked")
	public Set getStates(){
		return states;
	}
}
