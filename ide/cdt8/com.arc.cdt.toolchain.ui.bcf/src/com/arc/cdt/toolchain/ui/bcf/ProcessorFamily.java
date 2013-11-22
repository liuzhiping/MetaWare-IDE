package com.arc.cdt.toolchain.ui.bcf;

public enum ProcessorFamily {
	A4("ARC 4", "ARC4", null, "arc4"), 
	A5("ARC 5", "ARC5", "-a5", "arc5"), 
	ARC600("ARC 600", "ARC6", "-a6", "arc600"), 
	ARC601("ARC 601", "ARC601", "-a601", "arc601"), 
	ARC700("ARC 700", "ARC7", "-a7", "arc700"), 
	ARCEM("ARC EM", "ARCV2EM", "-arcv2em", "av2em"), 
	ARCHS("ARC HS", "ARCV2HS", "-arcv2hs", "av2hs");
	
	ProcessorFamily(String name, String debuggerWhichArc, String debuggerArg, String projectType){
		this.name = name;
		this.guihiliName = debuggerWhichArc;
		this.arg = debuggerArg;
		this.projectType = projectType;
	}
	private String name;
	private String guihiliName;
	private String arg;
	private String projectType;
	@Override
	public String toString() { return name; }
	
	public String getGuihiliName() { return guihiliName;}
	public String getDebuggerArg() { return arg; }
	public String getProjectType() { return projectType; }
}