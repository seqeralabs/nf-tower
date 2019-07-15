# Step 1: Build the app in image 'builder'
FROM node:11.14.0-alpine AS builder

RUN npm install -g @angular/cli

WORKDIR /usr/src/app
COPY . .
RUN npm install
RUN npm run build

# Step 2: Use build output from 'builder'
FROM nginx:stable-alpine
LABEL version="1.0"

COPY nginx.conf /etc/nginx/nginx.conf

WORKDIR /usr/share/nginx/html
COPY --from=builder /usr/src/app/dist/tower-web/ .
