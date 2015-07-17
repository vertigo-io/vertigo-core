package io.vertigo.core;

/**
 * @author pchretien
 */
public interface AppListener {
	//Phases
	//Start App
	//0. start Boot
	//1.a read parameters
	//1.b read definitions
	//1.c read components  (create and start all components)

	//2.a start Boot >> start engines ???? 
	//2.a start parameterSpace 
	//2.b start paramSpace
	//2.c start definitionSpace
	//2.d start componentSpace : postInit components (Initializer)

	//Stop App
	//stop componentSpace : stop all components (reverse order)
	//stop definitionSpace : clear definitions 
	//stop paramSpace : clear paramss
	//Stop Boot >> Stop engines

	//	void onPostBoot();

	void onPostStart();
}
