package org.lttng.flightbox.junit.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.lttng.flightbox.state.Automaton;
import org.lttng.flightbox.state.Event;
import org.lttng.flightbox.state.RegExp;
import org.lttng.flightbox.state.State;
import org.lttng.flightbox.state.Symbol;

public class TestRegExp {

	/** str1 : (q1,q2)*/
	ArrayList<Event> str1;
	
	/** str1 : (q1,q2,q2)*/
	ArrayList<Event> str2;

	Event q1;
	Event q2;
	Event q3;
	Event q4;
	Event q5;
	
	@Before
	public void setup(){
		// Symbole string is a HedgeState set
		q1 = new Event("q1");
		q2 = new Event("q2");
		q3 = new Event("q3");
		q4 = new Event("q4");
		q5 = new Event("q5");
		str1 = new ArrayList<Event>();
		str1.add(q1);
		str1.add(q2);
	}
	
	@Test
	public void testState(){
		State s1 = new State();
		State s2 = new State();
		State s3 = new State();
		s1.addTransition(q1, s2);
		s1.addTransition(q2, s3);
	}
	
	@Test
	public void testEquals(){
		State s1 = new State();
		State s2 = new State(s1.getId());
		assertTrue(s1.equals(s2));
		assertTrue(s2.equals(s1));
		
		Event q1 = new Event();
		Event q2 = new Event(q1.getId());
		assertTrue(q1.equals(q2));
		assertTrue(q2.equals(q1));		
	}
	
	@Test
	public void testAutomatonSize(){
		Automaton a = new Automaton();
		assertEquals(a.getSize(),2);

		State s1 = new State();
		State s2 = new State();
		State s3 = new State();
		
		State s = a.getStart();
		s.addTransition(q1, s1);
		s1.addTransition(q2, s2);
		s1.addTransition(q3, s3);
		assertEquals(a.getSize(),5);
	}
	
