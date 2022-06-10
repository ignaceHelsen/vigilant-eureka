# About

## Usage
First run in node-one:
$ mvn install

Then upload the mvn jar to ~/ on each node and change the hostname override in each script (only the index)

Then go into each node and run
```
$ python3 listenAndStartJar.py
```

Then run all ssh tunnels

-   From the project directory enter the following commands:

```
$ npm install
$ npm run build
$ npm start
```

-   Navigate to `http://localhost:9000/`.
-   Open your browser's development tools (usually F12).
-   Click the header text and at the same time check the console.

## Technologies used

-   webpack
    -   loaders for css, ttf, etc.
-   npm
-   Bootstrap
-   JQuery (exclusively for Bootstrap!)

