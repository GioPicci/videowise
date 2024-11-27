FROM nginx:alpine

# Set the working directory
WORKDIR /usr/share/nginx/html

# Copy the dist folder with your built frontend (HTML, CSS, JS)
COPY . /usr/share/nginx/html

# ENV variable for standalone client deploy
# ENV MAIN_SERVICE_URL="http://<your_main_service_ip>:8080"

# Make the entrypoint script executable
RUN chmod +x /usr/share/nginx/html/entrypoint.sh

# Expose the port
EXPOSE 80

# Set the entrypoint to the script
ENTRYPOINT ["/usr/share/nginx/html/entrypoint.sh"]

