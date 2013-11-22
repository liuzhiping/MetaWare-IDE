<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="helpsetname" select="'ARC600IPLib.hs'"/>

<xsl:variable name="mapfilename" select="document($helpsetname)//helpset/maps/mapref/@location"/>


<xsl:template match="/">
<html>
<xsl:apply-templates/> 
</html>
</xsl:template>

<xsl:template match='helpset'>
 <HEAD>
  <TITLE><xsl:value-of select="./title"/></TITLE>
  
  </HEAD>
  	<FRAMESET name="leftandright" cols="30%,1*" onLoad="if (location.search) frames['right'].document.location.search = location.search;">
  	<!--   = (location.search) ? location.search.substring(1, location.search.length) : 'http://www.arc.com';" -->
	 <FRAME>
         <xsl:attribute name="name">left</xsl:attribute>
         <xsl:attribute name="src"><xsl:value-of select="view/data"/></xsl:attribute>
 
	 </FRAME>
	 <FRAME>
         <xsl:attribute name="name">right</xsl:attribute>
         <xsl:attribute name="src">
         <xsl:variable name="x" select="//maps/homeID"/>
         <!-- xsl:value-of select="document($mapfilename)/map/mapID[@target=$x]/@url"/-->
	 <xsl:call-template name="getmapurl">
	   <xsl:with-param name="theID" select="$x" />
	  </xsl:call-template>
         </xsl:attribute>
 	 </FRAME>
	 </FRAMESET>
   <NOFRAMES>
  <BODY>
      <H1><xsl:value-of select="./title"/></H1>
       <P>Please use the navigation buttons below.</P>
      <xsl:apply-templates/>
  
  </BODY>
  </NOFRAMES>
</xsl:template>

<!-- Each VIEW element generates a link to select between TOC, Index etc -->
<xsl:template match="view"> 
   <!-- If the view is a search engine or favorites then do nothing -->
   <xsl:if test="(./type != 'javax.help.SearchView') and (./type != 'javax.help.FavoritesView') ">
	    <a>
	    <xsl:attribute name="style"></xsl:attribute>
	    <xsl:attribute name="target">left</xsl:attribute>
	    <xsl:attribute name="title"><xsl:value-of select="./label"/></xsl:attribute>
	    <xsl:attribute name="href"><xsl:value-of select="./data"/></xsl:attribute>
	    <xsl:value-of select="./label"/>
	    </a>
	     
     
   </xsl:if>
   
</xsl:template>

<!-- Generate a link for the Start Page. -->
<xsl:template match="homeID">
	<a>
	<xsl:attribute name="style"></xsl:attribute>
	<xsl:attribute name="target">right</xsl:attribute>
	<xsl:attribute name="title">Start Page</xsl:attribute>
	<xsl:attribute name="href"><xsl:variable name="x" select="."/>
	<!-- xsl:value-of select="document($mapfilename)/map/mapID[@target=$x]/@url"/-->
	 <xsl:call-template name="getmapurl">
	   <xsl:with-param name="theID" select="$x" />
	  </xsl:call-template>
	</xsl:attribute>Home Page
</a>
</xsl:template>

<xsl:template match="title">
</xsl:template>

<!-- Ignore mapref elements. -->
<xsl:template match="mapref">
</xsl:template>

<!-- Generate a link for any sub help sets. -->
<xsl:template match="subhelpset">
   <xsl:variable name="viewname" select="document($helpsetname)//helpset/view/name" />
    <!-- a>
    <xsl:attribute name="target">_top</xsl:attribute>
    <xsl:attribute name="title">Sub Help Set</xsl:attribute>
    <xsl:attribute name="href"><xsl:value-of select="@location"/></xsl:attribute>
    <xsl:value-of select="document(@location)/helpset/title"/>
    </a -->
   <!-- Generate a link for any sub help TOCs. -->
    <!-- xsl:variable name="subviewname" select="document(@location)//helpset/view/name" /-->
    <!-- Master:<xsl:value-of select="$viewname"/> -->
    <!-- Sub:<xsl:value-of select="$subviewname"/> -->
    <!-- Test:<xsl:value-of select="contains($subviewname,$viewname) and contains($viewname,$subviewname)"/> -->
    <!-- XMLfile: <xsl:value-of select="document(@location)/helpset/view[name=string($viewname)]/data"/> -->
    <xsl:choose>
	<xsl:when test="document(@location)/helpset/view/name=string($viewname)">
	<li>
	<a>
	    <xsl:attribute name="target">left</xsl:attribute>
	    <xsl:attribute name="title">Open <xsl:value-of select="document(@location)/helpset/title"/> Contents</xsl:attribute>
	    <xsl:attribute name="href">
	    <xsl:call-template name="getbase">
		 <xsl:with-param name="mystr" select="@location" />
	    </xsl:call-template>
	    <xsl:value-of select="document(@location)/helpset/view[name=string($viewname)]/data"/>
	    </xsl:attribute>
	   <xsl:value-of select="document(@location)/helpset/title"/>
	</a>
	</li>
	</xsl:when>
	<xsl:otherwise>
	</xsl:otherwise>
    </xsl:choose>
     
