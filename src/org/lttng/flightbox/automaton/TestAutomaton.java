package org.lttng.flightbox.automaton;

import org.junit.Test;

public class TestAutomaton {

	@Test
	public void testFileAutomaton() {
		State starting = new State("STARTING");
		State opened = new State("OPENED");
		State closed = new State("CLOSED");
		State seek = new State("SEEK");
		State read = new State("READ");
		State write = new State("WRITE");

		Event fsOpen = new Event("FS_OPEN");
		Event fsClose = new Event("FS_CLOSE");
		Event fsSeek = new Event("FS_SEEK");
		Event fsRead = new Event("FS_READ");
		Event fsWrite = new Event("FS_WRITE");

		starting.addTransition(fsOpen, opened);
		starting.addTransition(fsClose, closed);
		starting.addTransition(fsSeek, seek);
		starting.addTransition(fsRead, read);
		starting.addTransition(fsWrite, write);

		opened.addTransition(fsClose, closed);
		opened.addTransition(fsRead, read);
		opened.addTransition(fsWrite, write);
		opened.addTransition(fsSeek, seek);

		write.addTransition(fsClose, closed);
		write.addTransition(fsRead, read);
		write.addTransition(fsWrite, write);
		write.addTransition(fsSeek, seek);

		read.addTransition(fsClose, closed);
		read.addTransition(fsRead, read);
		read.addTransition(fsWrite, write);
		read.addTransition(fsSeek, seek);

		seek.addTransition(fsClose, closed);
		seek.addTransition(fsRead, read);
		seek.addTransition(fsWrite, write);
		seek.addTransition(fsSeek, seek);

		Automaton a = new Automaton();
		a.setStart(starting);
		System.out.println("original:");
		System.out.println(a.toString());
		System.out.println(a.getSizeWithTransitions());
		a.determinize();
		System.out.println("deterministic:");
		System.out.println(a.toString());
		System.out.println(a.getSizeWithTransitions());
	}

}
