/************************************************************************/
/************************************************************************/
/**                                                                     */
/** Copyright (c) ARC International 2008.                               */
/** All rights reserved                                                 */
/**                                                                     */
/** This software embodies materials and concepts which are             */
/** confidential to ARC International and is made                       */
/** available solely pursuant to the terms of a written license         */
/** agreement with ARC International                                    */
/**                                                                     */
/** For more information, contact support@arc.com                       */
/**                                                                     */
/**                                                                     */
/************************************************************************/
/************************************************************************/

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.arc.specio.hardware.AbstractionDefinition;
import com.arc.specio.hardware.AbstractionDefinitionPort;
import com.arc.specio.hardware.AdHocConnection;
import com.arc.specio.hardware.AddressBank;
import com.arc.specio.hardware.BankedBlock;
import com.arc.specio.hardware.BusDefinition;
import com.arc.specio.hardware.BusInterface;
import com.arc.specio.hardware.Component;
import com.arc.specio.hardware.ComponentInstance;
import com.arc.specio.hardware.ComponentPortDirection;
import com.arc.specio.hardware.Design;
import com.arc.specio.hardware.ExternalPortReference;
import com.arc.specio.hardware.Interconnection;
import com.arc.specio.hardware.Interface;
import com.arc.specio.hardware.InternalPortReference;
import com.arc.specio.hardware.MemoryMap;
import com.arc.specio.hardware.Model;
import com.arc.specio.hardware.Port;
import com.arc.specio.hardware.VDKSystem;
import com.arc.specio.hardware.VendorExtensions;
import com.arc.specio.hardware.browsing.ConfigurableParameters;
import com.arc.specio.util.Problem;
import com.arc.specio.util.VDKException;
import com.arc.specio.util.XMLAttribute;
import com.arc.specio.util.XMLElement;
import com.arc.specio.util.XMLWriter;
import com.arc.specio.util.XMLWriterException;

/*
 * hwarch
 * 
 * Description: 
 *    PMP Architecture creation featuring AM401v component.
 *    
 *  Author: Chuck Jordan
 */

/* This is a support class */
public class hwarch {

	private static String VENDOR  = "arc.com";
	private static String LIBRARY = "am_series";

	private static boolean EMIT_BROWSE_OUTPUT = true; 

	private static VDKSystem system           = null;
	private static Design myDesign            = null;
	

	private static HashMap<AdHocPortKey, AdHocConnection> adHocMap = new HashMap<AdHocPortKey, AdHocConnection> ();
	private static HashSet<AdHocPortKey> connectedInputs = new HashSet<AdHocPortKey>();
	private static int connId = 0;
	
	private static String arbBusInterfaces[] = {
		"iini",
		"dini",
		"as",
		"vdi",
		"vdo",
		"vlc"
	};

