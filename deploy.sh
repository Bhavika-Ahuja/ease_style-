#!/bin/bash

# StyleSync - Build and Deploy Script
# This script builds the project and provides deployment options

set -e  # Exit on error

echo "========================================="
echo "  StyleSync - Build & Deploy Script"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java version
echo "Checking Java version..."
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$java_version" -lt 17 ]; then
    echo -e "${RED}Error: Java 17 or higher is required. Current version: $java_version${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java version OK${NC}"
echo ""

# Check Maven
echo "Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven found${NC}"
echo ""

# Check MongoDB URI
if [ -z "$MONGODB_URI" ]; then
    echo -e "${YELLOW}Warning: MONGODB_URI environment variable not set${NC}"
    echo "Please set it before running the application:"
    echo 'export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/"'
    echo ""
fi

# Check MongoDB Database
if [ -z "$MONGODB_DATABASE" ]; then
    echo -e "${YELLOW}Warning: MONGODB_DATABASE not set. Using default: stylesync${NC}"
    export MONGODB_DATABASE="stylesync"
fi

# Clean and build
echo "Building project..."
mvn clean package

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Build successful!${NC}"
    echo ""
else
    echo -e "${RED}✗ Build failed!${NC}"
    exit 1
fi

# Check if WAR file exists
if [ ! -f "target/stylesync.war" ]; then
    echo -e "${RED}Error: WAR file not found in target/stylesync.war${NC}"
    exit 1
fi

echo "WAR file created: target/stylesync.war"
echo "Size: $(du -h target/stylesync.war | cut -f1)"
echo ""

# Deployment options
echo "========================================="
echo "  Deployment Options"
echo "========================================="
echo "1. Deploy to local Tomcat"
echo "2. Run with Docker"
echo "3. Exit (manual deployment)"
echo ""
read -p "Choose an option (1-3): " choice

case $choice in
    1)
        # Deploy to Tomcat
        echo ""
        read -p "Enter Tomcat webapps directory path: " tomcat_path
        
        if [ -d "$tomcat_path" ]; then
            echo "Copying WAR file to $tomcat_path..."
            cp target/stylesync.war "$tomcat_path/ROOT.war"
            echo -e "${GREEN}✓ Deployed to Tomcat!${NC}"
            echo ""
            echo "Start Tomcat and access: http://localhost:8080/"
        else
            echo -e "${RED}Error: Directory not found: $tomcat_path${NC}"
        fi
        ;;
        
    2)
        # Docker deployment
        echo ""
        echo "Building Docker image..."
        docker build -t stylesync:latest .
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Docker image built!${NC}"
            echo ""
            
            if [ -z "$MONGODB_URI" ]; then
                read -p "Enter MongoDB URI: " mongo_uri
                export MONGODB_URI="$mongo_uri"
            fi
            
            echo "Starting Docker container..."
            docker run -d -p 8080:8080 \
                -e MONGODB_URI="$MONGODB_URI" \
                -e MONGODB_DATABASE="$MONGODB_DATABASE" \
                --name stylesync \
                stylesync:latest
            
            echo -e "${GREEN}✓ Container started!${NC}"
            echo ""
            echo "Application URL: http://localhost:8080/"
            echo ""
            echo "View logs: docker logs -f stylesync"
            echo "Stop container: docker stop stylesync"
            echo "Remove container: docker rm stylesync"
        else
            echo -e "${RED}✗ Docker build failed!${NC}"
        fi
        ;;
        
    3)
        # Manual deployment
        echo ""
        echo "========================================="
        echo "  Manual Deployment Instructions"
        echo "========================================="
        echo ""
        echo "Local Tomcat:"
        echo "  1. Copy target/stylesync.war to Tomcat webapps/"
        echo "  2. Set environment variables"
        echo "  3. Start Tomcat"
        echo "  4. Access http://localhost:8080/stylesync"
        echo ""
        echo "Docker:"
        echo "  docker build -t stylesync ."
        echo "  docker run -p 8080:8080 \\"
        echo "    -e MONGODB_URI=\"your_uri\" \\"
        echo "    stylesync"
        echo ""
        echo "Cloud (Render/Railway):"
        echo "  1. Push to GitHub"
        echo "  2. Connect repository to platform"
        echo "  3. Add environment variables"
        echo "  4. Deploy"
        echo ""
        ;;
        
    *)
        echo -e "${RED}Invalid option${NC}"
        exit 1
        ;;
esac

echo ""
echo "========================================="
echo "  Environment Variables Required"
echo "========================================="
echo "MONGODB_URI: MongoDB Atlas connection string"
echo "MONGODB_DATABASE: Database name (default: stylesync)"
echo ""
echo "Set them with:"
echo "export MONGODB_URI=\"your_connection_string\""
echo "export MONGODB_DATABASE=\"stylesync\""
echo ""
echo "========================================="
echo "  Build Complete!"
echo "========================================="
