package io.vertigo.arch;

/**
 * A named group of packages — either a {@link ArchModule} or an {@link ArchLib} —
 * that can appear as a dependency in {@link ArchModule#depends(ArchDep...)}.
 */
sealed interface ArchDep permits ArchLib, ArchModule {
	String name();

	String[] packagePatterns();
}
