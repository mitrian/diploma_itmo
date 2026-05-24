FROM node:22-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY docker-entrypoint-https.sh /docker-entrypoint-https.sh
RUN chmod +x /docker-entrypoint-https.sh
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
ENTRYPOINT ["/docker-entrypoint-https.sh"]
CMD ["nginx", "-g", "daemon off;"]
EXPOSE 80 443
