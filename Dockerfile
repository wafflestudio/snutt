FROM node:8.15-alpine
WORKDIR /app
RUN apk add g++ make python
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run-script build
CMD ["npm", "start"]

EXPOSE 3000
