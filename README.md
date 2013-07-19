depclipse
=========

Description
-----------

*depclipse* is a dependency checker plugin for Eclipse developed by Jens Cornelis. It evolved as a branch of [JDepend4Eclipse](http://andrei.gmxhome.de/jdepend4eclipse/) by Andrei Loskutov. The focus of this branch lies on the definition of project rules for dependencies allowed between the java packages and the automated check for dependencies not explicitly allowed.

*depclipse* is a wrapper for running [JDepend](http://www.clarkware.com/software/JDepend.html) from within Eclipse and checking the projects dependencies in order to create rules for which of these correspond to the expected architecture of the project.


Installation
------------

The easiest way to install *depclipse* is to use the Eclipse Update Manager (available since Eclipse 3.5 Galileo):

_Go to Help-> Install new Software_

Add the Update Site: http://kelloggs.github.io/depclipse/ .


Kudos
-----

As mentioned, *depclipse* is a branch of [JDepend4Eclipse](http://andrei.gmxhome.de/jdepend4eclipse/) by Andrei Loskutov. It was developed as a Bachelor's Thesis at the University of Applied Sciences in Offenburg, and since its focus is different from the original implementation, we decided to branch the projects. 

The Core of both *depclipse* and [JDepend4Eclipse](http://andrei.gmxhome.de/jdepend4eclipse/) is [JDepend](http://www.clarkware.com/software/JDepend.html) from [Clarkware](http://www.clarkware.com Clarkware). Please visit their website to obtain information about JDepend.

Requirements
------------

*depclipse* requires Eclipse 3.5 or higher.

Usage - running depclipse for the first time
--------------------------------------------

You can run *depclipse* by right-clicking on your source folder from the Package Explorer.

![How to run depclise](https://github.com/Kelloggs/depclipse/raw/master/readme_images/run_depclipse.png)

If the root folder of your project does not already contain a file with your project's dependency rules, a new file is created. By default, this file is called `DependencyRules.jdr`. You can change this file name in the preferences of *depclipse* (Preferences -> Java -> depclipse).

*depclipse* automatically opens its perspective, to show you the results. If you are already used to JDepend4Eclipse, this view will be familiar to you. If you are interested in the metrics shown, you are welcome to visits the websites of [JDepend4Eclipse](http://andrei.gmxhome.de/jdepend4eclipse/)  and [JDepend](http://www.clarkware.com/software/JDepend.html). On the right side of this view, you can see the prohibited dependencies that were found. Using the Packages Explorer, you can select Packages of Classes in order to see which prohibited dependencies they are causing. 

![Check prohibited dependencies](https://github.com/Kelloggs/depclipse/raw/master/readme_images/pro_dep.png)

At this moment, you haven't yet allowed any of the dependencies within your project, which means that all of them will be shown to you as prohibited. You can edit your project rules using the *depclipse* Rule Editor. You can open the editor by opening your jdr file or by clicking the paragraph icon in the Prohibited Dependencies view.

![depclipse dependency editor](https://github.com/Kelloggs/depclipse/raw/master/readme_images/rule_editor.png)

On the left side of the editor, you can see the prohibited dependencies which were found during the last run of *depclipse*. You can allow these dependencies by just double-clicking on them. You can also mark multiple dependencies and allow all of them by clicking the "Allow" button on the bottom of the editor. Of course it is also possible to prohibit formerly allowed dependencies.

If your project evolves, your dependencies might change and there might exist rules for dependencies, which do not occur within your project anymore. They are marked as "Obsolete". You can remove all of them by clicking the "Remove obsolete Dependencies" button.

Remember, that you can use the *depclipse* preferences to specify filters. This comes in handy, if you do not want to check dependencies against java core classes, like *java.**.

Further reading
---------------

The usage of depclipse has been discussed in the publication

_Cornelis J, Dorer K (2010) Vermeidung der Abhängigkeitsdivergenz zwischen Design und Implementierung in Java, (2010)_

which had been accepted for the [STEP2010](http://www.step2010.de/) conference in Furtwangen. 

For further information about the metrics used, we suggest to visit the [JDepend](http://www.clarkware.com/software/JDepend.html) website.  