	public static void main(String[] args) {
		String xmlfilename;
		int numcpus;

		if (args.length < 2) {
			System.out.println("Usage: hwarch <hw arch xml> <numcpus>");
			System.exit(1);
		}

		System.out.println("**********************************************************");
		System.out.println("*                                                      ***");
		System.out.println("* DEMONSTRATION OF HARDWARE ARCH CREATION              ***");
		System.out.println("*                                                      ***");
		System.out.println("**********************************************************");


		xmlfilename = args[0];
		numcpus = Integer.valueOf(args[1],10);
		System.out.println("numcpus=" + numcpus);

		try {
			//NOTE: new ComponentInstance requires this try block...

			system = new VDKSystem();

			System.out.println("#### Building ARC model programmatically");

			myDesign = new Design(system, VENDOR, "hwarch_lib", "hwarch", "1.0");

			createMemoryAndPeripherals();

			for (int i=0;i<numcpus;++i) {

				createARC700(i);

			}

			System.out.println("#### Performing Validation");
			myDesign.validate();

			browseHwArch();
			layout(myDesign);
			/* Make a call to SAVE the design in XML form!!! */
			XMLWriter w = new XMLWriter(xmlfilename);
			w.setAttributeWrap(80);
			myDesign.exportXML(w);
			w.close();

			
		} catch (VDKException e) {
			String msg = e.getMessage();
			if (msg != null && !msg.equals(""))
				System.err.println(msg);
			ArrayList<Problem> probs = e.getProblems();
			if (probs.size() == 0)
				e.printStackTrace();
			for(int i=0; i<probs.size(); i++) {
				Problem p = probs.get(i);
				System.err.println(p.getFileName()+":"+p.getLine()+": "+p.getSeverity()+": "+p.getMessage());
			}
			System.exit(1);
		} catch (XMLWriterException ex) {
			System.err.println ("\nERROR: " + ex.getMessage() + "\n");
			System.exit(1);
		} catch (FileNotFoundException ex) {
			System.err.println ("\nERROR: " + ex.getMessage() + "\n");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();   
			System.exit(1);
		}
	}

//	private static void createClockMgt() throws VDKException
//	{	
//		ComponentInstance clkmgt = new ComponentInstance(system,
//				VENDOR,  /* vendor */
//				LIBRARY,               /* library */
//				"ClockManagementModule",         /* name */
//				"1.0",               /* version */
//		"iClockManagementModule");    /* instance name */
//		myDesign.addComponentInstance(clkmgt);		
//	}


	private static void createMemoryAndPeripherals() throws VDKException
	{
		/* arb_top */
		ComponentInstance arb_top = new ComponentInstance(system,
				VENDOR,
				LIBRARY,
				"arb_top",
				"1.0",
		        "arb_top");
		myDesign.addComponentInstance(arb_top);
		arb_top.setDisplayName("arb_top");

	    /* bridge_top */
	    ComponentInstance bridge_top = new ComponentInstance(system,
                VENDOR,
                LIBRARY,
                "bridge_top",
                "1.0",
                "bridge_top");
	    myDesign.addComponentInstance(bridge_top);
        bridge_top.setDisplayName("bridge_top");
        /* bvci busses include: bgt, bgi, arbt, arbi */

        /* p_arb_top */
		ComponentInstance p_arb_top = new ComponentInstance(system,
				VENDOR,
				LIBRARY,
				"p_arb_top",
				"1.0",
		        "p_arb_top");
		myDesign.addComponentInstance(p_arb_top);
		p_arb_top.setDisplayName("p_arb_top");
		/* busses: arbt, uart0, uart1, vmac0, pix, ide, sc, ac97, cfg, arbi, memoryAccess(MISC) */

		/* mem_arb_top */
		ComponentInstance mem_arb_top = new ComponentInstance(system,
				VENDOR,
				LIBRARY,
				"mem_arb_top",
				"1.0",
		        "mem_arb_top");
		myDesign.addComponentInstance(mem_arb_top);
		mem_arb_top.setDisplayName("mem_arb_top");
		
		/*
		 * Connect bridge_top
		 */
		Interconnection bgt_bus =
			new Interconnection (system,
					"bgt_bus",
					"arb_top",
					"bgt",
					"bridge_top",
			        "bgt");
		myDesign.addInterconnection(bgt_bus);	

		Interconnection bgi_bus =
			new Interconnection (system,
					"bgi_bus",
					"arb_top",
					"bgi",
					"bridge_top",
			        "bgi");
		myDesign.addInterconnection(bgi_bus);		

		Interconnection memarb_bus =
			new Interconnection (system,
					"memarb_bus",
					"arb_top",
					"sm",
					"mem_arb_top",
			        "sm");
		myDesign.addInterconnection(memarb_bus);

		Interconnection arbi_bus =
			new Interconnection (system,
					"arbi_bus",
					"p_arb_top",
					"arbi", /* target from p_arb */
					"bridge_top",
			        "arbi");
		myDesign.addInterconnection(arbi_bus);		

		Interconnection arbt_bus =
			new Interconnection (system,
					"arbt_bus",
					"p_arb_top",
					"arbt",
					"bridge_top",
			        "arbt");
		myDesign.addInterconnection(arbt_bus);		
		
		/* sdram_controller */
		ComponentInstance sdram_controller = new ComponentInstance(system,
				VENDOR,
				LIBRARY,
				"sdram_controller",
				"1.0",
		        "sdram_controller");
		myDesign.addComponentInstance(sdram_controller);
		sdram_controller.setDisplayName("sdram_controller");
		
		/*
		 * Connect SDRAM controller
		 */
		Interconnection sdram_ctrl_bus =
			new Interconnection (system,
					"sdram_ctrl_bus",
					"mem_arb_top",
					"smem",
					"sdram_controller",
			        "smem");
		myDesign.addInterconnection(sdram_ctrl_bus);

		/* uart0 */
		ComponentInstance uart0= new ComponentInstance(system,
				VENDOR,               /* vendor */
				LIBRARY,              /* library */
				"buart",              /* name */
				"1.0",                /* version */
		        "buart");             /* instance name */
		myDesign.addComponentInstance(uart0);
		uart0.setDisplayName("uart0");
		
		Interconnection uart0_bus =
			new Interconnection (system,
					"uart0_bus",
					"p_arb_top",
			        "uart0",
			        "buart",
			        "BVCITarget");
		myDesign.addInterconnection(uart0_bus);	
		
		ComponentInstance mem_model = new ComponentInstance(system,
				VENDOR,  /* vendor */
				LIBRARY,               /* library */
				"memory_model",         /* name */
				"1.0",               /* version */
		        "imemory_model");    /* instance name */
		myDesign.addComponentInstance(mem_model);	
		
		Interconnection sdram_bus =
			new Interconnection (system,
					"sdram_bus",
					"sdram_controller",
					"phySdram",
					"imemory_model",
			        "xsdram");
		myDesign.addInterconnection(sdram_bus);	
	}


