NOTE TO STARTEAM USERS:

This plugin "wraps" the Java util library. It references 
the source by a link.

Once you have checked out this directory, you must
modify the ".project" file so that the following
XML element references the path of the actual MW UTIL
source:

<linkedResources>
    <link> 
        <location>
          "path of MW UTIL source"
        </location>
    </link>
</linkedResources>