FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG PUBLIC_API_URL=http://localhost:8080/api/v1
ENV PUBLIC_API_URL=$PUBLIC_API_URL
RUN npm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=builder /app/dist ./dist
RUN npm install -g serve
EXPOSE 4321
CMD ["serve", "dist", "-p", "4321"]
