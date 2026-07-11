# barium
economy plugin for minecraft (paper)

## how to compile

NOTE: i only test this on linux (void) so i cant guarantee windows support. if there are any issues, please open an issue.
### windows
```bash
.\gradlew.bat build
```

copy the `build/libs/barium-0.0.1.jar` file to your server's `plugins` folder.

### linux
```bash
./gradlew build
```

copy the `build/libs/barium-0.0.1.jar` file to your server's `plugins` folder.

## run test server
downloads the paper jar and runs a test server on localhost (mostly useful for developing)
```bash
./gradlew runServer
```

## usage
to use barium build the plugin using the plugins/ directory. on first boot it will copy the default configuration from the jar into the plugins paper config directory

the config file has comments that explain what each config value does

you can use a plugin like luckperms to set each players permissions according to permissions.txt