</xsl:template>

<!-- ignore any presentation elements (JavaHelp 2.0) -->
<xsl:template match="presentation">
</xsl:template>

<!-- Template used to get the base URI of a file. -->
<!-- Uses a bit of recursion -->
<xsl:template name="getbase">
	<xsl:param name="mystr"/>
	<xsl:choose>
	  <xsl:when test="contains($mystr,'/')">
		<xsl:value-of select="concat(substring-before($mystr,'/'),'/')"/>
	  	<xsl:call-template name="getbase">
	  		<xsl:with-param name="mystr" select="substring-after($mystr,'/')" />
		</xsl:call-template>
	  </xsl:when>
	  <xsl:otherwise></xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- Template used to get a unique URL of a mapID from all mapfiles -->
<xsl:template name="getmapurl">
	<xsl:param name="theID"/>
	<xsl:choose>
		<xsl:when test="document($mapfilename)/map/mapID[@target=$theID]">
			<xsl:value-of select="document($mapfilename)/map/mapID[@target=$theID]/@url"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:for-each select="document($helpsetname)//helpset/subhelpset">
				<xsl:variable name="subhelpsetfilename" select="@location" />
				<xsl:variable name="submapfilename" select="document(@location)/helpset/maps/mapref/@location" />
				<xsl:variable name="thebase">
					<xsl:call-template name="getbase">
						<xsl:with-param name="mystr" select="@location" />
					</xsl:call-template>
				</xsl:variable>

				<!-- xsl:value-of select="document(concat($thebase,$submapfilename))/map/mapID/@target=$theID"/-->
				<xsl:choose>
					<xsl:when test="(document(concat($thebase,$submapfilename))/map/mapID/@target=$theID)">
						<xsl:value-of select="concat($thebase,document(concat($thebase,$submapfilename))/map/mapID[@target=$theID]/@url)"/>
					</xsl:when>
					<xsl:otherwise>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>




<!--  TOC File -->
<!-- Primary elements for TOC files. Also uses templates from the HS -->
<xsl:template match="toc">
 <HEAD>
  <TITLE>Table of Contents</TITLE>
  <link rel="stylesheet" type="text/css" href="navigator.css" title="Navigator style"></link>
 </HEAD>
  <BODY>
  <xsl:attribute name="onLoad">
  javascript:LE=document.links; for(i=0;i!=LE.length;i++) { var vHash = LE[i].hash; var vPathname = LE[i].pathname; LE[i].href = ''; LE[i].pathname = vPathname; LE[i].search = top.location.search; if (vHash) LE[i].hash = vHash.replace('#','');};
  </xsl:attribute>
  

  
  <!-- Each hs file VIEW element generates a link to select between TOC, Index etc -->
  <table>
  <caption><img src="images/hsiconbig.gif" alt="Help Set:"/><xsl:value-of select="document($helpsetname)//helpset/title" /></caption>
  <tr><td class="navigator">
  <xsl:apply-templates select="document($helpsetname)//helpset/view"></xsl:apply-templates>
