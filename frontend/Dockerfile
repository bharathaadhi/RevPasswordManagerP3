# Stage 1: Build the Angular application
FROM node:20-slim AS build
WORKDIR /app

# Install dependencies needed for native builds
RUN apt-get update && apt-get install -y python3 make g++ && rm -rf /var/lib/apt/lists/*

COPY package*.json ./
RUN npm install --loglevel warn --progress=false
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Serve the application using Nginx
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/rev-password-manager-ui/browser /usr/share/nginx/html
# If your build output directory is different, please adjust the path above.
# Usually for Angular 17+, it's dist/<project-name>/browser
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
