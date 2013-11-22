<?xml version='1.0' encoding='ISO-8859-1' ?>
<?xml-stylesheet type="text/xsl" href="hs2web.xsl"?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">

<helpset version="2.0">
 <title>TAD Help</title>
 <maps>
  <homeID>tad_help_htm_wp9000004</homeID>
  <mapref location="TAD_Help_map.xml" />
 </maps>

 <view>
  <name>IPTOC</name>
  <label>Contents</label>
  <type>javax.help.TOCView</type>
  <data>TAD_Help_toc.xml</data>
 </view>
 <view>
  <name>Index</name>
  <label>Index</label>
  <type>javax.help.IndexView</type>
  <data>TAD_Help_ndx.xml</data>
 </view>
 <view>
  <name>Search</name>
  <label>Search</label>
  <type>javax.help.SearchView</type>
  <data engine="com.sun.java.help.search.DefaultSearchEngine">
   JavaHelpSearch
  </data>
 </view>


 <view>
  <name>Favorites</name>
  <label>Favorites</label>
  <type>javax.help.FavoritesView</type>
 </view>

   <presentation default="true" displayviewimages="false">
      <name>MainWindow</name>
      <title>ARChitect Help</title>
      <toolbar>
         <helpaction image="images.back">javax.help.BackAction</helpaction>
         <helpaction image="images.forward">javax.help.ForwardAction</helpaction>
         <helpaction>javax.help.SeparatorAction</helpaction>
         <helpaction image="images.refresh">javax.help.ReloadAction</helpaction>
         <helpaction image="images.home">javax.help.HomeAction</helpaction>
         <helpaction>javax.help.SeparatorAction</helpaction>
         <helpaction image="images.addfave">javax.help.FavoritesAction</helpaction>
         <helpaction>javax.help.SeparatorAction</helpaction>
         <helpaction image="images.print">javax.help.PrintAction</helpaction>
         <helpaction image="images.pagesetup">javax.help.PrintSetupAction</helpaction>
      </toolbar>
   </presentation>

</helpset>
