@echo off
javac *.java --release 8
jar cmf manifest.mf CaveGen.jar *.class
jar cmf manifest2.mf CaveViewer.jar *.class
jar cmf manifest3.mf Seed.jar *.class
