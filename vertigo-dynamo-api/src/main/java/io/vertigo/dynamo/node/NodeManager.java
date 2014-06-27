package io.vertigo.dynamo.node;

import io.vertigo.kernel.component.Manager;

import java.util.List;


/**
 * Gestion des Nodes distribu�s .
 * Ce manager poss�de des nodes sous la forme de plugins qui permettent de 
 *  - producer : produire des travaux....
 *  - consumer : consommer des travaux (c'est � dire les r�aliser)
 *  - supervisor : v�rifier le bon fonctionnement du syst�me
 * 
 * @author npiedeloup, pchretien
 * @version $Id: NodeManager.java,v 1.4 2013/11/15 15:31:33 pchretien Exp $
 */
public interface NodeManager extends Manager {
	/**
	 * @return Liste des noeuds
	 */
	List<Node> getNodes();
}