	private static void createARC700(int cpu_index) throws VDKException
	{
		String cpu_name = "cpu" + cpu_index;
		ComponentInstance cpu = new ComponentInstance(system,
				VENDOR,  /* vendor */
				LIBRARY,               /* library */
				"arc700",         /* name */
				"1.0",               /* version */
				cpu_name);    /* instance name */
		myDesign.addComponentInstance(cpu);
		cpu.setDisplayName(cpu_name);
		
		//ConfigurableParameters cp = new ConfigurableParameters();
		//cp.setCPUParameterValue(cpu, 0, "identity", "" + cpu_index);
		
		String connection_name = "cpu" + cpu_index + "_bus";
		Interconnection cpubus =
			new Interconnection (system,
					connection_name,
					cpu_name,
					"iini",
					"arb_top",
					arbBusInterfaces[cpu_index]);
		myDesign.addInterconnection(cpubus);
		/*
		 * NOTE: This architecture isn't real. We are only connecting
		 * the "iini" bus interface. For real hardware the "dini" would also be connected to the arb_top.
		 */
	}


	private static void browseHwArch()
	{
		// Traverse model
		if (EMIT_BROWSE_OUTPUT) System.out.println("#### Browsing the Model");
		ComponentInstance[] yourComponentInstances = myDesign.getComponentInstances();
		for (int i = 0;	i < yourComponentInstances.length; i++) {
			ComponentInstance curComponentInstance = yourComponentInstances[i];
			Component curComponent = curComponentInstance.getComponent();
			if (EMIT_BROWSE_OUTPUT) System.out.println ("--------------------");
			if (EMIT_BROWSE_OUTPUT) System.out.println ("component instance : " + curComponentInstance.getInstanceName());
			if (EMIT_BROWSE_OUTPUT) System.out.println ("component          : " + curComponent.getName() + " ");
			BusInterface[] curBusInterfaces = curComponent.getBusInterfaces();
			for (int j = 0;	j < curBusInterfaces.length; j++) {
				BusInterface curBusInterface = curBusInterfaces[j];
				BusDefinition curBusDefinition = curBusInterface.getBusType();
				AbstractionDefinition curAbstractionDef = curBusInterface.getAbstractionType();
				if (EMIT_BROWSE_OUTPUT) System.out.println ("  bus interface     : " + curBusInterface.getName() + " ");
				if (EMIT_BROWSE_OUTPUT) System.out.println ("    bus definition  : " + curBusDefinition.getName() + " ");
				if (curAbstractionDef!=null) {
					if (EMIT_BROWSE_OUTPUT) System.out.println ("    abstraction def : " + curAbstractionDef.getName() + " ");
					AbstractionDefinitionPort[] curPorts = curAbstractionDef.getPorts();
					for (int k = 0;	k < curPorts.length; k++) {
						AbstractionDefinitionPort curPort = curPorts[k];
						// name not implemented yet on ports, so can't print yet
					}
				}
			}
			/* display ports too */
			Model model = curComponent.getModel();
			Port ports[] = model.getPorts();
			for (int k = 0; k < ports.length; ++k) {
				Port p = ports[k];
				if (EMIT_BROWSE_OUTPUT) System.out.println("  port " + p.getName() + " direction=" + p.getWire().getDirection());
			}

			//if (EMIT_BROWSE_OUTPUT) dumpConfigurableParams(curComponentInstance);

			MemoryMap mms[] = curComponent.getMemoryMaps();
			for (int j=0; j<mms.length; ++j) {
				MemoryMap mm = mms[j];
				if (mm == null) {
					if (EMIT_BROWSE_OUTPUT) System.out.println("  memoryMap null");
					continue;
				}
				if (EMIT_BROWSE_OUTPUT) System.out.println("  memoryMap " + mm.getName());
				AddressBank [] banks = mm.getBanks();
				for(int k=0; k<banks.length; k++) {
					AddressBank ab = banks[k];
					BankedBlock bb[] = ab.getAddressBlocks();

					if (EMIT_BROWSE_OUTPUT) System.out.println("  bank " + ab.getName());
					//if (EMIT_BROWSE_OUTPUT) System.out.println("    isParallel= " + ab.isParallel());
					if (EMIT_BROWSE_OUTPUT) System.out.println("    base address= " + "  0x" + Long.toHexString(ab.getBaseAddress()));
					for (int h=0;h<bb.length; h++) {
						BankedBlock b = bb[h];
						if (EMIT_BROWSE_OUTPUT) System.out.println("    bank " + h + " width=" + b.getWidth() + " range=" + b.getRange());
					}
				}
			}
		}

		Interconnection[] yourInterconnections = myDesign.getInterconnections();
		for (int i = 0; i < yourInterconnections.length; i++) {
			Interconnection curInterconnection = yourInterconnections[i];
			if (EMIT_BROWSE_OUTPUT) System.out.println ("--------------------");
			if (EMIT_BROWSE_OUTPUT) System.out.println ("interconnection :      " + curInterconnection.getName() + " ");
			if (EMIT_BROWSE_OUTPUT) System.out.println ("interfaces");
			Interface[] curInterfaces = curInterconnection.getActiveInterfaces();
			for (int j = 0; j < curInterfaces.length; j++) {
				Interface curInterface = curInterfaces[j];
				BusInterface curBusInterface = curInterface.getBusInterface();
				if (curBusInterface==null) {
					System.out.println("ERROR: null found for bus interface in " + curInterconnection.getName());
					System.exit(1);
				}
				ComponentInstance curComponentInstance = curInterface.getComponentInstance();
				if (EMIT_BROWSE_OUTPUT) System.out.println ("  component instance : " + curComponentInstance.getInstanceName() + " ");
				if (EMIT_BROWSE_OUTPUT) System.out.println ("  bus interface      : " + curBusInterface.getName() + " " + " master="+ curBusInterface.isMaster());
			}
		}

	}


