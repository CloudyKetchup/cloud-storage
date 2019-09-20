FROM node:12.4.0-alpine as build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . ./

RUN npm build

ENTRYPOINT ["npm", "start"]
