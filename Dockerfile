# Use Tomcat 10.1 with JDK 17
FROM tomcat:10.1-jdk17

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file to Tomcat webapps directory
COPY target/stylesync.war /usr/local/tomcat/webapps/ROOT.war

# Expose port 8080
EXPOSE 8080

# Set environment variables (will be overridden by runtime environment)
ENV MONGODB_URI=""
ENV MONGODB_DATABASE="stylesync"

# Start Tomcat
CMD ["catalina.sh", "run"]
