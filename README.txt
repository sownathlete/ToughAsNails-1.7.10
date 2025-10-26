-------------------------------------------
Original Workspace with changes to work in 2023
-------------------------------------------
Original source code with some changes so that it can work

Auto Setup
==============================
Download this src code and run gradlew idea or gradlew eclipse
==============================

Manual Setup
==============================
Glossary:
"-" = Remove
"+" = Add
Download the src from forge
Go to build.gradle and modify the following:
       -     url = "http://files.minecraftforge.net/maven" 
       +     url = "https://maven.minecraftforge.net"
       
       
       -    dependencies {
        classpath 'net.minecraftforge.gradle:ForgeGradle:1.2-SNAPSHOT'
    }
}
       +    dependencies {
        classpath ('com.anatawa12.forge:ForgeGradle:1.2-1.0.+') {

    changing = true
		  }
   }
}


AFTER:
"minecraft {
    version = "1.7.10-10.13.4.1614-1.7.10"
    runDir = "eclipse"
}" 
       +    tasks.withType(JavaCompile) {
            options.encoding = 'UTF-8'
            }

NEXT go to gradle\wrapper\gradle-wrapper.properties    
and modify:
       -    distributionUrl=https\://services.gradle.org/distributions/gradle-2.0-bin.zip
       +    distributionUrl=https\://services.gradle.org/distributions/gradle-4.4.1-bin.zip


Then open a terminal and run:
gradlew clean setDecompWorkspace (idea) or (eclipse)
==============================