</td>
  </tr>
  <tr>
  <td class="toc" colspan="2">
  <UL><xsl:attribute name="style">list-style-type: none;</xsl:attribute>


      <xsl:apply-templates/>

     </UL>
  <!-- Append all other subhelpset TOCs regardless of name or label in the view element -->
  <!-- Need to compare the view name against the subhelpset view name to do it properly -->
  <!-- UL><xsl:attribute name="style">list-style-type: none;</xsl:attribute>
    <xsl:for-each select="document($helpsetname)//helpset/subhelpset">
	<xsl:variable name="subhelpsetfilename" select="@location" />
	<xsl:variable name="subviewfilename" select="document(@location)/helpset/view/data" />
	<xsl:variable name="thebase">
		<xsl:call-template name="getbase">
			<xsl:with-param name="mystr" select="@location" />
		</xsl:call-template>
	</xsl:variable>
        <xsl:apply-templates select="document(concat($thebase,document(@location)//helpset/view/data))//toc/tocitem"></xsl:apply-templates>
      </xsl:for-each>
    
  </UL -->

  </td>
  </tr>
  <tr>
  <td class="subhelpsets">
  <ul>
    <li><span onClick="javascript: top.location.reload(true)" style="text-decoration: underline; color: blue" onMouseOver="style.cursor='pointer';" title="Reload Master Online Information Set">Top</span></li>

  <!-- Provide link to any sub help set's HTML navigation pages  -->
  <xsl:apply-templates select="document($helpsetname)//helpset/subhelpset"></xsl:apply-templates>
  </ul>
  </td>
  </tr>
  </table>
       
  </BODY>

</xsl:template>


<xsl:template match="tocitem">
  <LI><xsl:attribute name="style"> white-space: nowrap;</xsl:attribute>
  <xsl:choose>
   <xsl:when test="@image">
   <!-- Image has been defined in the tocitem element -->
	   <IMG>
	   <xsl:attribute name="alt"></xsl:attribute>
	   <xsl:attribute name="title">Click to Expand/Collapse</xsl:attribute>
	   <xsl:attribute name="src">
	   <xsl:variable name="y" select="@image"/>
	   <!-- xsl:value-of select="document($mapfilename)/map/mapID[@target=$y]/@url"/-->
	    <xsl:call-template name="getmapurl">
		 <xsl:with-param name="theID" select="$y" />
	    </xsl:call-template>
	   </xsl:attribute>
		 <xsl:choose>
		   <xsl:when test=".//tocitem">
	   	   <xsl:attribute name="OnClick">parentNode.getElementsByTagName('UL')[0].style.display = (parentNode.getElementsByTagName('UL')[0].style.display=='none')? 'list-item':'none';</xsl:attribute>
		   <xsl:attribute name="onMouseOver">style.cursor='pointer'</xsl:attribute>
		   </xsl:when>
		 </xsl:choose>

	   </IMG>
   </xsl:when>
   <xsl:otherwise>
   <!-- Use images as set in the toc element -->
	<IMG>
	<xsl:attribute name="alt"></xsl:attribute>
	<xsl:attribute name="title">Click to Expand/Collapse</xsl:attribute>
	<xsl:choose>
		<xsl:when test=".//tocitem">
			<xsl:attribute name="src">
			<xsl:variable name="y" select="/toc/@categoryclosedimage"/>
			<!-- xsl:value-of select="document($mapfilename)/map/mapID[@target=$y]/@url"/-->
			    <xsl:call-template name="getmapurl">
				 <xsl:with-param name="theID" select="$y" />
			    </xsl:call-template>
			</xsl:attribute>
			<xsl:attribute name="OnClick">parentNode.getElementsByTagName('UL')[0].style.display = (parentNode.getElementsByTagName('UL')[0].style.display=='none')? 'list-item':'none';</xsl:attribute>
			<xsl:attribute name="onMouseOver">style.cursor='pointer'</xsl:attribute>
		</xsl:when>
		<xsl:otherwise>
			<xsl:attribute name="src">
			<xsl:variable name="y" select="/toc/@topicimage"/>
			<!-- xsl:value-of select="document($mapfilename)/map/mapID[@target=$y]/@url"/-->
			    <xsl:call-template name="getmapurl">
				 <xsl:with-param name="theID" select="$y" />
			    </xsl:call-template>
			</xsl:attribute>
		</xsl:otherwise>
	</xsl:choose>
	</IMG>
   </xsl:otherwise>
  </xsl:choose>
  <A >
  <xsl:if test="@target and not (@target='')">
  <xsl:attribute name="title"><xsl:value-of select="@text"/></xsl:attribute>
  <xsl:attribute name="href"><xsl:variable name="x" select="@target"/>
    <!-- xsl:value-of select="document($mapfilename)/map/mapID[@target=$x]/@url"/-->
    <xsl:call-template name="getmapurl">
	 <xsl:with-param name="theID" select="$x" />
    </xsl:call-template>
  	
  </xsl:attribute>
  <xsl:attribute name="target">right</xsl:attribute>
  </xsl:if>
  <xsl:value-of select="@text"/>
  </A>  
