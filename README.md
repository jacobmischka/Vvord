Vvord
=====
Uses the 3DM XML merge tool (http://www.cs.hut.fi/~ctl/3dm/) to merge three Microsoft Word .docx files, one base and two child branches. 

The 3dm JAR file must be in the classpath when compiling the main class Vvord.java. 

When running Vvord, the 3dm JAR must also be in the classpath, as well as its dependencies which are explained on the tool's homepage linked above, java-getopt-1.0.8.jar (http://www.urbanophile.com/arenn/hacking/download.html) & xercesImpl.jar (http://xerces.apache.org/xerces-j/).

A compiled JAR, as well as a zip file containing the required libraries can be found on my website at http://www.mischka.info/Vvord/. The JAR is set so that the libs/ directory must be in the same directory as the JAR, not the dependency JARs themselves.

The tool saves several files per merge operation into the machine's temporary directory, so Windows users may want to be aware of that fact. The 3DM merge tool will create a "conflict.log" file in the current directory when a merge is not 100% perfect and successful, which will likely be upon every single merge. In most cases, the merge is successful even when it leaves a conflict log. The log file is overwritten upon each merge.

The tool intended for use via the molhado-word Word plugin to merge .docx files and their histories.

The tool is distributed under the GPLv3 license (http://www.gnu.org/licenses/gpl.txt), Copyright 2013 Jacob Mischka
