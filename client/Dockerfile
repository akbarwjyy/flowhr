FROM node:20-alpine AS builder
WORKDIR /app
RUN npm install -g pnpm
COPY package.json pnpm-lock.yaml* ./
RUN pnpm install --frozen-lockfile
COPY . .
ARG PUBLIC_API_URL=http://localhost:8080/api/v1
ENV PUBLIC_API_URL=$PUBLIC_API_URL
RUN pnpm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=builder /app/dist ./dist
RUN npm install -g serve
EXPOSE 4321
CMD ["serve", "dist", "-p", "4321"]