<xsl:choose>
   <xsl:when test=".//tocitem">
     <UL><xsl:attribute name="style">list-style-type: none;</xsl:attribute>
      <xsl:apply-templates/>
     </UL>
   </xsl:when>
</xsl:choose>

  
  </LI>
</xsl:template>

<!-- Index File -->
<!-- Primary elements for Index files. Also uses templates from the HS -->

<xsl:template match="index">
 <HEAD>
  <TITLE>Index</TITLE>
  <link rel="stylesheet" type="text/css" href="navigator.css" title="Navigator style"></link>

 </HEAD>

  <BODY>
  <!-- Each hs file VIEW element generates a link to select between TOC, Index etc -->
  <table>
  <caption><img src="images/hsiconbig.gif" alt="Help Set:"/><xsl:value-of select="document($helpsetname)//helpset/title" /></caption>
  <tr><td class="navigator">
  <xsl:apply-templates select="document($helpsetname)//helpset/view"></xsl:apply-templates>
  </td></tr>
  <tr>
  <td class="toc">
  
     <UL><xsl:attribute name="style">list-style-type: none;</xsl:attribute>

      <xsl:apply-templates/>

     </UL>
     
  <!-- Append all other subhelpset indexes regardless of name or label in the view element -->
  <UL><xsl:attribute name="style">list-style-type: none;</xsl:attribute>
    <xsl:for-each select="document($helpsetname)//helpset/subhelpset">
	<xsl:variable name="subhelpsetfilename" select="@location" />
	<xsl:variable name="subviewfilename" select="document(@location)/helpset/view/data" />
	<xsl:variable name="thebase">
		<xsl:call-template name="getbase">
			<xsl:with-param name="mystr" select="@location" />
		</xsl:call-template>
	</xsl:variable>
        <xsl:apply-templates select="document(concat($thebase,document(@location)//helpset/view[type='javax.help.IndexView']/data))//index/indexitem"></xsl:apply-templates>
      </xsl:for-each>
    
  </UL>
     
     
  </td>
  </tr>
  </table>

  </BODY>

</xsl:template>

<xsl:template match="indexitem">
  <LI style="white-space: nowrap">
  <A>
  <xsl:if test="@target">
  <xsl:attribute name="href">
  <xsl:variable name="x" select="@target"/>  
    <xsl:call-template name="getmapurl">
	 <xsl:with-param name="theID" select="$x" />
    </xsl:call-template>
  </xsl:attribute>
  <xsl:attribute name="title"><xsl:value-of select="@text"/></xsl:attribute>
  <xsl:attribute name="target">right</xsl:attribute>
  </xsl:if>
  <xsl:value-of select="@text"/>
  </A>
  <xsl:choose>
     <xsl:when test=".//indexitem">
       <UL><xsl:attribute name="style">list-style-type: none;</xsl:attribute>
        <xsl:apply-templates/>
       </UL>
     </xsl:when>
  </xsl:choose>
  </LI>
</xsl:template>


<!-- Map File -->
<!-- Primary elements for Map files. Also uses templates from the HS -->
<xsl:template match="map">
<HTML>
 <HEAD>
  <TITLE>Map File</TITLE>
  <link rel="stylesheet" type="text/css" href="navigator.css" title="Navigator style"></link>
 </HEAD>

  <BODY>
  <!-- Each hs file VIEW element generates a link to select between TOC, Index etc -->
  <table><tr><td class="navigator">
  <xsl:apply-templates select="document($helpsetname)//helpset/view"></xsl:apply-templates>
  </td></tr>
  <tr>
  <td class="toc">
  
     <UL><xsl:attribute name="style">list-style-type: none;</xsl:attribute>

      <xsl:apply-templates/>

     </UL>
     
     
     
  </td>
  </tr>
  </table>
  </BODY>
</HTML>

</xsl:template>


<xsl:template match="mapID">
  <LI>
  <A >
  <xsl:attribute name="href"><xsl:value-of select="@url"/></xsl:attribute>
  <xsl:attribute name="target">right</xsl:attribute>
  <xsl:attribute name="id"><xsl:value-of select="@target"/></xsl:attribute>
  <xsl:value-of select="@target"/>
  </A>
  </LI>
  <xsl:apply-templates/>
</xsl:template>


</xsl:stylesheet>