	@Test
	public void testReplaceTransition(){
		State s1 = new State();
		State s2 = new State();
		s1.addTransition(q1, s2);
		assertTrue(s1.getTransition(q1).equals(s2));
	}
/*
	@Test
	public void testSingle() throws GrammarException, IOException, ParseException{
		String regexp = "a";
		StringAutomatonBuilder b = new StringAutomatonBuilder();
		Automaton a = b.build(regexp);
		int x = a.getSizeWithTransitions();
		a.determinize();
		int y = a.getSizeWithTransitions();
		System.out.println(a.toString());
		assertEquals(x,y);
	}
	
	@Test
	public void testUnion() throws GrammarException, IOException, ParseException{
		String regexp = "a|b";
		StringAutomatonBuilder b = new StringAutomatonBuilder();
		Automaton a = b.build(regexp);
		StringAutomatonRunner runner = new StringAutomatonRunner();
		assertFalse(runner.process(a, ""));
		assertTrue(runner.process(a, "a"));
		assertTrue(runner.process(a, "b"));
		assertFalse(runner.process(a, "a,b"));
		assertFalse(runner.process(a, "b,a"));
		a.determinize();
		assertFalse(runner.process(a, ""));
		assertTrue(runner.process(a, "a"));
		assertTrue(runner.process(a, "b"));
		assertFalse(runner.process(a, "a,b"));
		assertFalse(runner.process(a, "b,a"));
	}
	
	@Test
	public void testConcat() throws GrammarException, IOException, ParseException{
		String regexp = "a,b,c";
		StringAutomatonBuilder b = new StringAutomatonBuilder();
		Automaton a = b.build(regexp);
		StringAutomatonRunner runner = new StringAutomatonRunner();
		assertFalse(runner.process(a, ""));
		assertFalse(runner.process(a, "a"));
		assertFalse(runner.process(a, "b"));
		assertTrue(runner.process(a, "a,b,c"));
		assertFalse(runner.process(a, "a,b,b"));
		assertFalse(runner.process(a, "b,a"));
		a.determinize();
		assertFalse(runner.process(a, ""));
		assertFalse(runner.process(a, "a"));
		assertFalse(runner.process(a, "b"));
		assertTrue(runner.process(a, "a,b,c"));
		assertFalse(runner.process(a, "a,b,b"));
		assertFalse(runner.process(a, "b,a"));
	}
	
	@Test
	public void testConcatAndUnion() throws GrammarException, IOException, ParseException{
		String regexp = "(a|b),c";
		StringAutomatonBuilder b = new StringAutomatonBuilder();
		Automaton a = b.build(regexp);
		StringAutomatonRunner runner = new StringAutomatonRunner();
		assertFalse(runner.process(a, ""));
		assertFalse(runner.process(a, "a"));
		assertFalse(runner.process(a, "b"));
		assertTrue(runner.process(a, "a,c"));
		assertTrue(runner.process(a, "b,c"));
		assertFalse(runner.process(a, "a,b"));
		a.determinize();
		assertFalse(runner.process(a, ""));
		assertFalse(runner.process(a, "a"));
		assertFalse(runner.process(a, "b"));
		assertTrue(runner.process(a, "a,c"));
		assertTrue(runner.process(a, "b,c"));
		assertFalse(runner.process(a, "a,b"));
	}
	
	@Test
	public void testKleen() throws GrammarException, IOException, ParseException{
		String regexp = "a*";
		StringAutomatonBuilder b = new StringAutomatonBuilder();
		Automaton a = b.build(regexp);
		StringAutomatonRunner runner = new StringAutomatonRunner();
		assertTrue(runner.process(a, ""));
		assertTrue(runner.process(a, "a"));
		assertTrue(runner.process(a, "a,a"));
		assertTrue(runner.process(a, "a,a,a"));
		assertFalse(runner.process(a, "b"));
		System.out.println("NFA kleen" + a.toString());
		a.determinize();
		System.out.println("DFA kleen" + a.toString());
		assertTrue(runner.process(a, ""));
		assertTrue(runner.process(a, "a"));
		assertTrue(runner.process(a, "a,a"));
		assertTrue(runner.process(a, "a,a,a"));
		assertFalse(runner.process(a, "b"));
	}
	
	@Test
	public void testOptional() throws GrammarException, IOException, ParseException{
		String regexp = "a?";
		StringAutomatonBuilder b = new StringAutomatonBuilder();
		Automaton a = b.build(regexp);
		StringAutomatonRunner runner = new StringAutomatonRunner();
		assertTrue(runner.process(a, ""));
		assertTrue(runner.process(a, "a"));
		assertFalse(runner.process(a, "a,a"));
		assertFalse(runner.process(a, "a,a,a"));
		assertFalse(runner.process(a, "b"));
		a.determinize();
		assertTrue(runner.process(a, ""));
		assertTrue(runner.process(a, "a"));
		assertFalse(runner.process(a, "a,a"));
		assertFalse(runner.process(a, "a,a,a"));
		assertFalse(runner.process(a, "b"));
	}
	@Test
	public void testOneOrMany() throws GrammarException, IOException, ParseException{
		String regexp = "a+";
		StringAutomatonBuilder b = new StringAutomatonBuilder();
		Automaton a = b.build(regexp);
		//assertEquals(a.getStart().getTransitions(Automaton.getEps()).size(),2);
		StringAutomatonRunner runner = new StringAutomatonRunner();
		assertFalse(runner.process(a, ""));
		assertTrue(runner.process(a, "a"));
		assertTrue(runner.process(a, "a,a"));
		assertTrue(runner.process(a, "a,a,a"));
		assertFalse(runner.process(a, "b"));
		a.determinize();
		assertFalse(runner.process(a, ""));
		assertTrue(runner.process(a, "a"));
		assertTrue(runner.process(a, "a,a"));
		assertTrue(runner.process(a, "a,a,a"));
		assertFalse(runner.process(a, "b"));
	}
	
	@Test
	public void testDeterminizeRedondant() throws GrammarException, IOException, ParseException{
		String regexp = "a|a*";
		StringAutomatonBuilder b = new StringAutomatonBuilder();
		Automaton a = b.build(regexp);
		assertEquals(a.getSize(),8);
		assertFalse(a.checkdeterminist());
		a.determinize();
		assertTrue(a.checkdeterminist());
		assertEquals(a.getSize(),3);
	}
	*/
	@Test
	public void testClosure(){
		State s1 = new State();
		State s2 = new State();
		State s3 = new State();
		State s4 = new State();
		State s5 = new State();
		Event eps = (Event) Automaton.getEps();
		s1.addTransition(eps, s2);
		s2.addTransition(eps, s3);
		// create a cycle to make sure the algorithm will not loop
		s2.addTransition(eps, s1);
		s3.addTransition(q1, s4);
		s4.addTransition(eps, s5);
		Automaton a = new Automaton();
		a.setStart(s1);
		a.setEnd(s4);
		Set<State> x = a.getClosure(s1);
		assertTrue(x.size()==3);
		State[] y = {s1,s2,s3};
		assertTrue(x.containsAll(Arrays.asList(y)));

	}
	

