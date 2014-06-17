package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

/**
 *
 * @author pchretien
 */
final class OOMTag {
	private final OOMTag parent;
	private final OOMObject currentOOM;
	private final OOMObject parentOOM;

	private OOMTag(final OOMTag parent, final OOMObject parentOOM, final OOMObject currentOOM) {
		this.parent = parent;
		this.currentOOM = currentOOM;
		this.parentOOM = parentOOM;
	}

	OOMTag getParent() {
		return parent;
	}

	OOMObject getCurrentOOM() {
		return currentOOM;
	}

	OOMObject getParentOOM() {
		return parentOOM;
	}

	//Le parent change et il y a un Tag courant
	OOMTag createTag(final OOMObject newCurrentOOM) {
		return new OOMTag(this, newCurrentOOM, newCurrentOOM);
	}

	//Le parent ne change pas et il n'y a plus de courant
	OOMTag createTag() {
		return new OOMTag(this, this.parentOOM, null);
	}

	static OOMTag createRootTag(final OOMObject root) {
		return new OOMTag(null, root, null);
	}
}