	/** connects an output port to an input port through an ad-hoc connection. The two pairs of arguments identify the 
	 * two ports to connect. The first pair must identify an output port, while the second pair must identify an input port. 
	 * Within a pair, the first argument is the component instance containing the port, the second parameter is the name of the port.
	 * One (but not both) of the two component instances can be null, in which case the component is assumed to be the container of 
	 * the other one and an externalPortReference is generated. 
	 * @param object
	 * @param string
	 * @param as210
	 * @param string2
	 */
	private static void adHocConnect(ComponentInstance outPortCI, String outPortName,
			ComponentInstance inPortCI, String inPortName) throws VDKException {
		// make sure not both component instances are null
		if (outPortCI == null && inPortCI == null)
			throw new VDKException("Only one component instance can be null");

		// make sure first port is output and second is input
		ComponentPortDirection inPortDir = null, outPortDir = null;
		if (outPortCI != null) {
			Component c = outPortCI.getComponent();
			Port p = c.getModel().getPortByName(outPortName);
			if (p == null)
				throw new VDKException("Port '"+outPortName+"' not found in component '"+outPortCI.getInstanceName()+"'");
			outPortDir = p.getWire().getDirection(); 
			if (outPortDir != ComponentPortDirection.OUT && outPortDir != ComponentPortDirection.INOUT) 
				throw new VDKException("Port '"+outPortName+"' of component instance '"+outPortCI.getInstanceName()+"' is expected to be an output, but it's not");
		}
		if (inPortCI != null) {
			Component c = inPortCI.getComponent();
			Port p = c.getModel().getPortByName(inPortName);
			if (p == null)
				throw new VDKException("Port '"+inPortName+"' not found in component '"+inPortCI.getInstanceName()+"'");
			inPortDir = p.getWire().getDirection(); 
			if (inPortDir != ComponentPortDirection.IN && inPortDir != ComponentPortDirection.INOUT) 
				throw new VDKException("Port '"+inPortName+"' of component instance '"+inPortCI.getInstanceName()+"' is expected to be an input, but it's not");
		}

		// make sure the input port was not already connected to a driver
		AdHocPortKey pk = new AdHocPortKey(inPortCI, inPortName);
		if (connectedInputs.contains(pk)) {
			if (inPortDir == ComponentPortDirection.IN) {
				String CIName = (inPortCI == null) ? "container component" : "component instance '"+inPortCI.getInstanceName()+"'";
				throw new VDKException("Input port " + inPortName + " of "+ CIName + " is already connected to an output port");
			} else {
				throw new Error("INOUT port not fully supported yet");
				// if an INOUT port is connected as input to more than one connection, we need to merge the two connections 
			}
		}
		Design design = (Design) ((outPortCI != null)? outPortCI.getParent() : inPortCI.getParent());

		pk = new AdHocPortKey(outPortCI, outPortName);

		ArrayList<InternalPortReference> ip = new ArrayList<InternalPortReference>();
		InternalPortReference[] ipArray = null;
		if (inPortCI != null) {
			InternalPortReference ref = new InternalPortReference(design.getHandle(), inPortCI.getInstanceName(), inPortName);
			ip.add(ref);
		}
		if (outPortCI != null) {
			InternalPortReference ref = new InternalPortReference(design.getHandle(), outPortCI.getInstanceName(), outPortName);
			ip.add(ref);
		}
		ipArray = (InternalPortReference[])ip.toArray(new InternalPortReference[ip.size()]);
		AdHocConnection conn = adHocMap.get(pk);
		if (conn == null) {
			conn = new AdHocConnection(design.getHandle(), "conn_"+connId, ipArray);
			connId++;
			design.addAdHocConnection(conn);
			adHocMap.put(pk, conn);
		}
		if (inPortCI == null) {
			ExternalPortReference ref = new ExternalPortReference(design.getHandle(), inPortName);
			conn.addExternalPortReference(ref);      
		}
		if (outPortCI == null) {
			ExternalPortReference ref = new ExternalPortReference(design.getHandle(), outPortName);
			conn.addExternalPortReference(ref);      
		}
		connectedInputs.add(new AdHocPortKey(inPortCI, inPortName));  
	}

