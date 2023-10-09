# FunctionVisualizer
![Screenshot 2023-10-05 134725](https://github.com/j-schall/FunctionVisualizer/assets/122560931/b817465a-6633-4cf6-9443-115d89c2b022)
With this tool you can:
<ul>
  <li>Visualize linear, propotional and quadratic functions</li>
  <li>Calculate intersections of two linear functions (Linear Equation Systems)</li>
  <li>Calculate linear functions with the help of two given points</li>
  <li>Highlight points in the coordinatesystem</li>
</ul>

## Installation Guide
### Windows
1. Go to realises and download the FunctionVisualizer_v.1.1.zip folder
2. Extract the downloaded ZIP folder
3. Double click on the *.exe file and start the programm

### Other Systems
1. Go to realises and download the FunctionVisualizer_jar.zip folder
2. Extract the JAR on your system
3. Check that you have Java on your System with: ```java --version```
<br>3.1. If it's not so, add the following repository to apt:<br>```sudo add-apt-repository ppa:openjdk-r/ppa```, <br>```sudo apt update```<br>
3.2. Then install Java: ```sudo apt install openjdk-17-jre-headless```
3.3. Download the JavaFX runtime: ```sudo apt install openjfx```
   <br>Or install manually <a href="https://gluonhq.com/products/javafx/">here<a><br>
4. Add your JavaFX path to the system variables: ```export PATH_TO_FX=/usr/share/openjfx/lib```
5. Start the programm: ```java --module-path $PATH_TO_FX --add-modules javafx.controls -jar FunctionVisualizer_v1.1.jar```

When you have problems, please inform me under the menuitem "issues".

