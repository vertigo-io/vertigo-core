# Vertigo-Core Developer Guide

> Guide de reference pour les developpeurs travaillant sur vertigo-core v5.0.0

---

## Table des matieres

1. [Vue d'ensemble](#1-vue-densemble)
2. [Architecture du Node](#2-architecture-du-node)
3. [Systeme de composants (CoreComponent)](#3-systeme-de-composants)
4. [Systeme de definitions](#4-systeme-de-definitions)
5. [Injection de dependances](#5-injection-de-dependances)
6. [AOP (Programmation orientee aspects)](#6-aop)
7. [Configuration](#7-configuration)
8. [Services integres (Managers)](#8-services-integres)
9. [Langage et utilitaires](#9-langage-et-utilitaires)
10. [Plugins fournis](#10-plugins-fournis)
11. [Propositions d'ameliorations](#11-propositions-dameliorations)

---

## 1. Vue d'ensemble

Vertigo-core est le noyau de la plateforme Vertigo. Il fournit :
- Un **conteneur de composants** leger avec injection de dependances (sans framework externe)
- Un **systeme de definitions** pour les modeles metier/techniques (immutables, charges au boot)
- Un support **AOP** via Javassist (proxies dynamiques)
- Des **services integres** : analytics, daemons, parametres, ressources, i18n
- Une **configuration fluent** via builders ou YAML

### Packages principaux

```
io.vertigo.core
├── analytics/          Tracing, metriques, health checks
├── daemon/             Taches planifiees en arriere-plan
├── impl/               Implementations internes des managers
├── lang/               Types fondamentaux, assertions, exceptions
│   └── json/           Adaptateurs JSON (GSON)
├── locale/             Internationalisation (i18n)
├── node/               Coeur du framework
│   ├── component/      Interfaces des composants + DI + AOP
│   ├── config/         Configuration (NodeConfig, ModuleConfig, YAML)
│   └── definition/     Registre des definitions
├── param/              Gestion des parametres de configuration
├── plugins/            Implementations des plugins
├── resource/           Resolution de ressources (classpath, fichier, URL)
└── util/               Utilitaires (reflection, dates, strings, XML)
```

**162 fichiers source Java** | **251 tests unitaires** | Java 21 | Maven

---

## 2. Architecture du Node

Le **Node** est le point d'entree central de Vertigo. Il porte tout le cycle de vie de l'application.

### `Node` (interface) - `io.vertigo.core.node`

```
o--->[starting]--->[active]--->[stopping]--->[closed]
```

| Methode | Description |
|---------|-------------|
| `getNode()` | Acces statique au node courant (singleton) |
| `getDefinitionSpace()` | Registre de toutes les definitions |
| `getComponentSpace()` | Registre de tous les composants |
| `getNodeConfig()` | Configuration du node |
| `getStart()` | Timestamp de demarrage |
| `registerPreActivateFunction(Runnable)` | Hook pre-activation |

### `AutoCloseableNode` - `io.vertigo.core.node`

Implementation concrete du Node qui implemente `AutoCloseable` pour un usage en try-with-resources :

```java
try (AutoCloseableNode node = new AutoCloseableNode(nodeConfig)) {
    // L'application est active ici
    Node.getNode().getComponentSpace().resolve(MyManager.class);
}
// Arret propre automatique
```

### Structure interne du Node

```
Node
├── NodeConfig          Configuration complete
├── DefinitionSpace     Registre des definitions (immutable apres boot)
│   └── Definition...   Elements du modele metier/technique
└── ComponentSpace      Registre des composants (singletons, thread-safe)
    ├── Manager...      Composants techniques (analytics, params, etc.)
    ├── Component...    Composants applicatifs
    ├── Plugin...       Strategies interchangeables (scope module)
    ├── Connector...    Ponts vers des librairies externes
    └── Amplifier...    Proxies generes automatiquement
```

---

## 3. Systeme de composants

### Hierarchie `CoreComponent`

`CoreComponent` est l'interface racine de tous les composants Vertigo. Il existe **4 types** de composants :

```
CoreComponent (marker interface)
├── Component       Composant general
│   └── Manager     Marqueur pour les composants techniques Vertigo
├── Plugin          Implementation de strategie (scope module)
├── Connector<C>    Pont vers une librairie externe
└── Amplifier       Proxy genere automatiquement (pas d'implementation)
```

### Tableau comparatif

| Type | API | Impl | Scope | Injectable dans |
|------|-----|------|-------|-----------------|
| **Component** | optionnelle | requise | node | tous les core-components |
| **Amplifier** | requise | non (proxy) | node | tous les core-components |
| **Plugin** | requise | requise | module | uniquement les components |
| **Connector** | non | requise | node | tous les core-components |

### `Component` - `io.vertigo.core.node.component`

Composant modulaire general. Peut avoir une interface API et une implementation.

```java
// Declaration dans la config
ModuleConfig.builder("myModule")
    .addComponent(MyService.class, MyServiceImpl.class)
    .build();

// Injection dans un autre composant
@Inject
private MyService myService;
```

### `Manager` - `io.vertigo.core.node.component`

Marqueur (`extends Component`) identifiant les composants techniques du framework. Exemples : `AnalyticsManager`, `ParamManager`, `DaemonManager`, `ResourceManager`, `LocaleManager`.

### `Plugin` - `io.vertigo.core.node.component`

Encapsule une strategie interchangeable. Concentre les dependances vers une librairie specifique. **Thread-safe et singleton.**

```java
// Exemple : plusieurs plugins de resolution de parametres
ModuleConfig.builder("params")
    .addPlugin(EnvParamPlugin.class)
    .addPlugin(PropertiesParamPlugin.class, Param.of("url", "config.properties"))
    .build();
```

### `Connector<C>` - `io.vertigo.core.node.component`

Pont generique vers une librairie/produit externe. Expose un client via `getClient()`. Supporte le nommage pour differencier plusieurs instances.

```java
public interface Connector<C> extends CoreComponent {
    String DEFAULT_CONNECTOR_NAME = "main";
    default String getName() { return DEFAULT_CONNECTOR_NAME; }
    C getClient();
}
```

### `Amplifier` - `io.vertigo.core.node.component`

Composant defini uniquement par son API (interface). L'implementation est un **proxy Java** genere automatiquement a partir d'annotations. Cas d'usage : clients SQL, web services, Redis.

### `Activeable` - `io.vertigo.core.node.component`

Interface de cycle de vie. Tout composant (Component, Plugin, Connector) peut l'implementer.

```java
public interface Activeable {
    void start();  // Appele apres creation et injection
    void stop();   // Appele avant destruction
}
```

### `ComponentSpace` - `io.vertigo.core.node.component`

Registre central des composants. Acces thread-safe par type ou par identifiant.

```java
// Acces par type
MyManager mgr = Node.getNode().getComponentSpace().resolve(MyManager.class);

// Verification d'existence
boolean exists = componentSpace.contains("myComponent");
```

---

## 4. Systeme de definitions

### `Definition` (interface) - `io.vertigo.core.node.definition`

Represente un element du modele metier ou technique. **Immutable**, charge au demarrage, identifie de maniere unique.

```java
public interface Definition {
    DefinitionId id();                    // Identifiant unique
    default String getName() {            // Nom = prefix + shortName
        return id().fullName();
    }
}
```

### `DefinitionId` - `io.vertigo.core.node.definition`

Identifiant unique compose d'un **prefix** (lie au type de definition) et d'un **shortName**.

Convention de nommage : `PrefixShortName` (ex : `DtPerson`, `TkLoadData`)

### `@DefinitionPrefix` - `io.vertigo.core.node.definition`

Annotation obligatoire sur les classes de definitions pour definir le prefix de nommage.

```java
@DefinitionPrefix("Dt")
public class DtDefinition extends AbstractDefinition { ... }
```

### `AbstractDefinition` - `io.vertigo.core.node.definition`

Classe de base pour les definitions avec gestion de l'identifiant.

### `DefinitionSpace` - `io.vertigo.core.node.definition`

Registre central de toutes les definitions. Fige apres le boot.

```java
DefinitionSpace defSpace = Node.getNode().getDefinitionSpace();
DtDefinition dtDef = defSpace.resolve("DtPerson", DtDefinition.class);
Set<DtDefinition> allDts = defSpace.getAll(DtDefinition.class);
```

### `DefinitionProvider` (interface) - `io.vertigo.core.node.definition`

Fournisseur de definitions (factory). Permet le chargement dynamique au boot.

### `SimpleDefinitionProvider` / `SimpleEnumDefinitionProvider`

Implementations simplifiees pour fournir des definitions via une liste ou un enum.

---

## 5. Injection de dependances

Le DI de Vertigo est **interne** (pas de Spring, Guice, CDI). Il utilise `jakarta.inject.@Inject`.

### `DIInjector` - `io.vertigo.core.node.component.di`

Coeur du systeme d'injection. Supporte :
- **Injection par constructeur** (prefere)
- **Injection par champ**
- **Types supportes** : simple, `Optional<T>`, `List<T>`

```java
public class MyServiceImpl implements MyService {
    @Inject
    public MyServiceImpl(
            ParamManager paramManager,           // Injection simple
            Optional<CachePlugin> cachePlugin,    // Injection optionnelle
            List<DataPlugin> dataPlugins) {       // Injection de liste
        // ...
    }
}
```

### `@ParamValue` - `io.vertigo.core.param`

Annotation pour injecter des parametres de configuration nommes :

```java
@Inject
public MyPlugin(@ParamValue("connectionName") String connectionName,
                @ParamValue("timeout") int timeout) { ... }
```

### `DIReactor` - `io.vertigo.core.node.component.di`

Resout l'ordre d'instanciation des composants en analysant le graphe de dependances.

### `DIDependency` - `io.vertigo.core.node.component.di`

Represente une dependance a resoudre. Gere le "unwrapping" de `Optional<T>` et `List<T>`.

---

## 6. AOP

### `Aspect` - `io.vertigo.core.node.component.aspect`

Interception transversale (logging, transactions, securite, monitoring).

### `@AspectAnnotation`

Meta-annotation qui marque une annotation comme declencheur d'aspect.

```java
@AspectAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional { }
```

### `AspectPlugin` - `io.vertigo.core.node.component`

Interface pour le moteur de proxy AOP.

```java
public interface AspectPlugin extends Plugin {
    <C> C wrap(C instance, Map<Method, List<Aspect>> joinPoints);
    <C> C unwrap(C component);
}
```

**Implementation fournie** : `JavassistAspectPlugin` (via Javassist)

### `AspectMethodInvocation` - `io.vertigo.core.node.component.aspect`

Contexte d'invocation dans un aspect. Permet le chainage des aspects.

```java
public Object invoke(Object[] args) {
    // Appelle le prochain aspect ou la methode reelle
    return proceed(args);
}
```

### `AmplifierMethod` / `@AmplifierMethodAnnotation`

Equivalent AOP pour les Amplifiers. L'annotation marque une methode d'amplifier, le `AmplifierMethod` definit le comportement a generer.

---

## 7. Configuration

### Hierarchie de configuration

```
NodeConfig                    Configuration globale du node
├── appName                   Nom de l'application
├── nodeId                    UUID unique du node
├── endPointOpt               Endpoint optionnel
├── activeFlags               Feature flags actifs
├── BootConfig                Configuration de demarrage
│   ├── LogConfig             Configuration des logs
│   └── coreComponentConfigs  Composants de boot
└── ModuleConfig[]            Modules de l'application
    ├── componentConfigs      Composants
    ├── pluginConfigs         Plugins
    ├── connectorConfigs      Connecteurs
    ├── amplifierConfigs      Amplifiers
    ├── aspectConfigs         Aspects
    ├── amplifierMethodConfigs
    └── definitionProviderConfigs
```

### `NodeConfig` (record) - `io.vertigo.core.node.config`

```java
NodeConfig nodeConfig = NodeConfig.builder()
    .withAppName("MyApp")
    .withEndPoint("http://localhost:8080")
    .addModule(moduleConfig)
    .build();
```

### `ModuleConfig` (record) - `io.vertigo.core.node.config`

```java
ModuleConfig module = ModuleConfig.builder("myModule")
    .addComponent(MyService.class, MyServiceImpl.class)
    .addPlugin(MyPlugin.class, Param.of("key", "value"))
    .addConnector(MyConnector.class)
    .addAmplifier(MyAmplifier.class)
    .addAspect(MyAspect.class)
    .addDefinitionProvider(MyDefProvider.class)
    .build();
```

### `Features<F>` (abstract) - `io.vertigo.core.node.config`

Classe de base pour declarer des modules via une API fluent. Les sous-classes implementent `buildFeatures()`.

```java
public class MyFeatures extends Features<MyFeatures> {
    public MyFeatures() { super("myModule"); }

    @Override
    protected void buildFeatures() {
        getModuleConfigBuilder()
            .addComponent(MyService.class, MyServiceImpl.class);
    }

    public MyFeatures withCache() {
        getModuleConfigBuilder().addPlugin(CachePlugin.class);
        return this;
    }
}

// Utilisation
NodeConfig.builder()
    .addModule(new MyFeatures().withCache().build())
    .build();
```

### Configuration YAML

`YamlNodeConfigBuilder` permet le chargement depuis des fichiers YAML :

```java
NodeConfig config = new YamlNodeConfigBuilder(
    Map.of("boot", "boot.yaml", "app", "app.yaml"))
    .withActiveFlags(Set.of("main"))
    .build();
```

Supporte les **flags** pour activer/desactiver des sections conditionnellement.

### `@Feature` - `io.vertigo.core.node.config`

Annotation declarant une feature dans le manifeste.

### `ComponentDiscovery` - `io.vertigo.core.node.config.discovery`

Decouverte automatique de composants par scan du classpath.

### `@NotDiscoverable` - `io.vertigo.core.node.config.discovery`

Exclut un composant de la decouverte automatique.

---

## 8. Services integres

### `AnalyticsManager` - `io.vertigo.core.analytics`

Service central de monitoring : tracing, metriques, health checks.

```java
@Inject private AnalyticsManager analyticsManager;

// Tracer un processus
analyticsManager.trace("sql", "selectUsers", tracer -> {
    tracer.incMeasure("nbRows", result.size());
    // ... execution
});

// Tracer avec retour
List<User> users = analyticsManager.traceWithReturn("sql", "selectUsers", tracer -> {
    return dao.findAll();
});

// Health checks
List<HealthCheck> checks = analyticsManager.getHealthChecks();
HealthStatus status = analyticsManager.aggregate(checks);

// Metriques
List<Metric> metrics = analyticsManager.getMetrics();
```

#### Sous-concepts analytics

| Classe | Package | Description |
|--------|---------|-------------|
| `Tracer` | `analytics.trace` | Contexte de trace actif (incMeasure, setTag, setMetadata) |
| `TraceSpan` | `analytics.trace` | Span complete avec duree, mesures, tags |
| `@Trace` | `analytics.trace` | Annotation pour tracer automatiquement une methode |
| `HealthCheck` | `analytics.health` | Resultat d'un controle de sante |
| `HealthMeasure` | `analytics.health` | Mesure de sante (GREEN/YELLOW/RED) |
| `HealthStatus` | `analytics.health` | Statut agrege (GOOD/CAUTION/FAILURE) |
| `@HealthChecked` | `analytics.health` | Annotation marquant un composant avec health check |
| `Metric` | `analytics.metric` | Mesure metrique (nom, valeur, status) |
| `@Metrics` | `analytics.metric` | Annotation marquant un provider de metriques |

### `DaemonManager` - `io.vertigo.core.daemon`

Gestion des taches planifiees en arriere-plan.

```java
// Declaration via annotation
public class MyDaemon implements Daemon {
    @DaemonScheduled(name = "DmnMyTask", periodInSeconds = 60)
    public void execute() { ... }
}

// Acces aux statistiques
List<DaemonStat> stats = daemonManager.getStats();
```

| Classe | Description |
|--------|-------------|
| `Daemon` | Interface d'un daemon |
| `@DaemonScheduled` | Annotation de planification |
| `DaemonStat` | Statistiques d'execution |
| `DaemonDefinition` | Definition d'un daemon |

### `ParamManager` - `io.vertigo.core.param`

Acces aux parametres de configuration depuis plusieurs sources.

```java
@Inject private ParamManager paramManager;

String dbUrl = paramManager.getParam("db.url").getValueAsString();
int port = paramManager.getParam("server.port").getValueAsInt();
Optional<Param> opt = paramManager.getOptionalParam("feature.flag");
```

| Classe | Description |
|--------|-------------|
| `Param` | Parametre type (String, int, long, boolean) |
| `@ParamValue` | Injection de parametre dans un constructeur |
| `ParamPlugin` | Interface de source de parametres |

### `ResourceManager` - `io.vertigo.core.resource`

Resolution de ressources par schema (classpath:, file:, http:).

```java
@Inject private ResourceManager resourceManager;
URL url = resourceManager.resolve("classpath:config/app.yaml");
```

### `LocaleManager` - `io.vertigo.core.locale`

Internationalisation avec support locale et timezone.

```java
@Inject private LocaleManager localeManager;
localeManager.add("io.vertigo.myapp.Messages", MessageKey.values());
String msg = localeManager.getMessage(MessageKey.HELLO, Locale.FRENCH);
```

| Classe | Description |
|--------|-------------|
| `LocaleMessageKey` | Interface pour les cles de messages (typiquement un enum) |
| `LocaleMessageText` | Texte localise avec parametres |

---

## 9. Langage et utilitaires

### `Assertion` - `io.vertigo.core.lang`

**Design by Contract** (inspire de B. Meyer / Eiffel). API fluent pour la validation runtime.

```java
Assertion.check()
    .isNotNull(object, "Object {0} required", name)
    .isNotBlank(str)
    .isTrue(value > 0, "Positive value required")
    .isFalse(closed, "Must not be closed")
    .when(optional != null, () -> Assertion.check()
        .isTrue(optional.isPresent(), "Must be present"));
```

| Methode | Exception levee |
|---------|-----------------|
| `isNotNull` / `isNull` | `NullPointerException` / `IllegalArgumentException` |
| `isTrue` / `isFalse` | `IllegalStateException` |
| `isNotBlank` | `IllegalArgumentException` |
| `when` | Conditionnel |
| `isValid` | Composition |

### `BasicType` (enum) - `io.vertigo.core.lang`

Systeme de types fondamentaux de Vertigo :

| Valeur | Classe Java | Categorie |
|--------|-------------|-----------|
| `Integer` | `Integer` / `int` | nombre |
| `Long` | `Long` / `long` | nombre |
| `Double` | `Double` / `double` | nombre |
| `BigDecimal` | `BigDecimal` | nombre |
| `Boolean` | `Boolean` / `boolean` | - |
| `String` | `String` | - |
| `LocalDate` | `LocalDate` | date |
| `Instant` | `Instant` | date |
| `DataStream` | `DataStream` | - |

Methodes utiles : `isAboutDate()`, `isNumber()`, `of(Class)`, `getJavaClass()`

### `Cardinality` (enum) - `io.vertigo.core.lang`

Cardinalite des champs : symbolise la multiplicite.

### `Builder<T>` (interface) - `io.vertigo.core.lang`

Interface marqueur pour le pattern Builder : `T build()`.

### `ListBuilder<T>` / `MapBuilder<K,V>` - `io.vertigo.core.lang`

Builders fluent pour construire des collections immutables.

```java
List<String> items = new ListBuilder<String>()
    .add("a")
    .addAll(otherList)
    .unmodifiable()
    .build();
```

### `Tuple<A, B>` (record) - `io.vertigo.core.lang`

Tuple generique immutable a deux elements : `val1()`, `val2()`.

### `DataStream` - `io.vertigo.core.lang`

Interface representant un flux de donnees avec nom de fichier, type MIME, longueur et `InputStream`.

### `TempFile` - `io.vertigo.core.lang`

Gestion securisee des fichiers temporaires.

### Exceptions

| Classe | Usage |
|--------|-------|
| `VSystemException` | Erreur systeme (unchecked) |
| `VUserException` | Erreur utilisateur/donnees (unchecked), supporte i18n via `LocaleMessageText` |
| `WrappedException` | Encapsule une checked exception en unchecked |

### Utilitaires (`io.vertigo.core.util`)

| Classe | Fonctionnalites principales |
|--------|----------------------------|
| `ClassUtil` | Reflection : instanciation, getters/setters, annotations, generics |
| `StringUtil` | format (MessageFormat-like), isBlank, camelCase/constCase |
| `BeanUtil` | Acces aux proprietes JavaBean par reflection |
| `DateUtil` | Manipulation de dates (LocalDate, Instant) |
| `DateQueryParserUtil` | Parsing de requetes de dates ("now-1d", "today+2m") |
| `FileUtil` | Operations sur fichiers |
| `XmlUtil` | Parsing XML (SAX) |
| `InjectorUtil` | Injection utilitaire hors conteneur |

### JSON (`io.vertigo.core.lang.json`)

| Classe | Description |
|--------|-------------|
| `CoreJsonAdapters` | Adaptateurs GSON pour les types Vertigo |
| `@JsonExclude` | Exclut un champ de la serialisation JSON |
| `UTCDateUtil` | Serialisation de dates en UTC |

### Annotations diverses

| Annotation | Package | Description |
|-----------|---------|-------------|
| `@Generated` | `lang` | Marque du code genere |
| `@Feature` | `node.config` | Declare une feature |
| `@NotDiscoverable` | `node.config.discovery` | Exclut de la decouverte auto |

---

## 10. Plugins fournis

### Plugins Analytics

| Classe | Description |
|--------|-------------|
| `LoggerAnalyticsConnectorPlugin` | Export analytics vers les logs |
| `SmartLoggerAnalyticsConnectorPlugin` | Export intelligent (seuil, duree) |
| `SocketLoggerAnalyticsConnectorPlugin` | Export via socket TCP |
| `SocketLoggerJsonAnalyticsConnectorPlugin` | Export socket en JSON |

Support Log4j : `AnalyticsSocketAppender`, `AnalyticsTcpSocketManager`

### Plugins Parametres

| Classe | Source |
|--------|--------|
| `EnvParamPlugin` | Variables d'environnement |
| `SystemPropertyParamPlugin` | Proprietes systeme Java |
| `PropertiesParamPlugin` | Fichiers .properties |
| `XmlParamPlugin` | Fichiers XML |
| `ManifestParamPlugin` | MANIFEST.MF du JAR |

### Plugins Ressources

| Classe | Schema |
|--------|--------|
| `ClassPathResourceResolverPlugin` | `classpath:...` |
| `LocalResourceResolverPlugin` | `file:...` (chemins relatifs) |
| `URLResourceResolverPlugin` | `http://...` / `https://...` |
| `UnsafeURLResourceResolverPlugin` | URLs sans validation stricte |

### Plugin AOP

| Classe | Description |
|--------|-------------|
| `JavassistAspectPlugin` | Proxy dynamique via Javassist |

---

## 11. Propositions d'ameliorations

### Haute priorite

#### 1. Moderniser `BasicType.of()` avec le pattern matching Java 21

Le code actuel utilise une cascade de `if/else if`. Java 21 permet une ecriture plus lisible :

```java
// Actuel (BasicType.java:106-133)
if (Integer.class.equals(type) || int.class.equals(type)) {
    basicType = BasicType.Integer;
} else if (Double.class.equals(type) || double.class.equals(type)) {
    // ...
}

// Proposition : Map statique pour un lookup O(1)
private static final Map<Class<?>, BasicType> CLASS_TO_TYPE = Map.ofEntries(
    Map.entry(Integer.class, BasicType.Integer),
    Map.entry(int.class, BasicType.Integer),
    Map.entry(Double.class, BasicType.Double),
    Map.entry(double.class, BasicType.Double),
    // ...
);

public static Optional<BasicType> of(final Class<?> type) {
    return Optional.ofNullable(CLASS_TO_TYPE.get(type));
}
```

#### 2. Nettoyer le code commente dans `AnalyticsTcpSocketManager`

Le fichier contient une methode `byteArrayToHex()` commentee (lignes 282-288). Soit la restaurer si utile, soit la supprimer (l'historique est dans git).

#### 3. Clarifier le TODO dans `YamlConfigParams`

Le commentaire `TODO : a refaire?` (ligne 27) est vague. Le remplacer par une description precise du probleme ou une issue trackee, ou le supprimer si non pertinent.

#### 4. Ajouter le raw type manquant sur `BasicType.getJavaClass()`

```java
// Actuel (BasicType.java:95)
public Class getJavaClass() { ... }

// Proposition
public Class<?> getJavaClass() { ... }
```

Idem pour `BasicType.of(final Class type)` -> `BasicType.of(final Class<?> type)`

### Moyenne priorite

#### 5. Enrichir les tests de `LocaleManager`

Seulement 1 classe de test pour un service aussi central (i18n). Ajouter des tests pour :
- Changement dynamique de locale
- Fallback quand une cle est absente
- Parametres dans les messages localises
- Gestion des timezones

#### 6. Tests de concurrence

Aucun test ne valide le comportement thread-safe des composants (qui est pourtant un contrat de `CoreComponent`). Ajouter des tests avec des threads concurrents sur :
- `ComponentSpace.resolve()` en parallele
- `DefinitionSpace.resolve()` en parallele
- `AnalyticsManager.trace()` imbrique dans des threads
- `DaemonManager` sous charge

#### 7. Moderniser `FactoryData` en record

La classe interne `FactoryData` dans `AnalyticsTcpSocketManager` (lignes 471-495) peut etre convertie en record Java pour simplifier.

#### 8. Ajouter des annotations de nullabilite

Annoter les API publiques avec `@Nullable` / `@NonNull` (Jakarta ou JSpecify) pour une meilleure integration IDE et une detection des erreurs a la compilation.

#### 9. Enrichir la couverture de tests des configurations invalides

Ajouter des tests pour les cas d'erreur :
- Dependances cycliques entre composants
- Configurations dupliquees
- Plugins sans composant parent
- Parametres manquants ou de mauvais type

### Basse priorite

#### 10. Mettre a jour les annees de copyright

Certains fichiers ont des dates 2013-2021 ou 2013-2023 alors que le projet est actif en 2025. Automatiser la mise a jour via un plugin Maven (par ex. `license-maven-plugin`).

#### 11. Documenter le constructeur deprecie de `AnalyticsTcpSocketManager`

Le constructeur `@Deprecated` (ligne 113) ne precise pas de timeline de suppression. Ajouter `@Deprecated(since = "x.x", forRemoval = true)` et un commentaire Javadoc.

#### 12. Explorer les sealed interfaces (Java 17+)

La hierarchie `CoreComponent` pourrait beneficier de `sealed` pour restreindre les implementations possibles :

```java
public sealed interface CoreComponent
    permits Component, Plugin, Connector, Amplifier { }
```

Cela rendrait le contrat explicite au niveau du type system.

#### 13. Considerer un eventuel remplacement de Javassist

Javassist est stable mais en mode maintenance. Pour le long terme, evaluer :
- **ByteBuddy** : plus moderne, meilleure compatibilite Java 21+
- **java.lang.reflect.Proxy** : pour les cas simples (interfaces uniquement)

Cela ne necessiterait qu'un nouveau `AspectPlugin` grace a l'architecture modulaire existante.

---

## Schemas recapitulatifs

### Cycle de vie du Node

```
1. Configuration    NodeConfig.builder()...build()
2. Instanciation    ComponentSpaceLoader cree les composants + DI
3. Proxying         AspectPlugin wrappe les composants avec les aspects
4. Activation       Activeable.start() sur chaque composant
5. Definitions      DefinitionProviders chargent les definitions
6. Pre-activation   Hooks registerPreActivateFunction()
7. ACTIF            L'application fonctionne
8. Arret            Activeable.stop() en ordre inverse
9. FERME            Toutes les ressources sont liberees
```

### Design Patterns utilises

| Pattern | Ou | Exemple |
|---------|-----|---------|
| **Singleton** | Node | `Node.getNode()` |
| **Registry** | ComponentSpace, DefinitionSpace | Registre central |
| **DI** | DIInjector | `@Inject` |
| **Strategy** | Plugin | Implementations interchangeables |
| **Proxy** | Javassist AOP, Amplifier | Proxies dynamiques |
| **Builder** | Config, collections | `NodeConfig.builder()` |
| **Template Method** | `Features.buildFeatures()` | Configuration de modules |
| **Design by Contract** | `Assertion.check()` | Validation runtime |
| **Decorator** | Aspects | Enveloppement de methodes |
