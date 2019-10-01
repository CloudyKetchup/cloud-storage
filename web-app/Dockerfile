FROM node:latest as build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . ./

RUN npm run build

RUN npm install -g serve

ENTRYPOINT ["serve", "-s", "build"]

