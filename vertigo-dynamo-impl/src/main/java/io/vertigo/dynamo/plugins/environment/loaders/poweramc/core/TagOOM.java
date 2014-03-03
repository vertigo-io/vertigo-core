package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

/**
 *
 * @author pchretien
 * @version $Id: TagOOM.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
final class TagOOM {
	private final TagOOM parent;
	private final ObjectOOM currentOOM;
	private final ObjectOOM parentOOM;

	private TagOOM(final TagOOM parent, final ObjectOOM parentOOM, final ObjectOOM currentOOM) {
		this.parent = parent;
		this.currentOOM = currentOOM;
		this.parentOOM = parentOOM;
	}

	TagOOM getParent() {
		return parent;
	}

	ObjectOOM getCurrentOOM() {
		return currentOOM;
	}

	ObjectOOM getParentOOM() {
		return parentOOM;
	}

	//Le parent change et il y a un Tag courant
	TagOOM createTag(final ObjectOOM newCurrentOOM) {
		return new TagOOM(this, newCurrentOOM, newCurrentOOM);
	}

	//Le parent ne change pas et il n'y a plus de courant
	TagOOM createTag() {
		return new TagOOM(this, this.parentOOM, null);
	}

	static TagOOM createRootTag(final ObjectOOM root) {
		return new TagOOM(null, root, null);
	}
}
