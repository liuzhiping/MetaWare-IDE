com.arc.cdt.debug.seecode.core

This plugin logically extends the CDT org.eclipse.cdt.debug.core.CDebugger extension point
to add a new C/C++ debugger, namely the MetaWare SeeCode debugger. This plugin is the "model"
portion. The UI portion in represented by the plugin "com.arc.cdt.debug.seecode.ui".

I use the term "logically extends" because if you look at the plugin.xml file, you will see
that there is no such extension. The reason is that as of Eclipse 3.2, we resorted to making
"com.arc.cdt.debug.seecode.ui" the plugin to extends the CDebugger extension point to get
around a plugin instantiation timing issue. The problem is documented in the javadoc of 
com.arc.cdt.debug.seecode.ui.SeeCodeDebugger.

In this plugin, com.arc.cdt.debug.seecode.internal.core.cdi.Session is the class that 
implements the CDI ICDISession interface. It is the "root" of the debugger interface
hierarchy.

The plugin "com.arc.seecode.engine" is the adapter by which this plugin accesses the
SeeCode engine.