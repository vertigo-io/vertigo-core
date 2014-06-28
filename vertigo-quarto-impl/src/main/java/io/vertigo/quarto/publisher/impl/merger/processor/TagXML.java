package io.vertigo.quarto.publisher.impl.merger.processor;

/**
 * Represente une balise XML pour faciliter les traitements des processor.
 * Contient le tag entier, son nom, sa position dans le XMl d'ou il est tir�,
 * si c'est une balise ouvrante et si il contenait un body.
 * @author npiedeloup
 * @version $Id: TagXML.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public final class TagXML {
	private final String fullTag;
	private final String tagName;
	private final int index;
	private final boolean openTag;
	private final boolean tagHasBody;

	/**
	 * Constructeur.
	 * @param fullTag Tag complet
	 * @param index index de sa position dans le XML d'ou il est tir�
	 */
	TagXML(final String fullTag, final int index) {
		this.fullTag = fullTag;
		tagName = getTagName(fullTag);
		this.index = index;
		openTag = !fullTag.startsWith("</");
		tagHasBody = !fullTag.endsWith("/>");
	}

	/**
	 * @return nom du tag
	 */
	public String getName() {
		return tagName;
	}

	/**
	 * @return tag complet (avec les < >)
	 */
	public String getFullTag() {
		return fullTag;
	}

	/**
	 * @return index de la position du tag dans le XML d'ou il est tir�
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return longueur du tag
	 */
	public int getLength() {
		return fullTag.length();
	}

	/**
	 * @return Si tag ouvrant
	 */
	public boolean isOpenTag() {
		return openTag;
	}

	/**
	 * @return Si le tag contenait un body
	 */
	public boolean hasBody() {
		return tagHasBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getFullTag() + ':' + getIndex();
	}

	private String getTagName(final String theFullTag) {
		if (theFullTag.startsWith("</")) {
			return theFullTag.substring(2, theFullTag.indexOf('>'));
		}
		int endIndex = theFullTag.indexOf(' ');
		if (endIndex == -1) {
			endIndex = theFullTag.indexOf('>');
		}
		return theFullTag.substring(1, endIndex);
	}
}
