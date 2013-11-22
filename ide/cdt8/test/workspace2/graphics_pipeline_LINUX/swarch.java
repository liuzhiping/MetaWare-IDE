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
import java.util.Iterator;

import com.arc.specio.hardware.ComponentInstance;
import com.arc.specio.hardware.Design;
import com.arc.specio.hardware.VDKSystem;
import com.arc.specio.software.ARCChipSupport;
import com.arc.specio.software.BooleanProperty;
import com.arc.specio.software.Channel;
import com.arc.specio.software.IntProperty;
import com.arc.specio.software.LongProperty;
import com.arc.specio.software.MemoryPool;
import com.arc.specio.software.OS;
import com.arc.specio.software.OSType;
import com.arc.specio.software.PreprocessorSymbol;
import com.arc.specio.software.Process;
import com.arc.specio.software.Property;
import com.arc.specio.software.Section;
import com.arc.specio.software.SoftwareArchitecture;
import com.arc.specio.software.SoftwareElement;
import com.arc.specio.software.SourceSet;
import com.arc.specio.software.Thread;
import com.arc.specio.util.IllegalValueException;
import com.arc.specio.util.Problem;
import com.arc.specio.util.VDKException;
import com.arc.specio.util.XMLWriter;
import com.arc.specio.util.XMLWriterException;


public class swarch {
	
  //private final static boolean USE_PROGRAMMATIC_HWARCH = true;
  private final static String HWXMLNAME = "hwarch.xml";
  
  private final static String SRCPATH = "src/";
  /* NOTES: Roberto recommends using forward slashes only */

  private final static int SHARED_MEM_SIZE = 2*1024*1024;  /* 2Mb */
  private final static int TOTAL_MEM_SIZE = 16*1024*1024; /* 16Mb */
  
  /* hwarch names */
  private final static String CPU_PREFIX        = "cpu";
  private final static String MEMORYINST        = "ssramInst";
  private final static String MEMORYPATH        = "mmap.bank1.ram0";
  private final static String SECOND_MEMORYPATH = "mmap.bank2.ram1";
  
  private final static int NUM_CPUS = 4;
  
  private final static int OS_SIZE = (((TOTAL_MEM_SIZE-SHARED_MEM_SIZE)/NUM_CPUS)) & ~0xfff;


  public final static int GR_THD      = 0;
  public final static int DISPLAY_THD = 1;
  public final static int RENDER_THD  = 2; /* render threads start at 2 */

  public final static int MAXR = 144;
  public final static int MAXC = 176;