  private static XMLElement layoutElement(ComponentInstance ci, int x, int y, int w, int h) {
    XMLElement el = new XMLElement("element");
    el.addAttribute(new XMLAttribute("name", ci.getInstanceName()));
    el.addAttribute(new XMLAttribute("type", "component_instance"));
    el.addAttribute(new XMLAttribute("x", ""+ x));
    el.addAttribute(new XMLAttribute("y", ""+ y));
    el.addAttribute(new XMLAttribute("width", ""+ w));
    el.addAttribute(new XMLAttribute("height", ""+ h));
    return el;
  }


  private static void layout(Design arch) {
    int CPU_W  = 100,   CPU_H =  50;
    int GRID_W = 200, GRID_H = 120;
    int offx   =  20, offy   = 20;
    int y0 = offy;
    int x0 = offx;

    VendorExtensions ext = arch.getVendorExtensions();
    XMLElement guiVendorExtension = new XMLElement("systemDesigner");
    XMLElement r = new XMLElement("routing");
    r.addAttribute(new XMLAttribute("router ", "0"));
    guiVendorExtension.addElement(r);

    try {
      ext.addVendorExtension(guiVendorExtension);
    }catch(VDKException ex){}
    int cpuCount = 0;
    while(true) {
      ComponentInstance ci = arch.getComponentInstanceByName("cpu"+cpuCount);
      if (ci == null)
        break;
      guiVendorExtension.addElement(layoutElement(ci, x0+GRID_W*cpuCount, y0, CPU_W, CPU_H));
      cpuCount++;
    } 
    cpuCount--;
    ComponentInstance arb_top = arch.getComponentInstanceByName("arb_top");
    int arbInstW = GRID_W*cpuCount+CPU_W;
    guiVendorExtension.addElement(layoutElement(arb_top, x0, y0+GRID_H, arbInstW, CPU_H));

    ComponentInstance mem_arb_top = arch.getComponentInstanceByName("mem_arb_top");
    guiVendorExtension.addElement(layoutElement(mem_arb_top, x0, y0+2*GRID_H, arbInstW/4, CPU_H));

    ComponentInstance bridge_top = arch.getComponentInstanceByName("bridge_top");
    guiVendorExtension.addElement(layoutElement(bridge_top, x0+arbInstW/3, y0+2*GRID_H, arbInstW/4, CPU_H));

    ComponentInstance sdram_controller = arch.getComponentInstanceByName("sdram_controller");
    guiVendorExtension.addElement(layoutElement(sdram_controller, x0, y0+3*GRID_H, arbInstW/4, CPU_H));

    ComponentInstance p_arb_top = arch.getComponentInstanceByName("p_arb_top");
    guiVendorExtension.addElement(layoutElement(p_arb_top, x0+2*arbInstW/3, y0+3*GRID_H, arbInstW/4, CPU_H));

    ComponentInstance imemory_model = arch.getComponentInstanceByName("imemory_model");
    guiVendorExtension.addElement(layoutElement(imemory_model, x0, y0+4*GRID_H, arbInstW/4, CPU_H));

    ComponentInstance buart = arch.getComponentInstanceByName("buart");
    guiVendorExtension.addElement(layoutElement(buart, x0+2*arbInstW/3, y0+4*GRID_H, arbInstW/4, CPU_H));
}


}


class AdHocPortKey {
	ComponentInstance ci;
	String portName;
	public AdHocPortKey(ComponentInstance ci,String portName) {
		this.ci = ci;
		this.portName = portName;
	}
	public boolean equals(Object obj) {
		if (obj instanceof AdHocPortKey)
			return ((AdHocPortKey)obj).ci == ci && ((AdHocPortKey)obj).portName.equals(portName);
		else
			return super.equals(obj);
	}
}

