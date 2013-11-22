com.arc.cdt.toolchain

This plugin is the "base" of all CDT plugins that implement the MetaWare-based compiler toolsets.
Among other things, it extends the CDT extension point, 
"org.eclipse.cdt.managedbuilder.core.buildDefinitions",
which defines the compiler, assembler, and linker, but only the those command-line options
that are common to all MetaWare toolsets.

Target specific compilers are implemented by plugins that use this one as a "base":

com.arc.cdt.toolchain.arc -- ARC 5 and later.
com.arc.cdt.toolchain.arc4 -- ARCtangent A4
com.arc.cdt.toolchain.arm  -- ARM 
com.arc.cdt.toolchain.vc   -- VideoCore
com.arc.cdt.toolchain.linux86  - native Linux for x86 (internal use only)
com.arc.cdt.toolchain.win32 -- native Windows (internal use only)


The plugins rely on a modified version of the GNU makefile generator, that is in the
package "com.arc.cdt.managedbuilder.makegen". We resorted to customizing the CDT GNU
makefile generator class by adding virtual methods that we can override.

Here's a quick summary of each package in this plugin:

com.arc.cdt.errorparsers
    Implements the CDT IErrorParser interface by which the output of the
    MetaWare compiler, assembler, and linker can be properly "parsed" so
    that errors can be properly posted to the Problems view.
    Each of these classes are instantiated by extending the 
    "org.eclipse.cdt.core.errorparser" extension point.
    
com.arc.cdt.managedbuilder.makegen
    Responsible for generating the makefile for "managed" projects.
    NOTE: in CDT 4.0, it appears that there will be project type that does
    not rely on makefiles. Thus, the significance of this package may
    change.
    
com.arc.cdt.managedbuilder.scannerconfig   
    Defines a container for storing predefined symbols and include file paths that
    are associated with the compiler.
    NOTE: it is not clear why we're required to provide the class in this package. It is
    duplicate of the one used by the GNU plugin, but buried within an "internal"
    package. CDT should have made a default one available publicly.
    
com.arc.cdt.scannerconfig
    Parses the verbose output of the compiler so that it can detect which symbols and
    include-file paths are predefined. Required by the C/C++ source code "indexer" that
    is implemented in org.eclipse.cdt.core.
    
com.arc.cdt.toolchain
    Implements the plugin class itself. Implements the IApplicabilityCalculator for
    determine which tool options to enable/disable in the project build dialog.
    Implements the IManagedIsToolChainSupported interface to inform CDT if a
    particular toolset is installed. These classes are referenced from the plugin.xml
    file.
    
com.arc.cdt.toolchain.ui
    Implements the preference page for how to invoke the compile command in "verbose"
    mode so as to capture predefined preprocessor symbols and include-file paths.
com.arc.flex
    Java interface to ARC's FlexLM license manager DLL. The IDE is no longer flexed, but
    this package is used to display the number of days left for the debugger license in
    the Debugger's "About" box.
com.arc.intro
    Extends the appropriate extension points to implement our own custom "Welcome" screen.