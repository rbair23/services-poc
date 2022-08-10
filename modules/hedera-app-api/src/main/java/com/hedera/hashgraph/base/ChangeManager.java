package com.hedera.hashgraph.base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ChangeManager {
	private List<Runnable> commitList = new LinkedList<>();

	public ChangeManager() {

	}

	public void addChange(Runnable r) {
		commitList.add(r);
	}

	public void commit() {
		for (final var runner : commitList) {
			runner.run(); // todo what if null
		}
		commitList.clear();
	}
}
