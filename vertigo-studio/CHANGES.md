Version history
===============

Running 0.9.5-SNAPSHOT
----------------------

more to come :)

Release 0.9.4 - 2017/03/13
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-093-to-094)

__In Bold__ : Potential compatibility problems 
* [All] Code cleaning, refactoring and documenting (and Stream java8, Optionnal, Methods refs, ...)
* [All] Always use WrappedException (wrap & unwrap), and params order changed
* [All] Moved dsl classes from core to dynamo
* [Studio] Added multi databases crebase.sql scripts
* [Studio] Fixed #6 ([PerformanceMetricEngine] Failed to execute tasks after one exeption on transaction)
* [Studio] Renamed readForUpdate to readOneForUpdate
* [Studio] Added operations enum to generated classes



Release 0.9.3 - 2016/10/11
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-092-to-093)

__In Bold__ : Potential compatibility problems 
* [All] Code cleaning, refactoring and documenting
* __[Studio] Renamed Role enum to Roles__
* [Studio] Fixed stereotype package
* [Studio] Removed persistence property from dtDefinition
* [Studio] Fixed getUri annotations for JPA
* [Studio] Added FK to Entity in Fragments
* [Studio] Renamed stereotype Data to ValueObject (always default sterotype)
* [Studio] Added DataSpace annotation
* [Studio] Fixed KleeGroup/vertigo#72
* [Studio] Fixed KleeGroup/vertigo#68
* [Studio] Fixed replace Option by jdk Optional


Release 0.9.2 - 2016/06/28
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-091-to-092)

__In Bold__ : Potential compatibility problems 
* [All] Code cleaning, refactoring and documenting
* [All] Updated 3rd party libs versions (freemarker)
*  [Studio] Fixed #58 missing SQL primary key
*  [Studio] Added SQLServer support (use param `baseCible:SqlServer` of `SqlGeneratorPlugin`)
* __[Studio] Renamed CRUD methods__ (get => read , getList => findAll)
* __[Studio] Aligned vertigo Option api to JDK api__ (`isPresent`, `ofNullable`, `orElse`, `of`)
*  [Studio] Added test of crebase.sql script on an mem H2 database
* [Studio] Refactored File generation
* __[Studio] Renamed templates path from `templates` to `template`__


Release 0.9.1 - 2016/02/05
----------------------