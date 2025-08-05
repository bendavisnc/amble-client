# amble-client

This is a poc project that's a modest aim to give friends a way to play chinese checkers in almost real time with persistent state based on a relational database.

This is the frontend part of the project, which is based on re-frame/reagent for the ui as well as martian for its openapi based client-api. 

Feel free to reach out with any questions or ideas.

## Development

```
# for css compilation
lessc less/amble.less resources/public/css/style.css

# for javascript with auto-compilation
clj -M:dev -m shadow.cljs.devtools.cli watch app
```

See Makefile for further context and for building release artifacts.

There's also a Dockerfile available and a docker build and run script, `dockerrunlocal.sh` for docker-based local development.

## Screenshot
![screencast](./screencast.webm)

## Thanks

Thanks to the friends who gave me the inspiration for this project alongside the memories of happy times shared between a board in real life.

And thanks to my "Mema", Mable, who taught me this fun game in the first place. 

![mable](./mable.jpeg)

