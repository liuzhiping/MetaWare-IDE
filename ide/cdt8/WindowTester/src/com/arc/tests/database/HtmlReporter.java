/*
 * HtmlReporter
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package com.arc.tests.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HtmlReporter {

    public static void main(String args[]) throws IOException{
        OutputStream output = System.out;
        if (args.length > 0){
            File f = new File(args[0]);
            output = new FileOutputStream(f);
        }
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));
        ITestDataBase db = DatabaseStuff.getDatabase();
        if (db != null){
            generateReport(db,writer);
        }
        writer.close();
    }
    
    private static void printSummaryRow(PrintWriter out, String label, int value){
        out.println("    <tr>");
        out.println("        <td><b>" + label + ":</b></td>");
        out.println("        <td>" + value + "</td>");
        out.println("    </tr>");
    }
   
    public static void generateReport(ITestDataBase database, PrintWriter out){
        Set<String> seenTests = new HashSet<String>();
        out.println("<html>");
        out.println("<META HTTP-EQUIV=\"Content-Style-Type\" CONTENT=\"text/css\">");
        out.println("<title>");
        out.println("IDE Test Results");
        out.println("</title>");
        out.println("<body>");
        out.println("<h1>IDE Test Results</h1>");
        out.println("<DIV STYLE=\"margin-left: 100px\">");
        out.println("<b>IDE Version:</b> " + database.getIdeVersion() );
        out.println("<br><b>OS Environment:</b> " + database.getOS());
        out.println("<br><b>WindowTester version:</b> " + database.getWindowTesterVersion());
        if (database.getStartDate().equals(database.getEndDate())){
            out.print("<br><b>Test date:</b> " + database.getStartDate());
        }
        else
            out.print("<br><B>Test dates:</b> " + database.getStartDate() + " through " + database.getEndDate());

        out.println("<p>");
       
        int passCount = 0;
        int notRunCount = 0;
        int failureCount = 0;
        int errorCount = 0;
        int manualCount = 0;
        int spotChecked = 0;
        int notApplicable = 0;
        ITestMetaData metaData = database.getMetaData();
        List<String>tests = metaData.getTestNames();
        for (String t: tests){           
            if (!database.isKnown(t)) {
                if (metaData.isManualTest(t))
                    manualCount++;
                else
                    notRunCount++;
            }
            else
            if (database.isPassed(t)) passCount++;
            else {
                String msg = database.getFailureMessage(t).toLowerCase();
                if (msg.startsWith("fail")){
                    failureCount++;
                }
                else if (msg.startsWith("manual")){
                    manualCount++;
                }
                else if (msg.startsWith("passed")){
                	passCount++;
                }
                else if (msg.startsWith("spot") || msg.startsWith("partial")){
                	spotChecked++;
                	passCount++;
                }
                else if (msg.startsWith("n/a")){
                	notApplicable++;
                }
                else if (msg.startsWith("not run:")){
                	if (metaData.isManualTest(t))
                		manualCount++;
                	else
                	    notRunCount++;
                }
                else {
                    errorCount++;
                }
            }
        }
        
        out.println("<table>");
        printSummaryRow(out,"Total number of tests",tests.size());
        printSummaryRow(out,"Number that passed",passCount);
        printSummaryRow(out,"Number that failed",failureCount);
        printSummaryRow(out,"Number with errors",errorCount);
        if (notApplicable != 0){
        	printSummaryRow(out,"Tests that do not apply",notApplicable);
        }
//        if (spotChecked != 0){
//        	 printSummaryRow(out,"Number that were spot-checked only",spotChecked);
//        }
        if (manualCount != 0)
            printSummaryRow(out,"Number of manual tests not run",manualCount);
        if (notRunCount != 0)
            printSummaryRow(out,"Number of automated tests not run",notRunCount);
        out.println("</table>");
        out.println("</div>");
        out.println("<p>");
        out.println("<table border=1 cellspacing=0 cellpadding=4 style='border-collapse:collapse;"+
                "border:solid green 2pt'>");
       
        for (String category: metaData.getCategories()){
            printCategoryRow(out, category);
            for (String testName: metaData.getTestsForCategory(category)){
                seenTests.add(testName);
                printRow(out, testName, database, metaData);
            }           
        }
        List<String>names = metaData.getUncategorizedTests();
        seenTests.addAll(names);
        List<String> allRanTests = database.getTests();
        List<String>extraTests = new ArrayList<String>(names);
        for (String s: allRanTests){
            if (!seenTests.contains(s)){
                extraTests.add(s);
            }
        }
        if (extraTests.size() > 0){
            printCategoryRow(out,"<i>Uncatagorized tests</i>");
            for (String name: extraTests){
                seenTests.add(name);
                printRow(out,name,database,metaData);
            }
        }
        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }
    
    private static final String HEADERS[] = new String[]{"Test name","Status", "Description", "Comment/Error message" };
    private static final String WIDTHS[] = new String[]{"80pt", "50pt", "45%", "30%" };

    private static void printCategoryRow (PrintWriter out, String category) {
        out.println("<tr>");
        out.println("    <td  valign=middle colspan=4 style='" + 
                    "border-top:solid green 1.5pt;height:12pt;border-bottom:none'>");
        out.println("        <font size=+1><b>" + category + "</b></font>");
        out.println("    </td>");
        out.println("</tr>");
      
        out.println("<tr>");
        for (int i = 0; i < HEADERS.length; i++) {
            out.println("    <td height=12  style='width:" + WIDTHS[i] + ";height:12pt;border-top:none;border-bottom:solid green 1.5pt'><i>" + HEADERS[i] + "</i></td>");
        }
        out.println("</tr>");
    }

    private static void printRow (PrintWriter out, String testName, ITestDataBase database, ITestMetaData metaData) {
        out.println("<tr>");
        out.print("    <td  style='border-bottom:solid green .5pt;"+
            "border-right:solid green .5pt'>" + testName);
        if (metaData.isManualTest(testName)) out.print(" <font size=\"-1\"><i>(manual)</i></font>");
        out.println("</td>");
        out.print("    <td style='border-bottom:solid green .5pt;"+
            "border-right:solid green .5pt'>");
        String errorMessage = "";
        boolean hasError = false;
        if (!database.isKnown(testName)) out.print("<i>Not run</i>");
        else if (database.isPassed(testName)) {
            out.print("<span style='color:green'>Passed</span>");
        }
        else {
            errorMessage = database.getFailureMessage(testName);
            int i = errorMessage.indexOf(':');
            String result;
            if (i < 0) result = "Error";
            else {
                result = errorMessage.substring(0,i);
                errorMessage = errorMessage.substring(i+1);
            }
            String s = result.toLowerCase();
            if (s.indexOf("passed") >= 0){
            	hasError = false;
            	out.print("<span style='color:green'>" + result + "</span>");
            }
            else if (s.startsWith("spot") || s.startsWith("partial") || s.startsWith("n/a")) {
            	out.print(result);
                hasError = false;
            }
            else if (s.equals("not run")){
            	out.print("<i>" + result + "</i>");
            	hasError = false;
            }
            else {       
            	hasError = true;
                out.print("<span style='color:red'>" + result + "</span>");
            }
        }
        out.println("</td>");
        out.print("    <td style='border-top:solid green .5pt;"+
            "border-right:solid green .5pt;border-bottom:solid green .5pt'>");
        String desc = metaData.getDescription(testName);
        if (desc == null) desc = "";
        out.println(desc + "</td>");
        if (errorMessage != null){
            out.print("    <td  style='border-bottom:solid green .5pt'>");
            if (errorMessage.length() > 0) {
            	if (hasError)
                    out.print("<span style='color:red'>");
            	out.print(fixupErrorMessage(errorMessage));
            	if (hasError)
            		out.print("</span>");
            }
            out.println("</td>");
        }    
        out.println("</tr>");
    }
    
    private static boolean hasLongToken(String s){
        String tokens[] = s.split(" ");
        for (String token: tokens){
            if (token.length() > 30) return true;
        }
        return false;
    }
    
    /**
     * Break up text with long token so that HTML column isn't too long.
     * @param msg message to be considered.
     * @return version of the message that won't cause column to be too wide.
     */
    private static String fixupErrorMessage(String msg){
        if (hasLongToken(msg)) {
            StringBuilder buf = new StringBuilder(msg.length()+10);
            String tokens[] = msg.split(" ");
            for (String s: tokens){
                if (s.length() > 30){
                    int i = s.indexOf('=');
                    if (i >= 0){
                        buf.append(fixupErrorMessage(s.substring(0,i)));
                        buf.append(" = ");
                        buf.append(fixupErrorMessage(s.substring(i+1)));              
                    }
                    else {
                        i = s.indexOf('.');
                        if (i >= 0){
                            buf.append(fixupErrorMessage(s.substring(0,i)));
                            buf.append(". ");
                            buf.append(fixupErrorMessage(s.substring(i+1)));        
                        }
                    }
                }
                else {
                    if (buf.length() > 0) buf.append(' ');
                    buf.append(s);
                }
            }
            return buf.toString();
        }
        else return msg;
    }
}