  /**
   * @param args
   */
  public static void main(String[] args) {

    VDKSystem system;
    Thread threads[] = new Thread[NUM_CPUS];
    Process p[] = new Process[NUM_CPUS];
    int base_address = 0x0; /* assume zero */
    
    boolean gracefull_exit = false;

    System.out.println("*********************************************");
    System.out.println("*                                         ***");
    System.out.println("* DEMONSTRATION OF SOFTWARE ARCH CREATION ***");
    System.out.println("*                                         ***");
    System.out.println("*********************************************");
    
    system = new VDKSystem(false);
    
    try {
      Design d = null;
    	  
        hwarch hw = new hwarch("hwarch", HWXMLNAME, NUM_CPUS); 	
        d = loadHwDesign(system, HWXMLNAME); /* load the hwarch design just created */
        
      /*
       * Create new Software Architecture
       */
      SoftwareArchitecture arch = new SoftwareArchitecture(system, "swarch", d);
      //dumpSWProperties(arch);
      
      for (int i=0; i < NUM_CPUS; i++) {
          String process_name;
          if (i == GR_THD) {
            process_name = "grmain";
          } else if (i == DISPLAY_THD) {
            process_name = "displaymain";
          } else {
            process_name = "rendermain"+Integer.toString(i-RENDER_THD);
          }

    	  /* instantiate an OS of type MQX */
    	  OS os = new OS(arch, "OS_" + i, "no_os");
                     /* Tested: "no_os" and "mqx" and "no_os_legacy". */

    	  /* set up the OS via various properties */
    	  os.setPropertyValue(OS.PHY_BASE_ADDR, Integer.toString(base_address));
    	  /*PHY_BASE_ADDR is address OS is to start @ */
    	  os.setPropertyValue(OS.SIZE, Integer.toString(OS_SIZE));
    	  /*SIZE is the number of bytes taken by OS */
    	  /* NOTE: PHY_BASE_ADDR & SIZE must indicate a physical regions
             for a memory that exists in the hardware architecture.
    	   */

          if (os.getType().equals(OSType.LINUX)==false) {
    	    os.setPropertyValue(OS.STACK_SIZE, "0x4000");
    	    /* good to make stack big */
    	    os.setPropertyValue(OS.HEAP_SIZE, "0x40000"); /* 256k */
          }

    	  /* MQX only properties */
    	  if (os.getType().equals(OSType.MQX)) {
    		  os.setPropertyValue(OS.BSP_DIR, "C:/ARC/mqx_rtos2.51_arc700");
    		  /* path to MQX deliverable from ARC */
    		  //os.setPropertyValue(OS.KERNEL_DATA_SIZE, value);
    	  }

    	  dumpSWProperties(os);

          /* add the cpu to the OS */
          os.addCPU(CPU_PREFIX + i + ".core");
            /* tell the OS which CPU it is using */

    	  /* create a new process instance */
    	  p[i] = new Process(arch, process_name);

          if (i==0) /* only first is MASTER */
            ((BooleanProperty)(p[i].getProperty(ARCChipSupport.VR_PROCESS_MASTER))).setValue(true);
          
    	  /* one process must be the master and have master=true */
    	  //dumpSWProperties(p[i]);
    	  /* other process properties:
    	   *   external_makefile = user_defs.mk
    	   */

    	  p[i].addPreprocessorSymbol(
    			  new PreprocessorSymbol(arch, "NUMBER_OF_CPU_ISLANDS", Integer.toString(NUM_CPUS)));
    	  /* define the symbol NUMBER_OF_CPU_ISLANDS to be used in the app */

    	  os.addProcess(p[i]); /* add process to OS */

    	  /* create a thread */
    	  threads[i] = new Thread(arch, "thd_" + process_name);
    	  /* no need to set "assign_to_processor" since not SMP system */
    	  //dumpSWProperties(threads[i]);

    	  /* create a source set for this process */
    	  SourceSet ss = new SourceSet(arch, "my_source_set"+i);
    	  /* add the source files to the source set */
          if (i == GR_THD) {
    	    ss.addSource(SRCPATH + "grmain.c");
    	    ss.addSource(SRCPATH + "grapi.c");
    	    ss.addSource(SRCPATH + "screen.c");
    	    threads[i].setMainFunction("_grmain");
                                        /* entry-point for this thread */
          } else if (i == DISPLAY_THD) {
    	    ss.addSource(SRCPATH + "displaymain.c");
    	    ss.addSource(SRCPATH + "plot.c");
    	    ss.addSource(SRCPATH + "screen.c");
    	    threads[i].setMainFunction("_displaymain");
                                        /* entry-point for this thread */
          } else {
    	    ss.addSource(SRCPATH + "rendermain.c");
    	    ss.addSource(SRCPATH + "plot3d.c");
    	    ss.addSource(SRCPATH + "plot.c");
    	    ss.addSource(SRCPATH + "screen.c");
            /* TODO: render shouldn't need screen... remove dependency */
    	    threads[i].setMainFunction("_rendermain");
                                        /* entry-point for this thread */
          }
    	  p[i].addThread(threads[i]);

    	  //dumpSWProperties(ss);

    	  p[i].add(ss);  /* attach the source-set to the process */

          base_address += OS_SIZE;

      }

      /* look-up sram instance in hardware architecture */
      ComponentInstance sram = d.getComponentInstanceByName(MEMORYINST);
      /* HWARCH browse^^^^ lookup memory in hwarch */

      /* map section */
      Section sect = new Section(arch, ".frame", p[DISPLAY_THD], sram, MEMORYPATH);
      
      
      /*
       * Coordination API objects
       */
 

      /*
       * MEMORY POOLs
       */     
      //MemoryPool mp = new MemoryPool(arch, "raster_pool", ARCChipSupport.VR_ARC_MEMORY_POOL_SHARED_MEMORY);
      MemoryPool mp = new MemoryPool(arch, "raster_pool", ARCChipSupport.VR_ARC_MEMORY_POOL_SHARED_MEMORY_CIRCULAR_BUFFER);
      mp.setDescription("raster_pool holds the rasters for the display");
      mp.setPropertyValue(ARCChipSupport.VR_MEMORY_INSTANCE_NAME,MEMORYINST);
      mp.setPropertyValue(ARCChipSupport.VR_BANKPATH, MEMORYPATH);
      mp.setNodeCount(8);
      mp.setNodeSize(MAXC*MAXR);
      for (int j=0;j<(NUM_CPUS-2);j++) {
        mp.addConsumer(threads[RENDER_THD+j]);
      }
      mp.addProducer(threads[DISPLAY_THD]);
 
      
      MemoryPool gpool = new MemoryPool(arch, "grcmd_pool", ARCChipSupport.VR_ARC_MEMORY_POOL_SHARED_MEMORY);
      //MemoryPool gpool = new MemoryPool(arch, "grcmd_pool", ARCChipSupport.VR_ARC_MEMORY_POOL_SHARED_MEMORY_CIRCULAR_BUFFER);
      gpool.setDescription("grcmd_pool holds the messages to the renderer");
      gpool.setPropertyValue(ARCChipSupport.VR_MEMORY_INSTANCE_NAME,MEMORYINST);
      gpool.setPropertyValue(ARCChipSupport.VR_BANKPATH, MEMORYPATH);
      gpool.setNodeCount(256*(NUM_CPUS-2));
      gpool.setNodeSize(128);
      gpool.addConsumer(threads[GR_THD]);
      for (int j=0;j<(NUM_CPUS-2);j++) {
        gpool.addProducer(threads[RENDER_THD+j]);
      }
 

      /*
       * CHANNELs
       */     
      for (int j=0;j<(NUM_CPUS-2);j++) {
        Channel rch = new Channel(arch, "render_channel"+j, ARCChipSupport.VR_ARC_CHANNEL_SHARED_MEMORY);
        //rch.setDescription("todo");
        rch.setPropertyValue(ARCChipSupport.VR_INSTRUMENT,"true");
        rch.setPropertyValue(ARCChipSupport.VR_MEMORY_INSTANCE_NAME,MEMORYINST);
        rch.setPropertyValue(ARCChipSupport.VR_BANKPATH, SECOND_MEMORYPATH);
        rch.setPropertyValue(ARCChipSupport.VR_CHANNEL_BUFFER_SIZE,
                             "512");
        //rch.setPropertyValue(ARCChipSupport.VR_CHANNEL_FIXED_SIZE,
        //                   "-1"); /* TODO: size of msghldr */

        rch.addProducer(threads[GR_THD]);
        rch.addConsumer(threads[RENDER_THD+j]);

        p[RENDER_THD+j].addPreprocessorSymbol(new PreprocessorSymbol(arch,
                                       "IN_CHANNEL",
                                       "render_channel"+j));

        Channel dch = new Channel(arch, "display_channel"+j, ARCChipSupport.VR_ARC_CHANNEL_SHARED_MEMORY);
        dch.setPropertyValue(ARCChipSupport.VR_INSTRUMENT,"true");
        //dch.setDescription("todo");
        dch.setPropertyValue(ARCChipSupport.VR_MEMORY_INSTANCE_NAME,MEMORYINST);
        dch.setPropertyValue(ARCChipSupport.VR_BANKPATH, SECOND_MEMORYPATH);
        dch.setPropertyValue(ARCChipSupport.VR_CHANNEL_BUFFER_SIZE,
                             "256");
        //dch.setPropertyValue(ARCChipSupport.VR_CHANNEL_FIXED_SIZE,
        //                   "-1"); /* TODO: size of display_msg */

        dch.addProducer(threads[RENDER_THD+j]);
        dch.addConsumer(threads[DISPLAY_THD]);

        p[RENDER_THD+j].addPreprocessorSymbol(new PreprocessorSymbol(arch,
                                       "OUT_CHANNEL",
                                       "display_channel"+j));

      }

    /* build up list of out-going channel list for graphics stage to use */
    String list = "{ ";
    for (int i = 0; i < (NUM_CPUS-2); ++i)
    {
      list += "render_channel"+i;
      if (i != ((NUM_CPUS-2)-1))
        list += ", ";
    }
    list += " }";
    p[GR_THD].addPreprocessorSymbol(new PreprocessorSymbol(arch,
                                       "OUT_CHANNEL_LIST",
                                       list));

    /* build up list of incomming channel list for display stage to use */
    list = "{ ";
    for (int i = 0; i < (NUM_CPUS-2); ++i)
    {
      list += "display_channel"+i;
      if (i != ((NUM_CPUS-2)-1))
        list += ", ";
    }
    list += " }";
    p[DISPLAY_THD].addPreprocessorSymbol(new PreprocessorSymbol(arch,
                                       "IN_CHANNEL_LIST",
                                       list));

      /*
       * Model is complete, so write it out now
       */
      XMLWriter w = new XMLWriter("swarch.xml");
      w.setAttributeWrap(80);
      arch.exportXML(w);
      w.close();

      System.out.println("**********************************************");
      System.out.println("*                                          ***");
      System.out.println("* SUCCESS! swarch.xml has been written     ***");
      System.out.println("*                                          ***");
      System.out.println("**********************************************");
      gracefull_exit = true;
      
    } catch (VDKException e) {
      String msg = e.getMessage();
      if (msg != null && !msg.equals(""))
        System.err.println(msg);
      ArrayList<Problem> probs = e.getProblems();
      if (probs.size() == 0)
        e.printStackTrace();
      for(int i=0; i<probs.size(); i++) {
        Problem prob = probs.get(i);
        System.err.println(prob.getFileName()+":"+prob.getLine()+": "+prob.getSeverity()+": "+prob.getMessage());
      }
    } catch (IllegalValueException e) {
        String msg = e.getMessage();
        if (msg != null && !msg.equals(""))
          System.err.println(msg);
//        ArrayList<Problem> probs = e.getProblems();
//        if (probs.size() == 0)
//          e.printStackTrace();
//        for(int i=0; i<probs.size(); i++) {
//          Problem prob = probs.get(i);
//          System.err.println(p.getFileName()+":"+prob.getLine()+": "+prob.getSeverity()+": "+prob.getMessage());
//        } 
    } catch (XMLWriterException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();      
    } catch (IOException e) {
      e.printStackTrace();      
    }
    
    if (gracefull_exit)
    	System.exit(0);
    else
    	System.exit(1); /* Important to exit with 1 so that Make will stop */
  }
  
  
  /* loadHwDesign() will load a pre-existing hardware architecture IP-XACT
       design file */
  private static Design loadHwDesign(VDKSystem system, String designName){
	  Design topLevelDesign=null;

	  System.out.println("Loading Hardware Architecture");

	  try {
		  topLevelDesign = Design.create (system, designName);
		  
		  //topLevelDesign = new Design();

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
		      return null;
	  }
	  return topLevelDesign;
  }
  
  
  private static void dumpSWProperties(SoftwareElement swobj) {
	  System.out.println("Properties for " + swobj.getName());
	  System.out.println("---------------------------------");
	  for (Iterator<Property> pit = swobj.getOrderedPropertiesIterator(); pit.hasNext(); ) {
		  Property prop = pit.next();
		  System.out.print(prop.getName() + " = ");
		  if (prop instanceof LongProperty)
		    System.out.println(((LongProperty)prop).getValueAsHexString());  
		  else if (prop instanceof IntProperty)
		    System.out.println("0x" + Long.toHexString(((IntProperty)prop).getValue()));
		  else
		    System.out.println(prop.getValueAsString());
	  }
	  System.out.println("---------------------------------");
  }

}

