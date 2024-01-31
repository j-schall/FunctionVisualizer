FROM debian:latest
RUN apt-get update && apt-get upgrade -y && apt install openjdk-17-jre-headless -y && apt install openjfx -y && apt install xauth -y &&  apt install x11-apps -y && apt install xorg -y
WORKDIR /etc/functionVisualizer
ENV DISPLAY=:0
COPY ./FunctionVisualizer-1.0-SNAPSHOT-shaded.jar .
CMD ["java", "--module-path", "/usr/share/openjfx/lib", "--add-modules", "javafx.controls", "-jar", "FunctionVisualizer-1.0-SNAPSHOT-shaded.jar"]