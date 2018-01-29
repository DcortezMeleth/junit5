/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.platform.commons.annotation.ExecutionMode;

class NodeWalker {

	private final LockManager lockManager;

	NodeWalker(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	public <C extends EngineExecutionContext> void walk(NodeExecutor<C> nodeExecutor) {
		if (nodeExecutor.getNode().getExclusiveResources().isEmpty()) {
			nodeExecutor.getChildren().forEach(this::walk);
		}
		else {
			Set<ExclusiveResource> allResources = new HashSet<>(nodeExecutor.getNode().getExclusiveResources());
			doForChildrenRecursively(nodeExecutor, child -> {
				allResources.addAll(child.getNode().getExclusiveResources());
				child.setForcedExecutionMode(ExecutionMode.SameThread);
			});
			nodeExecutor.setResourceLock(lockManager.getLockForResources(allResources));
		}
	}

	private <C extends EngineExecutionContext> void doForChildrenRecursively(NodeExecutor<C> parent,
			Consumer<NodeExecutor<C>> consumer) {
		parent.getChildren().forEach(child -> {
			consumer.accept(child);
			doForChildrenRecursively(child, consumer);
		});
	}

}