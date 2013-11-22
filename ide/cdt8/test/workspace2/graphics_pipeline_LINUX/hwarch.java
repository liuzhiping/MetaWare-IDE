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

import com.arc.specio.hardware.*;
import com.arc.specio.hardware.browsing.*;
import com.arc.specio.util.IllegalValueException;
import com.arc.specio.util.VDKException;
import com.arc.specio.util.XMLWriter;
import com.arc.specio.util.Problem;

import com.arc.specio.util.XMLWriterException;
//import com.arc.vrcc.model.NDModelException;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

/*
 * hwarch
 * 
 * Description: 
 *    This test constructs a single CPU architecture composed of ARC700, bvciinterconnect, uart, sram.
 *    They are connected together with their bus interfaces.
 *    Use to test specio and ARC XML changes.
 *    
 *  Author: Chuck Jordan (stealing some of Marco's example code too).
 */

/* This is a support class */
public class hwarch {
	
    private static boolean EMIT_BROWSE_OUTPUT = true; 


	hwarch (String name, String xmlfilename, int num_cpus) {

		ComponentInstance cpu[] = new ComponentInstance[num_cpus];
		String title = name;
		
		try {
			VDKSystem system = new VDKSystem(false);
			//NOTE: new ComponentInstance requires this try block...

			System.out.println("#### Building ARC model programmatically");

			Design myDesign = new Design(system, "ARCINTERNATIONAL", "hwarch_lib", "hwarch", "1.0");
			
			
			for (int i=0; i<num_cpus; ++i) {
				cpu[i] = new ComponentInstance(system,
							"ARCINTERNATIONAL",  /* vendor */
							"VDK",               /* library */
							"ARC700",            /* name */
							"0.1",               /* version */
							"cpu" + i);          /* instance name */
				myDesign.addComponentInstance(cpu[i]);
				cpu[i].setDescription("ARC700 is ARC Internationals high-end CPU");
			}
			

			ComponentInstance arbInst = new ComponentInstance(system,
					"ARCINTERNATIONAL",  /* vendor */
					"VDK",               /* library */
					"Arbiter",           /* name */
					"1.0",               /* version */
			        "arbInst");          /* instance name */
			myDesign.addComponentInstance(arbInst);
			arbInst.setDescription("The Arbiter is a temporary component for simulation only");
		
			ComponentInstance bvci_top = new ComponentInstance(system,
					"ARCINTERNATIONAL",  /* vendor */
					"VDK",               /* library */
					"bridgetop",         /* name */
					"1.0",               /* version */
			        "bridgeTopInst");    /* instance name */
			myDesign.addComponentInstance(bvci_top);

			/* uart: */
			ComponentInstance uart = new ComponentInstance(system,
					"ARCINTERNATIONAL",  /* vendor */
					"VDK",               /* library */
					"BVCIUART",          /* name */
					"1.1",               /* version */
			        "uart0");            /* instance name */
			myDesign.addComponentInstance(uart);
			uart.setDescription("It is descriable for each CPU to have a UART to support a console for debugging when simulating.");

			/* ssram & controller: */
			ComponentInstance ssramControllerInst = new ComponentInstance(system,
					"ARCINTERNATIONAL",  /* vendor */
					"VDK",               /* library */
					"ssramcontroller",   /* name */
					"1.0",               /* version */
			        "ssramControllerInst"); /* instance name */
			myDesign.addComponentInstance(ssramControllerInst);
			ssramControllerInst.setDescription("SRAM Memory Controller");
			
			ComponentInstance ssramInst = new ComponentInstance(system,
					"ARCINTERNATIONAL",  /* vendor */
					"VDK",               /* library */
					"SSRAM",             /* name */
					"1.1",               /* version */
			        "ssramInst");        /* instance name */
			myDesign.addComponentInstance(ssramInst);
			ssramInst.setDescription("SRAM Memory");

			for (int i=0; i<num_cpus; ++i) {
				/* wire up cpu0 to bridge/arb */
				Interconnection iBus2arbiter = new Interconnection (system, "instruction_bus"+i, "cpu"+i, "InstructionInterface", "arbInst", "IBus"+i);
				myDesign.addInterconnection(iBus2arbiter);
				/*
					iBus2arbiter <==> cpu0:BVCI.InstructionInterface
					iBus2arbiter <==> arbInst:BVCI.InstructionInterface
				 */

				Interconnection dBus2arbiter = new Interconnection (system, "data_bus"+i, "cpu"+i, "DataInterface", "arbInst", "DBus"+i);
				myDesign.addInterconnection(dBus2arbiter);
				/*
			 		dBus2arbiter <==> cpu0:BVCI.DataInterface
             		dBus2arbiter <==> arbInst:BVCI.DataInterface 
				 */
			}

            /* wire bridgetop to arbInst */
			Interconnection arbiter2bridge = new Interconnection (system, "arbiter2bridge", "arbInst", "perhipheralBridge", "bridgeTopInst", "BridgeTopTarget");
			myDesign.addInterconnection(arbiter2bridge);
            /*
            arbiter2bridge <==> arbInst:BVCI.BridgeInitiatorInterface
            arbiter2bridge <==> bridgeTopInst:BVCI.BridgeTopTarget 
            */
			
			/* wire up uart to bridgeTopInst */
			Interconnection bridge2uart = new Interconnection (system, "bridge2uart", "uart0", "HostInterface", "bridgeTopInst", "HostInterface");
			myDesign.addInterconnection(bridge2uart);
            /*
            bridge2uart <==> bridgeTopInst:pBVCI.HostInterface
            bridge2uart <==> uart0:pBVCI.HostInterface
            */
			
			/* wire up sram controller to bvci */
			Interconnection arbiter2ssramController = new Interconnection (system, "arbiter2ssramController", "arbInst", "ssramMemController", "ssramControllerInst", "SramInterface");
			myDesign.addInterconnection(arbiter2ssramController);
            /*
            arbiter2ssramController <==> arbInst:BVCI.SramMemoryInterface
            arbiter2ssramController <==> ssramControllerInst:BVCI.SramInterface
            */
			
			/* wire up sram to sram controller */
			Interconnection ssramController2sram = new Interconnection (system, "ssramController2sram", "ssramControllerInst", "PhysicalSSRAM", "ssramInst", "PhysicalSSRAM");
			myDesign.addInterconnection(ssramController2sram);
            /*
            ssramController2sram <==> ssramInst:SSRAM.PhysicalSSRAM
            ssramController2sram <==> ssramControllerInst:SSRAM.PhysicalSSRAM 
            */

			System.out.println("#### Performing Validation");
			myDesign.validate();

			// Traverse model
			if (EMIT_BROWSE_OUTPUT) System.out.println("#### Browsing the Model");
			ComponentInstance[] yourComponentInstances = myDesign.getComponentInstances();
			for (int i = 0;	i < yourComponentInstances.length; i++) {
				ComponentInstance curComponentInstance = yourComponentInstances[i];
				Component curComponent = curComponentInstance.getComponent();
				if (EMIT_BROWSE_OUTPUT) System.out.println ("--------------------");
				if (EMIT_BROWSE_OUTPUT) System.out.println ("component instance : " + curComponentInstance.getInstanceName() + " " + curComponentInstance.getUniqueId());
				if (EMIT_BROWSE_OUTPUT) System.out.println ("component          : " + curComponent.getName() + " " + curComponent.getUniqueId());
				BusInterface[] curBusInterfaces = curComponent.getBusInterfaces();
				for (int j = 0;	j < curBusInterfaces.length; j++) {
					BusInterface curBusInterface = curBusInterfaces[j];
					BusDefinition curBusDefinition = curBusInterface.getBusType();
					AbstractionDefinition curAbstractionDef = curBusInterface.getAbstractionType();
					if (EMIT_BROWSE_OUTPUT) System.out.println ("  bus interface     : " + curBusInterface.getName() + " " + curBusInterface.getUniqueId());
					if (EMIT_BROWSE_OUTPUT) System.out.println ("    bus definition  : " + curBusDefinition.getName() + " " + curBusDefinition.getUniqueId());
					if (EMIT_BROWSE_OUTPUT) System.out.println ("    abstraction def : " + curAbstractionDef.getName() + " " + curAbstractionDef.getUniqueId());
					AbstractionDefinitionPort[] curPorts = curAbstractionDef.getPorts();
					for (int k = 0;	k < curPorts.length; k++) {
						AbstractionDefinitionPort curPort = curPorts[k];
						// name not implemented yet on ports, so can't print yet
					}
				}
				
				if (EMIT_BROWSE_OUTPUT) dumpConfigurableParams(curComponentInstance);
				
				
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
					  if (EMIT_BROWSE_OUTPUT) System.out.println("    isParallel= " + ab.isParallel());
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
				if (EMIT_BROWSE_OUTPUT) System.out.println ("interconnection :      " + curInterconnection.getName() + " " + curInterconnection.getUniqueId());
				if (EMIT_BROWSE_OUTPUT) System.out.println ("interfaces");
				Interface[] curInterfaces = curInterconnection.getActiveInterfaces();
				for (int j = 0; j < curInterfaces.length; j++) {
					Interface curInterface = curInterfaces[j];
					BusInterface curBusInterface = curInterface.getBusInterface();
					ComponentInstance curComponentInstance = curInterface.getComponentInstance();
					if (EMIT_BROWSE_OUTPUT) System.out.println ("  component instance : " + curComponentInstance.getInstanceName() + " " + curComponentInstance.getUniqueId());
					if (EMIT_BROWSE_OUTPUT) System.out.println ("  bus interface      : " + curBusInterface.getName() + " " + curBusInterface.getUniqueId() + " master="+ curBusInterface.isMaster());
				}
			}

			/* TODO: Make a call to SAVE the design in XML form!!! */
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

	private static boolean hasCPU(Component c)
	{
//		Cpu cpus[] = c.getCpus();
//		if (cpus.length!=0)
//			return true;
//		else
//			return false;
		if (c.getName().equalsIgnoreCase("arc700"))
			return true;
		else
			return false;
	}
	
	private static void dumpConfigurableParams(ComponentInstance ci)
	{
		ConfigurableParameters cpi = new ConfigurableParameters();
		if (hasCPU(ci.getComponent())) {
			ArrayList<String> names = cpi.getAllCPUParameterNames(ci, 0); /* only 1 cpu for now */
			if (names.size() != 0) {
				System.out.println("   CPU Parameters for ComponentInstance " + ci.getInstanceName());
				System.out.println("   ---");
				for (Iterator it = names.iterator(); it.hasNext(); ) {
					String name = (String)it.next();
					String value = cpi.getCPUParameterValue(ci, 0, name);
					System.out.println("   " + name + " = " + value);
				}
				System.out.println("   ---");
			}
		}
		ArrayList<String> modelNames = cpi.getAllModelParameterNames(ci);
		if (modelNames.size() != 0) {
			System.out.println("   Model Parameters for ComponentInstance " + ci.getInstanceName());
			System.out.println("   ---");
			for (Iterator it = modelNames.iterator(); it.hasNext(); ) {
				String name = (String)it.next();
				String value = cpi.getModelParameterValue(ci, name);
				System.out.println("   " + name + " = " + value);
			}
			System.out.println("   ---");
		}
	}

}