	@Test
	public void testDeterminize(){
		// (a|b)*aa
		ArrayList<State> s = new ArrayList<State>();
		for (int i=0;i<10;i++){
			s.add(new State(i));
		}
		
		Event eps = (Event) Automaton.getEps();
		s.get(0).addTransition(eps, s.get(1));
		s.get(0).addTransition(eps, s.get(7));
		s.get(1).addTransition(eps, s.get(4));
		s.get(1).addTransition(eps, s.get(2));
		s.get(2).addTransition(q1, s.get(3));
		s.get(4).addTransition(q2, s.get(5));
		s.get(3).addTransition(eps, s.get(6));
		s.get(5).addTransition(eps, s.get(6));
		s.get(6).addTransition(eps, s.get(1));
		s.get(6).addTransition(eps, s.get(7));
		s.get(7).addTransition(q1, s.get(8));
		s.get(8).addTransition(q1, s.get(9));
		
		Automaton a = new Automaton();
		a.setStart(s.get(0));
		a.setEnd(s.get(9));
		Set<State> x = a.getClosure(s.get(0));
		Set<State> y = a.move(x, q1);
		Set<State> n = a.getClosure(y);
		
		assertTrue(y.size()==2);
		State[] z = {s.get(3),s.get(8)};
		assertTrue(y.containsAll(Arrays.asList(z)));
		
		x = a.getClosure(s.get(0));
		y = a.move(x, q2);
		assertTrue(y.size()==1);
		State[] zz = {s.get(5)};
		assertTrue(y.containsAll(Arrays.asList(zz)));
		
		//System.out.println("NFA " + a.toString());
		a.determinize();
		//System.out.println("DFA " + a.toString());
		assertTrue(a.getSize()==4);
	}
	
	@Test
	public void testRegExpEmpty(){
		RegExp re = RegExp.makeEmpty();
		List<Symbol> content = new ArrayList<Symbol>();
		assertTrue(re.match(content));
	}
	
	@Test
	public void testRegExpSingle(){
		RegExp re = RegExp.makeSingle(q1);
		List<Symbol> content = new ArrayList<Symbol>();
		assertFalse(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q1);
		assertFalse(re.match(content));
		content.clear();
		content.add(q2);
		assertFalse(re.match(content));
	}

	@Test
	public void testRegExpConcat(){
		RegExp re = RegExp.makeConcat(RegExp.makeSingle(q1), RegExp.makeSingle(q2));
		List<Symbol> content = new ArrayList<Symbol>();
		assertFalse(re.match(content));
		content.add(q1);
		assertFalse(re.match(content));
		content.add(q2);
		assertTrue(re.match(content));
		content.clear();
		content.add(q1);
		content.add(q1);
		assertFalse(re.match(content));
	}

	@Test
	public void testRegExpUnion(){
		RegExp re = RegExp.makeUnion(RegExp.makeSingle(q1), RegExp.makeSingle(q2));
		List<Symbol> content = new ArrayList<Symbol>();
		assertFalse(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q2);
		assertFalse(re.match(content));
		content.clear();
		content.add(q2);
		assertTrue(re.match(content));
		content.add(q1);
		assertFalse(re.match(content));
	}
	
	@Test
	public void testRegExpKleen(){
		RegExp re = RegExp.makeSingle(q1);
		re = RegExp.makeKleen(re);
		List<Symbol> content = new ArrayList<Symbol>();
		assertTrue(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q2);
		assertFalse(re.match(content));
	}
	
	@Test
	public void testRegExpKleen2(){
		RegExp r1 = RegExp.makeSingle(q1);
		RegExp r2 = RegExp.makeSingle(q2);
		RegExp re = RegExp.makeConcat(r1, r2);
		re = RegExp.makeKleen(re);
		
		List<Symbol> content = new ArrayList<Symbol>();
		assertTrue(re.match(content));
		content.add(q1);
		assertFalse(re.match(content));
		content.add(q2);
		assertTrue(re.match(content));
		content.add(q1);
		assertFalse(re.match(content));
		content.add(q2);
		assertTrue(re.match(content));
	}

	@Test
	public void testRegExpOneOrMore(){
		RegExp re = RegExp.makeSingle(q1);
		re = RegExp.makeOneOrMore(re);
		List<Symbol> content = new ArrayList<Symbol>();
		assertFalse(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q2);
		assertFalse(re.match(content));
	}
	
	@Test
	public void testRegExpOptional(){
		RegExp re = RegExp.makeSingle(q1);
		re = RegExp.makeOptional(re);
		List<Symbol> content = new ArrayList<Symbol>();
		assertTrue(re.match(content));
		content.add(q1);
		assertTrue(re.match(content));
		content.add(q1);
		assertFalse(re.match(content));
		content.clear();
		content.add(q2);
		assertFalse(re.match(content));
	}
}

